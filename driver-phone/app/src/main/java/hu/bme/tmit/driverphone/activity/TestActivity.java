package hu.bme.tmit.driverphone.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import hu.bme.tmit.driverphone.R;
import hu.bme.tmit.driverphone.neuralnet.ConvNetLite;

public class TestActivity extends AppCompatActivity {

    ConvNetLite convNetLite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);


        convNetLite = new ConvNetLite();
        convNetLite.init(getApplicationContext());
        convNetLite.runInference();
    }

}
