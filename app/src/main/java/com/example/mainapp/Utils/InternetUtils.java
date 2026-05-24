package com.example.mainapp.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.example.mainapp.R;

/**
 * Represents the InternetUtils component in the application.
 *
 * This class is responsible for handling the logic, data flow,
 * and interactions related to its specific feature inside the Android app.
 */
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

    /**
     * Checks for internet connection. If not connected, shows an alert dialog
     * and executes a Runnable (typically to finish the activity).
     * @param context The context (should be an Activity).
     * @param onNoInternet Action to perform if there is no internet.
     * @return true if connected, false otherwise.
     */
    public static boolean checkConnection(Context context, Runnable onNoInternet) {
        if (!isInternetConnected(context)) {
            new AlertDialog.Builder(context)
                    .setTitle("אין חיבור לאינטרנט")
                    .setMessage("פעולה זו דורשת חיבור פעיל לאינטרנט. אנא בדוק את החיבור ונסה שוב.")
                    .setCancelable(false)
                    .setPositiveButton("הבנתי", (dialog, which) -> {
                        if (onNoInternet != null) onNoInternet.run();
                    })
                    .show();
            return false;
        }
        return true;
    }


}