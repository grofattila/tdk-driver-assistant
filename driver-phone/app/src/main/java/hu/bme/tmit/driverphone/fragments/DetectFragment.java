package hu.bme.tmit.driverphone.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import hu.bme.tmit.driverphone.R;
import hu.bme.tmit.driverphone.activity.HomeActivity;
import hu.bme.tmit.driverphone.listener.CameraImageListener;
import hu.bme.tmit.driverphone.neuralnet.ssd.Detection;
import hu.bme.tmit.driverphone.neuralnet.ssd.DetectionList;
import hu.bme.tmit.driverphone.util.Usage.UsageLoggerService;
import hu.bme.tmit.driverphone.util.camera.AutoFitTextureView;

import static android.hardware.camera2.CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP;
import static android.hardware.camera2.CameraMetadata.CONTROL_AF_STATE_ACTIVE_SCAN;
import static android.hardware.camera2.CaptureRequest.CONTROL_AF_MODE;

public class DetectFragment extends Fragment
        implements ActivityCompat.OnRequestPermissionsResultCallback {


    private static final int sImageFormat = ImageFormat.YUV_420_888;
    /**
     * Tag for the {@link Log}.
     */
    private static final String TAG = DetectFragment.class.getName();
    public static boolean accessGranted = true;
    private final CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        private void process(CaptureResult result) {
        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request,
                                        CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request,
                                       TotalCaptureResult result) {
            process(result);
        }

    };

    private DetectionList detectionList = null;
    private SurfaceView surfaceView;
    private SurfaceHolder mHolder;
    private Intent usageLoggerService;

    private CameraResultReceiver cameraResultReceiver;
    private CameraImageListener cameraImageListener;
    private AutoFitTextureView mTextureView;
    private RelativeLayout layout;
    private TextView resultText;
    private String mCameraId;
    private CameraCaptureSession mCaptureSession;
    private CameraDevice mCameraDevice;
    private Size mPreviewSize;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private ImageReader mImageReader;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CaptureRequest mPreviewRequest;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

    };

    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener =
            new TextureView.SurfaceTextureListener() {

                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
                    openCamera(width, height);
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
                    configureTransform(width, height);
                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {

                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture texture) {

                }

            };

    private int count;

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, chooses the smallest one whose
     * width and height are at least as large as the respective requested values, and whose aspect
     * ratio matches with the specified value.
     *
     * @param choices     The list of sizes that the camera supports for the intended output class
     * @param width       The minimum desired width
     * @param height      The minimum desired height
     * @param aspectRatio The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */

    private static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        for (Size s : choices) {
            Log.d("choices: ", "H: " + s.getHeight() + " - W: " + s.getWidth());
        }

        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }

        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new hu.bme.tmit.driverphone.fragments.DetectFragment.CompareSizesByArea());
        } else {
            return choices[0];
        }
    }

    public static DetectFragment newInstance() {
        DetectFragment fragment = new DetectFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_detect, container, false);
        initView(v);
        return v;
    }

    private void initView(View v) {
        mTextureView = v.findViewById(R.id.cameraPreview);
        resultText = v.findViewById(R.id.result_text);
        resultText.setTextColor(Color.BLACK);
        layout = v.findViewById(R.id.camera_background);
        surfaceView = v.findViewById(R.id.surfaceView);
        surfaceView.setZOrderOnTop(true);
        mHolder = surfaceView.getHolder();
        mHolder.setFormat(PixelFormat.TRANSPARENT);
    }

    @Override
    public void onResume() {
        super.onResume();
        accessGranted = true;
        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).

        cameraResultReceiver = new CameraResultReceiver();
        IntentFilter filter = new IntentFilter("detection");
        getActivity().registerReceiver(cameraResultReceiver, filter);

        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startBackgroundThread();
            }
        }, 500);

        usageLoggerService = new Intent(getActivity(), UsageLoggerService.class);
        getActivity().startService(usageLoggerService);
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.e(TAG, "onPause");
        closeCamera();
        stopBackgroundThread();
        Intent intent = new Intent(getActivity(), UsageLoggerService.class);
        getActivity().stopService(intent);
        getActivity().unregisterReceiver(cameraResultReceiver);
    }

    /**
     * Sets up member variables related to camera.
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    private void setUpCameraOutputs(int width, int height) {
        CameraManager manager = (CameraManager) getActivity().getApplicationContext().getSystemService(getActivity().getApplicationContext().CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

                StreamConfigurationMap map = characteristics.get(SCALER_STREAM_CONFIGURATION_MAP);

                List<Size> outputSizes = Arrays.asList(map.getOutputSizes(sImageFormat));
                Size largest = Collections.max(outputSizes, new hu.bme.tmit.driverphone.fragments.DetectFragment.CompareSizesByArea());

                cameraImageListener = new CameraImageListener(getActivity().getApplicationContext());

                mImageReader = ImageReader.newInstance(2976, 2976, sImageFormat, 2);
                mImageReader.setOnImageAvailableListener(cameraImageListener, mBackgroundHandler);
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), 2976, 2976, new Size(1, 1));
                Log.e(TAG, "WIDTH: " + mPreviewSize.getWidth() + " HEIGHT: " + mPreviewSize.getHeight());

                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    private void openCamera(int width, int height) {
        setUpCameraOutputs(width, height);
        configureTransform(width, height);
        CameraManager manager = (CameraManager) getActivity().getApplicationContext().getSystemService(getActivity().getApplicationContext().CAMERA_SERVICE);
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    /**
     * Closes the current {@link CameraDevice}.
     */
    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (mCaptureSession != null) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (mCameraDevice != null) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (mImageReader != null) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        try {
            if (cameraImageListener != null)
                cameraImageListener.shutDown();
            if (mBackgroundThread != null) {
                mBackgroundThread.quitSafely();
                mBackgroundThread.join();
                mBackgroundThread = null;
                mBackgroundHandler = null;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        }
    }

    /**
     * Creates a new {@link CameraCaptureSession} for camera preview.
     */
    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Log.e(TAG, "mPreviewSize.getWidth(): " + mPreviewSize.getWidth() + ", mPreviewSize.getHeight(): " + mPreviewSize.getHeight());

            Surface surface = new Surface(texture);
            Surface mImageSurface = mImageReader.getSurface();
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(mImageSurface);
            mPreviewRequestBuilder.addTarget(surface);

            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(Arrays.asList(mImageSurface, surface),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            Log.e(TAG, "onConfigured");
                            if (mCameraDevice == null) return;

                            // When the session is ready, we start displaying the preview.
                            mCaptureSession = cameraCaptureSession;
                            try {
                                // Auto focus should be continuous for camera preview.
                                mPreviewRequestBuilder.set(CONTROL_AF_MODE, CONTROL_AF_STATE_ACTIVE_SCAN);

                                // Finally, we start displaying the camera preview.
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                            Toast.makeText(getActivity().getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
                        }
                    }, null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */

    private void configureTransform(int viewWidth, int viewHeight) {

        if (mTextureView == null || mPreviewSize == null) return;

        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }


    /**
     * Compares two {@code Size}s based on their areas.
     */
    private static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    public class CameraResultReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String detectionListJson = intent.getStringExtra("detectionListData");
            Log.d(TAG, "onReceive: data received -------------> " + detectionListJson);

            detectionList = new Gson().fromJson(detectionListJson, DetectionList.class);
            resultText.setText(detectionListJson);
            drawRectangle(detectionList);
        }
    }


    private int getScreenSizeWidth() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        return width;
    }


    private void drawRectangle(DetectionList detectionList) {
        Canvas canvas = mHolder.lockCanvas();

        Log.d(TAG, "drawRectangle: W: " + canvas.getWidth());
        Log.d(TAG, "drawRectangle: H: " + canvas.getHeight());

        if (canvas == null) {
            Log.e(TAG, "Cannot draw onto the canvas as it's null");
        } else {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

            Paint myPaint = new Paint();
            myPaint.setColor(Color.GREEN);
            myPaint.setStrokeWidth(10);
            myPaint.setStyle(Paint.Style.STROKE);

            Paint textBackground = new Paint();
            textBackground.setColor(Color.GREEN);
            textBackground.setStrokeWidth(10);
            textBackground.setStyle(Paint.Style.FILL_AND_STROKE);

            Paint pen = new Paint();
            pen.setColor(Color.BLACK);
            pen.setStyle(Paint.Style.FILL);
            pen.setTextSize(35);


            for (Detection d : detectionList.getDetectionList()) {


                canvas.drawRect(1080 * d.getXmin(), (1080 * d.getYmin()) - 50, (1080 * d.getXmin()) + (d.getClassName().length() * 20), (1080 * d.getYmin()), textBackground);
                canvas.drawRect(1080 * d.getXmin(), 1080 * d.getYmin(), 1080 * d.getXmax(), 1080 * d.getYmax(), myPaint);
                canvas.drawText(d.getClassName(), 1080 * d.getXmin(), (1080 * d.getYmin()) - 10, pen);
            }
            mHolder.unlockCanvasAndPost(canvas);
        }

    }
}
