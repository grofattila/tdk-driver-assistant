package hu.bme.tmit.driverphone.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import hu.bme.tmit.driverphone.R;
import hu.bme.tmit.driverphone.util.device.DevicePreferences;

public class NeuralNetPathDialog extends DialogFragment {
    private Button positive;
    private Button negativ;
    private TextView oldNeuralNetPath;
    private EditText newNeuralNetPath;

    private OnCompleteListener positiveButtonHit;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_neural_network_path, null);

        oldNeuralNetPath = (TextView)v.findViewById(R.id.new_neural_network_path_old);
        oldNeuralNetPath.setText(new DevicePreferences(getActivity().getApplicationContext()).getNeuralNetLocation());

        newNeuralNetPath = (EditText)v.findViewById(R.id.new_neural_network_path);
        if (new DevicePreferences(getActivity().getApplicationContext()).getNeuralNetLocation() != null)
            newNeuralNetPath.setText(new DevicePreferences(getActivity().getApplicationContext()).getNeuralNetLocation());
        positive = (Button)v.findViewById(R.id.dialog_server_ip_address_positive_button);
        negativ = (Button)v.findViewById(R.id.dialog_server_ip_address_negativ_button);

        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DevicePreferences(getActivity().getApplicationContext()).setNeuralNetLocation(newNeuralNetPath.getText().toString());
                positiveButtonHit.onComplete();
                dismiss();
            }
        });

        negativ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        builder.setView(v);
        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return dialog;
    }

    public interface OnCompleteListener {
        void onComplete();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.positiveButtonHit = (OnCompleteListener)activity;
        }
        catch (final ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnCompleteListener");
        }
    }
}
