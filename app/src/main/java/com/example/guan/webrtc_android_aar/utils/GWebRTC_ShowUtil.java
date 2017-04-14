package com.example.guan.webrtc_android_aar.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by guan on 2/25/17.
 */

public class GWebRTC_ShowUtil {

    public static Toast logToast;

    // Log |msg| and Toast about it.
    public static void logAndToast(Context context,String msg) {
        String TAG=context.toString();
        Log.e(TAG, msg);
        if (logToast != null) {
            logToast.cancel();
        }
        logToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        logToast.show();
    }

}
