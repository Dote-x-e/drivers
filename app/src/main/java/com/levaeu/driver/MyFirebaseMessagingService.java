package com.levaeu.driver;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.general.files.FireTripStatusMsg;
import com.general.files.MyApp;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.utils.Utils;

import java.io.IOException;
import java.util.Map;

/**
 * Created by Admin on 29-07-2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static final String TAG1 = "MyFirebaseIIDService";

    String authorizedEntity; // Project id from Google Developer Console
    String scope = "GCM"; // e.g. communicating using GCM, but you can use any
    // URL-safe characters up to a maximum of 1000, or
    // you can also leave it blank.

    @Override
    public void onNewToken(String s) {
        // depricated
//        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        if (!Utils.checkText(authorizedEntity)) {
            authorizedEntity = MyApp.getInstance().getGeneralFun(this).retrieveValue(Utils.APP_GCM_SENDER_ID_KEY);
        }
        String refreshedToken = null;
        try {
            refreshedToken = FirebaseInstanceId.getInstance(FirebaseApp.initializeApp(this)).getToken(authorizedEntity, scope);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Displaying token on logcat

        sendRegistrationToServer(refreshedToken);
        super.onNewToken(s);
    }


    private void sendRegistrationToServer(String token) {
        //You can implement this method to store the token on your server
        //Not required for current project
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Map data = remoteMessage.getData();



        if (!Utils.checkText(authorizedEntity)) {
            authorizedEntity = MyApp.getInstance().getGeneralFun(this).retrieveValue(Utils.APP_GCM_SENDER_ID_KEY);
        }


        if (remoteMessage == null || remoteMessage.getData() == null/* || remoteMessage.getNotification().getBody() == null*/) {
            return;
        }

        String message = remoteMessage.getData().get("message");

        new FireTripStatusMsg(MyApp.getInstance() != null ? MyApp.getInstance().getCurrentAct() : getApplicationContext(), "Push").fireTripMsg(message);
    }
}