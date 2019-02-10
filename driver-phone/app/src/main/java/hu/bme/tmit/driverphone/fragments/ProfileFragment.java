package hu.bme.tmit.driverphone.fragments;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import hu.bme.tmit.driverphone.R;
import hu.bme.tmit.driverphone.util.datadownload.DownloadAsyncTask;
import hu.bme.tmit.driverphone.util.device.DevicePreferences;

/**
 * Felhasználói beállításokért felelős Fragment.
 */
public class ProfileFragment extends Fragment implements ProfileFragmentInterface {

    private static final String USER_NAME = "user_name";
    private OnFragmentInteractionListener mListener;
    private static final String TAG = ProfileFragment.class.getName();

    // View
    private TextView userNameTextView;
    private Button logOut;
    private Button uploadFTP;
    private String userEmail;
    private TextView neuralNetworkPath;
    private Button downloadNewModel;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(String userName) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(USER_NAME, userName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userEmail = getArguments().getString(USER_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        initView(v);
        return v;
    }

    private void initView(View v) {
        userNameTextView = v.findViewById(R.id.fragment_profile_user_name);
        userNameTextView.setText(userEmail);
        neuralNetworkPath = v.findViewById(R.id.neural_network_path_view);
        neuralNetworkPath.setText(new DevicePreferences(getContext()).getNeuralNetLocation());
        neuralNetworkPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick:");
                mListener.showNeuralNetPathDialog();
            }
        });
        logOut = v.findViewById(R.id.fragment_profile_log_out);
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.logOutProfileFragment();
            }
        });
        uploadFTP = v.findViewById(R.id.fragment_profile_ftp_upload);
        uploadFTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.ftpUpload();
            }
        });
        downloadNewModel = v.findViewById(R.id.fragment_profile_download);
        downloadNewModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadAsyncTask downloadAsyncTask = new DownloadAsyncTask();
                downloadAsyncTask.execute(getContext());

            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.logOutProfileFragment();
            mListener.ftpUpload();
            mListener.showNeuralNetPathDialog();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void updateNeuralNetworkPath() {
        neuralNetworkPath.setText(new DevicePreferences(getContext()).getNeuralNetLocation());
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void logOutProfileFragment();
        void ftpUpload();
        void showNeuralNetPathDialog();
    }

}
