package com.levaeu.driver;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.general.files.ConfigPubNub;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.GetLocationUpdates;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.utils.CabRequestStatus;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.GenerateAlertBox;
import com.view.MTextView;
import com.view.simpleratingbar.SimpleRatingBar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

@SuppressWarnings("ResourceType")
public class CabRequestedActivity extends AppCompatActivity implements GenerateAlertBox.HandleAlertBtnClick, OnMapReadyCallback, GetLocationUpdates.LocationUpdatesListener {

    public GeneralFunctions generalFunc;
    MTextView leftTitleTxt;
    MTextView rightTitleTxt;
    ProgressBar mProgressBar;
    RelativeLayout progressLayout;
    String message_str;
    String msgCode;
    MTextView pNameTxtView;
    MTextView locationAddressTxt, ufxlocationAddressTxt;
    MTextView destAddressTxt;
    LinearLayout viewDetailsArea;
    String pickUpAddress = "";
    String destinationAddress = "";
    boolean eFly = false;

    GenerateAlertBox generateAlert;
    int maxProgressValue = 30;
    MediaPlayer mp = new MediaPlayer();
    private MTextView textViewShowTime, ufxtvTimeCount; // will show the time
    private CountDownTimer countDownTimer; // built in android class
    // CountDownTimer
    private long totalTimeCountInMilliseconds = maxProgressValue * 1 * 1000; // total count down time in
    // milliseconds
    private long timeBlinkInMilliseconds = 10 * 1000; // start time of start blinking
    private boolean blink; // controls the blinking .. on and off

    private MTextView locationAddressHintTxt, ufxlocationAddressHintTxt;
    private MTextView destAddressHintTxt;
    private MTextView serviceType, ufxserviceType;

    SimpleRatingBar ratingBar;
    boolean istimerfinish = false;
    boolean isloadedAddress = false;
    FrameLayout progressLayout_frame, ufxprogressLayout_frame;
    MTextView specialHintTxt, specialValTxt;
    String specialUserComment = "";

    boolean isUfx = false;
    ImageView backImageView;
    MTextView pkgType;
    private Location userLocation;
    MTextView moreSeriveTxt;

    String LBL_REQUEST, LBL_DELIVERY, LBL_RIDE, LBL_JOB_TXT, LBL_RENTAL_RIDE_REQUEST,LBL_RENTAL_AIRCRAFT_REQUEST;
    String LBL_RECIPIENT, LBL_PAYMENT_MODE_TXT, LBL_TOTAL_DISTANCE, LBL_Total_Fare_TXT, LBL_POOL_REQUEST, LBL_PERSON,LBL_FLY_REQUEST;
    String REQUEST_TYPE = "";
    long milliLeft;
    String iCabRequestId = "";
    String userProfileJson = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        setContentView(R.layout.activity_cab_requested);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
        generalFunc.removeValue(Utils.DRIVER_ACTIVE_REQ_MSG_KEY);


        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        MyApp.getInstance().stopAlertService();

        message_str = getIntent().getStringExtra("Message");
        msgCode = generalFunc.getJsonValue("MsgCode", message_str);
        iCabRequestId = generalFunc.getJsonValue("iCabRequestId", message_str);


        if (generalFunc.containsKey(Utils.DRIVER_REQ_COMPLETED_MSG_CODE_KEY + msgCode)) {
            // generalFunc.restartApp();
            // MyApp.getInstance().restartWithGetDataApp();
            finish();
            return;
        } else {
            generalFunc.storeData(Utils.DRIVER_REQ_COMPLETED_MSG_CODE_KEY + msgCode, "true");
            generalFunc.storeData(Utils.DRIVER_REQ_COMPLETED_MSG_CODE_KEY + msgCode, "" + System.currentTimeMillis());
        }
        generalFunc.storeData(Utils.DRIVER_CURRENT_REQ_OPEN_KEY, "true");

        (new CabRequestStatus(getActContext())).updateDriverRequestStatus(1, generalFunc.getJsonValue("PassengerId", message_str), "Open", "", msgCode);

        if (GetLocationUpdates.getInstance() != null) {
            GetLocationUpdates.getInstance().startLocationUpdates(this, this);
        }

        moreSeriveTxt = (MTextView) findViewById(R.id.moreSeriveTxt);

        leftTitleTxt = (MTextView) findViewById(R.id.leftTitleTxt);
        rightTitleTxt = (MTextView) findViewById(R.id.rightTitleTxt);
        pNameTxtView = (MTextView) findViewById(R.id.pNameTxtView);
        locationAddressTxt = (MTextView) findViewById(R.id.locationAddressTxt);
        ufxlocationAddressTxt = (MTextView) findViewById(R.id.ufxlocationAddressTxt);
        locationAddressHintTxt = (MTextView) findViewById(R.id.locationAddressHintTxt);
        ufxlocationAddressHintTxt = (MTextView) findViewById(R.id.ufxlocationAddressHintTxt);
        destAddressHintTxt = (MTextView) findViewById(R.id.destAddressHintTxt);
        destAddressTxt = (MTextView) findViewById(R.id.destAddressTxt);
        viewDetailsArea = (LinearLayout) findViewById(R.id.viewDetailsArea);
        progressLayout = (RelativeLayout) findViewById(R.id.progressLayout);
        specialHintTxt = (MTextView) findViewById(R.id.specialHintTxt);
        specialValTxt = (MTextView) findViewById(R.id.specialValTxt);
        backImageView = (ImageView) findViewById(R.id.backImageView);
        pkgType = (MTextView) findViewById(R.id.pkgType);
        backImageView.setVisibility(View.GONE);

