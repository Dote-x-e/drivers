package com.general.files;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;

import com.levaeu.driver.BuildConfig;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.GenerateAlertBox;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Admin on 19-06-2017.
 */

public class GetUserData {

    GeneralFunctions generalFunc;
    Context mContext;
    boolean releaseCurrActInstance = true;

    public GetUserData(GeneralFunctions generalFunc, Context mContext) {
        this.generalFunc = generalFunc;
        this.mContext = mContext;
        this.releaseCurrActInstance = true;

    }

    public GetUserData(GeneralFunctions generalFunc, Context mContext, boolean releaseCurrActInstance) {
        this.generalFunc = generalFunc;
        this.mContext = mContext;
        this.releaseCurrActInstance = releaseCurrActInstance;
    }

    public void getData() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getDetail");
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("vDeviceType", Utils.deviceType);
        parameters.put("UserType", Utils.app_type);
        parameters.put("AppVersion", BuildConfig.VERSION_NAME);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(mContext, parameters);
        exeWebServer.setLoaderConfig(mContext, true, generalFunc);
        exeWebServer.setIsDeviceTokenGenerate(true, "vDeviceToken", generalFunc);
        exeWebServer.setCancelAble(false);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObject=generalFunc.getJsonObject(responseString);

            if (responseStringObject != null && !responseStringObject.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObject);

                String message = generalFunc.getJsonValueStr(Utils.message_str, responseStringObject);

                if (message.equals("SESSION_OUT")) {
                    MyApp.getInstance().notifySessionTimeOut();
                    Utils.runGC();
                    return;
                }

                if (isDataAvail == true) {
                    generalFunc.storeData(Utils.USER_PROFILE_JSON, generalFunc.getJsonValueStr(Utils.message_str, responseStringObject));
                    new OpenMainProfile(mContext,
                            generalFunc.getJsonValueStr(Utils.message_str, responseStringObject), true, generalFunc).startProcess();
                    if (releaseCurrActInstance) {
                        Handler handler = new Handler();
                        handler.postDelayed(() -> {
                            try {
                                ActivityCompat.finishAffinity((Activity) mContext);
                                Utils.runGC();
                            } catch (Exception e) {

                            }
                        }, 300);
                    }
                } else {
                    if (!generalFunc.getJsonValueStr("isAppUpdate", responseStringObject).trim().equals("")
                            && generalFunc.getJsonValueStr("isAppUpdate", responseStringObject).equals("true")) {

                    } else {

                        if (generalFunc.getJsonValueStr(Utils.message_str, responseStringObject).equalsIgnoreCase("LBL_CONTACT_US_STATUS_NOTACTIVE_COMPANY") ||
                                generalFunc.getJsonValueStr(Utils.message_str, responseStringObject).equalsIgnoreCase("LBL_ACC_DELETE_TXT") ||
                                generalFunc.getJsonValueStr(Utils.message_str, responseStringObject).equalsIgnoreCase("LBL_CONTACT_US_STATUS_NOTACTIVE_DRIVER")) {

                            GenerateAlertBox alertBox = generalFunc.notifyRestartApp("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObject)));
                            alertBox.setCancelable(false);
                            alertBox.setBtnClickList(btn_id -> {

                                if (btn_id == 1) {
                                    MyApp.getInstance().logOutFromDevice(true);
                                }
                            });
                            return;
                        }

                    }

                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_TRY_AGAIN_TXT"), "", generalFunc.retrieveLangLBl("", "LBL_RETRY_TXT"), buttonId -> generalFunc.restartApp());
                }
            } else {

                generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_TRY_AGAIN_TXT"), "", generalFunc.retrieveLangLBl("", "LBL_RETRY_TXT"), buttonId -> generalFunc.restartApp());
            }
        });
        exeWebServer.execute();
    }
}
