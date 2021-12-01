package com.levaeu.driver;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.adapter.files.DrawerAdapter;
import com.adapter.files.ManageVehicleListAdapter;
import com.fragments.InactiveFragment;
import com.general.files.AddDrawer;
import com.general.files.AlarmReceiver;
import com.general.files.AppFunctions;
import com.general.files.BackgroundAppReceiver;
import com.general.files.Closure;
import com.general.files.ConfigPubNub;
import com.general.files.CustomDialog;
import com.general.files.DividerItemDecoration;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.FireTripStatusMsg;
import com.general.files.GeneralFunctions;
import com.general.files.GetAddressFromLocation;
import com.general.files.GetLocationUpdates;
import com.general.files.InternetConnection;
import com.general.files.MapAnimator;
import com.general.files.MyApp;
import com.general.files.NotificationScheduler;
import com.general.files.StartActProcess;
import com.general.files.UpdateDirections;
import com.general.files.UpdateDriverStatus;
import com.general.files.UpdateFrequentTask;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.kyleduo.switchbutton.SwitchButton;
import com.pubnub.api.enums.PNStatusCategory;
import com.utils.CommonUtilities;
import com.utils.Logger;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.SelectableRoundedImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends BaseActivity implements AdapterView.OnItemClickListener, OnMapReadyCallback, GetLocationUpdates.LocationUpdatesListener, GoogleMap.OnCameraChangeListener, UpdateFrequentTask.OnTaskRunCalled,
        ManageVehicleListAdapter.OnItemClickList, GetAddressFromLocation.AddressFound {

    public GeneralFunctions generalFunc;
    public DrawerLayout mDrawerLayout;
    public Location userLocation;

    MTextView titleTxt;
    ImageView menuImgView;
    ListView menuListView;
    DrawerAdapter drawerAdapter;
    ArrayList<String[]> list_menu_items;
    SupportMapFragment map;
    GoogleMap gMap;
    boolean isFirstLocation = true;
    ImageView userLocBtnImgView;
    ImageView userHeatmapBtnImgView;


    MTextView onlineOfflineTxtView;
    MTextView ufxonlineOfflineTxtView, ufxTitleonlineOfflineTxtView;
    MTextView carNumPlateTxt;
    MTextView carNameTxt;
    MTextView changeCarTxt;
    MTextView addressTxtView, addressTxtViewufx;
    SwitchButton onlineOfflineSwitch, ufxonlineOfflineSwitch;
    ImageView refreshImgView;

    boolean isOnlineOfflineSwitchCalled = false;

    public boolean isDriverOnline = false;

    Intent startUpdatingStatus;

    String radiusval = "0";

    ArrayList<String> items_txt_car = new ArrayList<String>();
    ArrayList<String> items_txt_car_json = new ArrayList<String>();
    ArrayList<String> items_isHail_json = new ArrayList<String>();
    ArrayList<String> items_car_id = new ArrayList<String>();

    android.support.v7.app.AlertDialog list_car;
    android.support.v7.app.AlertDialog gender;

    MTextView joblocHTxtView, joblocHTxtViewufx;

    boolean isOnlineAvoid = false;

    String assignedTripId = "";
    String ENABLE_HAIL_RIDES = "";

    GetAddressFromLocation getAddressFromLocation;

    ExecuteWebServerUrl heatMapAsyncTask;
    HashMap<String, String> onlinePassengerLocList = new HashMap<String, String>();
    HashMap<String, String> historyLocList = new HashMap<String, String>();
    ArrayList<TileOverlay> mapOverlayList = new ArrayList<>();

    double radius_map = 0;

    Boolean isShowNearByPassengers = false;

    public String app_type = "Ride";
    int currentRequestPositions = 0;
    UpdateFrequentTask updateRequest;
    //BackgroundAppReceiver bgAppReceiver;

    boolean isCurrentReqHandled = false;

    LinearLayout left_linear;

    ImageView imgSetting;

    LinearLayout logoutarea;
    ImageView logoutimage;
    MTextView logoutTxt;
    public String selectedcar = "";

    LinearLayout mapbottomviewarea;
    RelativeLayout mapviewarea;
    boolean iswallet = false;

    SelectableRoundedImageView hileimagview;


    InternetConnection intCheck;
    boolean isrefresh = false;

    private String getState = "GPS";
    ImageView menuufxImgView;

    RelativeLayout rideviewarea, ufxarea;
    MTextView ufxDrivername;

    boolean isFirstAddressLoaded = false;
    RelativeLayout pendingarea, upcomginarea;

    MTextView pendingjobHTxtView, pendingjobValTxtView, upcomingjobHTxtView, upcomingjobValTxtView;
    LinearLayout pendingMainArea;
    LinearLayout botomarea;
    MTextView radiusTxtView, radiusTxtViewufx;
    ImageView imageradius, headerLogo, imageradiusufx;

    RelativeLayout activearea;
    private JSONObject obj_userProfile;

    String HailEnableOnDriverStatus = "";

    boolean isBtnClick = false;

    boolean isCarChangeTxt = false;
    LinearLayout joblocareaufx;
    LinearLayout workArea;
    View workAreaLine;
    MTextView workTxt;
    MTextView btn_edit;
    boolean isfirstZoom = false;
    Location lastPublishedLoc = null;
    double PUBSUB_PUBLISH_DRIVER_LOC_DISTANCE_LIMIT = 5;

    AddDrawer addDrawer;

    AlertDialog cashBalAlertDialog;

    /*EndOfTheDay Trip view declaration start*/
    SelectableRoundedImageView EODTripImageview;
    Marker sourceMarker, destMarker;
    private AlertDialog confirmDialog;
    private BottomSheetDialog faredialog;
    LinearLayout eodLocationArea;
    LinearLayout removeEodTripArea;
    MTextView addressTxt;
    int height;
    UpdateDirections updateDirections;
    /*EndOfTheDay Trip view declaration end*/
    CustomDialog customDialog;

    String LBL_LOAD_ADDRESS="",LBL_GO_ONLINE_TXT="",LBL_GO_OFFLINE_TXT="";
    String LBL_ONLINE="",LBL_OFFLINE="";
    public boolean isUfxServicesEnabled=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        intCheck = new InternetConnection(this);
        isCarChangeTxt = true;
        obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
        changeObj();


        String advertise_banner_data = generalFunc.getJsonValueStr("advertise_banner_data", obj_userProfile);
        if (advertise_banner_data != null && !advertise_banner_data.equalsIgnoreCase("")) {
            if (generalFunc.getJsonValue("image_url", advertise_banner_data) != null && !generalFunc.getJsonValue("image_url", advertise_banner_data).equalsIgnoreCase("")) {
                HashMap<String, String> map = new HashMap<>();
                map.put("image_url", generalFunc.getJsonValue("image_url", advertise_banner_data));
                map.put("tRedirectUrl", generalFunc.getJsonValue("tRedirectUrl", advertise_banner_data));
                map.put("vImageWidth", generalFunc.getJsonValue("vImageWidth", advertise_banner_data));
                map.put("vImageHeight", generalFunc.getJsonValue("vImageHeight", advertise_banner_data));

            }
        }

        PUBSUB_PUBLISH_DRIVER_LOC_DISTANCE_LIMIT = GeneralFunctions.parseDoubleValue(5, generalFunc.retrieveValue(Utils.PUBSUB_PUBLISH_DRIVER_LOC_DISTANCE_LIMIT));

        getAddressFromLocation = new GetAddressFromLocation(getActContext(), generalFunc);
        getAddressFromLocation.setAddressList(this);


        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        refreshImgView = (ImageView) findViewById(R.id.refreshImgView);
        menuImgView = (ImageView) findViewById(R.id.menuImgView);
        pendingarea = (RelativeLayout) findViewById(R.id.pendingarea);
        upcomginarea = (RelativeLayout) findViewById(R.id.upcomginarea);
        pendingarea.setOnClickListener(new setOnClickList());
        upcomginarea.setOnClickListener(new setOnClickList());
        rideviewarea = (RelativeLayout) findViewById(R.id.rideviewarea);
        pendingjobHTxtView = (MTextView) findViewById(R.id.pendingjobHTxtView);
        pendingjobValTxtView = (MTextView) findViewById(R.id.pendingjobValTxtView);
        upcomingjobHTxtView = (MTextView) findViewById(R.id.upcomingjobHTxtView);
        upcomingjobValTxtView = (MTextView) findViewById(R.id.upcomingjobValTxtView);
        radiusTxtView = (MTextView) findViewById(R.id.radiusTxtView);
        radiusTxtViewufx = (MTextView) findViewById(R.id.radiusTxtViewufx);
        imageradius = (ImageView) findViewById(R.id.imageradius);
        imageradiusufx = (ImageView) findViewById(R.id.imageradiusufx);
        headerLogo = (ImageView) findViewById(R.id.headerLogo1);
        activearea = (RelativeLayout) findViewById(R.id.activearea);
        joblocareaufx = (LinearLayout) findViewById(R.id.joblocareaufx);
        workArea = (LinearLayout) findViewById(R.id.workArea);
        workAreaLine = (View) findViewById(R.id.workAreaLine);
        workTxt = (MTextView) findViewById(R.id.workTxt);
        btn_edit = (MTextView) findViewById(R.id.btn_edit);
        btn_edit.setOnClickListener(new setOnClickList());


        radiusTxtView.setOnClickListener(new setOnClickList());
        radiusTxtViewufx.setOnClickListener(new setOnClickList());

        imageradius.setOnClickListener(new setOnClickList());
        imageradiusufx.setOnClickListener(new setOnClickList());
        refreshImgView.setOnClickListener(new setOnClickList());
        ufxarea = (RelativeLayout) findViewById(R.id.ufxarea);
        if (app_type.equals(Utils.CabGeneralType_UberX)) {
            rideviewarea.setVisibility(View.GONE);
            ufxarea.setVisibility(View.VISIBLE);
            setRadiusVal();
        } else {
            rideviewarea.setVisibility(View.VISIBLE);
            ufxarea.setVisibility(View.GONE);
        }

        if (app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX)) {
            setRadiusVal();
        }

        menuListView = (ListView) findViewById(R.id.menuListView);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        userLocBtnImgView = (ImageView) findViewById(R.id.userLocBtnImgView);
        userHeatmapBtnImgView = (ImageView) findViewById(R.id.userHeatmapBtnImgView);
        menuufxImgView = (ImageView) findViewById(R.id.menuufxImgView);
        joblocHTxtView = (MTextView) findViewById(R.id.joblocHTxtView);
        joblocHTxtViewufx = (MTextView) findViewById(R.id.joblocHTxtViewufx);
        addressTxtView = (MTextView) findViewById(R.id.addressTxtView);
        addressTxtViewufx = (MTextView) findViewById(R.id.addressTxtViewufx);
        menuufxImgView.setOnClickListener(new setOnClickList());
        ufxDrivername = (MTextView) findViewById(R.id.ufxDrivername);
        pendingMainArea = (LinearLayout) findViewById(R.id.pendingMainArea);
        botomarea = (LinearLayout) findViewById(R.id.botomarea);

        pendingjobHTxtView.setText(generalFunc.retrieveLangLBl("Pending Jobs", "LBL_PENDING_JOBS"));
        upcomingjobHTxtView.setText(generalFunc.retrieveLangLBl("Upcoming Jobs", "LBL_UPCOMING_JOBS"));

        joblocHTxtView.setText(generalFunc.retrieveLangLBl("Your Job Location", "LBL_YOUR_JOB_LOCATION_TXT"));
        joblocHTxtViewufx.setText(generalFunc.retrieveLangLBl("Your Job Location", "LBL_YOUR_JOB_LOCATION_TXT"));

        LBL_LOAD_ADDRESS= generalFunc.retrieveLangLBl("", "LBL_LOAD_ADDRESS");
        LBL_GO_ONLINE_TXT= generalFunc.retrieveLangLBl("", "LBL_GO_ONLINE_TXT");
        LBL_GO_OFFLINE_TXT=generalFunc.retrieveLangLBl("", "LBL_GO_OFFLINE_TXT");
        LBL_ONLINE=generalFunc.retrieveLangLBl("", "LBL_ONLINE");
        LBL_OFFLINE=generalFunc.retrieveLangLBl("", "LBL_OFFLINE");

        addressTxtView.setText(LBL_LOAD_ADDRESS);
        addressTxtViewufx.setText(LBL_LOAD_ADDRESS);
        btn_edit.setText(generalFunc.retrieveLangLBl("", "LBL_EDIT"));
        workTxt.setText(generalFunc.retrieveLangLBl("", "LBL_EDIT_WORK_LOCATION"));
        handleWorkAddress();

        showHeatMap();

        hileimagview = (SelectableRoundedImageView) findViewById(R.id.hileImageview);
        hileimagview.setOnClickListener(new setOnClickList());

        new CreateRoundedView(getActContext().getResources().getColor(R.color.appThemeColor_1), Utils.dipToPixels(getActContext(), 35), 2,
                getActContext().getResources().getColor(R.color.appThemeColor_1), hileimagview);

        hileimagview.setColorFilter(getActContext().getResources().getColor(R.color.white));

        /*EndOfTheDay Trip view initialization start*/
        ((MTextView) findViewById(R.id.destinationModeHintTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_DESTINATION_MODE_ON_TXT"));

        eodLocationArea = (LinearLayout) findViewById(R.id.eodLocationArea);
        removeEodTripArea = (LinearLayout) findViewById(R.id.removeEodTripArea);
        addressTxt = (MTextView) findViewById(R.id.addressTxt);

        EODTripImageview = (SelectableRoundedImageView) findViewById(R.id.EODTripImageview);
        EODTripImageview.setOnClickListener(new setOnClickList());
        removeEodTripArea.setOnClickListener(new setOnClickList());

        new CreateRoundedView(getActContext().getResources().getColor(R.color.appThemeColor_1), Utils.dipToPixels(getActContext(), 35), 2,
                getActContext().getResources().getColor(R.color.appThemeColor_1), EODTripImageview);

        EODTripImageview.setColorFilter(getActContext().getResources().getColor(R.color.white));
        /*EndOfTheDay Trip view initialization end*/


        mapviewarea = (RelativeLayout) findViewById(R.id.mapviewarea);
        mapbottomviewarea = (LinearLayout) findViewById(R.id.mapbottomviewarea);

        logoutarea = (LinearLayout) findViewById(R.id.logoutarea);
        logoutimage = (ImageView) findViewById(R.id.logoutimage);
        logoutTxt = (MTextView) findViewById(R.id.logoutTxt);
        logoutTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SIGNOUT_TXT"));
        logoutarea.setOnClickListener(new setOnClickList());

        left_linear = (LinearLayout) findViewById(R.id.left_linear);

        onlineOfflineTxtView = (MTextView) findViewById(R.id.onlineOfflineTxtView);
        ufxonlineOfflineTxtView = (MTextView) findViewById(R.id.ufxonlineOfflineTxtView);
        ufxTitleonlineOfflineTxtView = (MTextView) findViewById(R.id.ufxTitleonlineOfflineTxtView);
        carNumPlateTxt = (MTextView) findViewById(R.id.carNumPlateTxt);
        carNameTxt = (MTextView) findViewById(R.id.carNameTxt);
        changeCarTxt = (MTextView) findViewById(R.id.changeCarTxt);
        onlineOfflineSwitch = (SwitchButton) findViewById(R.id.onlineOfflineSwitch);
        ufxonlineOfflineSwitch = (SwitchButton) findViewById(R.id.ufxonlineOfflineSwitch);

        map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapV2);

        imgSetting = (ImageView) findViewById(R.id.imgSetting);
        imgSetting.setOnClickListener(new setOnClickList());

        startUpdatingStatus = new Intent(getApplicationContext(), UpdateDriverStatus.class);
        //bgAppReceiver = new BackgroundAppReceiver(getActContext());

        android.view.Display display = ((android.view.WindowManager) getActContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        left_linear.getLayoutParams().width = display.getWidth() * 75 / 100;
        left_linear.requestLayout();


        ufxDrivername.setText(generalFunc.getJsonValueStr("vName", obj_userProfile) + " "
                + generalFunc.getJsonValueStr("vLastName", obj_userProfile));

        addDrawer = new AddDrawer(getActContext(), obj_userProfile);

        setGeneralData();

        // buildMenu();

        setUserInfo();

        if (generalFunc.getJsonValueStr("RIDE_LATER_BOOKING_ENABLED", obj_userProfile).equalsIgnoreCase("Yes")) {
            pendingMainArea.setVisibility(View.VISIBLE);
            botomarea.setVisibility(View.VISIBLE);
        } else {
            pendingMainArea.setVisibility(View.GONE);
            botomarea.setVisibility(View.GONE);
        }

        map.getMapAsync(MainActivity.this);

        menuImgView.setOnClickListener(new setOnClickList());
        changeCarTxt.setOnClickListener(new setOnClickList());

        userLocBtnImgView.setOnClickListener(new setOnClickList());
        userHeatmapBtnImgView.setOnClickListener(new setOnClickList());



        if (savedInstanceState != null) {
            String restratValue_str = savedInstanceState.getString("RESTART_STATE");

            if (restratValue_str != null && !restratValue_str.equals("") && restratValue_str.trim().equals("true")) {
                generalFunc.restartApp();
            }
        }

        generalFunc.storeData(Utils.DRIVER_CURRENT_REQ_OPEN_KEY, "false");

        JSONArray arr_CurrentRequests = generalFunc.getJsonArray("CurrentRequests", obj_userProfile);

        if (arr_CurrentRequests != null && arr_CurrentRequests.length() > 0) {
            updateRequest = new UpdateFrequentTask(5 * 1000);
//            this.updateRequest = updateRequest;
            updateRequest.setTaskRunListener(this);
            updateRequest.startRepeatingTask();
        } else {
            removeOldRequestsCode();
            isCurrentReqHandled = true;
        }

        //registerBackgroundAppReceiver();

        // if (generalFunc.getJsonValue("APP_TYPE", userProfileJson).equalsIgnoreCase("UberX")) {
        if (generalFunc.getJsonValueStr("APP_TYPE", obj_userProfile).equalsIgnoreCase("UberX")) {
            changeCarTxt.setText(generalFunc.getJsonValueStr("vName", obj_userProfile) + " "
                    + generalFunc.getJsonValueStr("vLastName", obj_userProfile));
            changeCarTxt.setOnClickListener(null);

            carNumPlateTxt.setVisibility(View.GONE);
            carNameTxt.setVisibility(View.GONE);
        }

        String eStatus = generalFunc.getJsonValueStr("eStatus", obj_userProfile);

        if (eStatus.equalsIgnoreCase("inactive")) {
            mapbottomviewarea.setVisibility(View.GONE);
            mapviewarea.setVisibility(View.GONE);
            hileimagview.setVisibility(View.GONE);
            EODTripImageview.setVisibility(View.GONE);
            headerLogo.setVisibility(View.VISIBLE);
            InactiveFragment inactiveFragment = new InactiveFragment();
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            if (app_type.equals(Utils.CabGeneralType_UberX)) {
                activearea.setVisibility(View.GONE);
                ft.replace(R.id.containerufx, inactiveFragment);
                ft.commit();

            } else {
                ft.replace(R.id.container, inactiveFragment);
                ft.commit();
            }
        } else {
            setSwitchEvents();
            if (app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX)) {
                joblocareaufx.setVisibility(View.GONE);
            }

            if (app_type.equals(Utils.CabGeneralType_UberX)) {

                refreshImgView.setVisibility(View.VISIBLE);
            }

            headerLogo.setVisibility(View.GONE);

            if (isDriverOnline) {
                isHailRideOptionEnabled();
            }
            mapbottomviewarea.setVisibility(View.VISIBLE);
            mapviewarea.setVisibility(View.VISIBLE);


            handleNoLocationDial();

        }

        generalFunc.deleteTripStatusMessages();

        GetLocationUpdates.getInstance().setTripStartValue(false, false, "");

        boolean isEmailVerified=generalFunc.getJsonValueStr("eEmailVerified", obj_userProfile).equalsIgnoreCase("YES");
        boolean isPhoneVerified=generalFunc.getJsonValueStr("ePhoneVerified", obj_userProfile).equalsIgnoreCase("YES");

        if (!isEmailVerified ||
                !isPhoneVerified) {

            Bundle bn = new Bundle();
            if (!isEmailVerified &&
                    !isPhoneVerified) {
                bn.putString("msg", "DO_EMAIL_PHONE_VERIFY");
            } else if (!isEmailVerified) {
                bn.putString("msg", "DO_EMAIL_VERIFY");
            } else if (!isPhoneVerified) {
                bn.putString("msg", "DO_PHONE_VERIFY");
            }

            if (!eStatus.equalsIgnoreCase("inactive")) {
                //  bn.putString("UserProfileJson", userProfileJson);
                showMessageWithAction(onlineOfflineTxtView, generalFunc.retrieveLangLBl("", "LBL_ACCOUNT_VERIFY_ALERT_TXT"), bn);
            }
        }

        if ((app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX) && generalFunc.getJsonValueStr("eShowVehicles", obj_userProfile).equalsIgnoreCase("No"))) {
            (findViewById(R.id.changeCarArea)).setVisibility(View.GONE);
        }

    }

    private void changeObj() {
        app_type = generalFunc.getJsonValueStr("APP_TYPE", obj_userProfile);
        String UFX_SERVICE_AVAILABLE = generalFunc.getJsonValueStr("UFX_SERVICE_AVAILABLE", obj_userProfile);
        isUfxServicesEnabled = !Utils.checkText(UFX_SERVICE_AVAILABLE) || (UFX_SERVICE_AVAILABLE!=null &&UFX_SERVICE_AVAILABLE.equalsIgnoreCase("Yes"));
    }

    private void setSwitchEvents() {
        if (app_type.equals(Utils.CabGeneralType_UberX)) {

            ufxonlineOfflineSwitch.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        isOnlineOfflineSwitchCalled = true;
                        break;
                }
                return false;
            });

            ufxonlineOfflineSwitch.setOnCheckedChangeListener((compoundButton, b) -> {

                if (!intCheck.isNetworkConnected()) {
                    isOnlineOfflineSwitchCalled = false;
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("No Internet Connection", "LBL_NO_INTERNET_TXT"));
                    return;
                }

                if (b) {
                    ufxonlineOfflineSwitch.setThumbColorRes(R.color.white);
                    ufxonlineOfflineSwitch.setBackColorRes(R.color.Green);
                } else {
                    ufxonlineOfflineSwitch.setThumbColorRes(R.color.white);
                    ufxonlineOfflineSwitch.setBackColorRes(android.R.color.holo_red_dark);
                }

                if (isOnlineAvoid) {
                    isOnlineAvoid = false;
                    isOnlineOfflineSwitchCalled = false;
                    return;
                }

                goOnlineOffline(b, true);
                isOnlineOfflineSwitchCalled = false;
            });

        } else {
            onlineOfflineSwitch.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        isOnlineOfflineSwitchCalled = true;
                        break;
                }
                return false;
            });

            onlineOfflineSwitch.setOnCheckedChangeListener((compoundButton, b) -> {

                if (!intCheck.isNetworkConnected()) {
                    isOnlineOfflineSwitchCalled = false;
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("No Internet Connection", "LBL_NO_INTERNET_TXT"));
                    return;
                }

                if (b) {
                    onlineOfflineSwitch.setThumbColorRes(R.color.Green);
                    onlineOfflineSwitch.setBackColorRes(android.R.color.white);
                } else {
                    onlineOfflineSwitch.setThumbColorRes(android.R.color.holo_red_dark);
                    onlineOfflineSwitch.setBackColorRes(android.R.color.white);
                }

                if (isOnlineAvoid) {
                    isOnlineAvoid = false;
                    isOnlineOfflineSwitchCalled = false;
                    return;
                }

                goOnlineOffline(b, true);
                isOnlineOfflineSwitchCalled = false;
                MainActivity.super.onResume();
            });

        }
    }

    private void showHeatMap() {
        if (app_type.equalsIgnoreCase(Utils.CabGeneralType_UberX) || generalFunc.isDeliverOnlyEnabled()) {
            userHeatmapBtnImgView.setVisibility(View.GONE);
        } else {
            userHeatmapBtnImgView.setVisibility(View.VISIBLE);
        }
    }


    public void handleWorkAddress() {
        if (generalFunc.getJsonValueStr("PROVIDER_AVAIL_LOC_CUSTOMIZE", obj_userProfile).equalsIgnoreCase("Yes")) {

            if (generalFunc.getJsonValueStr("eSelectWorkLocation", obj_userProfile).equalsIgnoreCase("Fixed")) {
                String WORKLOCATION=generalFunc.retrieveValue(Utils.WORKLOCATION);
                if (!WORKLOCATION.equals("")) {
                    addressTxtView.setText(WORKLOCATION);
                    addressTxtViewufx.setText(WORKLOCATION);
                } else {
                    if (userLocation != null) {
                        getAddressFromLocation.setLocation(userLocation.getLatitude(), userLocation.getLongitude());
                        getAddressFromLocation.execute();
                        addressTxtView.setText(LBL_LOAD_ADDRESS);
                        addressTxtViewufx.setText(LBL_LOAD_ADDRESS);
                    }
                }
            } else {
                if (userLocation != null) {
                    getAddressFromLocation.setLocation(userLocation.getLatitude(), userLocation.getLongitude());
                    getAddressFromLocation.execute();
                    addressTxtView.setText(LBL_LOAD_ADDRESS);
                    addressTxtViewufx.setText(LBL_LOAD_ADDRESS);

                }

            }
        }
    }

    public void setRadiusVal() {

        if (obj_userProfile != null && !generalFunc.getJsonValueStr("eUnit", obj_userProfile).equalsIgnoreCase("KMs")) {

            radiusTxtView.setText(generalFunc.retrieveLangLBl("Within", "LBL_WITHIN") + " " + radiusval + " " + generalFunc.retrieveLangLBl("", "LBL_MILE_DISTANCE_TXT") + " " +
                    generalFunc.retrieveLangLBl("Work Radius", "LBL_RADIUS"));
            radiusTxtViewufx.setText(generalFunc.retrieveLangLBl("Within", "LBL_WITHIN") + " " + radiusval + " " + generalFunc.retrieveLangLBl("", "LBL_MILE_DISTANCE_TXT") + " " +
                    generalFunc.retrieveLangLBl("Work Radius", "LBL_RADIUS"));
        } else {
            radiusTxtView.setText(generalFunc.retrieveLangLBl("Within", "LBL_WITHIN") + " " + radiusval + " " + generalFunc.retrieveLangLBl("", "LBL_KM_DISTANCE_TXT") + " " +
                    generalFunc.retrieveLangLBl("Work Radius", "LBL_RADIUS"));
            radiusTxtViewufx.setText(generalFunc.retrieveLangLBl("Within", "LBL_WITHIN") + " " + radiusval + " " + generalFunc.retrieveLangLBl("", "LBL_KM_DISTANCE_TXT") + " " +
                    generalFunc.retrieveLangLBl("Work Radius", "LBL_RADIUS"));
        }

    }

    private void isHailRideOptionEnabled() {

        if ((faredialog != null && faredialog.isShowing()) || eodLocationArea.getVisibility() == View.VISIBLE) {
            hileimagview.setVisibility(View.GONE);
            EODTripImageview.setVisibility(View.GONE);
            return;
        }

        enableEOD();

        boolean eDestinationMode = generalFunc.getJsonValueStr("eDestinationMode", obj_userProfile).equalsIgnoreCase("Yes");
        if(eDestinationMode)
        {
            return;
        }

        if (!HailEnableOnDriverStatus.equalsIgnoreCase("") && HailEnableOnDriverStatus.equalsIgnoreCase("No")) {
            hileimagview.setVisibility(View.GONE);
            return;
        }
        String eStatus = generalFunc.getJsonValueStr("eStatus", obj_userProfile);

        if (!eStatus.equalsIgnoreCase("inactive")) {
            ENABLE_HAIL_RIDES = generalFunc.getJsonValueStr("ENABLE_HAIL_RIDES", obj_userProfile);
            if (ENABLE_HAIL_RIDES.equalsIgnoreCase("Yes")
                    && HailEnableOnDriverStatus.equalsIgnoreCase("Yes") && !generalFunc.isDeliverOnlyEnabled()) {
                hileimagview.setVisibility(View.VISIBLE);
            } else {
                hileimagview.setVisibility(View.GONE);
            }
        } else {
            hileimagview.setVisibility(View.GONE);
        }


    }

    public void removeOldRequestsCode() {

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getActContext());
        Map<String, ?> keys = mPrefs.getAll();

        for (Map.Entry<String, ?> entry : keys.entrySet()) {

            if (entry.getKey().contains(Utils.DRIVER_REQ_CODE_PREFIX_KEY)) {
                //generalFunc.removeValue(entry.getKey());
                Long CURRENTmILLI = System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 1);
                String value_=generalFunc.retrieveValue(entry.getKey())+ "";
                long value = generalFunc.parseLongValue(0, value_);
                if (CURRENTmILLI >= value) {
                    generalFunc.removeValue(entry.getKey());
                }
            }
        }
    }


    boolean isFirstRunTaskSkipped = false;

    @Override
    public void onTaskRun() {
        if (isFirstRunTaskSkipped == false) {
            isFirstRunTaskSkipped = true;
            return;
        }
        if (generalFunc.retrieveValue(Utils.DRIVER_CURRENT_REQ_OPEN_KEY).equals("true")) {
            return;
        }

        JSONArray arr_CurrentRequests = generalFunc.getJsonArray("CurrentRequests", obj_userProfile);

        if (currentRequestPositions < arr_CurrentRequests.length()) {
            JSONObject obj_temp = generalFunc.getJsonObject(arr_CurrentRequests, currentRequestPositions);

            String message_str = generalFunc.getJsonValueStr("tMessage", obj_temp).replace("\\\"", "\"");

            String MsgCode=generalFunc.getJsonValue("MsgCode", message_str);
            String codeKey = Utils.DRIVER_REQ_CODE_PREFIX_KEY + MsgCode;

            if (generalFunc.retrieveValue(codeKey).equals("") && !generalFunc.containsKey(Utils.DRIVER_REQ_COMPLETED_MSG_CODE_KEY + MsgCode)) {
                generalFunc.storeData(codeKey, "true");

                generalFunc.storeData(Utils.DRIVER_CURRENT_REQ_OPEN_KEY, "true");

                (new FireTripStatusMsg(getActContext(), "Script")).fireTripMsg(message_str);
            }

            currentRequestPositions++;
        } else if (updateRequest != null) {
            updateRequest.stopRepeatingTask();
            updateRequest = null;

            isCurrentReqHandled = true;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putString("RESTART_STATE", "true");
        super.onSaveInstanceState(outState);
    }

    public void setWalletInfo() {
        obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
        changeObj();
        String user_available_balance=generalFunc.getJsonValueStr("user_available_balance", obj_userProfile);
        String LBL_WALLET_BALANCE=generalFunc.retrieveLangLBl("wallet Balance", "LBL_WALLET_BALANCE");
        ((MTextView) findViewById(R.id.walletbalncetxt)).setText(LBL_WALLET_BALANCE + ": " + generalFunc.convertNumberWithRTL(user_available_balance));

        if (addDrawer != null) {
            addDrawer.changeUserProfileJson(obj_userProfile);
            addDrawer.walletbalncetxt.setText(LBL_WALLET_BALANCE + ": " + generalFunc.convertNumberWithRTL(user_available_balance));

        }
    }

    public void setUserInfo() {
        obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
        changeObj();
        ((MTextView) findViewById(R.id.userNameTxt)).setText(generalFunc.getJsonValue("vName", obj_userProfile) + " "
                + generalFunc.getJsonValue("vLastName", obj_userProfile));
        setWalletInfo();

        String DRIVER_ONLINE_KEY=generalFunc.retrieveValue(Utils.DRIVER_ONLINE_KEY);

        if (app_type.equals(Utils.CabGeneralType_UberX)) {

            if (DRIVER_ONLINE_KEY != null && DRIVER_ONLINE_KEY.equalsIgnoreCase("true")) {
                ufxonlineOfflineTxtView.setText(LBL_GO_OFFLINE_TXT);
                ufxTitleonlineOfflineTxtView.setText(LBL_ONLINE);
            } else {
                ufxonlineOfflineTxtView.setText(LBL_GO_ONLINE_TXT);
                ufxTitleonlineOfflineTxtView.setText(LBL_OFFLINE);
            }

            (new AppFunctions(getActContext())).checkProfileImage((SelectableRoundedImageView) findViewById(R.id.driverImgView), obj_userProfile.toString(), "vImage");

        } else {

            (new AppFunctions(getActContext())).checkProfileImage((SelectableRoundedImageView) findViewById(R.id.userPicImgView), obj_userProfile.toString(), "vImage");
            if (DRIVER_ONLINE_KEY != null && DRIVER_ONLINE_KEY.equalsIgnoreCase("true")) {
                onlineOfflineTxtView.setText(LBL_GO_OFFLINE_TXT);
            } else {
                onlineOfflineTxtView.setText(LBL_GO_ONLINE_TXT);
            }

        }
        (new AppFunctions(getActContext())).checkProfileImage((SelectableRoundedImageView) findViewById(R.id.userImgView), obj_userProfile.toString(), "vImage");


        if (app_type.equals(Utils.CabGeneralType_UberX)) {
            changeCarTxt.setText(generalFunc.getJsonValueStr("vName", obj_userProfile) + " "
                    + generalFunc.getJsonValueStr("vLastName", obj_userProfile));
            changeCarTxt.setOnClickListener(null);
            carNumPlateTxt.setVisibility(View.GONE);
            carNameTxt.setVisibility(View.GONE);
        } else {
            changeCarTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CHANGE"));
        }

        if (isCarChangeTxt) {
            String iDriverVehicleId = generalFunc.getJsonValueStr("iDriverVehicleId", obj_userProfile);
            setCarInfo(iDriverVehicleId);
        }


    }

    public void showMessageWithAction(View view, String message, final Bundle bn) {
        Snackbar snackbar = Snackbar
                .make(view, message, Snackbar.LENGTH_INDEFINITE).setAction(generalFunc.retrieveLangLBl("", "LBL_BTN_VERIFY_TXT"), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        new StartActProcess(getActContext()).startActForResult(VerifyInfoActivity.class, bn, Utils.VERIFY_INFO_REQ_CODE);

                    }
                });
        snackbar.setActionTextColor(getActContext().getResources().getColor(R.color.verfiybtncolor));
        snackbar.setDuration(10000);
        snackbar.show();
    }


    public void setGeneralData() {
        HashMap<String,String> storeData=new HashMap<>();
        storeData.put(Utils.MOBILE_VERIFICATION_ENABLE_KEY, generalFunc.getJsonValueStr("MOBILE_VERIFICATION_ENABLE", obj_userProfile));
        storeData.put("LOCATION_ACCURACY_METERS", generalFunc.getJsonValueStr("LOCATION_ACCURACY_METERS", obj_userProfile));
        storeData.put("DRIVER_LOC_UPDATE_TIME_INTERVAL", generalFunc.getJsonValueStr("DRIVER_LOC_UPDATE_TIME_INTERVAL", obj_userProfile));
        storeData.put(Utils.REFERRAL_SCHEME_ENABLE, generalFunc.getJsonValueStr("REFERRAL_SCHEME_ENABLE", obj_userProfile));

        storeData.put(Utils.WALLET_ENABLE, generalFunc.getJsonValueStr("WALLET_ENABLE", obj_userProfile));
        storeData.put(Utils.REFERRAL_SCHEME_ENABLE, generalFunc.getJsonValueStr("REFERRAL_SCHEME_ENABLE", obj_userProfile));
        storeData.put(Utils.SMS_BODY_KEY, generalFunc.getJsonValueStr(Utils.SMS_BODY_KEY, obj_userProfile));
        storeData.put(Utils.PUBSUB_PUBLISH_DRIVER_LOC_DISTANCE_LIMIT, generalFunc.getJsonValueStr("PUBSUB_PUBLISH_DRIVER_LOC_DISTANCE_LIMIT", obj_userProfile));
        generalFunc.storeData(storeData);
    }

    public void setCarInfo(String iDriverVehicleId) {
        if (!iDriverVehicleId.equals("") && !iDriverVehicleId.equals("0")) {
            String vLicencePlateNo = generalFunc.getJsonValueStr("vLicencePlateNo", obj_userProfile);
            carNumPlateTxt.setText(vLicencePlateNo);
            carNumPlateTxt.setVisibility(View.VISIBLE);

            String vMake = generalFunc.getJsonValueStr("vMake", obj_userProfile);
            String vModel = generalFunc.getJsonValueStr("vModel", obj_userProfile);

            selectedcar = iDriverVehicleId;

            if (app_type.equals(Utils.CabGeneralType_UberX)) {
                changeCarTxt.setText(generalFunc.getJsonValueStr("vName", obj_userProfile) + " "
                        + generalFunc.getJsonValueStr("vLastName", obj_userProfile));
                changeCarTxt.setOnClickListener(null);
                carNumPlateTxt.setVisibility(View.GONE);
                carNameTxt.setVisibility(View.GONE);
            } else {
                carNameTxt.setText(vMake + " " + vModel);
                carNameTxt.setVisibility(View.VISIBLE);
                changeCarTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CHANGE"));
            }
        } else {
            if (app_type.equals(Utils.CabGeneralType_UberX)) {
                changeCarTxt.setText(generalFunc.getJsonValueStr("vName", obj_userProfile) + " "
                        + generalFunc.getJsonValueStr("vLastName", obj_userProfile));
                changeCarTxt.setOnClickListener(null);
                carNumPlateTxt.setVisibility(View.GONE);
                carNameTxt.setVisibility(View.GONE);
            } else {
                changeCarTxt.setText(generalFunc.retrieveLangLBl("Choose car", "LBL_CHOOSE_CAR"));
            }

        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

        if (this.userLocation == null || isShowNearByPassengers == false) {
            return;
        }

        VisibleRegion vr = getMap().getProjection().getVisibleRegion();
        final LatLng mainCenter = vr.latLngBounds.getCenter();
        final LatLng northeast = vr.latLngBounds.northeast;
        final LatLng southwest = vr.latLngBounds.southwest;

        final double radius_map = GeneralFunctions.calculationByLocation(mainCenter.latitude, mainCenter.longitude, southwest.latitude, southwest.longitude, "KM");

        boolean isWithin1m = radius_map > this.radius_map + 0.001;

        if (isWithin1m == true)
            getNearByPassenger(String.valueOf(radius_map), mainCenter.latitude, mainCenter.longitude);

        this.radius_map = radius_map;
    }

    public void configHeatMapView(boolean isShowNearByPassengers) {
        this.isShowNearByPassengers = isShowNearByPassengers;
        userHeatmapBtnImgView.setImageResource(isShowNearByPassengers ? R.mipmap.ic_heatmap_on : R.mipmap.ic_heatmap_off);
        if (mapOverlayList.size() > 0) {
            for (int i = 0; i < mapOverlayList.size(); i++) {
                if (mapOverlayList.get(i) != null) {

                    mapOverlayList.get(i).setVisible(isShowNearByPassengers);

                    if (isShowNearByPassengers) {

                        //handle heat map view
                        if (isfirstZoom) {
                            isfirstZoom = false;
                            getMap().moveCamera(CameraUpdateFactory.zoomTo(14f));
                        }
                    } else {
                        userLocBtnImgView.performClick();
                    }
                }

            }
        }

        if (cameraForUserPosition() != null)
            onCameraChange(cameraForUserPosition());

    }

    public void onMapReady(GoogleMap googleMap) {

        (findViewById(R.id.LoadingMapProgressBar)).setVisibility(View.GONE);

        this.gMap = googleMap;

        if (generalFunc.checkLocationPermission(true)) {
            getMap().setMyLocationEnabled(false);
            getMap().setPadding(0, 0, 0, Utils.dipToPixels(getActContext(), 90));
            getMap().getUiSettings().setTiltGesturesEnabled(false);
            getMap().getUiSettings().setZoomControlsEnabled(false);
            getMap().getUiSettings().setCompassEnabled(false);
            getMap().getUiSettings().setMyLocationButtonEnabled(false);
        }
        getMap().setOnCameraChangeListener(this);

        getMap().setOnMarkerClickListener(marker -> {
            marker.hideInfoWindow();
            return true;
        });

        if (GetLocationUpdates.retrieveInstance() != null) {
            GetLocationUpdates.getInstance().stopLocationUpdates(this);
        }

        GetLocationUpdates.getInstance().setTripStartValue(false, false, "");
        GetLocationUpdates.getInstance().startLocationUpdates(this, this);

    }

    public GoogleMap getMap() {
        return this.gMap;
    }

    public void callgederApi(String egender) {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "updateUserGender");
        parameters.put("UserType", Utils.userType);
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("eGender", egender);


        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

            String message = generalFunc.getJsonValue(Utils.message_str, responseString);
            if (isDataAvail) {
                generalFunc.storeData(Utils.USER_PROFILE_JSON, message);
                obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
                changeObj();

                if (addDrawer != null) {
                    addDrawer.changeUserProfileJson(obj_userProfile);
                }

                imgSetting.performClick();
            }
        });
        exeWebServer.execute();
    }

    public void genderDailog() {
        closeDrawer();

        final Dialog builder = new Dialog(getActContext(), R.style.Theme_Dialog);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        builder.setContentView(R.layout.gender_view);
        builder.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        final MTextView genderTitleTxt = (MTextView) builder.findViewById(R.id.genderTitleTxt);
        final MTextView maleTxt = (MTextView) builder.findViewById(R.id.maleTxt);
        final MTextView femaleTxt = (MTextView) builder.findViewById(R.id.femaleTxt);
        final ImageView gendercancel = (ImageView) builder.findViewById(R.id.gendercancel);
        final ImageView gendermale = (ImageView) builder.findViewById(R.id.gendermale);
        final ImageView genderfemale = (ImageView) builder.findViewById(R.id.genderfemale);
        final LinearLayout male_area = (LinearLayout) builder.findViewById(R.id.male_area);
        final LinearLayout female_area = (LinearLayout) builder.findViewById(R.id.female_area);

        genderTitleTxt.setText(generalFunc.retrieveLangLBl("Select your gender to continue", "LBL_SELECT_GENDER"));
        maleTxt.setText(generalFunc.retrieveLangLBl("Male", "LBL_MALE_TXT"));
        femaleTxt.setText(generalFunc.retrieveLangLBl("FeMale", "LBL_FEMALE_TXT"));

        gendercancel.setOnClickListener(v -> builder.dismiss());

        male_area.setOnClickListener(v -> {
            callgederApi("Male");
            builder.dismiss();

        });
        female_area.setOnClickListener(v -> {
            callgederApi("Female");
            builder.dismiss();

        });

        builder.show();

    }

    public void goOnlineOffline(final boolean isGoOnline, final boolean isMessageShown) {

        handleNoLocationDial();
        if (isGoOnline && (userLocation == null || userLocation.getLatitude() == 0.0 || userLocation.getLongitude() == 0.0)) {
            generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("Application is not able to get your accurate location. Please try again. \n" +
                    "If you still face the problem, please try again in open sky instead of closed area.", "LBL_NO_LOC_GPS_GENERAL"));
            onlineOfflineSwitch.setChecked(false);
            onlineOfflineSwitch.setThumbColorRes(android.R.color.holo_red_dark);
            onlineOfflineSwitch.setBackColorRes(android.R.color.white);

            ufxonlineOfflineSwitch.setChecked(false);
            ufxonlineOfflineSwitch.setThumbColorRes(R.color.white);
            ufxonlineOfflineSwitch.setBackColorRes(android.R.color.holo_red_dark);
            setOfflineState();
            return;
        }
        isHailRideOptionEnabled();

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "updateDriverStatus");
        parameters.put("iDriverId", generalFunc.getMemberId());

        if (isGoOnline) {
            parameters.put("Status", "Available");
            parameters.put("isUpdateOnlineDate", "true");
        } else {
            parameters.put("Status", "Not Available");
        }
        if (userLocation != null) {
            parameters.put("latitude", "" + userLocation.getLatitude());
            parameters.put("longitude", "" + userLocation.getLongitude());
        }

        parameters.put("isOnlineSwitchPressed", isOnlineOfflineSwitchCalled ? "true" : "");

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setCancelAble(false);

        if (isMessageShown) {
            exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        }

        exeWebServer.setIsDeviceTokenGenerate(true, "vDeviceToken", generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            if (!isMessageShown) {
                return;
            }

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);
                String message = generalFunc.getJsonValue(Utils.message_str, responseString);

                if (message.equals("SESSION_OUT")) {
                    MyApp.getInstance().notifySessionTimeOut();
                    Utils.runGC();
                    return;
                }
                HashMap<String,String> storeData=new HashMap<>();
                storeData.put(Utils.DRIVER_DESTINATION_AVAILABLE_KEY, generalFunc.getJsonValue(Utils.DRIVER_DESTINATION_AVAILABLE_KEY, responseString));
                storeData.put(Utils.ENABLE_DRIVER_DESTINATIONS_KEY, generalFunc.getJsonValue(Utils.ENABLE_DRIVER_DESTINATIONS_KEY, responseString));
                generalFunc.storeData(storeData);

                if (isDataAvail) {

                    HailEnableOnDriverStatus = generalFunc.getJsonValue("Enable_Hailtrip", responseString);


                    if (isGoOnline) {

                        String isExistUberXServices =generalFunc.getJsonValue("isExistUberXServices", responseString);
                        if ((isExistUberXServices.equalsIgnoreCase("Yes") || isExistUberXServices.equalsIgnoreCase("true"))&& !generalFunc.isDeliverOnlyEnabled()) {
                            workArea.setVisibility(View.VISIBLE);
                            workAreaLine.setVisibility(View.VISIBLE);

                        } else {
                            workArea.setVisibility(View.GONE);
                            workAreaLine.setVisibility(View.GONE);

                        }

                        if (message.equals("REQUIRED_MINIMUM_BALNCE")) {
                            isHailRideOptionEnabled();

                            Bundle bn = new Bundle();
                            bn.putString("UserProfileJson", obj_userProfile.toString());
                            buildLowBalanceMessage(getActContext(), generalFunc.getJsonValue("Msg", responseString), bn);
                        }
                        setOnlineState();
                        generalFunc.showMessage(generalFunc.getCurrentView((Activity) getActContext()), generalFunc.retrieveLangLBl("", "LBL_ONLINE_HEADER_TXT"));


                    } else {
                        workArea.setVisibility(View.GONE);
                        workAreaLine.setVisibility(View.GONE);
                        setOfflineState();
                        generalFunc.showMessage(generalFunc.getCurrentView((Activity) getActContext()), generalFunc.retrieveLangLBl("", "LBL_OFFLINE_HEADER_TXT"));

                    }

                    if (generalFunc.getJsonValue("UberX_message", responseString) != null && !generalFunc.getJsonValue("UberX_message", responseString).equalsIgnoreCase("")) {
                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue("UberX_message", responseString)));
                    }
                } else {

                    if (generalFunc.getJsonValue("Enable_Hailtrip", responseString) != null & !generalFunc.getJsonValue("Enable_Hailtrip", responseString).equalsIgnoreCase("")) {

                        HailEnableOnDriverStatus = generalFunc.getJsonValue("Enable_Hailtrip", responseString);
                    }

                    Logger.d("SUBSCRIPTION","1");

                    isOnlineAvoid = true;
                    if (app_type.equals(Utils.CabGeneralType_UberX)) {

                        if (isGoOnline ) {
                            ufxonlineOfflineSwitch.setChecked(false);
                        } else {
                            ufxonlineOfflineSwitch.setChecked(true);
                        }

                    } else {
                        if (isGoOnline) {
                            onlineOfflineSwitch.setChecked(false);
                        } else {
                            onlineOfflineSwitch.setChecked(true);
                        }
                    }


                    Bundle bn = new Bundle();
                    bn.putString("msg", "" + message);
                    String eStatus = generalFunc.getJsonValueStr("eStatus", obj_userProfile);

                    if (!eStatus.equalsIgnoreCase("inactive")) {
                        Logger.d("SUBSCRIPTION","2");

                        if (message.equals("DO_EMAIL_PHONE_VERIFY") || message.equals("DO_PHONE_VERIFY") || message.equals("DO_EMAIL_VERIFY")) {
                            accountVerificationAlert(generalFunc.retrieveLangLBl("", "LBL_ACCOUNT_VERIFY_ALERT_TXT"), bn);
                            return;
                        }
                    }

                    Logger.d("SUBSCRIPTION","4");

                    if (isGoOnline && !message.equalsIgnoreCase("PENDING_SUBSCRIPTION")) {
                        isHailRideOptionEnabled();
                    } else {
                        hileimagview.setVisibility(View.GONE);
                        EODTripImageview.setVisibility(View.GONE);
                    }

                    if (Utils.checkText(message) && message.equals("PENDING_SUBSCRIPTION") && isGoOnline) {
                        Logger.d("SUBSCRIPTION","3"+isGoOnline);
                        showSubscriptionStatusDialog(false,message);
                        return;
                    }

                    if (Utils.checkText(message) && message.equals("REQUIRED_MINIMUM_BALNCE") && isGoOnline) {

                        isHailRideOptionEnabled();
                        bn.putString("UserProfileJson", obj_userProfile.toString());

                        buildLowBalanceMessage(getActContext(), generalFunc.getJsonValue("Msg", responseString), bn);
                        return;
                    }



                    if (Utils.checkText(message) && !message.equals("PENDING_SUBSCRIPTION")) {

                        if (message.equalsIgnoreCase("LBL_INACTIVE_CARS_MESSAGE_TXT")) {
                            Logger.d("SUBSCRIPTION","5");
                            hileimagview.setVisibility(View.GONE);
                            EODTripImageview.setVisibility(View.GONE);
                            GenerateAlertBox alertBox = new GenerateAlertBox(getActContext());
                            alertBox.setContentMessage("", generalFunc.retrieveLangLBl("", message));
                            alertBox.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
                            alertBox.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_CONTACT_US_TXT"));
                            alertBox.setBtnClickList(btn_id -> {

                                alertBox.closeAlertBox();
                                if (btn_id == 0) {
                                    new StartActProcess(getActContext()).startAct(ContactUsActivity.class);
                                }
                            });
                            alertBox.showAlertBox();
                        } else {
                            if (generalFunc.getJsonValue("isShowContactUs", responseString) != null && generalFunc.getJsonValue("isShowContactUs", responseString).equalsIgnoreCase("Yes")) {
                                Logger.d("SUBSCRIPTION","6");
                                final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
                                generateAlert.setCancelable(false);
                                generateAlert.setBtnClickList(btn_id -> {
                                    if (btn_id == 0) {


                                    } else if (btn_id == 1) {
                                        Intent intent = new Intent(MainActivity.this, ContactUsActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                        // finish();

                                    }
                                });

                                generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                                generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
                                generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_CONTACT_US_TXT"));

                                generateAlert.showAlertBox();


                            } else {

                                if (message.equalsIgnoreCase("LBL_PENDING_MIXSUBSCRIPTION"))
                                {
                                    hileimagview.setVisibility(View.GONE);
                                    EODTripImageview.setVisibility(View.GONE);

                                    showSubscriptionStatusDialog(false,message);
                                }else {
                                    Logger.d("SUBSCRIPTION", "7");
                                    generalFunc.showGeneralMessage("",
                                            generalFunc.retrieveLangLBl(generalFunc.getJsonValue(Utils.message_str, responseString),
                                                    generalFunc.getJsonValue(Utils.message_str, responseString)));
                                }
                            }
                        }
                    }
                }
            } else {

                if (intCheck.isNetworkConnected()) {
                    isOnlineAvoid = true;

                    if (app_type.equals(Utils.CabGeneralType_UberX)) {

                        if (isGoOnline == true) {
                            ufxonlineOfflineSwitch.setChecked(false);
                        } else {
                            ufxonlineOfflineSwitch.setChecked(true);
                        }

                    } else {
                        if (isGoOnline == true) {
                            onlineOfflineSwitch.setChecked(false);
                        } else {
                            onlineOfflineSwitch.setChecked(true);
                        }
                    }
                }
                Logger.d("SUBSCRIPTION","8");
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

    public void showSubscriptionStatusDialog(boolean checkOnlineAvailability,String message) {

        String messageStr=message.equalsIgnoreCase("LBL_PENDING_MIXSUBSCRIPTION")?message:"LBL_SUBSCRIPTION_REQ_SH_LBL";

        if (checkOnlineAvailability) {
            if (isDriverOnline) {

                setOfflineState();
                isOnlineAvoid = true;
                if (app_type.equals(Utils.CabGeneralType_UberX)) {
                    ufxonlineOfflineSwitch.setChecked(false);
                    ufxonlineOfflineSwitch.setThumbColorRes(R.color.white);
                    ufxonlineOfflineSwitch.setBackColorRes(android.R.color.holo_red_dark);


                } else {
                    onlineOfflineSwitch.setChecked(false);
                    onlineOfflineSwitch.setThumbColorRes(android.R.color.holo_red_dark);
                    onlineOfflineSwitch.setBackColorRes(android.R.color.white);


                }

            }else {
                subscriptionDialog(messageStr);
                return;
            }
        }

       subscriptionDialog(messageStr);
    }

    private void subscriptionDialog(String messageStr) {
        if (customDialog != null) {
            customDialog.closeDialog();
        }


        customDialog = new CustomDialog(getActContext());
        customDialog.setDetails(generalFunc.retrieveLangLBl("", "LBL_SUBSCRIPTION_REQ_H_LBL"), generalFunc.retrieveLangLBl("",messageStr), generalFunc.retrieveLangLBl("", "LBL_SUBSCRIBE"), generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"), false, R.mipmap.ic_menu_subscription, true, 1);
        customDialog.setRoundedViewBackgroundColor(R.color.white);
        customDialog.setIconTintColor(R.color.appThemeColor_1);
        customDialog.setBtnRadius(10);
        customDialog.setTitleTxtColor(R.color.appThemeColor_1);
        customDialog.setPositiveBtnBackColor(R.color.appThemeColor_1_Light);
        customDialog.setNegativeBtnBackColor(R.color.appThemeColor_1_Light);
        customDialog.createDialog();
        customDialog.setPositiveButtonClick(new Closure() {
            @Override
            public void exec() {

            }
        });
        customDialog.setNegativeButtonClick(new Closure() {
            @Override
            public void exec() {

            }
        });
        customDialog.show();
    }

    public void setOfflineState() {
        isDriverOnline = false;
        if (app_type.equals(Utils.CabGeneralType_UberX)) {
            ufxonlineOfflineTxtView.setText(LBL_GO_ONLINE_TXT);
            ufxTitleonlineOfflineTxtView.setText(LBL_OFFLINE);
        } else {
            onlineOfflineTxtView.setText(LBL_GO_ONLINE_TXT);
        }

        hileimagview.setVisibility(View.GONE);
        EODTripImageview.setVisibility(View.GONE);
        removeEODTripData(false);
        stopService(startUpdatingStatus);

        generalFunc.storeData(Utils.DRIVER_ONLINE_KEY, "false");

        ConfigPubNub.getInstance().unSubscribeToCabRequestChannel();

        NotificationScheduler.cancelReminder(MyApp.getInstance().getCurrentAct(), AlarmReceiver.class);
    }

    public void setOnlineState() {

        isHailRideOptionEnabled();
        isDriverOnline = true;
        if (app_type.equals(Utils.CabGeneralType_UberX)) {
            ufxonlineOfflineTxtView.setText(LBL_GO_OFFLINE_TXT);
            ufxTitleonlineOfflineTxtView.setText(LBL_ONLINE);
        } else {
            onlineOfflineTxtView.setText(LBL_GO_OFFLINE_TXT);
        }


        if (!generalFunc.isServiceRunning(UpdateDriverStatus.class)) {
            startService(startUpdatingStatus);
        }

        generalFunc.storeData(Utils.DRIVER_ONLINE_KEY, "true");

        updateLocationToPubNub();

        ConfigPubNub.getInstance().subscribeToCabRequestChannel();

    }

    public void accountVerificationAlert(String message, final Bundle bn) {
        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(btn_id -> {
            if (btn_id == 1) {
                generateAlert.closeAlertBox();
                (new StartActProcess(getActContext())).startActForResult(VerifyInfoActivity.class, bn, Utils.VERIFY_INFO_REQ_CODE);
            } else if (btn_id == 0) {
                generateAlert.closeAlertBox();
            }
        });
        generateAlert.setContentMessage("", message);
        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_CANCEL_TRIP_TXT"));
        generateAlert.showAlertBox();
    }

    public void updateLocationToPubNub() {
        if (isDriverOnline == true && userLocation != null && userLocation.getLongitude() != 0.0 && userLocation.getLatitude() != 0.0) {
            if (lastPublishedLoc != null) {

                if (userLocation.distanceTo(lastPublishedLoc) < PUBSUB_PUBLISH_DRIVER_LOC_DISTANCE_LIMIT) {
                    return;
                } else {
                    lastPublishedLoc = userLocation;
                }

            } else {
                lastPublishedLoc = userLocation;
            }


            ConfigPubNub.getInstance().publishMsg(generalFunc.getLocationUpdateChannel(), generalFunc.buildLocationJson(userLocation));
        }
    }

    public void getNearByPassenger(String radius, double center_lat, double center_long) {

        if (heatMapAsyncTask != null) {
            heatMapAsyncTask.cancel(true);
            heatMapAsyncTask = null;
        }

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "loadPassengersLocation");
        parameters.put("Radius", radius);
        parameters.put("Latitude", String.valueOf(center_lat));
        parameters.put("Longitude", String.valueOf(center_long));
        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        this.heatMapAsyncTask = exeWebServer;

        exeWebServer.setDataResponseListener(responseString -> {

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail) {
                    JSONArray dataLocArr = generalFunc.getJsonArray(Utils.message_str, responseString);

                    ArrayList<LatLng> listTemp = new ArrayList<LatLng>();
                    ArrayList<LatLng> Online_listTemp = new ArrayList<LatLng>();
                    for (int i = 0; i < dataLocArr.length(); i++) {
                        JSONObject obj_temp = generalFunc.getJsonObject(dataLocArr, i);

                        String type = generalFunc.getJsonValueStr("Type", obj_temp);

                        double lat = GeneralFunctions.parseDoubleValue(0.0, generalFunc.getJsonValueStr("Latitude", obj_temp));
                        double longi = GeneralFunctions.parseDoubleValue(0.0, generalFunc.getJsonValueStr("Longitude", obj_temp));


                        if (type.equalsIgnoreCase("Online")) {

                            String iUserId = generalFunc.getJsonValueStr("iUserId", obj_temp);

                            if (!onlinePassengerLocList.containsKey("ID_" + type + "_" + iUserId)) {
                                onlinePassengerLocList.put("ID_" + type + "_" + iUserId, "True");

                                Online_listTemp.add(new LatLng(lat, longi));
                            }


                        } else {
                            String iTripId = generalFunc.getJsonValueStr("iTripId", obj_temp);
                            if (!historyLocList.containsKey("ID_" + type + "_" + iTripId)) {
                                historyLocList.put("ID_" + type + "_" + iTripId, "True");

                                listTemp.add(new LatLng(lat, longi));
                            }
                        }
                    }

                    if (listTemp.size() > 0) {
                        mapOverlayList.add(getMap().addTileOverlay(new TileOverlayOptions().tileProvider(
                                new HeatmapTileProvider.Builder().gradient(new Gradient(new int[]{Color.rgb(153, 0, 0), Color.WHITE}, new float[]{0.2f, 1.5f})).data(listTemp).build())));
                    }
                    if (Online_listTemp.size() > 0) {
                        mapOverlayList.add(getMap().addTileOverlay(new TileOverlayOptions().tileProvider(
                                new HeatmapTileProvider.Builder().gradient(new Gradient(new int[]{Color.rgb(0, 51, 0), Color.WHITE}, new float[]{0.2f, 1.5f}, 1000)).data(Online_listTemp).build())));
                    }
                    if (!isShowNearByPassengers) {

                        configHeatMapView(false);
                    } else {
                        configHeatMapView(true);
                    }
                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

    public void configCarList(final boolean isCarUpdate, final String selectedCarId,
                              final int position) {
        final HashMap<String, String> parameters = new HashMap<String, String>();
        if (!isCarUpdate) {
            parameters.put("type", "LoadAvailableCars");
        } else {
            parameters.put("type", "SetDriverCarID");
            parameters.put("iDriverVehicleId", selectedCarId);
        }
        parameters.put("iDriverId", generalFunc.getMemberId());

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail) {

                    if (!isCarUpdate) {
                        LoadCarList(generalFunc.getJsonArray(Utils.message_str, responseString));
                    } else {

                        String vLicencePlateNo = generalFunc.getJsonValue("vLicencePlate", items_txt_car_json.get(position));
                        carNumPlateTxt.setText(vLicencePlateNo);
                        carNumPlateTxt.setVisibility(View.VISIBLE);

                        if (items_isHail_json.get(position).equalsIgnoreCase("Yes")) {
                            if (isDriverOnline) {
                                HailEnableOnDriverStatus = "Yes";
                                isHailRideOptionEnabled();
                            } else {
                                hileimagview.setVisibility(View.GONE);
                            }

                        } else {
                            HailEnableOnDriverStatus = "No";
                            hileimagview.setVisibility(View.GONE);
                        }
                        if (isDriverOnline) {
                            enableEOD();
                        } else {
                            EODTripImageview.setVisibility(View.GONE);
                        }

                        String vMake = generalFunc.getJsonValue("vMake", items_txt_car_json.get(position));
                        String vModel = generalFunc.getJsonValue("vTitle", items_txt_car_json.get(position));

                        carNameTxt.setText(vMake + " " + vModel);
                        selectedcar = selectedCarId;
                        carNameTxt.setVisibility(View.VISIBLE);
                        changeCarTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CHANGE"));

                        generalFunc.showMessage(generalFunc.getCurrentView(MainActivity.this), generalFunc.retrieveLangLBl("", "LBL_INFO_UPDATED_TXT"));
                    }

                } else {
                    String msg = generalFunc.getJsonValue(Utils.message_str, responseString);
                    if (msg.equalsIgnoreCase("LBL_INACTIVE_CARS_MESSAGE_TXT")) {
                        GenerateAlertBox alertBox = new GenerateAlertBox(getActContext());
                        alertBox.setContentMessage("", generalFunc.retrieveLangLBl("", msg));
                        alertBox.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
                        alertBox.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_CONTACT_US_TXT"));
                        alertBox.setBtnClickList(btn_id -> {

                            alertBox.closeAlertBox();
                            if (btn_id == 0) {
                                new StartActProcess(getActContext()).startAct(ContactUsActivity.class);
                            }
                        });
                        alertBox.showAlertBox();
                    } else {

                        if ((msg.equalsIgnoreCase("PENDING_SUBSCRIPTION") || msg.equalsIgnoreCase("LBL_PENDING_MIXSUBSCRIPTION"))) {

                            showSubscriptionStatusDialog(true,msg);
                        }
                        else {
                            generalFunc.showGeneralMessage("",
                                    generalFunc.retrieveLangLBl("", msg));
                        }
                    }
                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }


    public void LoadCarList(JSONArray array) {

        items_txt_car.clear();
        items_car_id.clear();
        items_txt_car_json.clear();
        items_isHail_json.clear();
        final ArrayList list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj_temp = generalFunc.getJsonObject(array, i);

            items_txt_car.add(generalFunc.getJsonValue("vMake", obj_temp) + " " + generalFunc.getJsonValue("vTitle", obj_temp));

            items_car_id.add(generalFunc.getJsonValueStr("iDriverVehicleId", obj_temp));
            items_txt_car_json.add(obj_temp.toString());
            items_isHail_json.add(generalFunc.getJsonValueStr("Enable_Hailtrip", obj_temp));

            HashMap<String, String> map = new HashMap<String, String>();
            map.put("car", items_txt_car.get(i).toString());
            map.put("iDriverVehicleId", items_car_id.get(i).toString());
            map.put("vLicencePlate", generalFunc.getJsonValueStr("vLicencePlate", obj_temp));
            list.add(map);
        }

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());

        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_selectcar_view, null);

        final MTextView vehTitleTxt = (MTextView) dialogView.findViewById(R.id.VehiclesTitleTxt);
        final MTextView mangeVehiclesTxt = (MTextView) dialogView.findViewById(R.id.mangeVehiclesTxt);
        final MTextView addVehiclesTxt = (MTextView) dialogView.findViewById(R.id.addVehiclesTxt);
        final RecyclerView vehiclesRecyclerView = (RecyclerView) dialogView.findViewById(R.id.vehiclesRecyclerView);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(vehiclesRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL_LIST);
        vehiclesRecyclerView.addItemDecoration(dividerItemDecoration);

        builder.setView(dialogView);
        vehTitleTxt.setText(generalFunc.retrieveLangLBl("Select Your Vehicles", "LBL_SELECT_CAR_TXT"));
        if (!app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX)) {
            mangeVehiclesTxt.setText(generalFunc.retrieveLangLBl("", "LBL_MANAGE_VEHICLES"));
        } else {
            mangeVehiclesTxt.setText(generalFunc.retrieveLangLBl("", "LBL_MANANGE_SERVICES"));
        }
        addVehiclesTxt.setText(generalFunc.retrieveLangLBl("ADD NEW", "LBL_ADD_VEHICLES"));

        ManageVehicleListAdapter adapter = new ManageVehicleListAdapter(getActContext(), list, generalFunc, selectedcar);
        vehiclesRecyclerView.setAdapter(adapter);
        adapter.setOnItemClickList(this);

        mangeVehiclesTxt.setOnClickListener(v -> {
            list_car.dismiss();

            Bundle bn = new Bundle();
            bn.putString("app_type", app_type);
            bn.putString("iDriverVehicleId", generalFunc.getJsonValueStr("iDriverVehicleId", obj_userProfile));

            if (app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX) && isUfxServicesEnabled) {
                bn.putString("selView", "vehicle");
                bn.putString("apptype", app_type);
                bn.putInt("totalVehicles", 1);
                bn.putString("UBERX_PARENT_CAT_ID", generalFunc.getJsonValueStr("UBERX_PARENT_CAT_ID", obj_userProfile));
                new StartActProcess(getActContext()).startActWithData(UploadDocTypeWiseActivity.class, bn);
            } else {
                new StartActProcess(getActContext()).startActWithData(ManageVehiclesActivity.class, bn);
            }
        });

        addVehiclesTxt.setOnClickListener(v -> list_car.dismiss());

        builder.setNegativeButton(generalFunc.retrieveLangLBl("", "LBL_MANAGE_VEHICLES"), (dialog, which) -> {
            dialog.cancel();

            Bundle bn = new Bundle();
            bn.putString("app_type", app_type);
            bn.putString("iDriverVehicleId", generalFunc.getJsonValueStr("iDriverVehicleId", obj_userProfile));

            new StartActProcess(getActContext()).startActWithData(ManageVehiclesActivity.class, bn);

        });

        builder.setPositiveButton(generalFunc.retrieveLangLBl("ADD NEW", "LBL_ADD_VEHICLES"), (dialog, which) -> {

            dialog.cancel();
            Bundle bn = new Bundle();
            bn.putString("app_type", app_type);
            (new StartActProcess(getActContext())).startActWithData(AddVehicleActivity.class, bn);
        });


        list_car = builder.create();
        if (generalFunc.isRTLmode()) {
            generalFunc.forceRTLIfSupported(list_car);
        }
        list_car.show();
        final Button positiveButton = list_car.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(getResources().getColor(R.color.appThemeColor_1));
        final Button negativeButton = list_car.getButton(AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setTextColor(getResources().getColor(R.color.black));
        list_car.setOnCancelListener(dialogInterface -> Utils.hideKeyboard(getActContext()));
    }

    @Override
    public void onLocationUpdate(Location location) {

        try {
            if (location == null) {
                return;
            }

            if (isShowNearByPassengers) {
                return;
            }
            if (generalFunc.checkLocationPermission(true) && getMap() != null && !getMap().isMyLocationEnabled()) {
                getMap().setMyLocationEnabled(true);
            }

            this.userLocation = location;

            if (updateDirections != null) {
                updateDirections.changeUserLocation(location);
            }

            CameraPosition cameraPosition = cameraForUserPosition();

            if (cameraPosition != null)
                getMap().moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            if (!isFirstAddressLoaded) {
                getAddressFromLocation.setLocation(userLocation.getLatitude(), userLocation.getLongitude());
                getAddressFromLocation.execute();
                isFirstAddressLoaded = true;
            }

            if (isFirstLocation && generalFunc.getJsonValueStr("eEmailVerified", obj_userProfile).equalsIgnoreCase("YES") &&
                    generalFunc.getJsonValueStr("ePhoneVerified", obj_userProfile).equalsIgnoreCase("YES")) {

                isFirstLocation = false;

                String isGoOnline = generalFunc.retrieveValue(Utils.GO_ONLINE_KEY);

                if ((isGoOnline != null && !isGoOnline.equals("") && isGoOnline.equals("Yes"))) {
                    long lastTripTime = GeneralFunctions.parseLongValue(0, generalFunc.retrieveValue(Utils.LAST_FINISH_TRIP_TIME_KEY));
                    long currentTime = Calendar.getInstance().getTimeInMillis();

                    if ((currentTime - lastTripTime) < 25000) {
                        if (generalFunc.isLocationEnabled()) {
                            isOnlineOfflineSwitchCalled = true;
                            if (app_type.equals(Utils.CabGeneralType_UberX)) {
                                ufxonlineOfflineSwitch.setChecked(true);
                            } else {
                                onlineOfflineSwitch.setChecked(true);
                            }
                        }
                    }
                    HashMap<String,String> storeData=new HashMap<>();
                    storeData.put(Utils.GO_ONLINE_KEY, "No");
                    storeData.put(Utils.LAST_FINISH_TRIP_TIME_KEY, "0");
                    generalFunc.storeData(storeData);

                }

                if (generalFunc.isLocationEnabled() && generalFunc.getJsonValueStr("vAvailability", obj_userProfile).equals("Available") && !isDriverOnline) {
                    isOnlineOfflineSwitchCalled = true;
                    if (app_type.equals(Utils.CabGeneralType_UberX)) {
                        ufxonlineOfflineSwitch.setChecked(true);
                    } else {
                        onlineOfflineSwitch.setChecked(true);
                    }
                }
            }
        } catch (Exception e) {

        }
    }

    public void pubNubStatus(PNStatusCategory status) {

    }

    public CameraPosition cameraForUserPosition() {
        double currentZoomLevel = getMap().getCameraPosition().zoom;

        // if (Utils.defaultZomLevel > currentZoomLevel) {
        currentZoomLevel = Utils.defaultZomLevel;
        //}
        if (userLocation != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(this.userLocation.getLatitude(), this.userLocation.getLongitude()))
                    .zoom((float) currentZoomLevel).build();

            return cameraPosition;
        } else {
            return null;
        }
    }

    public void openMenuProfile() {
        Bundle bn = new Bundle();
        // bn.putString("UserProfileJson", userProfileJson);
        bn.putBoolean("isDriverOnline", isDriverOnline);
        new StartActProcess(getActContext()).startActForResult(MyProfileActivity.class, bn, Utils.MY_PROFILE_REQ_CODE);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, final View view, int position, long l) {
        int itemId = GeneralFunctions.parseIntegerValue(0, list_menu_items.get(position)[2]);
        Bundle bn = new Bundle();
        // bn.putString("UserProfileJson", userProfileJson);

        Utils.hideKeyboard(MainActivity.this);

        drawerAdapter.notifyDataSetChanged();

        switch (itemId) {
            case Utils.MENU_PROFILE:
                openMenuProfile();
                break;

            case Utils.MENU_SET_AVAILABILITY:

                break;

            case Utils.MENU_PAYMENT:
                new StartActProcess(getActContext()).startActForResult(CardPaymentActivity.class, bn, Utils.CARD_PAYMENT_REQ_CODE);
                break;

            case Utils.MENU_RIDE_HISTORY:
                new StartActProcess(getActContext()).startActWithData(RideHistoryActivity.class, bn);
                break;

            case Utils.MENU_BOOKINGS:
                new StartActProcess(getActContext()).startActWithData(MyBookingsActivity.class, bn);
                break;

            case Utils.MENU_FEEDBACK:
                new StartActProcess(getActContext()).startActWithData(DriverFeedbackActivity.class, bn);
                break;
            case Utils.MENU_BANK_DETAIL:
                new StartActProcess(getActContext()).startActWithData(BankDetailActivity.class, bn);
                break;

            case Utils.MENU_ABOUT_US:
                new StartActProcess(getActContext()).startAct(StaticPageActivity.class);
                break;
            case Utils.MENU_POLICY:
                (new StartActProcess(getActContext())).openURL(CommonUtilities.SERVER_URL + "privacy-policy");
                break;
            case Utils.MENU_CONTACT_US:
                new StartActProcess(getActContext()).startAct(ContactUsActivity.class);
                break;
            case Utils.MENU_YOUR_DOCUMENTS:
                bn.putString("PAGE_TYPE", "Driver");
                bn.putString("iDriverVehicleId", "");
                new StartActProcess(getActContext()).startActWithData(ListOfDocumentActivity.class, bn);
                break;

            case Utils.MENU_TRIP_STATISTICS:
                new StartActProcess(getActContext()).startActWithData(StatisticsActivity.class, bn);
                break;
            case Utils.MENU_MANAGE_VEHICLES:

                bn.putString("iDriverVehicleId", generalFunc.getJsonValueStr("iDriverVehicleId", obj_userProfile));
                bn.putString("app_type", app_type);

                if (app_type.equalsIgnoreCase(Utils.CabGeneralType_UberX) || (app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX) && generalFunc.getJsonValueStr("eShowVehicles", obj_userProfile).equalsIgnoreCase("No"))) {
                    bn.putString("UBERX_PARENT_CAT_ID", generalFunc.getJsonValueStr("UBERX_PARENT_CAT_ID", obj_userProfile));

                } else if ((app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX)&&isUfxServicesEnabled) && generalFunc.getJsonValueStr("eShowVehicles", obj_userProfile).equalsIgnoreCase("Yes")) {
                    bn.putString("apptype", app_type);
                    bn.putString("selView", "vehicle");
                    bn.putInt("totalVehicles", 1);
                    bn.putString("UBERX_PARENT_CAT_ID", app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX) ? generalFunc.getJsonValueStr("UBERX_PARENT_CAT_ID", obj_userProfile) : "");
                    new StartActProcess(getActContext()).startActWithData(UploadDocTypeWiseActivity.class, bn);

                } else {
                    new StartActProcess(getActContext()).startActWithData(ManageVehiclesActivity.class, bn);
                }
                break;

            case Utils.MENU_HELP:
                new StartActProcess(getActContext()).startAct(HelpActivity.class);
                break;

            case Utils.MENU_WALLET:
                iswallet = true;
                new StartActProcess(getActContext()).startActWithData(MyWalletActivity.class, bn);
                break;

            case Utils.MENU_WAY_BILL:


                break;
            case Utils.MENU_ACCOUNT_VERIFY:

                boolean isEmailVerified=generalFunc.getJsonValueStr("eEmailVerified", obj_userProfile).equalsIgnoreCase("YES");
                boolean isPhoneVerified=generalFunc.getJsonValueStr("ePhoneVerified", obj_userProfile).equalsIgnoreCase("YES");

                if (!isEmailVerified ||
                        !isPhoneVerified) {
                    Bundle bn1 = new Bundle();
                    if (!isEmailVerified &&
                            !isPhoneVerified) {
                        bn1.putString("msg", "DO_EMAIL_PHONE_VERIFY");
                    } else if (!isEmailVerified) {
                        bn1.putString("msg", "DO_EMAIL_VERIFY");
                    } else if (!isPhoneVerified) {
                        bn1.putString("msg", "DO_PHONE_VERIFY");
                    }

                    new StartActProcess(getActContext()).startActForResult(VerifyInfoActivity.class, bn1, Utils.VERIFY_INFO_REQ_CODE);
                }
                break;
            case Utils.MENU_INVITE_FRIEND:
                new StartActProcess(getActContext()).startActWithData(InviteFriendsActivity.class, bn);
                break;

            case Utils.MENU_EMERGENCY_CONTACT:
                new StartActProcess(getActContext()).startAct(EmergencyContactActivity.class);
                break;
            case Utils.MENU_NOTIFICATION:

                break;

            case Utils.MENU_SUPPORT:
                new StartActProcess(getActContext()).startAct(SupportActivity.class);
                break;
            case Utils.MENU_YOUR_TRIPS:
                new StartActProcess(getActContext()).startActWithData(HistoryActivity.class, bn);
                break;

            case Utils.MENU_SIGN_OUT:
                MyApp.getInstance().logOutFromDevice(false);
                break;

            case Utils.MENU_MY_HEATVIEW:
                new StartActProcess(getActContext()).startActWithData(MyHeatViewActivity.class, bn);
                break;
        }

        closeDrawer();
    }

    public void checkDrawerState() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            closeDrawer();
        } else {
            openDrawer();
        }
    }

    public void closeDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    public void openDrawer() {
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    public Context getActContext() {
        return MainActivity.this;
    }

    public void checkIsDriverOnline() {
        if (isDriverOnline) {
            stopService(startUpdatingStatus);

            for (int i = 0; i < 1000; i++) {
            }
        }
    }


    /*public void registerBackgroundAppReceiver() {

        unRegisterBackgroundAppReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Utils.BACKGROUND_APP_RECEIVER_INTENT_ACTION);

        registerReceiver(bgAppReceiver, filter);
    }

    public void unRegisterBackgroundAppReceiver() {
        if (bgAppReceiver != null) {
            try {
                unregisterReceiver(bgAppReceiver);
            } catch (Exception e) {

            }
        }
    }*/

    public void getWalletBalDetails() {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "GetMemberWalletBalance");
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("UserType", Utils.app_type);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), false, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail) {
                    try {
                        JSONObject  userProfileObj = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
                        userProfileObj.put("user_available_balance", generalFunc.getJsonValue("MemberBalance", responseString));
                        generalFunc.storeData(Utils.USER_PROFILE_JSON, userProfileObj.toString());

                        obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
                        changeObj();
                        setWalletInfo();
                    } catch (Exception e) {

                    }
                }
            }
        });
        exeWebServer.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isCarChangeTxt = false;
        getWalletBalDetails();
        obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
        changeObj();
        if (addDrawer != null) {
            addDrawer.obj_userProfile = obj_userProfile;
            addDrawer.buildDrawer();
        }

        handleWorkAddress();

        if (isDriverOnline) {
            isHailRideOptionEnabled();
        }

        obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));

        if (addDrawer != null) {
            addDrawer.changeUserProfileJson(generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON)));
        }

        if (app_type.equals(Utils.CabGeneralType_UberX) || app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX)) {
            getUserstatus();
        }

        setUserInfo();
        if (iswallet) {
            obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));

            iswallet = false;

            if (addDrawer != null) {
                addDrawer.changeUserProfileJson(obj_userProfile);
                addDrawer.setIswallet(false);
            }
        }

        if (generalFunc.retrieveValue(Utils.DRIVER_ONLINE_KEY).equals("false") && isDriverOnline) {
            setOfflineState();
            isOnlineAvoid = true;
            if (app_type.equals(Utils.CabGeneralType_UberX)) {
                ufxonlineOfflineSwitch.setChecked(false);

            } else {
                onlineOfflineSwitch.setChecked(false);
            }

        }
    }

    @Override
    protected void onPause() {
        super.onPause();

//        if (getLastLocation != null) {
//            getLastLocation.stopLocationUpdates();
//        }
    }

    public MyApp getApp() {
        return ((MyApp) getApplication());
    }

    public void configBackground() {

        if (!isCurrentReqHandled) {
            generalFunc.removeValue(Utils.DRIVER_ACTIVE_REQ_MSG_KEY);
            return;
        }

        if (!getApp().isMyAppInBackGround()) {
            if (!getApp().isMyAppInBackGround() && isDriverOnline) {
                setOnlineState();
            }

            if (generalFunc.containsKey(Utils.DRIVER_ACTIVE_REQ_MSG_KEY)) {

                String msg = generalFunc.retrieveValue(Utils.DRIVER_ACTIVE_REQ_MSG_KEY);

                generalFunc.removeValue(Utils.DRIVER_ACTIVE_REQ_MSG_KEY);

                generalFunc.storeData(Utils.DRIVER_CURRENT_REQ_OPEN_KEY, "true");

                (new FireTripStatusMsg(getActContext(), "PubSub")).fireTripMsg(msg);

            }
        }
    }


    public void removeLocationUpdates() {

        if (GetLocationUpdates.retrieveInstance() != null) {
            GetLocationUpdates.getInstance().stopLocationUpdates(this);
        }

        this.userLocation = null;
    }

    @Override
    protected void onDestroy() {
        try {
            checkIsDriverOnline();
            removeLocationUpdates();
            //unRegisterBackgroundAppReceiver();

            if (getAddressFromLocation != null) {
                getAddressFromLocation.setAddressList(null);
                getAddressFromLocation = null;
            }

            if (gMap != null) {
                this.gMap.setOnCameraChangeListener(null);
                this.gMap = null;
            }

            if (heatMapAsyncTask != null) {
                heatMapAsyncTask.cancel(true);
                heatMapAsyncTask = null;
            }

            if (updateRequest != null) {
                updateRequest.stopRepeatingTask();
                updateRequest = null;
            }

            Utils.runGC();
        } catch (Exception e) {

        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {


        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            closeDrawer();
            return;
        }

        if (faredialog != null && faredialog.isShowing()) {
            return;
        }


        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(btn_id -> {
            if (btn_id == 0) {
                generateAlert.closeAlertBox();
            } else {
                generateAlert.closeAlertBox();
                MyApp.getInstance().onTerminate();
                MainActivity.super.onBackPressed();
            }
        });

        generateAlert.setContentMessage(generalFunc.retrieveLangLBl("Exit App", "LBL_EXIT_APP_TITLE_TXT"), generalFunc.retrieveLangLBl("Are you sure you want to exit?", "LBL_WANT_EXIT_APP_TXT"));
        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_YES"));
        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_NO"));
        generateAlert.showAlertBox();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.MY_PROFILE_REQ_CODE && resultCode == RESULT_OK && data != null) {
            // String userProfileJson = data.getStringExtra("UserProfileJson");
            obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
            changeObj();
            if (addDrawer != null) {
                addDrawer.changeUserProfileJson(obj_userProfile);
            }

            setUserInfo();
            ((MTextView) findViewById(R.id.userNameTxt)).setText(generalFunc.getJsonValueStr("vName", obj_userProfile) + " "
                    + generalFunc.getJsonValueStr("vLastName", obj_userProfile));
        } else if (requestCode == Utils.VERIFY_INFO_REQ_CODE && resultCode == RESULT_OK && data != null) {
            String msgType = data.getStringExtra("MSG_TYPE");
            if (msgType.equalsIgnoreCase("EDIT_PROFILE")) {
                //openMenuProfile();
                addDrawer.openMenuProfile();
            }

            obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
            changeObj();
            addDrawer.obj_userProfile = obj_userProfile;
            addDrawer.buildDrawer();

        } else if (requestCode == Utils.VERIFY_INFO_REQ_CODE) {

            obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
            changeObj();
            addDrawer.obj_userProfile = obj_userProfile;
            addDrawer.buildDrawer();

            //buildMenu();
        } else if (requestCode == Utils.CARD_PAYMENT_REQ_CODE && resultCode == RESULT_OK && data != null) {

            obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
            changeObj();
            if (addDrawer != null) {
                addDrawer.changeUserProfileJson(obj_userProfile);
            }

        } else if (requestCode == Utils.REQUEST_CODE_GPS_ON) {
            handleNoLocationDial();
        } else if (requestCode == Utils.REQUEST_CODE_NETWOEK_ON) {
            handleNoNetworkDial();
        } else if (resultCode == RESULT_OK && data != null && data.hasExtra("isMoneyAddedOrTransferred")) {

            if (isDriverOnline) {
                if (app_type.equals(Utils.CabGeneralType_UberX)) {
                    ufxonlineOfflineSwitch.setChecked(true);

                } else {
                    onlineOfflineSwitch.setChecked(true);
                }
            }
        }
        /*EndOfTheDay view click event*/
        else if (requestCode == Utils.SEARCH_PICKUP_LOC_REQ_CODE && resultCode == RESULT_OK && data != null && gMap != null) {
            drawRoute(data);

        }

    }

    /*EndOfTheDay Trip Implementation Start */

    public void isRouteDrawn() {
        hileimagview.setVisibility(View.GONE);
        EODTripImageview.setVisibility(View.GONE);
        userHeatmapBtnImgView.setVisibility(View.GONE);

        handleMapAnimation();

        if (!updateDirections.data.hasExtra("eDestinationMode")) {
            if (faredialog == null) {
                openDestinationConfirmationDialog();
            } else if (faredialog != null && !faredialog.isShowing()) {

                faredialog.show();
            }
        } else {
            if (eodLocationArea.getVisibility() == View.GONE) {
                eodLocationArea.setVisibility(View.VISIBLE);
                addressTxt.setText(updateDirections.data.getStringExtra("Address"));
            }

        }
    }


    private void enableEOD() {
        boolean eDestinationMode = generalFunc.getJsonValueStr("eDestinationMode", obj_userProfile).equalsIgnoreCase("Yes");
        boolean ENABLE_DRIVER_DESTINATIONS = generalFunc.retrieveValue(Utils.ENABLE_DRIVER_DESTINATIONS_KEY).equalsIgnoreCase("Yes") && !eDestinationMode;
        EODTripImageview.setVisibility(ENABLE_DRIVER_DESTINATIONS ? View.VISIBLE : View.GONE);
        JSONObject DriverDestinationData_obj = generalFunc.getJsonObject("DriverDestinationData", obj_userProfile);

        if (eDestinationMode && DriverDestinationData_obj != null && DriverDestinationData_obj.length() > 0) {
            Intent data = new Intent();
            data.putExtra("Latitude", generalFunc.getJsonValueStr("tDestinationStartedLatitude", DriverDestinationData_obj));
            data.putExtra("Longitude", generalFunc.getJsonValueStr("tDestinationStartedLongitude", DriverDestinationData_obj));
            data.putExtra("Address", generalFunc.getJsonValueStr("tDestinationStartedAddress", DriverDestinationData_obj));
            data.putExtra("eDestinationMode", generalFunc.getJsonValueStr("eDestinationMode", obj_userProfile));

            drawRoute(data);
        }

    }

    private void drawRoute(Intent data) {
        String destlat = data.getStringExtra("Latitude");
        String destlong = data.getStringExtra("Longitude");

        Location destLoc = new Location("temp");
        destLoc.setLatitude(generalFunc.parseDoubleValue(0.0, destlat));
        destLoc.setLongitude(generalFunc.parseDoubleValue(0.0, destlong));

        if (updateDirections == null) {
            updateDirections = new UpdateDirections(getActContext(), gMap, userLocation, destLoc);

        }
        if (updateDirections != null) {
            updateDirections.changeUserLocation(userLocation);
            updateDirections.setIntentData(data);
            if (!data.hasExtra("eDestinationMode")) {
                updateDirections.scheduleDirectionUpdate();
            } else {
                updateDirections.updateDirections();
            }
        }
    }


    public void handleMapAnimation() {

        try {
            LatLng sourceLocation = new LatLng(updateDirections.userLocation.getLatitude(), updateDirections.userLocation.getLongitude());
            LatLng destLocation = new LatLng(updateDirections.destinationLocation.getLatitude(), updateDirections.destinationLocation.getLongitude());

            MapAnimator.getInstance().stopRouteAnim();

            LatLng fromLnt = new LatLng(sourceLocation.latitude, sourceLocation.longitude);
            LatLng toLnt = new LatLng(destLocation.latitude, destLocation.longitude);

            if (destMarker != null) {
                destMarker.remove();
                destMarker = null;
            }
            MarkerOptions markerOptions_destLocation = new MarkerOptions();
            markerOptions_destLocation.position(toLnt);
            markerOptions_destLocation.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_dest_select)).anchor(0.5f,
                    0.5f);
            destMarker = getMap().addMarker(markerOptions_destLocation);

            if (sourceMarker != null) {
                sourceMarker.remove();
                sourceMarker = null;
            }
            MarkerOptions markerOptions_sourceLocation = new MarkerOptions();
            markerOptions_sourceLocation.position(fromLnt);
            markerOptions_sourceLocation.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_source_select)).anchor(0.5f,
                    0.5f);
            sourceMarker = getMap().addMarker(markerOptions_sourceLocation);

            buildMarkers();

        } catch (Exception e) {
            // Backpress done by user then app crashes

            e.printStackTrace();
        }

    }

    private void buildMarkers() {
        {
            map.getView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @SuppressLint("NewApi") // We check which build version we are using.
                @Override
                public void onGlobalLayout() {

                    boolean isBoundIncluded = false;

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();

                    if (sourceMarker != null) {
                        isBoundIncluded = true;
                        builder.include(sourceMarker.getPosition());
                    }


                    if (destMarker != null) {
                        isBoundIncluded = true;
                        builder.include(destMarker.getPosition());
                    }


                    if (isBoundIncluded) {

                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            map.getView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            map.getView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }

                        LatLngBounds bounds = builder.build();

                        DisplayMetrics metrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(metrics);
                        int height_ = metrics.heightPixels;
                        int width = metrics.widthPixels;
                        // Set Padding according to included bounds
                        int padding = 25; // offset from edges of the map in pixels
                        int height_NW;
                        if (faredialog != null && faredialog.isShowing()) {
                            height_NW = (height_ - height) - Utils.dipToPixels(getActContext(), 80);
                            Logger.d("HEIGHT", "" + height);
                            Logger.d("height_NW", "" + height_NW);
                        } else {
                            height_NW = height_ - Utils.dipToPixels(getActContext(), 140) - Utils.dipToPixels(getActContext(), 80);
                            Logger.d("height_NW", "" + height_NW);
                        }


                        try {
                            /*  Method 3 */
                            padding = (int) (((height_NW + 5) * 0.100) / 2);
                            Logger.e("MapHeight", "cameraUpdate" + padding);
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(),
                                    width, (height_NW + 5), padding);
                            getMap().animateCamera(cameraUpdate);
                        } catch (Exception e) {
                            e.printStackTrace();
                            /*  Method 1 */
                            getMap().moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), width, (height_NW + 5), padding));
                        }


                    }

                }
            });
        }
    }


    public void openDestinationConfirmationDialog() {
        if (faredialog != null) {
            faredialog.dismiss();
        }

        faredialog = new BottomSheetDialog(getActContext());


        View bottomSheetView = faredialog.getWindow().getDecorView().findViewById(android.support.design.R.id.design_bottom_sheet);
        BottomSheetBehavior.from(bottomSheetView).setHideable(false);
        setCancelable(faredialog, false);


        ProgressBar mProgressBar = (ProgressBar) faredialog.findViewById(R.id.mProgressBar);
//        View shadowView = (View) faredialog.findViewById(R.id.shadowView);
//        View leftSeperationLine = (View) faredialog.findViewById(R.id.leftSeperationLine);

        MButton btn_type2 = ((MaterialRippleLayout) faredialog.findViewById(R.id.btn_type2)).getChildView();

        int submitBtnId = Utils.generateViewId();
        btn_type2.setId(submitBtnId);
        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_START_DEST_MODE_TXT"));

        /*ViewTreeObserver vto = layout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                height = layout.getMeasuredHeight();
                mBehavior.setPeekHeight(height);

            }
        });*/

        height = Utils.dpToPx(380, getActContext());

        mProgressBar.getIndeterminateDrawable().setColorFilter(
                getActContext().getResources().getColor(R.color.appThemeColor_2), android.graphics.PorterDuff.Mode.SRC_IN);
        mProgressBar.setIndeterminate(true);
        mProgressBar.setVisibility(View.VISIBLE);
//        shadowView.setVisibility(View.VISIBLE);


        btn_type2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDestination(faredialog);
            }
        });

        faredialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
            }
        });
        faredialog.show();

    }

    private String getRemaningDest() {
        String destAddressSHLbl = "";
        int MAX_DRIVER_DESTINATIONS = GeneralFunctions.parseIntegerValue(0, generalFunc.getJsonValueStr("MAX_DRIVER_DESTINATIONS", obj_userProfile));
        int iDestinationCount = GeneralFunctions.parseIntegerValue(0, generalFunc.getJsonValueStr("iDestinationCount", obj_userProfile));
        String remainingDestCount = generalFunc.convertNumberWithRTL("" + (MAX_DRIVER_DESTINATIONS - iDestinationCount));
        destAddressSHLbl = generalFunc.retrieveLangLBl("", "LBL_DESTINATION") + ": " + remainingDestCount + " " + generalFunc.retrieveLangLBl("", "LBL_REMAINIG_TXT");
        return destAddressSHLbl;

    }

    private void removeEODTripData(boolean resetHail) {
        if (sourceMarker != null) {
            sourceMarker.remove();
            sourceMarker = null;
        }

        if (destMarker != null) {
            destMarker.remove();
            destMarker = null;
        }


        if (updateDirections != null) {
            updateDirections.releaseTask();
            updateDirections = null;
        }

        eodLocationArea.setVisibility(View.GONE);
        showHeatMap();

        if (resetHail) {
            isHailRideOptionEnabled();
        }

        if (gMap!=null)
        gMap.clear();

    }


    public void confirmDestination(BottomSheetDialog dialog1) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());
        builder.setTitle("");
        String message = generalFunc.retrieveLangLBl("", "LBL_START_DESTINATION_TRIP");
        builder.setMessage(message);

        builder.setPositiveButton(generalFunc.retrieveLangLBl("", "LBL_BTN_YES_TXT"), (dialog, which) -> {

        });
        builder.setNegativeButton(generalFunc.retrieveLangLBl("", "LBL_NO"), (dialog, which) -> {
        });

        confirmDialog = builder.create();
        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(confirmDialog);
        }
        confirmDialog.show();

        confirmDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startDriverDestination(dialog1, updateDirections.data);


            }
        });

        confirmDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmDialog.dismiss();

            }
        });
    }

    public void startDriverDestination(BottomSheetDialog dialog1, Intent data) {
        String destlat = data.getStringExtra("Latitude");
        String destlong = data.getStringExtra("Longitude");
        String destAddress = data.getStringExtra("Address");

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "startDriverDestination");
        parameters.put("iDriverId", generalFunc.getMemberId());
        parameters.put("tRootDestLatitudes", updateDirections != null ? TextUtils.join(",", updateDirections.lattitudeList) : "");
        parameters.put("tRootDestLongitudes", updateDirections != null ? TextUtils.join(",", updateDirections.longitudeList) : "");
        parameters.put("tAdress", destAddress);
        parameters.put("eStatus", "Active");
        parameters.put("tDriverDestLatitude", destlat);
        parameters.put("tDriverDestLongitude", destlong);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setIsDeviceTokenGenerate(true, "vDeviceToken", generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            confirmDialog.dismiss();

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail == true) {
                    String message = generalFunc.getJsonValue(Utils.message_str, responseString);
                    generalFunc.storeData(Utils.USER_PROFILE_JSON, message);
                    obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
                    changeObj();
                    dialog1.dismiss();

                    JSONObject DriverDestinationData_obj = generalFunc.getJsonObject("DriverDestinationData", obj_userProfile);
                    data.putExtra("Latitude", generalFunc.getJsonValueStr("tDestinationStartedLatitude", DriverDestinationData_obj));
                    data.putExtra("Longitude", generalFunc.getJsonValueStr("tDestinationStartedLongitude", DriverDestinationData_obj));
                    data.putExtra("Address", generalFunc.getJsonValueStr("tDestinationStartedAddress", DriverDestinationData_obj));
                    data.putExtra("eDestinationMode", generalFunc.getJsonValueStr("eDestinationMode", obj_userProfile));


                    if (updateDirections != null) {
                        updateDirections.setIntentData(data);
                        updateDirections.scheduleDirectionUpdate();
                    }

                    addressTxt.setText(data.getStringExtra("Address"));

                    eodLocationArea.setVisibility(View.VISIBLE);
                    handleMapAnimation();

                } else {

                    String message_str = generalFunc.getJsonValue(Utils.message_str, responseString);
                    String message = generalFunc.retrieveLangLBl(message_str, message_str);
                    generalFunc.showGeneralMessage("", message);
                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

    public void buildMsgOnEODCancelRequests() {
        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(btn_id -> {
            if (btn_id == 0) {
                generateAlert.closeAlertBox();
            } else {
                CancelDriverDestination();
            }

        });

        generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", "LBL_END_DESTINATION_TRIP"));

        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_YES_TXT"));
        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_NO_TXT"));
        generateAlert.showAlertBox();
    }

    public void CancelDriverDestination() {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "CancelDriverDestination");
        parameters.put("iDriverId", generalFunc.getMemberId());

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setIsDeviceTokenGenerate(true, "vDeviceToken", generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail == true) {
                    String message = generalFunc.getJsonValue(Utils.message_str, responseString);
                    generalFunc.storeData(Utils.USER_PROFILE_JSON, message);
                    obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
                    changeObj();

                    generalFunc.storeData(Utils.DRIVER_DESTINATION_AVAILABLE_KEY, generalFunc.getJsonValue(Utils.DRIVER_DESTINATION_AVAILABLE_KEY, message));

                    removeEODTripData(true);
                } else {
                    generalFunc.showError();
                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }


    public void setCancelable(Dialog dialogview, boolean cancelable) {
        final Dialog dialog = dialogview;
        View touchOutsideView = dialog.getWindow().getDecorView().findViewById(android.support.design.R.id.touch_outside);
        View bottomSheetView = dialog.getWindow().getDecorView().findViewById(android.support.design.R.id.design_bottom_sheet);
        dialog.setCancelable(cancelable);

        if (cancelable) {
            touchOutsideView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dialog.isShowing()) {
                        dialog.cancel();
                    }
                }
            });
            BottomSheetBehavior.from(bottomSheetView).setHideable(true);
        } else {
            touchOutsideView.setOnClickListener(null);
            BottomSheetBehavior.from(bottomSheetView).setHideable(false);
        }
    }

    /*EndOfTheDay Trip Implementation End */

    @Override
    public void onItemClick(int position, int viewClickId) {
        list_car.dismiss();

        String selected_carId = items_car_id.get(position);

        configCarList(true, selected_carId, position);
    }


    public void handleNoNetworkDial() {
        String eStatus = generalFunc.getJsonValueStr("eStatus", obj_userProfile);
        if (!eStatus.equalsIgnoreCase("inactive")) {

            if (intCheck.isNetworkConnected() && intCheck.check_int()) {

            }

            if (!intCheck.isNetworkConnected() && !intCheck.check_int()) {
            } else {
                handleNoLocationDial();
            }
        }
    }

    public void handleNoLocationDial() {
        if (!generalFunc.isLocationEnabled() && isDriverOnline == true) {
            if (app_type.equals(Utils.CabGeneralType_UberX)) {
                ufxonlineOfflineSwitch.setChecked(false);
            } else {
                onlineOfflineSwitch.setChecked(false);
            }
        }
    }


    @Override
    public void onAddressFound(String address, double latitude, double longitude) {

        if (generalFunc.getJsonValueStr("PROVIDER_AVAIL_LOC_CUSTOMIZE", obj_userProfile).equalsIgnoreCase("Yes") && generalFunc.getJsonValueStr("eSelectWorkLocation", obj_userProfile).equalsIgnoreCase("Fixed")) {
            String WORKLOCATION=generalFunc.retrieveValue(Utils.WORKLOCATION);
            if (!WORKLOCATION.equals("")) {
                addressTxtView.setText(WORKLOCATION);
            } else {
                addressTxtView.setText(address);
                addressTxtViewufx.setText(address);
            }
        } else {
            addressTxtView.setText(address);
            addressTxtViewufx.setText(address);
        }
    }



    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(MainActivity.this);

            if (view.getId() == menuImgView.getId()) {
//                checkDrawerState();
                if (addDrawer != null) {
                    addDrawer.checkDrawerState(true);
                }
            } else if (view.getId() == userLocBtnImgView.getId()) {
                if (userLocation == null) {
                    return;
                }
                CameraPosition cameraPosition = cameraForUserPosition();
                if (cameraPosition != null)
                    getMap().animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            } else if (view.getId() == userHeatmapBtnImgView.getId()) {
                if (userLocation == null) {
                    return;
                }
                isfirstZoom = true;
                configHeatMapView(isShowNearByPassengers ? false : true);
            } else if (view.getId() == changeCarTxt.getId()) {
                configCarList(false, "", 0);
            } else if (view.getId() == imgSetting.getId()) {
                closeDrawer();
                obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
                changeObj();

                if (addDrawer != null) {
                    addDrawer.obj_userProfile = obj_userProfile;
                }

                if (generalFunc.retrieveValue(Utils.FEMALE_RIDE_REQ_ENABLE).equalsIgnoreCase("yes")) {
                    if (generalFunc.getJsonValueStr("eGender", obj_userProfile).equalsIgnoreCase("feMale")) {
                        new StartActProcess(getActContext()).startAct(PrefranceActivity.class);
                    } else {
                        if (generalFunc.getJsonValueStr("eGender", obj_userProfile).equals("")) {
                            genderDailog();

                        } else {
                            menuListView.performItemClick(view, 0, Utils.MENU_PROFILE);
                        }
                    }
                } else {
                    menuListView.performItemClick(view, 0, Utils.MENU_PROFILE);
                }

            } else if (view.getId() == logoutarea.getId()) {
                final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
                generateAlert.setCancelable(false);
                generateAlert.setBtnClickList(btn_id -> {
                    if (btn_id == 0) {
                        generateAlert.closeAlertBox();
                    } else {
                        generateAlert.closeAlertBox();
                        MyApp.getInstance().logOutFromDevice(false);
                    }
                });
                generateAlert.setContentMessage(generalFunc.retrieveLangLBl("Logout", "LBL_LOGOUT"), generalFunc.retrieveLangLBl("Are you sure you want to logout?", "LBL_WANT_LOGOUT_APP_TXT"));
                generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_YES"));
                generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_NO"));
                generateAlert.showAlertBox();

            } else if (view.getId() == hileimagview.getId()) {
                if (!intCheck.isNetworkConnected() && !intCheck.check_int()) {
                    generalFunc.showMessage(menuImgView, generalFunc.retrieveLangLBl("No Internet Connection", "LBL_NO_INTERNET_TXT"));
                } else {
                    if (!isBtnClick) {
                        isBtnClick = true;
                        checkHailType();
                    }
                }
            } else if (view.getId() == menuufxImgView.getId()) {
//                checkDrawerState();
                if (addDrawer != null) {
                    addDrawer.checkDrawerState(true);
                }
            } else if (view.getId() == pendingarea.getId()) {
                Bundle bn = new Bundle();
                bn.putBoolean("ispending", true);
                new StartActProcess(getActContext()).startActWithData(HistoryActivity.class, bn);
            } else if (view.getId() == upcomginarea.getId()) {

                Bundle bn = new Bundle();
                bn.putBoolean("isupcoming", true);
                new StartActProcess(getActContext()).startActWithData(HistoryActivity.class, bn);

            } else if (view.getId() == radiusTxtView.getId()) {

            } else if (view.getId() == imageradius.getId()) {

            } else if (view.getId() == refreshImgView.getId()) {
                isFirstAddressLoaded = false;
                onLocationUpdate(GetLocationUpdates.getInstance().getLastLocation());
                getUserstatus();
            } else if (view.getId() == imageradiusufx.getId()) {

            } else if (view.getId() == btn_edit.getId()) {

            }
            /*EndOfTheDay Click events */
            else if (view.getId() == removeEodTripArea.getId()) {
                setBounceAnimation(removeEodTripArea, () -> {
                    buildMsgOnEODCancelRequests();
                });

            }
            else if (view.getId() == EODTripImageview.getId()) {

                if (generalFunc.retrieveValue(Utils.DRIVER_DESTINATION_AVAILABLE_KEY).equalsIgnoreCase("Yes")) {
                    Bundle bn = new Bundle();
                    bn.putString("requestType", "endOfDayTrip");
                    bn.putString("locationArea", "dest");

                    if (userLocation != null) {
                        bn.putDouble("lat", userLocation.getLatitude());
                        bn.putDouble("long", userLocation.getLongitude());
                    }


                    new StartActProcess(getActContext()).startActForResult(SearchLocationActivity.class, bn, Utils.SEARCH_PICKUP_LOC_REQ_CODE);

                } else {
                    String message = generalFunc.retrieveLangLBl("", "LBL_DRIVER_DEST_LIMIT_REACHED") + " " + generalFunc.parseIntegerValue(0, generalFunc.getJsonValueStr("MAX_DRIVER_DESTINATIONS", obj_userProfile)) + " " + generalFunc.retrieveLangLBl("", "LBL_FOR_A_DAY");
                    Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE);
                    snackbar.setActionTextColor(getActContext().getResources().getColor(R.color.verfiybtncolor));
                    snackbar.setDuration(10000);
                    snackbar.show();
                }

            }
        }
    }

    private void setBounceAnimation(View view, BounceAnimListener bounceAnimListener) {
        Animation anim = AnimationUtils.loadAnimation(getActContext(), R.anim.bounce_interpolator);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                if (bounceAnimListener != null) {
                    bounceAnimListener.onAnimationFinished();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(anim);
    }

    private interface BounceAnimListener {
        void onAnimationFinished();
    }

    public void getUserstatus() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "GetUserStats");
        parameters.put("iDriverId", generalFunc.getMemberId());

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), false, generalFunc);
        exeWebServer.setIsDeviceTokenGenerate(true, "vDeviceToken", generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail == true) {

                    pendingjobValTxtView.setText(generalFunc.getJsonValue("Pending_Count", responseString));

                    upcomingjobValTxtView.setText(generalFunc.getJsonValue("Upcoming_Count", responseString));

                    radiusval = generalFunc.getJsonValue("Radius", responseString);
                    setRadiusVal();

                }
            }
        });
        exeWebServer.execute();
    }


    private void checkHailType() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "CheckVehicleEligibleForHail");
        parameters.put("iDriverId", generalFunc.getMemberId());

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);

        exeWebServer.setDataResponseListener(responseString -> {

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail == true) {

                    isBtnClick = false;
                    Bundle bn = new Bundle();
                    bn.putString("userLocation", userLocation + "");
                    bn.putDouble("lat", userLocation.getLatitude());
                    bn.putDouble("long", userLocation.getLongitude());
                    new StartActProcess(getActContext()).startActWithData(HailActivity.class, bn);
                } else {
                    isBtnClick = false;

                    String message = generalFunc.getJsonValue(Utils.message_str, responseString);

                    if (message.equals("REQUIRED_MINIMUM_BALNCE")) {
                        isHailRideOptionEnabled();
                        Bundle bn = new Bundle();
                        bn.putString("UserProfileJson", obj_userProfile.toString());
                        buildLowBalanceMessage(getActContext(), generalFunc.getJsonValue("Msg", responseString), bn);
                        return;
                    }
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));

                }
            } else {
                isBtnClick = false;
            }
        });
        exeWebServer.execute();

    }

    public interface OnAlertButtonClickListener {
        void onAlertButtonClick(int buttonId);
    }

    public void buildLowBalanceMessage(final Context context, String message, final Bundle bn) {

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.design_cash_balance_dialoge, null);
        builder.setView(dialogView);

        final MTextView addNowTxtArea = (MTextView) dialogView.findViewById(R.id.addNowTxtArea);
        final MTextView msgTxt = (MTextView) dialogView.findViewById(R.id.msgTxt);
        final MTextView skipTxtArea = (MTextView) dialogView.findViewById(R.id.skipTxtArea);
        final MTextView titileTxt = (MTextView) dialogView.findViewById(R.id.titileTxt);
        titileTxt.setText(generalFunc.retrieveLangLBl("", "LBL_LOW_BALANCE"));

        boolean isCash=generalFunc.getJsonValue("APP_PAYMENT_MODE", bn.getString("UserProfileJson")).equalsIgnoreCase("Cash");

        if (isCash) {
            addNowTxtArea.setText(generalFunc.retrieveLangLBl("", "LBL_CONTACT_US_TXT"));
        } else {
            addNowTxtArea.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_NOW"));
        }


        skipTxtArea.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
        msgTxt.setText(message);


        skipTxtArea.setOnClickListener(view -> cashBalAlertDialog.dismiss());

        addNowTxtArea.setOnClickListener(view -> {
            cashBalAlertDialog.dismiss();
            if (isCash) {
                new StartActProcess(context).startAct(ContactUsActivity.class);

            } else {
                new StartActProcess(context).startActWithData(MyWalletActivity.class, bn);
            }

        });
        cashBalAlertDialog = builder.create();
        cashBalAlertDialog.setCancelable(false);
        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(cashBalAlertDialog);
        }
        cashBalAlertDialog.show();
    }
}