        progressLayout_frame = (FrameLayout) findViewById(R.id.progressLayout_frame);
        ufxprogressLayout_frame = (FrameLayout) findViewById(R.id.ufxprogressLayout_frame);


        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        ratingBar = (SimpleRatingBar) findViewById(R.id.ratingBar);

        textViewShowTime = (MTextView) findViewById(R.id.tvTimeCount);
        ufxtvTimeCount = (MTextView) findViewById(R.id.ufxtvTimeCount);
        serviceType = (MTextView) findViewById(R.id.serviceType);
        ufxserviceType = (MTextView) findViewById(R.id.ufxserviceType);

        (findViewById(R.id.menuImgView)).setVisibility(View.GONE);
        leftTitleTxt.setVisibility(View.VISIBLE);
        rightTitleTxt.setVisibility(View.VISIBLE);


        maxProgressValue = GeneralFunctions.parseIntegerValue(30, generalFunc.retrieveValue("RIDER_REQUEST_ACCEPT_TIME"));
        totalTimeCountInMilliseconds = maxProgressValue * 1 * 1000; // total count down time in
        textViewShowTime.setText(maxProgressValue + ":" + "00");
        mProgressBar.setMax(maxProgressValue);
        mProgressBar.setProgress(maxProgressValue);


        generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setBtnClickList(this);
        generateAlert.setCancelable(false);

