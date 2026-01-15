package com.example.mainapp.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.example.mainapp.R;

public class InternetUtils {
    public static boolean isInternetConnected(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }

    public static boolean isInternetConnectedWithAlert(Context context) {
        if (isInternetConnected(context)) return true;
        new AlertDialog.Builder(context)
                .setTitle("אין חיבור לאינטרנט")
                .setMessage("מסך זה מצריך גישה לאינטרנט, התחבר לאינטרנט על מנת לגשת למסך")
                .setPositiveButton("אישור", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();

        return false;
    }

}
