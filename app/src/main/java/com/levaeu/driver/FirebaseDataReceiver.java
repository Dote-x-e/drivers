package com.levaeu.driver;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.utils.Logger;
import com.utils.Utils;

/**
 * Created by Admin on 09-08-2017.
 */

public class FirebaseDataReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.d("FirebaseDataReceiver", "called");

        GeneralFunctions generalFunctions = MyApp.getInstance().getGeneralFun(context);

        generalFunctions.storeData("isnotification", true + "");
    }
}
