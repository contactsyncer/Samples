package com.apps.meet.userauthentication;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Created by dharamvir on 16/06/17.
 */
public class MyFcmListenerService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage message){

        String from = message.getFrom();
        Map data = message.getData();

       String val1 = (String)data.get("key1");
        String val2 = (String)data.get("key2");
        String val3 = (String)data.get("key3");
        String val4 = (String)data.get("key4");

        Log.d("Notification received", val1);
        Log.d("Notification received", val2);
        Log.d("Notification received", val3);
        Log.d("Notification received", val4);

       // do your custom handling of notification

    }


}

