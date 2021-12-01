package com.general.files;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.levaeu.driver.ActiveTripActivity;
import com.levaeu.driver.ChatActivity;
import com.levaeu.driver.DriverArrivedActivity;
import com.levaeu.driver.R;
import com.squareup.picasso.Picasso;
import com.utils.CommonUtilities;
import com.utils.Logger;
import com.utils.Utils;
import com.view.MTextView;
import com.view.SelectableRoundedImageView;
import com.view.simpleratingbar.SimpleRatingBar;

import org.json.JSONObject;

import java.util.HashMap;

public class OpenPassengerDetailDialog {

    Context mContext;
    HashMap<String, String> data_trip;
    GeneralFunctions generalFunc;

    android.support.v7.app.AlertDialog alertDialog;

    ProgressBar LoadingProgressBar;
    boolean isnotification;
    String vImage = "";
    String vName = "";

    public OpenPassengerDetailDialog(Context mContext, HashMap<String, String> data_trip, GeneralFunctions generalFunc, boolean isnotification) {
        this.mContext = mContext;
        this.data_trip = data_trip;
        this.generalFunc = generalFunc;
        this.isnotification = isnotification;

        show();
    }

    public void sinchCall() {
        if (MyApp.getInstance().getCurrentAct() != null && (MyApp.getInstance().getCurrentAct() instanceof DriverArrivedActivity ||
                MyApp.getInstance().getCurrentAct() instanceof ActiveTripActivity)) {
            String userProfileJsonObj = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);

            if (generalFunc.isCallPermissionGranted(false) == false) {
                generalFunc.isCallPermissionGranted(true);
            } else {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("Id", generalFunc.getMemberId());

                hashMap.put("Name", generalFunc.getJsonValue("vName", userProfileJsonObj));
                hashMap.put("PImage", generalFunc.getJsonValue("vImage", userProfileJsonObj));
                hashMap.put("type", Utils.userType);


                if (MyApp.getInstance().getCurrentAct() instanceof DriverArrivedActivity) {
                    DriverArrivedActivity activity = (DriverArrivedActivity) MyApp.getInstance().getCurrentAct();

                } else {


                    ActiveTripActivity activity = (ActiveTripActivity) MyApp.getInstance().getCurrentAct();

                }


            }


        }
    }

    public void show() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(mContext);
        builder.setTitle("");

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.design_passenger_detail_dialog, null);
        builder.setView(dialogView);

        LoadingProgressBar = ((ProgressBar) dialogView.findViewById(R.id.LoadingProgressBar));

        ((MTextView) dialogView.findViewById(R.id.rateTxt)).setText(generalFunc.convertNumberWithRTL(data_trip.get("PRating")));
        ((MTextView) dialogView.findViewById(R.id.nameTxt)).setText(data_trip.get("PName"));
        vName = data_trip.get("PName");

        String msg = "";
        if (data_trip.get("REQUEST_TYPE").equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
            msg = generalFunc.retrieveLangLBl("", "LBL_USER_DETAIL");
        } else {
            msg = generalFunc.retrieveLangLBl("", "LBL_PASSENGER_DETAIL");
        }

        ((MTextView) dialogView.findViewById(R.id.passengerDTxt)).setText(msg);
        ((MTextView) dialogView.findViewById(R.id.callTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_CALL_TXT"));
        ((MTextView) dialogView.findViewById(R.id.msgTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_MESSAGE_TXT"));
        ((SimpleRatingBar) dialogView.findViewById(R.id.ratingBar)).setRating(generalFunc.parseFloatValue(0, data_trip.get("PRating")));
        if (data_trip.containsKey("eBookingFrom") && Utils.checkText(data_trip.get("eBookingFrom")) && data_trip.get("eBookingFrom").equalsIgnoreCase(Utils.eSystem_Type_KIOSK))
        {
            (dialogView.findViewById(R.id.msgArea)).setVisibility(View.GONE);
        }
        else
        {
            (dialogView.findViewById(R.id.msgArea)).setVisibility(View.VISIBLE);
        }
        String image_url = CommonUtilities.USER_PHOTO_PATH + data_trip.get("PassengerId") + "/"
                + data_trip.get("PPicName");


        if (!data_trip.get("PPicName").equals("")) {
            vImage = image_url;
        }

        Picasso.get()
                .load(image_url)
                .placeholder(R.mipmap.ic_no_pic_user)
                .error(R.mipmap.ic_no_pic_user)
                .into(((SelectableRoundedImageView) dialogView.findViewById(R.id.passengerImgView)));

        (dialogView.findViewById(R.id.callArea)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (alertDialog != null) {
                    alertDialog.dismiss();
                }
                ///getMaskNumber();
                String userprofileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
                if (generalFunc.getJsonValue("RIDE_DRIVER_CALLING_METHOD", userprofileJson).equals("Voip")) {
                    sinchCall();
                } else {
                    getMaskNumber();
                }
            }
        });


        (dialogView.findViewById(R.id.msgArea)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (alertDialog != null) {
                    alertDialog.dismiss();
                }

                Bundle bnChat = new Bundle();

                bnChat.putString("iFromMemberId", data_trip.get("PassengerId"));
                bnChat.putString("FromMemberImageName", data_trip.get("PPicName"));
                bnChat.putString("iTripId", data_trip.get("iTripId"));
                bnChat.putString("FromMemberName", data_trip.get("PName"));

                new StartActProcess(mContext).startActWithData(ChatActivity.class, bnChat);

            }
        });

        (dialogView.findViewById(R.id.closeImg)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (alertDialog != null) {
                    alertDialog.dismiss();
                }
            }
        });


        alertDialog = builder.create();
        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(alertDialog);
        }
        alertDialog.show();
        if (isnotification) {
            isnotification = false;
            dialogView.findViewById(R.id.msgArea).performClick();
        }
    }

    public void getMaskNumber() {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getCallMaskNumber");
        parameters.put("iTripid", data_trip.get("iTripId"));
        parameters.put("UserType", Utils.userType);
        parameters.put("iMemberId", generalFunc.getMemberId());

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(mContext, parameters);
        exeWebServer.setLoaderConfig(mContext, true, generalFunc);

        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObject=generalFunc.getJsonObject(responseString);
            if (responseStringObject != null && !responseStringObject.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObject);

                if (isDataAvail == true) {
                    String message = generalFunc.getJsonValueStr(Utils.message_str, responseStringObject);
                    call(message);
                } else {
                    call(data_trip.get("PPhone"));

                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

    public void call(String phoneNumber) {

        try {

            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            // callIntent.setData(Uri.parse("tel:" + data_trip.get("PPhoneC") + "" + data_trip.get("PPhone")));
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            mContext.startActivity(callIntent);

        } catch (Exception e) {
        }
    }

}
