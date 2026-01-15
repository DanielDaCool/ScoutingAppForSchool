package com.example.mainapp.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.widget.Toast;

public class InternetReciver extends BroadcastReceiver {
    private static final String TAG = "WifiReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            if (InternetUtils.isInternetConnected(context)) {
                Toast.makeText(context, "מחובר לאינטרנט ✓",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "מנותק מהאינטרנט ✗",
                        Toast.LENGTH_LONG).show();
            }
        }
    }


}
