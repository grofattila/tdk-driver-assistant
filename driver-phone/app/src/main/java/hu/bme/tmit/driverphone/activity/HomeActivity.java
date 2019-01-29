package hu.bme.tmit.driverphone.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;

import hu.bme.tmit.driverphone.R;
import hu.bme.tmit.driverphone.dialog.NeuralNetPathDialog;
import hu.bme.tmit.driverphone.fragments.DetectFragment;
import hu.bme.tmit.driverphone.fragments.ProfileFragment;
import hu.bme.tmit.driverphone.fragments.ProfileFragmentInterface;
import hu.bme.tmit.driverphone.fragments.RecordFragment;
import hu.bme.tmit.driverphone.service.FtpUploadThread;
import hu.bme.tmit.driverphone.util.Permission;

/**
 * This is the Main activity, this is responsible
 */
public class HomeActivity extends FragmentActivity implements ProfileFragment.OnFragmentInteractionListener, NeuralNetPathDialog.OnCompleteListener {

    private static final String TAG = HomeActivity.class.getName();

    // View
    private BottomNavigationView bottomNavigationView;

    //Firebase
    private FirebaseAuth auth;

    private static int selectedMenu = R.id.action_detect;

    private ProfileFragmentInterface profileFragmentInterfaceListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initFirebase();
        initView();
        initListener();
        initPermission();
    }

    private void initListener() {
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                        Log.d(TAG, "onNavigationItemSelected: " + item.getItemId());

                        switch (item.getItemId()) {
                            case R.id.action_detect:
                                selectedMenu = R.id.action_detect;
                                loadDetectFragment();
                                break;
                            case R.id.action_record:
                                selectedMenu = R.id.action_record;
                                loadRecordFragment();
                                break;
                            case R.id.action_profile:
                                selectedMenu = R.id.action_profile;
                                loadProfileFragment();
                                break;
                        }
                        return true;
                    }
                });
    }

    private void initPermission() {
        Permission.checkAllPermissions(this);
    }


    private void initView() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        loadDetectFragment();
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
    }

    private void logOut() {
        auth.signOut();
        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
        finish();
    }

    private void loadProfileFragment() {
        ProfileFragment profileFragment = ProfileFragment.newInstance(auth.getCurrentUser().getEmail());
        setListener(profileFragment);
        getFragmentManager().beginTransaction().replace(R.id.home_frame_layout, profileFragment).commit();
    }

    private void loadDetectFragment() {
        getFragmentManager().beginTransaction().replace(R.id.home_frame_layout, DetectFragment.newInstance()).commit();
    }

    private void loadRecordFragment() {
        getFragmentManager().beginTransaction().replace(R.id.home_frame_layout, RecordFragment.newInstance()).commit();
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadSelectedMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void logOutProfileFragment() {
        logOut();
    }

    @Override
    public void ftpUpload() {
        Log.d(TAG, "ftpUpload");
        new FtpUploadThread(this).start();
    }

    @Override
    public void showNeuralNetPathDialog() {
        Log.d(TAG, "showNeuralNetPathDialog");
        new NeuralNetPathDialog().show(getSupportFragmentManager(), "dialog");
    }

    public String getUserName() {
        return auth.getCurrentUser().getEmail();
    }

    private void loadSelectedMenu() {

        Log.d(TAG, "loadSelectedMenu: " + selectedMenu);

        switch (selectedMenu) {
            case R.id.action_detect:
                loadDetectFragment();
                break;
            case R.id.action_record:
                loadRecordFragment();
                break;
            case R.id.action_profile:
                loadProfileFragment();
                break;
            default:
                loadDetectFragment();
                break;
        }
    }


    @Override
    public void onComplete() {
        Log.d(TAG, "onComplete");
        profileFragmentInterfaceListener.updateNeuralNetworkPath();
    }

    public void setListener(ProfileFragmentInterface profileFragmentInterfaceListener) {
        this.profileFragmentInterfaceListener = profileFragmentInterfaceListener;
    }

}