        REQUEST_TYPE = generalFunc.getJsonValue("REQUEST_TYPE", message_str);

        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapV2_calling_driver);

        fm.getMapAsync(this);

        LBL_REQUEST = generalFunc.retrieveLangLBl("Request", "LBL_REQUEST");
        LBL_DELIVERY = generalFunc.retrieveLangLBl("Delivery", "LBL_DELIVERY");
        LBL_RIDE = generalFunc.retrieveLangLBl("Ride", "LBL_RIDE");
        LBL_JOB_TXT = generalFunc.retrieveLangLBl("Job", "LBL_JOB_TXT");
        LBL_RENTAL_RIDE_REQUEST = generalFunc.retrieveLangLBl("", "LBL_RENTAL_RIDE_REQUEST");
        LBL_RENTAL_AIRCRAFT_REQUEST = generalFunc.retrieveLangLBl("", "LBL_RENTAL_AIRCRAFT_REQUEST");
        LBL_RECIPIENT = generalFunc.retrieveLangLBl("", "LBL_RECIPIENT");
        LBL_PAYMENT_MODE_TXT = generalFunc.retrieveLangLBl("", "LBL_PAYMENT_MODE_TXT");
        LBL_TOTAL_DISTANCE = generalFunc.retrieveLangLBl("", "LBL_TOTAL_DISTANCE");
        LBL_Total_Fare_TXT = generalFunc.retrieveLangLBl("", "LBL_Total_Fare_TXT");
        LBL_POOL_REQUEST = generalFunc.retrieveLangLBl("Ride", "LBL_POOL_REQUEST");
        LBL_PERSON = generalFunc.retrieveLangLBl("", "LBL_PERSON");
        LBL_FLY_REQUEST = generalFunc.retrieveLangLBl("", "LBL_FLY_REQUEST");
        setData();
        setLabels();


        startTimer(totalTimeCountInMilliseconds);

        progressLayout.setOnClickListener(new setOnClickList());
        leftTitleTxt.setOnClickListener(new setOnClickList());
        rightTitleTxt.setOnClickListener(new setOnClickList());
        viewDetailsArea.setOnClickListener(new setOnClickList());
        moreSeriveTxt.setOnClickListener(new setOnClickList());

        int color = getResources().getColor(R.color.appThemeColor_1);

        new CreateRoundedView(color, Utils.dipToPixels(getActContext(), 7), 0, color, moreSeriveTxt);
    }

    public void setLabels() {
        /*Multi Delivery Lables*/

        ((MTextView) findViewById(R.id.recipientHintTxt)).setText(generalFunc.isRTLmode() ? ":" + LBL_RECIPIENT : LBL_RECIPIENT + ":");

        ((MTextView) findViewById(R.id.paymentModeHintTxt)).setText(generalFunc.isRTLmode() ? ":" + LBL_PAYMENT_MODE_TXT : LBL_PAYMENT_MODE_TXT + ":");

        ((MTextView) findViewById(R.id.totalMilesHintTxt)).setText(generalFunc.isRTLmode() ? ":" + LBL_TOTAL_DISTANCE : LBL_TOTAL_DISTANCE + ":");

        ((MTextView) findViewById(R.id.totalFareHintTxt)).setText(generalFunc.isRTLmode() ? ":" + LBL_Total_Fare_TXT : LBL_Total_Fare_TXT + ":");

        leftTitleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_DECLINE_TXT"));
        rightTitleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ACCEPT_TXT"));
        if (REQUEST_TYPE.equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
            locationAddressHintTxt.setText(generalFunc.retrieveLangLBl("", "LBL_PICKUP_LOCATION_HEADER_TXT"));
            destAddressHintTxt.setText(generalFunc.retrieveLangLBl("", "LBL_DEST_ADD_TXT"));
        } else {
            locationAddressHintTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SENDER_LOCATION"));
            destAddressHintTxt.setText(generalFunc.retrieveLangLBl("", "LBL_RECEIVER_LOCATION"));
        }
        ufxlocationAddressHintTxt.setText(generalFunc.retrieveLangLBl("Job Location", "LBL_JOB_LOCATION_TXT"));

        ((MTextView) findViewById(R.id.hintTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_HINT_TAP_TXT"));
        specialHintTxt.setText(generalFunc.retrieveLangLBl("Special Instruction", "LBL_SPECIAL_INSTRUCTION_TXT"));
        moreSeriveTxt.setText(generalFunc.retrieveLangLBl("", "LBL_VIEW_REQUESTED_SERVICES"));


    }


    public void setData() {

        new CreateRoundedView(Color.parseColor("#000000"), Utils.dipToPixels(getActContext(), 122), 0, Color.parseColor("#FFFFFF"), findViewById(R.id.bgCircle));
        pNameTxtView.setText(generalFunc.getJsonValue("PName", message_str));
        ratingBar.setRating(GeneralFunctions.parseFloatValue(0, generalFunc.getJsonValue("PRating", message_str)));

        double pickupLat = GeneralFunctions.parseDoubleValue(0.0, generalFunc.getJsonValue("sourceLatitude", message_str));
        double pickupLog = GeneralFunctions.parseDoubleValue(0.0, generalFunc.getJsonValue("sourceLongitude", message_str));

        double desLat = 0.0;
        double destLog = 0.0;

        String destLatitude=generalFunc.getJsonValue("destLatitude", message_str);
        String destLongitude=generalFunc.getJsonValue("destLongitude", message_str);
        if (!destLatitude.isEmpty() && !destLongitude.isEmpty()) {

            desLat = GeneralFunctions.parseDoubleValue(0.0, destLatitude);
            destLog = GeneralFunctions.parseDoubleValue(0.0, destLongitude);

            if (desLat == 0.0 && destLog == 0.0) {
                destAddressTxt.setVisibility(View.GONE);
                destAddressHintTxt.setVisibility(View.GONE);
            } else {
                destAddressTxt.setVisibility(View.VISIBLE);
                destAddressHintTxt.setVisibility(View.VISIBLE);
            }
        }
        String serverKey = generalFunc.retrieveValue(Utils.GOOGLE_SERVER_ANDROID_DRIVER_APP_KEY);
        String url_str = "https://maps.googleapis.com/maps/api/directions/json?origin=" + pickupLat + "," + pickupLog + "&" + "destination=" + (desLat != 0.0 ? desLat : pickupLat) + "," + (destLog != 0.0 ? destLog : pickupLog) + "&sensor=true&key=" + serverKey + "&language=" + generalFunc.retrieveValue(Utils.GOOGLE_MAP_LANGUAGE_CODE_KEY) + "&sensor=true";

        if (iCabRequestId != null && !iCabRequestId.equals("")) {
            getAddressFormServer();
        } else {
            findAddressByDirectionAPI(url_str);
        }

        LinearLayout packageInfoArea = (LinearLayout) findViewById(R.id.packageInfoArea);

        String VehicleTypeName = generalFunc.getJsonValue("VehicleTypeName", message_str);
        String SelectedTypeName = generalFunc.getJsonValue("SelectedTypeName", message_str);
        eFly = generalFunc.getJsonValue("eFly", message_str).equalsIgnoreCase("Yes");

        if (REQUEST_TYPE.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
            isUfx = true;
            progressLayout_frame.setVisibility(View.GONE);
            locationAddressTxt.setVisibility(View.GONE);
            locationAddressHintTxt.setVisibility(View.GONE);
            destAddressHintTxt.setVisibility(View.GONE);
            destAddressTxt.setVisibility(View.GONE);
            ufxlocationAddressTxt.setVisibility(View.VISIBLE);
            ufxlocationAddressHintTxt.setVisibility(View.VISIBLE);
            ufxprogressLayout_frame.setVisibility(View.VISIBLE);
            specialHintTxt.setVisibility(View.VISIBLE);
            specialValTxt.setVisibility(View.VISIBLE);

            ((MTextView) findViewById(R.id.requestType)).setText(LBL_JOB_TXT + "  " + LBL_REQUEST);

            (findViewById(R.id.ufxserviceType)).setVisibility(View.VISIBLE);
            ufxserviceType.setText(SelectedTypeName);
            packageInfoArea.setVisibility(View.GONE);
        } else if (REQUEST_TYPE.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX)) {
            ((MTextView) findViewById(R.id.requestType)).setText(LBL_JOB_TXT + "  " + LBL_REQUEST);
            (findViewById(R.id.serviceType)).setVisibility(View.VISIBLE);
            serviceType.setText(SelectedTypeName);
            packageInfoArea.setVisibility(View.GONE);
        } else if (REQUEST_TYPE.equals("Deliver")) {
            (findViewById(R.id.packageInfoArea)).setVisibility(View.VISIBLE);
            ((MTextView) findViewById(R.id.packageInfoTxt)).setText(generalFunc.getJsonValue("PACKAGE_TYPE", message_str));

            if (VehicleTypeName != null && !VehicleTypeName.equalsIgnoreCase("")) {
                ((MTextView) findViewById(R.id.requestType)).setText(LBL_DELIVERY + " " + LBL_REQUEST + " (" + VehicleTypeName + ")");

            } else {
                ((MTextView) findViewById(R.id.requestType)).setText(LBL_DELIVERY + " " + LBL_REQUEST);
            }
        } else if (REQUEST_TYPE.equalsIgnoreCase(Utils.eType_Multi_Delivery)) {
            destAddressHintTxt.setVisibility(View.GONE);
            destAddressTxt.setVisibility(View.GONE);
            (findViewById(R.id.packageInfoArea)).setVisibility(View.GONE);
            (findViewById(R.id.deliver_Area)).setVisibility(View.VISIBLE);

            if (VehicleTypeName != null && !VehicleTypeName.equalsIgnoreCase("")) {
                ((MTextView) findViewById(R.id.requestType)).setText(LBL_DELIVERY + " " + LBL_REQUEST + " (" + VehicleTypeName + ")");

            } else {
                ((MTextView) findViewById(R.id.requestType)).setText(LBL_DELIVERY + " " + LBL_REQUEST);
            }

        } else {
            (findViewById(R.id.packageInfoArea)).setVisibility(View.GONE);

            if (VehicleTypeName != null && !VehicleTypeName.equalsIgnoreCase("")) {
                ((MTextView) findViewById(R.id.requestType)).setText(((eFly?LBL_FLY_REQUEST:LBL_RIDE+ " " + LBL_REQUEST))  + " (" + VehicleTypeName + ")");

            }else {
                ((MTextView) findViewById(R.id.requestType)).setText(((eFly?LBL_FLY_REQUEST:LBL_RIDE+ " " + LBL_REQUEST)) );

            }

        }
        String ePoolRequest = generalFunc.getJsonValue("ePoolRequest", message_str);
        if (ePoolRequest != null && ePoolRequest.equalsIgnoreCase("Yes")) {
            ((MTextView) findViewById(R.id.requestType)).setText(
                    LBL_POOL_REQUEST + " ( " + generalFunc.convertNumberWithRTL(generalFunc.getJsonValue("iPersonSize", message_str)) + " " +
                            LBL_PERSON + " )");
        }
    }

    public void getAddressFormServer() {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getCabRequestAddress");
        parameters.put("iCabRequestId", iCabRequestId);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);

        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObj = generalFunc.getJsonObject(responseString);

            if (responseStringObj != null && !responseStringObj.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObj);

                if (isDataAvail) {

                    String MessageJson = generalFunc.getJsonValueStr(Utils.message_str, responseStringObj);
                    pickUpAddress = generalFunc.getJsonValue("tSourceAddress", MessageJson);
                    destinationAddress = generalFunc.getJsonValue("tDestAddress", MessageJson);
                    eFly = generalFunc.getJsonValue("eFly", MessageJson).equalsIgnoreCase("Yes");
                    if (isUfx) {
                        String tUserComment = generalFunc.getJsonValue("tUserComment", MessageJson);

                        if (tUserComment != null && !tUserComment.equals("")) {
                            specialUserComment = tUserComment;
                            specialValTxt.setText(tUserComment);
                        } else {
                            specialValTxt.setText("------------");
                        }
                    }

                    String moreServices = generalFunc.getJsonValue("moreServices", MessageJson);
                    if (!moreServices.equals("") && moreServices.equals("Yes")) {
                        specialValTxt.setVisibility(View.GONE);
                        specialHintTxt.setVisibility(View.GONE);
                        moreSeriveTxt.setVisibility(View.VISIBLE);
                    }

                    String VehicleTypeName = generalFunc.getJsonValue("VehicleTypeName", MessageJson);

                    if (REQUEST_TYPE.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
                        ((MTextView) findViewById(R.id.requestType)).setText(LBL_JOB_TXT + "  " + LBL_REQUEST);
                    } else if (REQUEST_TYPE.equalsIgnoreCase(Utils.eType_Multi_Delivery)) {

                        if (VehicleTypeName != null && !VehicleTypeName.equalsIgnoreCase("")) {
                            ((MTextView) findViewById(R.id.requestType)).setText(LBL_DELIVERY + " " + LBL_REQUEST + " (" + VehicleTypeName + ")");

                        } else {
                            ((MTextView) findViewById(R.id.requestType)).setText(LBL_DELIVERY + " " + LBL_REQUEST);
                        }

                        if (REQUEST_TYPE.equalsIgnoreCase(Utils.eType_Multi_Delivery) || generalFunc.getJsonValue("eType", MessageJson).equalsIgnoreCase(Utils.eType_Multi_Delivery)) {

                            int Total_Delivery = GeneralFunctions.parseIntegerValue(0, generalFunc.getJsonValue("Total_Delivery", MessageJson));
                            String ePayType = generalFunc.getJsonValue("ePayType", MessageJson);
                            String fTripGenerateFare = generalFunc.getJsonValue("fTripGenerateFare", MessageJson);
                            String fDistance = generalFunc.getJsonValue("fDistance", MessageJson);

                            if (Total_Delivery == 1) {
                                ((MTextView) findViewById(R.id.recipientHintTxt)).setText(LBL_RECIPIENT + ":");
                            } else {
                                ((MTextView) findViewById(R.id.recipientHintTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_MULTI_RECIPIENTS") + ":");
                            }

                            ((MTextView) findViewById(R.id.recipientValTxt)).setText(Utils.checkText("" + Total_Delivery) ? " " + ("" + Total_Delivery).trim() : "");

                            ((MTextView) findViewById(R.id.paymentModeValTxt)).setText(Utils.checkText(ePayType) ? " " + ePayType.trim() : "");
                            if (generalFunc.getJsonValue("ePayWallet", MessageJson).equalsIgnoreCase("Yes")) {
                                ((MTextView) findViewById(R.id.paymentModeValTxt)).setText(generalFunc.retrieveLangLBl("Wallet", "LBL_WALLET_TXT"));
                            }

                            ((MTextView) findViewById(R.id.totalMilesValTxt)).setText(Utils.checkText(fDistance) ? " " + fDistance.trim() : "");

                            ((MTextView) findViewById(R.id.totalFareValTxt)).setText(Utils.checkText(fTripGenerateFare) ? " " + fTripGenerateFare.trim() : "");

                            destAddressHintTxt.setVisibility(View.GONE);
                            destAddressTxt.setVisibility(View.GONE);
                            (findViewById(R.id.packageInfoArea)).setVisibility(View.GONE);
                        }

                    } else if (REQUEST_TYPE.equals("Deliver") || REQUEST_TYPE.equalsIgnoreCase(Utils.eType_Multi_Delivery)) {

                        if (VehicleTypeName != null && !VehicleTypeName.equalsIgnoreCase("")) {
                            ((MTextView) findViewById(R.id.requestType)).setText(LBL_DELIVERY + " " + LBL_REQUEST + " (" + VehicleTypeName + ")");
                        } else {
                            ((MTextView) findViewById(R.id.requestType)).setText(LBL_DELIVERY + " " + LBL_REQUEST);
                        }
                    } else {

                        if (VehicleTypeName != null && !VehicleTypeName.equalsIgnoreCase("")) {
                            ((MTextView) findViewById(R.id.requestType)).setText((eFly?LBL_FLY_REQUEST:LBL_RIDE+ " " + LBL_REQUEST)  + " (" + VehicleTypeName + ")");

                        } else {
                            ((MTextView) findViewById(R.id.requestType)).setText((eFly?LBL_FLY_REQUEST:LBL_RIDE+ " " + LBL_REQUEST));
                        }

                        String PackageName = generalFunc.getJsonValue("PackageName", MessageJson);
                        if (PackageName != null && !PackageName.equalsIgnoreCase("")) {
                            pkgType.setVisibility(View.VISIBLE);
                            pkgType.setText(PackageName);

                            if (VehicleTypeName != null && !VehicleTypeName.equalsIgnoreCase("")) {
                                ((MTextView) findViewById(R.id.requestType)).setText((eFly?LBL_RENTAL_AIRCRAFT_REQUEST:LBL_RENTAL_RIDE_REQUEST) + " (" + VehicleTypeName + ")");
                            } else {
                                ((MTextView) findViewById(R.id.requestType)).setText((eFly?LBL_RENTAL_AIRCRAFT_REQUEST:LBL_RENTAL_RIDE_REQUEST));
                            }
                        }

                    }

                    isloadedAddress = true;

                    if (destinationAddress.equalsIgnoreCase("")) {
                        destinationAddress = "----";
                    }

                    destAddressTxt.setText(destinationAddress);
                    locationAddressTxt.setText(pickUpAddress);
                    ufxlocationAddressTxt.setText(pickUpAddress);

                    String ePoolRequest = generalFunc.getJsonValue("ePoolRequest", message_str);

                    if (ePoolRequest != null && ePoolRequest.equalsIgnoreCase("Yes")) {
                        ((MTextView) findViewById(R.id.requestType)).setText(
                                LBL_POOL_REQUEST + " ( " + generalFunc.convertNumberWithRTL(generalFunc.getJsonValue("iPersonSize", message_str)) + " " +
                                        LBL_PERSON + " )");
                    }


                } else {
                    new Handler().postDelayed(() -> getAddressFormServer(), 2000);

                }
            } else {
                new Handler().postDelayed(() -> getAddressFormServer(), 2000);
            }
        });
        exeWebServer.execute();
    }


    public void findAddressByDirectionAPI(final String url) {

        final ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), url, true);


        exeWebServer.setDataResponseListener(responseString -> {

            if (responseString != null && !responseString.equals("")) {

                String status = generalFunc.getJsonValue("status", responseString);

                if (status.equals("OK")) {

                    JSONArray obj_routes = generalFunc.getJsonArray("routes", responseString);
                    if (obj_routes != null && obj_routes.length() > 0) {
                        JSONObject obj_legs = generalFunc.getJsonObject(generalFunc.getJsonArray("legs", generalFunc.getJsonObject(obj_routes, 0).toString()), 0);

                        pickUpAddress = generalFunc.getJsonValue("start_address", obj_legs.toString());
                        destinationAddress = generalFunc.getJsonValue("end_address", obj_legs.toString());
                    }

                    isloadedAddress = true;

                    if (destinationAddress.equalsIgnoreCase("")) {
                        destinationAddress = "----";
                    }

                    destAddressTxt.setText(destinationAddress);
                    locationAddressTxt.setText(pickUpAddress);
                    ufxlocationAddressTxt.setText(pickUpAddress);
                } else {
                    new Handler().postDelayed(() -> findAddressByDirectionAPI(url), 2000);
                }
            } else {
                new Handler().postDelayed(() -> findAddressByDirectionAPI(url), 2000);

            }
        });
        exeWebServer.execute();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (istimerfinish) {

            trimCache(getActContext());
            istimerfinish = false;
            backImageView.setVisibility(View.VISIBLE);
            finish();
        }
    }

    public static void trimCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    @Override
    protected void onDestroy() {
        if (GetLocationUpdates.retrieveInstance() != null) {
            GetLocationUpdates.getInstance().stopLocationUpdates(this);
        }

        super.onDestroy();
        removeCustoNotiSound();
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeSound();
    }

    @Override
    public void handleBtnClick(int btn_id) {
        Utils.hideKeyboard(CabRequestedActivity.this);

        cancelRequest();
    }

    public void acceptRequest() {
        /*Stop Timer*/
        if (countDownTimer!=null)
        {
            countDownTimer.cancel();
        }
        progressLayout.setClickable(false);
        rightTitleTxt.setEnabled(false);
        leftTitleTxt.setEnabled(false);
        generateTrip();
    }

    public void generateTrip() {

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), generateTripParams());
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setCancelAble(false);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObj = generalFunc.getJsonObject(responseString);

            if (responseStringObj != null && !responseStringObj.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObj);

                if (isDataAvail) {

                    if (GetLocationUpdates.retrieveInstance() != null) {
                        GetLocationUpdates.getInstance().stopLocationUpdates(this);
                    }

                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                    }

                    removeCustoNotiSound();

                    MyApp.getInstance().restartWithGetDataApp();

                } else {
                    final String msg_str = generalFunc.getJsonValueStr(Utils.message_str, responseStringObj);

                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                    }

                    removeCustoNotiSound();

                    GenerateAlertBox alertBox = generalFunc.notifyRestartApp("", generalFunc.retrieveLangLBl("", msg_str));
                    alertBox.setCancelable(false);
                    alertBox.setBtnClickList(btn_id -> {
                        if (msg_str.equals(Utils.GCM_FAILED_KEY) || msg_str.equals(Utils.APNS_FAILED_KEY) || msg_str.equals("LBL_SERVER_COMM_ERROR") || msg_str.equals("DO_RESTART")) {
                            MyApp.getInstance().restartWithGetDataApp();
                        } else {
                            CabRequestedActivity.super.onBackPressed();
                        }
                    });
                }
            } else {
                rightTitleTxt.setEnabled(true);
                leftTitleTxt.setEnabled(true);
                //startTimer(milliLeft); // start Timer From Paused Seconds - if required in future
                generalFunc.showError(i -> MyApp.getInstance().restartWithGetDataApp());
            }
        });
        exeWebServer.execute();
    }

    public void declineTripRequest() {
        (new CabRequestStatus(getActContext())).updateDriverRequestStatus(1, generalFunc.getJsonValue("PassengerId", message_str), "Decline", "", msgCode);

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "DeclineTripRequest");
        parameters.put("DriverID", generalFunc.getMemberId());
        parameters.put("PassengerID", generalFunc.getJsonValue("PassengerId", message_str));
        parameters.put("vMsgCode", msgCode);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setCancelAble(false);
        exeWebServer.setDataResponseListener(responseString -> {
            cancelRequest();
        });
        exeWebServer.execute();
    }

    public HashMap<String, String> generateTripParams() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "GenerateTrip");
        parameters.put("DriverID", generalFunc.getMemberId());
        parameters.put("PassengerID", generalFunc.getJsonValue("PassengerId", message_str));
        parameters.put("start_lat", generalFunc.getJsonValue("sourceLatitude", message_str));
        parameters.put("start_lon", generalFunc.getJsonValue("sourceLongitude", message_str));
        parameters.put("iCabBookingId", generalFunc.getJsonValue("iBookingId", message_str));
        parameters.put("iCabRequestId", iCabRequestId);
        parameters.put("sAddress", pickUpAddress);
        parameters.put("GoogleServerKey", generalFunc.retrieveValue(Utils.GOOGLE_SERVER_ANDROID_DRIVER_APP_KEY));
        parameters.put("vMsgCode", msgCode);
        parameters.put("UserType", Utils.app_type);

        if (userLocation != null) {
            parameters.put("vLatitude", "" + userLocation.getLatitude());
            parameters.put("vLongitude", "" + userLocation.getLongitude());
        } else if (GetLocationUpdates.getInstance() != null && GetLocationUpdates.getInstance().getLastLocation() != null) {
            Location lastLocation = GetLocationUpdates.getInstance().getLastLocation();

            parameters.put("vLatitude", "" + lastLocation.getLatitude());
            parameters.put("vLongitude", "" + lastLocation.getLongitude());
        }

        parameters.put("REQUEST_TYPE", REQUEST_TYPE);

        if (REQUEST_TYPE.equalsIgnoreCase(Utils.eType_Multi_Delivery)) {
            parameters.put("ride_type", REQUEST_TYPE);
        }

        return parameters;
    }

    public void cancelRequest() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        generalFunc.storeData(Utils.DRIVER_CURRENT_REQ_OPEN_KEY, "false");

        cancelCabReq();

        try {
            CabRequestedActivity.super.onBackPressed();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void startTimer(long totalTimeCountInMilliseconds) {

        countDownTimer = new CountDownTimer(totalTimeCountInMilliseconds, 1000) {
            // 1000 means, onTick function will be called at every 1000
            // milliseconds

            @Override
            public void onTick(long leftTimeInMilliseconds) {
                milliLeft=leftTimeInMilliseconds;
                long seconds = leftTimeInMilliseconds / 1000;
                // i++;
                // Setting the Progress Bar to decrease wih the timer
                mProgressBar.setProgress((int) (leftTimeInMilliseconds / 1000));
                textViewShowTime.setTextAppearance(getActContext(), android.R.color.holo_green_dark);

                if ((seconds % 5) == 0) {
                    try {
//                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//                        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
//                        r.play();

                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                        if (generalFunc.getJsonValue("DIAL_NOTIFICATION", userProfileJson).equalsIgnoreCase("dial_notification_1.mp3")) {
                            notification = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getActContext().getPackageName() + "/" + R.raw.notification_1);
                        } else if (generalFunc.getJsonValue("DIAL_NOTIFICATION", userProfileJson).equalsIgnoreCase("dial_notification_2.mp3")) {
                            notification = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getActContext().getPackageName() + "/" + R.raw.notification_2);
                        }
                        else if (generalFunc.getJsonValue("DIAL_NOTIFICATION", userProfileJson).equalsIgnoreCase("dial_notification_3.mp3")) {
                            notification = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getActContext().getPackageName() + "/" + R.raw.notification_3);
                        }
                        else if (generalFunc.getJsonValue("DIAL_NOTIFICATION", userProfileJson).equalsIgnoreCase("dial_notification_4.mp3")) {
                            notification = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getActContext().getPackageName() + "/" + R.raw.notification_4);
                        }
                        else if (generalFunc.getJsonValue("DIAL_NOTIFICATION", userProfileJson).equalsIgnoreCase("dial_notification_5.mp3")) {
                            notification = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getActContext().getPackageName() + "/" + R.raw.notification_5);
                        }
                        else if (generalFunc.getJsonValue("DIAL_NOTIFICATION", userProfileJson).equalsIgnoreCase("dial_notification_6.mp3")) {
                            notification = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getActContext().getPackageName() + "/" + R.raw.notification_6);
                        }
                        else if (generalFunc.getJsonValue("DIAL_NOTIFICATION", userProfileJson).equalsIgnoreCase("dial_notification_7.mp3")) {
                            notification = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getActContext().getPackageName() + "/" + R.raw.notification_7);
                        }

                        else if (generalFunc.getJsonValue("DIAL_NOTIFICATION", userProfileJson).equalsIgnoreCase("dial_notification_8.mp3")) {
                            notification = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getActContext().getPackageName() + "/" + R.raw.notification_8);
                        }
                        else if (generalFunc.getJsonValue("DIAL_NOTIFICATION", userProfileJson).equalsIgnoreCase("dial_notification_9.mp3")) {
                            notification = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getActContext().getPackageName() + "/" + R.raw.notification_9);
                        }
                        else if (generalFunc.getJsonValue("DIAL_NOTIFICATION", userProfileJson).equalsIgnoreCase("dial_notification_10.mp3")) {
                            notification = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getActContext().getPackageName() + "/" + R.raw.notification_10);
                        }
                        mp = MediaPlayer.create(getApplicationContext(), notification);
                        mp.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (leftTimeInMilliseconds < timeBlinkInMilliseconds) {

                    if (blink) {
                        textViewShowTime.setVisibility(View.VISIBLE);
                        ufxtvTimeCount.setVisibility(View.VISIBLE);
                    } else {
                        textViewShowTime.setVisibility(View.INVISIBLE);
                        ufxtvTimeCount.setVisibility(View.INVISIBLE);
                    }

                    blink = !blink;
                }

                textViewShowTime
                        .setText(String.format("%02d", seconds / 60) + ":" + String.format("%02d", seconds % 60));
                ufxtvTimeCount
                        .setText(String.format("%02d", seconds / 60) + ":" + String.format("%02d", seconds % 60));

            }

            @Override
            public void onFinish() {
                istimerfinish = true;

                (new CabRequestStatus(getActContext())).updateDriverRequestStatus(1, generalFunc.getJsonValue("PassengerId", message_str), "Timeout", "", msgCode);

                textViewShowTime.setVisibility(View.VISIBLE);
                progressLayout.setClickable(false);
                rightTitleTxt.setEnabled(false);
                cancelRequest();
            }

        }.start();

    }


    public void playMedia() {
        removeSound();
        try {
            mp = new MediaPlayer();
            AssetFileDescriptor afd;
            afd = getAssets().openFd("ringtone.mp3");
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mp.prepare();
            mp.setLooping(true);
            mp.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //milan code for working all app

//        try {
//            mp = MediaPlayer.create(getActContext(), R.raw.ringdriver); mp.setLooping(true); mp.start(); }
//        catch (IllegalStateException e) { } catch (Exception e) { }
    }


    private void removeCustoNotiSound() {
        if (mp != null) {
            mp.stop();
            mp = null;
        }

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    public void removeSound() {
        if (mp != null) {
            mp.stop();
        }
    }

    public void cancelCabReq() {
        String PassengerId=generalFunc.getJsonValue("PassengerId", message_str);
        ConfigPubNub.getInstance().publishMsg("PASSENGER_" + PassengerId,
                generalFunc.buildRequestCancelJson(PassengerId, msgCode));
        generalFunc.storeData(Utils.DRIVER_CURRENT_REQ_OPEN_KEY, "false");
    }

    public Context getActContext() {
        return CabRequestedActivity.this;
    }

    @Override
    public void onBackPressed() {
        cancelCabReq();
        removeCustoNotiSound();
        super.onBackPressed();


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (GetLocationUpdates.retrieveInstance() != null) {
            GetLocationUpdates.getInstance().stopLocationUpdates(this);
        }
        GetLocationUpdates.getInstance().startLocationUpdates(this, this);

        double user_lat = GeneralFunctions.parseDoubleValue(0.0, generalFunc.getJsonValue("sourceLatitude", message_str));
        double user_lon = GeneralFunctions.parseDoubleValue(0.0, generalFunc.getJsonValue("sourceLongitude", message_str));

        googleMap.getUiSettings().setZoomControlsEnabled(false);

        MarkerOptions marker_opt = new MarkerOptions().position(new LatLng(user_lat, user_lon));

        int icon = R.drawable.taxi_passanger;

        if (REQUEST_TYPE.equals(Utils.CabGeneralType_UberX)) {
            icon = R.drawable.ufxprovider;
        }
        if (REQUEST_TYPE.equals("Deliver") || REQUEST_TYPE.equals(Utils.eType_Multi_Delivery)) {
            icon = R.drawable.taxi_passenger_delivery;
        }

        marker_opt.icon(BitmapDescriptorFactory.fromResource(icon)).anchor(0.5f, 0.5f);

        googleMap.addMarker(marker_opt);

        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(user_lat, user_lon))
                .zoom(16).build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

    @Override
    public void onLocationUpdate(Location location) {
        this.userLocation = location;
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(CabRequestedActivity.this);
            switch (view.getId()) {
                case R.id.progressLayout:
                    acceptRequest();
                    break;
                case R.id.leftTitleTxt:
                    declineTripRequest();
                    break;
                case R.id.rightTitleTxt:
                    acceptRequest();
                    break;
                case R.id.viewDetailsArea:
                    Bundle bn = new Bundle();
                    bn.putString("TripId", "");
                    bn.putString("iCabBookingId", generalFunc.getJsonValue("iBookingId", message_str));
                    bn.putString("iCabRequestId", iCabRequestId);
                    bn.putString("Status", "cabRequestScreen");

                    break;
                case R.id.moreSeriveTxt:
                    Bundle bundle = new Bundle();
                    bundle.putString("iCabRequestId", iCabRequestId);
                    break;
            }
        }
    }


}
