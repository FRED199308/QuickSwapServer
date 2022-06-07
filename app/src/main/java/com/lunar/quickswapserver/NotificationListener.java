package com.lunar.quickswapserver;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;

@SuppressLint("OverrideAbstract")
public class NotificationListener extends NotificationListenerService {

    private static final String TAG = "NotificationListener";
    private static final String WA_PACKAGE = "com.gbwhatsapp";

    @Override
    public void onListenerConnected() {
        Log.i(TAG, "Notification Listener connected");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (!sbn.getPackageName().equals(WA_PACKAGE))
        {
            Notification notification = sbn.getNotification();
            Bundle bundle = notification.extras;
            System.err.println("This is bu :"+bundle);
            return;
        }
        else{

            Notification notification = sbn.getNotification();
            Bundle bundle = notification.extras;
            System.err.println("This is bu :"+bundle);
            String from = bundle.getString(NotificationCompat.EXTRA_TITLE);
            String message = bundle.getString(NotificationCompat.EXTRA_TEXT);

            Log.i(TAG, "From: " + from);
            Log.i(TAG, "Message: " + message);
            String mPhoneNumber = from;
          //  mPhoneNumber = mPhoneNumber.replaceAll(" ", "").replaceAll(" ", "").replaceAll("-","");
            String mMessage = "Hello world";
            String mSendToWhatsApp = "https://wa.me/" + mPhoneNumber + "?text="+mMessage;
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(
                            mSendToWhatsApp
                    )));
        }


    }

}