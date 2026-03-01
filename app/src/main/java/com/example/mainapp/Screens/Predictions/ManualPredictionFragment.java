package com.example.mainapp.Screens.Predictions;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mainapp.R;
import com.example.mainapp.Utils.DatabaseUtils.DataHelper;

import java.util.concurrent.CountDownLatch;

public class ManualPredictionFragment extends Fragment {

    private EditText edtRedTeam1, edtRedTeam2, edtRedTeam3;
    private EditText edtBlueTeam1, edtBlueTeam2, edtBlueTeam3;
    private Button btnCalculate;
    private TextView predictionTxt;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manual_prediction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edtRedTeam1   = view.findViewById(R.id.edtRedTeam1);
        edtRedTeam2   = view.findViewById(R.id.edtRedTeam2);
        edtRedTeam3   = view.findViewById(R.id.edtRedTeam3);
        edtBlueTeam1  = view.findViewById(R.id.edtBlueTeam1);
        edtBlueTeam2  = view.findViewById(R.id.edtBlueTeam2);
        edtBlueTeam3  = view.findViewById(R.id.edtBlueTeam3);
        btnCalculate  = view.findViewById(R.id.btnCalculate);
        predictionTxt = view.findViewById(R.id.txtResult);

        btnCalculate.setOnClickListener(v -> {
            if (validateInputs()) {
                calculate();
            }
        });
    }

    private boolean validateInputs() {
        EditText[] all = {edtRedTeam1, edtRedTeam2, edtRedTeam3,
                edtBlueTeam1, edtBlueTeam2, edtBlueTeam3};
        for (EditText et : all) {
            if (et.getText().toString().trim().isEmpty()) {
                Toast.makeText(requireContext(), "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    private void calculate() {
        predictionTxt.setText("מחשב חיזוי...");
        predictionTxt.setTextColor(0xFF7C6F8E);

        String[] red  = {edtRedTeam1.getText().toString().trim(),
                edtRedTeam2.getText().toString().trim(),
                edtRedTeam3.getText().toString().trim()};
        String[] blue = {edtBlueTeam1.getText().toString().trim(),
                edtBlueTeam2.getText().toString().trim(),
                edtBlueTeam3.getText().toString().trim()};

        CountDownLatch latch = new CountDownLatch(6);
        double[] avgs = new double[6];

        for (int i = 0; i < 3; i++) {
            final int ri = i, bi = i + 3;
            DataHelper.getInstance().getAvgOfTeam(red[i], 1, new DataHelper.DataCallback<Double>() {
                @Override public void onSuccess(Double d) { avgs[ri] = d; latch.countDown(); }
                @Override public void onFailure(String e) { avgs[ri] = 0; latch.countDown(); }
            });
            DataHelper.getInstance().getAvgOfTeam(blue[i], 1, new DataHelper.DataCallback<Double>() {
                @Override public void onSuccess(Double d) { avgs[bi] = d; latch.countDown(); }
                @Override public void onFailure(String e) { avgs[bi] = 0; latch.countDown(); }
            });
        }

        new Thread(() -> {
            try {
                latch.await();
                double redAvg  = (avgs[0] + avgs[1] + avgs[2]) / 3.0;
                double blueAvg = (avgs[3] + avgs[4] + avgs[5]) / 3.0;
                requireActivity().runOnUiThread(() -> {
                    String result;
                    if (redAvg > blueAvg) {
                        result = "הברית האדומה תנצח! 🔴\n";
                        predictionTxt.setTextColor(Color.rgb(255, 80, 80));
                    } else if (blueAvg > redAvg) {
                        result = "הברית הכחולה תנצח! 🔵\n";
                        predictionTxt.setTextColor(Color.rgb(80, 140, 255));
                    } else {
                        result = "תיקו! ⚖️\n";
                        predictionTxt.setTextColor(0xFFF0E6FF);
                    }
                    result += String.format("אדום: %.2f | כחול: %.2f", redAvg, blueAvg);
                    predictionTxt.setText(result);
                });
            } catch (InterruptedException e) { e.printStackTrace(); }
        }).start();
    }
}