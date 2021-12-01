package com.levaeu.driver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import com.adapter.files.CustSpinnerAdapter;
import com.general.files.CancelTripDialog;
import com.general.files.ConfigPubNub;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.GetLocationUpdates;
import com.general.files.InternetConnection;
import com.general.files.MyApp;
import com.general.files.OpenPassengerDetailDialog;
import com.general.files.StartActProcess;
import com.general.files.UpdateDirections;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.utils.AnimateMarker;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.SelectableRoundedImageView;
import com.view.editBox.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class DriverArrivedActivity extends BaseActivity implements OnMapReadyCallback, GetLocationUpdates.LocationUpdatesListener {

    public String tripId = "";
    public ImageView emeTapImgView;
    public MTextView timeTxt;
    GeneralFunctions generalFunc;
    MTextView titleTxt;
    MButton btn_type2;
    public HashMap<String, String> data_trip;
    SupportMapFragment map;
    GoogleMap gMap;
    Location userLocation;
    MTextView addressTxt;
    String REQUEST_TYPE = "";
    android.support.v7.app.AlertDialog list_navigation;
    UpdateDirections updateDirections;
    Marker driverMarker;
    boolean isnotification = false;

    InternetConnection intCheck;
    AnimateMarker animateMarker;

    /*Multi Delivery View*/
    private ImageView viewDetailsImgView;
    private MTextView subTitleTxt;

    JSONObject userProfileJsonObj = null;
    boolean isCurrentLocationFocused = false;

    String ENABLE_DIRECTION_SOURCE_DESTINATION_DRIVER_APP = "";
    AlertDialog dialog_declineOrder;
    String vImage = "";
    String vName = "";

    //#UberPool
    LinearLayout deliveryDetailsArea;
    MTextView pickupTxt;
    MTextView pickupNameTxt, personTxt;
    boolean isPoolRide = false;
    ImageView googleImage;
    boolean isFirstMapMove = true;
    LinearLayout navigationArea;

    boolean isIntializeDirectionUpdate = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_arrived);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        animateMarker = new AnimateMarker();

        animateMarker.driverMarkerAnimFinished = true;

        userProfileJsonObj = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));

        generalFunc.storeData(Utils.DRIVER_ONLINE_KEY, "false");
        isnotification = getIntent().getBooleanExtra("isnotification", false);

        subTitleTxt = (MTextView) findViewById(R.id.subTitleTxt);
        googleImage = (ImageView) findViewById(R.id.googleImage);
        navigationArea = (LinearLayout) findViewById(R.id.navigationArea);
        intCheck = new InternetConnection(getActContext());

        ENABLE_DIRECTION_SOURCE_DESTINATION_DRIVER_APP = generalFunc.getJsonValueStr("ENABLE_DIRECTION_SOURCE_DESTINATION_DRIVER_APP", userProfileJsonObj);


        //gps view declaration end

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        addressTxt = (MTextView) findViewById(R.id.addressTxt);
        btn_type2 = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();
        map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapV2);
        deliveryDetailsArea = (LinearLayout) findViewById(R.id.deliveryDetailsArea);
        pickupTxt = (MTextView) findViewById(R.id.pickupTxt);
        pickupNameTxt = (MTextView) findViewById(R.id.pickupNameTxt);
        personTxt = (MTextView) findViewById(R.id.personTxt);

        (findViewById(R.id.backImgView)).setVisibility(View.GONE);
        btn_type2.setId(Utils.generateViewId());

        emeTapImgView = (ImageView) findViewById(R.id.emeTapImgView);
        emeTapImgView.setOnClickListener(new setOnClickList());

        timeTxt = (MTextView) findViewById(R.id.timeTxt);

        timeTxt.setVisibility(View.GONE);

        // Multi Delivery
        viewDetailsImgView = (ImageView) findViewById(R.id.viewDetailsImgView);
        viewDetailsImgView.setOnClickListener(new setOnClickList());

        pickupTxt.setText(generalFunc.retrieveLangLBl("", "LBL_PICKUP"));


        setData();
        setLabels();


        String last_trip_data = generalFunc.getJsonValue("TripDetails", userProfileJsonObj.toString());
        if (generalFunc.getJsonValue("eServiceLocation", last_trip_data) != null && generalFunc.getJsonValue("eServiceLocation", last_trip_data).equalsIgnoreCase("Driver")) {
            isIntializeDirectionUpdate = false;
            navigationArea.setVisibility(View.GONE);
            timeTxt.setVisibility(View.GONE);
            btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_MARK_USER_ARRIVED_BTN_TITLE"));
        }

        if (REQUEST_TYPE.equalsIgnoreCase(Utils.eType_Multi_Delivery)) {
            findViewById(R.id.googleImage).setVisibility(View.GONE);
            subTitleTxt.setVisibility(View.VISIBLE);
            subTitleTxt.setOnClickListener(new setOnClickList());
            viewDetailsImgView.setVisibility(View.VISIBLE);
            findViewById(R.id.detailIconArea).setVisibility(View.VISIBLE);
        } else if (generalFunc.getJsonValue("ePoolRide", last_trip_data).equalsIgnoreCase("Yes")) {

            pickupNameTxt.setText(data_trip.get("PName") + " " + data_trip.get("vLastName"));
            findViewById(R.id.iv_callRicipient).setVisibility(View.GONE);
            personTxt.setVisibility(View.VISIBLE);
            personTxt.setText(generalFunc.convertNumberWithRTL(generalFunc.getJsonValue("iPersonSize", last_trip_data)) + " " + generalFunc.retrieveLangLBl("", "LBL_PERSON"));
            isPoolRide = true;
            deliveryDetailsArea.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) googleImage.getLayoutParams();
            params.setMargins(0, 0, 0, Utils.dipToPixels(getActContext(), 73));
            googleImage.setLayoutParams(params);
            ConfigPubNub.getInstance().subscribeToCabRequestChannel();
            btn_type2.setText(generalFunc.retrieveLangLBl("Mark As PIckUp", "LBL_POOL_MARK_AS_PICKUP"));
            Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(mToolbar);
            viewDetailsImgView.setVisibility(View.GONE);
            findViewById(R.id.detailIconArea).setVisibility(View.GONE);

        } else {
            Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(mToolbar);
            viewDetailsImgView.setVisibility(View.GONE);
            findViewById(R.id.detailIconArea).setVisibility(View.GONE);
        }

        new CreateRoundedView(getActContext().getResources().getColor(R.color.appThemeColor_2), Utils.dipToPixels(getActContext(), 40), 0,
                getActContext().getResources().getColor(R.color.appThemeColor_2), findViewById(R.id.driverImgView));

        if (Utils.checkText(generalFunc.retrieveValue("OPEN_CHAT"))) {
            JSONObject OPEN_CHAT_DATA_OBJ = generalFunc.getJsonObject(generalFunc.retrieveValue("OPEN_CHAT"));
            generalFunc.removeValue("OPEN_CHAT");

            if (OPEN_CHAT_DATA_OBJ != null) {
                new StartActProcess(getActContext()).startActWithData(ChatActivity.class, generalFunc.createChatBundle(OPEN_CHAT_DATA_OBJ));
            }
        }

        generalFunc.storeData(Utils.DriverWaitingTime, "0");
        generalFunc.storeData(Utils.DriverWaitingSecTime, "0");

        map.getMapAsync(this);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) titleTxt.getLayoutParams();
        params.setMargins(Utils.dipToPixels(getActContext(), 20), 0, 0, 0);
        titleTxt.setLayoutParams(params);

        btn_type2.setOnClickListener(new setOnClickAct());


        if (savedInstanceState != null) {
            // Restore value of members from saved state
            String restratValue_str = savedInstanceState.getString("RESTART_STATE");

            if (restratValue_str != null && !restratValue_str.equals("") && restratValue_str.trim().equals("true")) {
                generalFunc.restartApp();
            }
        }

        if (generalFunc.isRTLmode()) {
            (findViewById(R.id.navStripImgView)).setRotation(180);
        }

        GetLocationUpdates.getInstance().setTripStartValue(false, true, data_trip.get("TripId"));

    }

    @Override
    public void onBackPressed() {
        return;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (this.userLocation != null) {
            onLocationUpdate(this.userLocation);
        }

        if (updateDirections != null) {
            updateDirections.scheduleDirectionUpdate();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        if (updateDirections != null) {
            updateDirections.releaseTask();
        }
    }


    public void setTimetext(String distance, String time) {
        try {
            timeTxt.setVisibility(View.VISIBLE);
            String userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
            String distance_str = "";
            if (userProfileJson != null && !generalFunc.getJsonValue("eUnit", userProfileJson).equalsIgnoreCase("KMs")) {
                timeTxt.setText(time + " " + generalFunc.retrieveLangLBl("to reach", "LBL_REACH_TXT") + " & " + distance + " " + generalFunc.retrieveLangLBl("", "LBL_MILE_DISTANCE_TXT") + " " + generalFunc.retrieveLangLBl("away", "LBL_AWAY_TXT"));
            } else {
                timeTxt.setText(time + " " + generalFunc.retrieveLangLBl("to reach", "LBL_REACH_TXT") + " & " + distance + " " + generalFunc.retrieveLangLBl("", "LBL_KM_DISTANCE_TXT") + " " + generalFunc.retrieveLangLBl("away", "LBL_AWAY_TXT"));

            }
        } catch (Exception e) {

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putString("RESTART_STATE", "true");
        super.onSaveInstanceState(outState);
    }


    public void handleNoLocationDial() {

        if (generalFunc.isLocationEnabled()) {
            resetData();
        }

    }


    public void checkUserLocation() {
        if (generalFunc.isLocationEnabled() && (userLocation == null || userLocation.getLatitude() == 0.0 || userLocation.getLongitude() == 0.0)) {
            showprogress();
        } else {
            hideprogress();
        }
    }

    public void internetIsBack() {
        if (updateDirections != null) {
            updateDirections.scheduleDirectionUpdate();
        }
    }

    public void showprogress() {
        isCurrentLocationFocused = false;
        findViewById(R.id.errorLocArea).setVisibility(View.VISIBLE);
        findViewById(R.id.googleImage).setVisibility(View.GONE);

        findViewById(R.id.mProgressBar).setVisibility(View.VISIBLE);
        ((ProgressBar) findViewById(R.id.mProgressBar)).setIndeterminate(true);
        ((ProgressBar) findViewById(R.id.mProgressBar)).getIndeterminateDrawable().setColorFilter(
                getActContext().getResources().getColor(R.color.appThemeColor_1), android.graphics.PorterDuff.Mode.SRC_IN);

    }

    public void hideprogress() {

        findViewById(R.id.errorLocArea).setVisibility(View.GONE);
        if (!REQUEST_TYPE.equalsIgnoreCase(Utils.eType_Multi_Delivery)) {
            findViewById(R.id.googleImage).setVisibility(View.GONE);
        }
        findViewById(R.id.googleImage).setVisibility(View.VISIBLE);

        if (findViewById(R.id.mProgressBar) != null) {
            findViewById(R.id.mProgressBar).setVisibility(View.GONE);
        }
    }

    private void resetData() {
        if (intCheck.isNetworkConnected() && intCheck.check_int() && addressTxt.getText().equals(generalFunc.retrieveLangLBl("Loading address", "LBL_LOAD_ADDRESS"))) {
            setData();
        }

        if (!isCurrentLocationFocused) {
            setData();
            checkUserLocation();
        } else {
            checkUserLocation();
        }
    }


    public void setLabels() {

        subTitleTxt.setText(generalFunc.retrieveLangLBl("Cancel", "LBL_BTN_CANCEL_TXT"));

        setPageName();
        timeTxt.setText("--" + generalFunc.retrieveLangLBl("to reach", "LBL_REACH_TXT"));
        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_ARRIVED_TXT"));
        ((MTextView) findViewById(R.id.navigateTxt)).setText(generalFunc.retrieveLangLBl("Navigate", "LBL_NAVIGATE"));


        // No location found but gps is on

        ((MTextView) findViewById(R.id.errorTitleTxt)).setText(generalFunc.retrieveLangLBl("Waiting for your location.", "LBL_LOCATION_FATCH_ERROR_TXT"));

        ((MTextView) findViewById(R.id.errorSubTitleTxt)).setText(generalFunc.retrieveLangLBl("Try to fetch  your accurate location. \"If you still face the problem, go to open sky instead of closed area\".", "LBL_NO_LOC_GPS_TXT"));

    }

    public void setPageName() {
        if (REQUEST_TYPE.equals("Deliver") || REQUEST_TYPE.equalsIgnoreCase(Utils.eType_Multi_Delivery)) {
            titleTxt.setText(generalFunc.retrieveLangLBl("Pickup Delivery", "LBL_PICKUP_DELIVERY"));
        } else if (REQUEST_TYPE.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
            titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_JOB_LOCATION_TXT"));
        } else {
            titleTxt.setText(generalFunc.retrieveLangLBl("Pick Up Passenger", "LBL_PICK_UP_PASSENGER"));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.gMap = googleMap;

        if (generalFunc.checkLocationPermission(true)) {
            getMap().setMyLocationEnabled(false);
        }

        getMap().getUiSettings().setTiltGesturesEnabled(false);
        getMap().getUiSettings().setCompassEnabled(false);
        getMap().getUiSettings().setMyLocationButtonEnabled(false);

        getMap().setOnMarkerClickListener(marker -> {
            marker.hideInfoWindow();
            return true;
        });


        double passenger_lat = GeneralFunctions.parseDoubleValue(0.0, data_trip.get("sourceLatitude"));
        double passenger_lon = GeneralFunctions.parseDoubleValue(0.0, data_trip.get("sourceLongitude"));

        MarkerOptions marker_passenger_opt = new MarkerOptions()
                .position(new LatLng(passenger_lat, passenger_lon));

        int icon = R.drawable.taxi_passanger;
        if (generalFunc.getJsonValueStr("APP_TYPE", userProfileJsonObj).equalsIgnoreCase("UberX")) {
            // marker_passenger_opt.icon(BitmapDescriptorFactory.fromResource(R.drawable.ufxprovider)).anchor(0.5f, 0.5f);
            icon = R.drawable.ufxprovider;
        }

        if (REQUEST_TYPE.equals(Utils.CabGeneralType_UberX)) {
            icon = R.drawable.ufxprovider;
        }
        if (REQUEST_TYPE.equals("Deliver") || REQUEST_TYPE.equals(Utils.eType_Multi_Delivery)) {
            icon = R.drawable.taxi_passenger_delivery;
        }


        marker_passenger_opt.icon(BitmapDescriptorFactory.fromResource(icon)).anchor(0.5f,
                1);
        getMap().addMarker(marker_passenger_opt).setFlat(true);


        checkUserLocation();

        if (GetLocationUpdates.retrieveInstance() != null) {
            GetLocationUpdates.getInstance().stopLocationUpdates(this);
        }
        GetLocationUpdates.getInstance().setTripStartValue(false, true, data_trip.get("TripId"));
        GetLocationUpdates.getInstance().startLocationUpdates(this, this);
    }

    public GoogleMap getMap() {
        return this.gMap;
    }

    public void setData() {

        HashMap<String, String> data = (HashMap<String, String>) getIntent().getSerializableExtra("TRIP_DATA");

        this.data_trip = data;


        if (!data_trip.get("PPicName").equals("")) {
            vImage = CommonUtilities.USER_PHOTO_PATH + data_trip.get("passengerId") + "/"
                    + data_trip.get("PPicName");
        }
        vName = data_trip.get("PName");


        double passenger_lat = GeneralFunctions.parseDoubleValue(0.0, data_trip.get("sourceLatitude"));
        double passenger_lon = GeneralFunctions.parseDoubleValue(0.0, data_trip.get("sourceLongitude"));

        addressTxt.setText(generalFunc.retrieveLangLBl("Loading address", "LBL_LOAD_ADDRESS"));
        addressTxt.setText(data_trip.get("tSaddress"));


        (findViewById(R.id.navigateArea)).setOnClickListener(new setOnClickAct("" + passenger_lat, "" + passenger_lon));

        REQUEST_TYPE = data_trip.get("REQUEST_TYPE");

        setPageName();
    }


    @Override
    public void onLocationUpdate(Location location) {
        if (location == null) {
            isCurrentLocationFocused = false;
            return;
        }

        if (gMap == null) {
            this.userLocation = location;
            return;
        }

        updateDriverMarker(new LatLng(location.getLatitude(), location.getLongitude()));

        this.userLocation = location;

        if (!ENABLE_DIRECTION_SOURCE_DESTINATION_DRIVER_APP.equalsIgnoreCase("Yes")) {
            if (!data_trip.get("DestLocLatitude").equals("") && !data_trip.get("DestLocLatitude").equals("0")
                    && !data_trip.get("DestLocLongitude").equals("") && !data_trip.get("DestLocLongitude").equals("0")) {

                double passenger_lat = GeneralFunctions.parseDoubleValue(0.0, data_trip.get("DestLocLatitude"));
                double passenger_lon = GeneralFunctions.parseDoubleValue(0.0, data_trip.get("DestLocLongitude"));
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()));
                builder.include(new LatLng(passenger_lat, passenger_lon));
                gMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), Utils.dipToPixels(getActContext(), 40)));

            }
        }

        if (ENABLE_DIRECTION_SOURCE_DESTINATION_DRIVER_APP.equalsIgnoreCase("Yes") || (data_trip.get("DestLocLatitude").equals("") || data_trip.get("DestLocLatitude").equals("0")
                || data_trip.get("DestLocLongitude").equals("") || data_trip.get("DestLocLongitude").equals("0"))) {
            try {
                if (isFirstMapMove) {
                    getMap().moveCamera(CameraUpdateFactory.newCameraPosition(cameraForUserPosition(location, true)));
                    isFirstMapMove = false;
                } else {
                    getMap().animateCamera(CameraUpdateFactory.newCameraPosition(cameraForUserPosition(location, false)), 1000, null);
                }
            } catch (Exception e) {

            }
        }
        checkUserLocation();

        if (updateDirections == null) {
            if (isIntializeDirectionUpdate) {
                Location destLoc = new Location("gps");
                destLoc.setLatitude(GeneralFunctions.parseDoubleValue(0.0, data_trip.get("sourceLatitude")));
                destLoc.setLongitude(GeneralFunctions.parseDoubleValue(0.0, data_trip.get("sourceLongitude")));
                updateDirections = new UpdateDirections(getActContext(), gMap, userLocation, destLoc);
                updateDirections.scheduleDirectionUpdate();
            }
        } else if (updateDirections != null) {
            updateDirections.changeUserLocation(location);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.REQUEST_CODE_GPS_ON) {
            handleNoLocationDial();
        }
    }


    public void updateDriverMarker(final LatLng newLocation) {
        if (MyApp.getInstance().isMyAppInBackGround() || gMap == null) {
            return;
        }

        if (driverMarker == null) {

            if (generalFunc.getJsonValueStr("APP_TYPE", userProfileJsonObj).equalsIgnoreCase("UberX") || REQUEST_TYPE.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {

                String image_url = CommonUtilities.PROVIDER_PHOTO_PATH + generalFunc.getMemberId() + "/" + generalFunc.getJsonValueStr("vImage", userProfileJsonObj);
                View marker_view = ((LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                        .inflate(R.layout.uberx_provider_maker_design, null);
                SelectableRoundedImageView providerImgView = (SelectableRoundedImageView) marker_view
                        .findViewById(R.id.providerImgView);

                providerImgView.setImageResource(R.mipmap.ic_no_pic_user);

                final View finalMarker_view = marker_view;
                if (Utils.checkText(generalFunc.getJsonValueStr("vImage", userProfileJsonObj))) {

                    MarkerOptions markerOptions_driver = new MarkerOptions();
                    markerOptions_driver.position(newLocation);
                    markerOptions_driver.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getActContext(), finalMarker_view))).anchor(0.5f,
                            0.5f).flat(true);
                    driverMarker = gMap.addMarker(markerOptions_driver);

                    Picasso.get()
                            .load(image_url/*"http://www.hellocle.com/wp-content/themes/hello/images/hello-logo-stone.png"*/)
                            .placeholder(R.mipmap.ic_no_pic_user).error(R.mipmap.ic_no_pic_user)
                            .into(providerImgView, new Callback() {
                                @Override
                                public void onSuccess() {
                                    driverMarker.setIcon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getActContext(), finalMarker_view)));
                                }

                                @Override
                                public void onError(Exception e) {
                                    driverMarker.setIcon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getActContext(), finalMarker_view)));
                                }
                            });

                    driverMarker.setFlat(true);
                    driverMarker.setAnchor(0.5f, 1);
                    driverMarker.setTitle(generalFunc.getMemberId());
                } else {
                    MarkerOptions markerOptions_driver = new MarkerOptions();
                    markerOptions_driver.position(newLocation);
                    markerOptions_driver.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getActContext(), finalMarker_view))).anchor(0.5f,
                            1.5f).flat(true);
                    driverMarker = gMap.addMarker(markerOptions_driver);
                    driverMarker.setFlat(true);
                    driverMarker.setAnchor(0.5f, 1);
                    driverMarker.setTitle(generalFunc.getMemberId());

                }
            } else {


                int iconId = R.mipmap.car_driver;

                if (data_trip.containsKey("vVehicleType")) {

                    if (data_trip.get("vVehicleType").equalsIgnoreCase("Bike")) {
                        iconId = R.mipmap.car_driver_1;
                    } else if (data_trip.get("vVehicleType").equalsIgnoreCase("Cycle")) {
                        iconId = R.mipmap.car_driver_2;
                    } else if (data_trip.get("vVehicleType").equalsIgnoreCase("Truck")) {
                        iconId = R.mipmap.car_driver_4;
                    } else if (data_trip.get("vVehicleType").equalsIgnoreCase("Fly")) {
                        iconId = R.mipmap.ic_fly_icon;
                    }
                }

                MarkerOptions markerOptions_driver = new MarkerOptions();
                markerOptions_driver.position(newLocation);
//                markerOptions_driver.icon(BitmapDescriptorFactory.fromResource(R.mipmap.car_driver)).anchor(0.5f, 0.5f).flat(true);
                markerOptions_driver.icon(BitmapDescriptorFactory.fromResource(iconId)).anchor(0.5f, 0.5f).flat(true);

                driverMarker = gMap.addMarker(markerOptions_driver);
                driverMarker.setTitle(generalFunc.getMemberId());

            }

        }

        if (this.userLocation != null && newLocation != null) {
            LatLng currentLatLng = new LatLng(this.userLocation.getLatitude(), this.userLocation.getLongitude());

            float rotation = driverMarker == null ? 0 : driverMarker.getRotation();

            if (animateMarker.currentLng != null) {
                rotation = (float) animateMarker.bearingBetweenLocations(animateMarker.currentLng, newLocation);
            } else {
                rotation = (float) animateMarker.bearingBetweenLocations(currentLatLng, newLocation);
            }

            if (generalFunc.getJsonValueStr("APP_TYPE", userProfileJsonObj).equalsIgnoreCase("UberX") || REQUEST_TYPE.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
                rotation = 0;
            }

            if (driverMarker != null) {
                driverMarker.setTitle(generalFunc.getMemberId());
            }

            HashMap<String, String> previousItemOfMarker = animateMarker.getLastLocationDataOfMarker(driverMarker);

            HashMap<String, String> data_map = new HashMap<>();
            data_map.put("vLatitude", "" + newLocation.latitude);
            data_map.put("vLongitude", "" + newLocation.longitude);
            data_map.put("iDriverId", "" + generalFunc.getMemberId());
            data_map.put("RotationAngle", "" + rotation);
            data_map.put("LocTime", "" + System.currentTimeMillis());

            Location location = new Location("marker");
            location.setLatitude(newLocation.latitude);
            location.setLongitude(newLocation.longitude);

            if (animateMarker.toPositionLat.get("" + newLocation.latitude) == null || animateMarker.toPositionLong.get("" + newLocation.longitude) == null) {
                if (previousItemOfMarker.get("LocTime") != null && !previousItemOfMarker.get("LocTime").equals("")) {
                    long previousLocTime = GeneralFunctions.parseLongValue(0, previousItemOfMarker.get("LocTime"));
                    long newLocTime = GeneralFunctions.parseLongValue(0, data_map.get("LocTime"));

                    if (previousLocTime != 0 && newLocTime != 0) {
                        if ((newLocTime - previousLocTime) > 0 && !animateMarker.driverMarkerAnimFinished) {
                            animateMarker.addToListAndStartNext(driverMarker, this.gMap, location, rotation, 850, tripId, data_map.get("LocTime"));
                        } else if ((newLocTime - previousLocTime) > 0) {
                            animateMarker.animateMarker(driverMarker, this.gMap, location, rotation, 850, tripId, data_map.get("LocTime"));
                        }
                    } else if ((previousLocTime == 0 || newLocTime == 0) && animateMarker.driverMarkerAnimFinished == false) {
                        animateMarker.addToListAndStartNext(driverMarker, this.gMap, location, rotation, 850, tripId, data_map.get("LocTime"));
                    } else {
                        animateMarker.animateMarker(driverMarker, this.gMap, location, rotation, 850, tripId, data_map.get("LocTime"));
                    }
                } else if (!animateMarker.driverMarkerAnimFinished) {
                    animateMarker.addToListAndStartNext(driverMarker, this.gMap, location, rotation, 850, tripId, data_map.get("LocTime"));
                } else {
                    animateMarker.animateMarker(driverMarker, this.gMap, location, rotation, 850, tripId, data_map.get("LocTime"));
                }
            }
        }

    }


    public static Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    public CameraPosition cameraForUserPosition(Location location, boolean isFirst) {
        double currentZoomLevel = getMap().getCameraPosition().zoom;

        if (isFirst) {
            currentZoomLevel = Utils.defaultZomLevel;
        }
        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(location.getLatitude(), location.getLongitude()))
                .zoom((float) currentZoomLevel).build();

        return cameraPosition;
    }


    public void tripCancelled(String msg) {
        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(btn_id -> {
            generateAlert.closeAlertBox();
            generalFunc.saveGoOnlineInfo();
            // generalFunc.restartApp();
            MyApp.getInstance().restartWithGetDataApp();
        });
        generateAlert.setContentMessage("", msg);
        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
        generateAlert.showAlertBox();
    }

    public void buildMsgOnArrivedBtn() {
        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(btn_id -> {
            if (btn_id == 0) {
                btn_type2.setEnabled(true);
                generateAlert.closeAlertBox();
            } else {
                btn_type2.setEnabled(true);
                setDriverStatusToArrived();
            }

        });

        String last_trip_data = generalFunc.getJsonValue("TripDetails", userProfileJsonObj.toString());


        String msg = "";
        if (REQUEST_TYPE.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
            msg = generalFunc.retrieveLangLBl("", "LBL_ARRIVED_CONFIRM_DIALOG_SERVICES");
        } else if (REQUEST_TYPE.equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
            msg = generalFunc.retrieveLangLBl("", "LBL_ARRIVED_CONFIRM_DIALOG_TXT");
        } else if (REQUEST_TYPE.equalsIgnoreCase("Deliver") || REQUEST_TYPE.equalsIgnoreCase(Utils.eType_Multi_Delivery)) {
            msg = generalFunc.retrieveLangLBl("", "LBL_ARRIVED_CONFIRM_DIALOG_DELIVERY");
        }
        if (REQUEST_TYPE.equalsIgnoreCase(Utils.CabGeneralType_UberX) && generalFunc.getJsonValue("eServiceLocation", last_trip_data) != null && generalFunc.getJsonValue("eServiceLocation", last_trip_data).equalsIgnoreCase("Driver")) {
            msg = generalFunc.retrieveLangLBl("", "LBL_CONFIRM_NOTE_USER_ARRIVED");
        }
        generateAlert.setContentMessage("", msg);
        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("ok", "LBL_YES"));
        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("cancel", "LBL_NO"));

        generateAlert.showAlertBox();
    }

    public void setDriverStatusToArrived() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "DriverArrived");
        parameters.put("iDriverId", generalFunc.getMemberId());
        parameters.put("TripId", data_trip.get("TripId"));
        if (isPoolRide) {
            MyApp.getInstance().ispoolRequest = true;
        }

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObject=generalFunc.getJsonObject(responseString);

            if (responseStringObject != null && !responseStringObject.equals("")) {
                MyApp.getInstance().ispoolRequest = false;
                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObject);

                if (isDataAvail == true) {

                    JSONObject message = generalFunc.getJsonObject(Utils.message_str, responseStringObject);

                    data_trip.put("DestLocLatitude", generalFunc.getJsonValueStr("DLatitude", message));
                    data_trip.put("DestLocLongitude", generalFunc.getJsonValueStr("DLongitude", message));
                    data_trip.put("DestLocAddress", generalFunc.getJsonValueStr("DAddress", message));
                    data_trip.put("eTollSkipped", generalFunc.getJsonValueStr("eTollSkipped", message));
                    data_trip.put("vTripStatus", "Arrived");

                    if (updateDirections != null) {
                        updateDirections.releaseTask();
                        updateDirections = null;
                    }

                    stopProcess();
                    MyApp.getInstance().restartWithGetDataApp();

                } else {
                    String msg_str = generalFunc.getJsonValueStr(Utils.message_str, responseStringObject);
                    if (msg_str.equals("DO_RESTART") ||
                            msg_str.equals(Utils.GCM_FAILED_KEY) || msg_str.equals(Utils.APNS_FAILED_KEY) || msg_str.equals("LBL_SERVER_COMM_ERROR")) {
                        generalFunc.restartApp();
                    } else {
                        generalFunc.showGeneralMessage("",
                                generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObject)));
                    }

                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.trip_accept_menu, menu);

        if (REQUEST_TYPE.equals("Deliver")) {
            menu.findItem(R.id.menu_passenger_detail).setTitle(generalFunc.retrieveLangLBl("View Delivery Details", "LBL_VIEW_DELIVERY_DETAILS"));
            menu.findItem(R.id.menu_cancel_trip).setTitle(generalFunc.retrieveLangLBl("Cancel Delivery", "LBL_CANCEL_DELIVERY"));
        } else {
            String msg = "";
            String msgCancel = "";
            if (REQUEST_TYPE != null && REQUEST_TYPE.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
                msg = generalFunc.retrieveLangLBl("", "LBL_VIEW_USER_DETAIL");
                msgCancel = generalFunc.retrieveLangLBl("", "LBL_CANCEL_JOB");
            } else if (REQUEST_TYPE != null && (REQUEST_TYPE.equalsIgnoreCase("Deliver") || REQUEST_TYPE.equalsIgnoreCase(Utils.eType_Multi_Delivery))) {
                msg = generalFunc.retrieveLangLBl("", "LBL_VIEW_DELIVERY_DETAILS");
                msgCancel = generalFunc.retrieveLangLBl("", "LBL_CANCEL_DELIVERY");
            } else {
                msg = generalFunc.retrieveLangLBl("", "LBL_VIEW_PASSENGER_DETAIL");
                msgCancel = generalFunc.retrieveLangLBl("", "LBL_CANCEL_TRIP");

            }

            menu.findItem(R.id.menu_passenger_detail).setTitle(msg);
            menu.findItem(R.id.menu_cancel_trip).setTitle(msgCancel);
        }

        if (!REQUEST_TYPE.equals(Utils.CabGeneralType_UberX) && generalFunc.getJsonValue("WAYBILL_ENABLE", userProfileJsonObj) != null && generalFunc.getJsonValueStr("WAYBILL_ENABLE", userProfileJsonObj).equalsIgnoreCase("yes")) {
            menu.findItem(R.id.menu_waybill_trip).setTitle(generalFunc.retrieveLangLBl("Way Bill", "LBL_MENU_WAY_BILL")).setVisible(true);
        }
        else
        {
            menu.findItem(R.id.menu_waybill_trip).setTitle(generalFunc.retrieveLangLBl("Way Bill", "LBL_MENU_WAY_BILL")).setVisible
                    (false);


        }

        menu.findItem(R.id.menu_call).setTitle(generalFunc.retrieveLangLBl("Call", "LBL_CALL_ACTIVE_TRIP"));
        if (REQUEST_TYPE.equals(Utils.CabGeneralType_UberX)) {
            String last_trip_data = generalFunc.getJsonValue("TripDetails", userProfileJsonObj.toString());
            if (!generalFunc.getJsonValue("moreServices", last_trip_data).equalsIgnoreCase("") && generalFunc.getJsonValue("moreServices", last_trip_data).equalsIgnoreCase("Yes")) {
                menu.findItem(R.id.menu_specialInstruction).setTitle(generalFunc.retrieveLangLBl("Special Instruction", "LBL_TITLE_REQUESTED_SERVICES"));
            } else {
                menu.findItem(R.id.menu_specialInstruction).setTitle(generalFunc.retrieveLangLBl("Special Instruction", "LBL_SPECIAL_INSTRUCTION_TXT"));
            }
        }
        menu.findItem(R.id.menu_message).setTitle(generalFunc.retrieveLangLBl("Message", "LBL_MESSAGE_ACTIVE_TRIP"));
        menu.findItem(R.id.menu_sos).setTitle(generalFunc.retrieveLangLBl("Emergency or SOS", "LBL_EMERGENCY_SOS_TXT")).setVisible(false);

        menu.findItem(R.id.menu_sos).setVisible(false);
        if (REQUEST_TYPE.equals(Utils.CabGeneralType_UberX)) {
            menu.findItem(R.id.menu_passenger_detail).setVisible(true);
            menu.findItem(R.id.menu_call).setVisible(false);
            menu.findItem(R.id.menu_message).setVisible(false);
            menu.findItem(R.id.menu_specialInstruction).setVisible(true);
        } else if (REQUEST_TYPE.equals(Utils.CabGeneralType_UberX)) {
            menu.findItem(R.id.menu_passenger_detail).setVisible(false);
            menu.findItem(R.id.menu_call).setVisible(true);
            menu.findItem(R.id.menu_message).setVisible(true);
            if (generalFunc.getJsonValue("WAYBILL_ENABLE", userProfileJsonObj) != null && generalFunc.getJsonValueStr("WAYBILL_ENABLE", userProfileJsonObj).equalsIgnoreCase("yes")) {
                menu.findItem(R.id.menu_waybill_trip).setTitle(generalFunc.retrieveLangLBl("Way Bill", "LBL_MENU_WAY_BILL")).setVisible(true); }
            else
            { menu.findItem(R.id.menu_waybill_trip).setTitle(generalFunc.retrieveLangLBl("Way Bill", "LBL_MENU_WAY_BILL")).setVisible(false); }
        } else {
            menu.findItem(R.id.menu_passenger_detail).setVisible(true);
            menu.findItem(R.id.menu_call).setVisible(false);
            menu.findItem(R.id.menu_message).setVisible(false);
            menu.findItem(R.id.menu_specialInstruction).setVisible(false);
            if (generalFunc.getJsonValue("WAYBILL_ENABLE", userProfileJsonObj) != null && generalFunc.getJsonValueStr("WAYBILL_ENABLE", userProfileJsonObj).equalsIgnoreCase("yes")) { menu.findItem(R.id.menu_waybill_trip).setTitle(generalFunc.retrieveLangLBl("Way Bill", "LBL_MENU_WAY_BILL")).setVisible(true); }
            else
            { menu.findItem(R.id.menu_waybill_trip).setTitle(generalFunc.retrieveLangLBl("Way Bill", "LBL_MENU_WAY_BILL")).setVisible
                        (false); }
        }


        Utils.setMenuTextColor(menu.findItem(R.id.menu_sos), getResources().getColor(R.color.appThemeColor_TXT_1));
        Utils.setMenuTextColor(menu.findItem(R.id.menu_call), getResources().getColor(R.color.appThemeColor_TXT_1));
        Utils.setMenuTextColor(menu.findItem(R.id.menu_message), getResources().getColor(R.color.appThemeColor_TXT_1));
        Utils.setMenuTextColor(menu.findItem(R.id.menu_passenger_detail), getResources().getColor(R.color.appThemeColor_TXT_1));
        Utils.setMenuTextColor(menu.findItem(R.id.menu_cancel_trip), getResources().getColor(R.color.appThemeColor_TXT_1));
        Utils.setMenuTextColor(menu.findItem(R.id.menu_waybill_trip), getResources().getColor(R.color.appThemeColor_TXT_1));
        Utils.setMenuTextColor(menu.findItem(R.id.menu_specialInstruction), getResources().getColor(R.color.appThemeColor_TXT_1));
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void getMaskNumber() {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getCallMaskNumber");
        parameters.put("iTripid", data_trip.get("iTripId"));
        parameters.put("UserType", Utils.userType);
        parameters.put("iMemberId", generalFunc.getMemberId());

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);

        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObj=generalFunc.getJsonObject(responseString);

            if (responseStringObj != null && !responseStringObj.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObj);

                if (isDataAvail == true) {
                    String message = generalFunc.getJsonValueStr(Utils.message_str, responseStringObj);
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
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);

        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public void getDeclineReasonsList() {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "GetCancelReasons");
        parameters.put("iTripId", data_trip.get("iTripId"));
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("eUserType", Utils.app_type);
        ExecuteWebServerUrl exeServerTask = new ExecuteWebServerUrl(getActContext(), parameters);
        exeServerTask.setLoaderConfig(getActContext(), true, generalFunc);
        exeServerTask.setDataResponseListener(responseString -> {
            JSONObject responseStringObj=generalFunc.getJsonObject(responseString);

            if (!responseStringObj.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObj);

                if (isDataAvail) {
                    showDeclineReasonsAlert(responseStringObj);
                } else {
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObj)));
                }

            } else {
                generalFunc.showError();
            }

        });
        exeServerTask.execute();
    }

    String titleDailog = "";

    public void showDeclineReasonsAlert(JSONObject responseString) {

        if (data_trip.get("eType").equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
            titleDailog = generalFunc.retrieveLangLBl("", "LBL_CANCEL_TRIP");
        } else if (data_trip.get("eType").equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
            titleDailog = generalFunc.retrieveLangLBl("", "LBL_CANCEL_BOOKING");
        } else {
            titleDailog = generalFunc.retrieveLangLBl("", "LBL_CANCEL_DELIVERY");
        }
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());
        builder.setTitle(titleDailog);

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.decline_order_dialog_design, null);
        builder.setView(dialogView);

        MaterialEditText reasonBox = (MaterialEditText) dialogView.findViewById(R.id.inputBox);
        reasonBox.setVisibility(View.GONE);

        reasonBox.setBothText("", generalFunc.retrieveLangLBl("", "LBL_ENTER_REASON"));

        builder.setPositiveButton(generalFunc.retrieveLangLBl("", "LBL_YES"), (dialog, which) -> {

        });

        builder.setNegativeButton(generalFunc.retrieveLangLBl("", "LBL_NO"), (dialog, which) -> {
        });


        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map = new HashMap<>();
        map.put("title", "-- " + generalFunc.retrieveLangLBl("Select Reason", "LBL_SELECT_CANCEL_REASON") + " --");
        map.put("id", "");
        list.add(map);
        JSONArray arr_msg = generalFunc.getJsonArray(Utils.message_str, responseString);
        if (arr_msg != null) {

            for (int i = 0; i < arr_msg.length(); i++) {

                JSONObject obj_tmp = generalFunc.getJsonObject(arr_msg, i);


                HashMap<String, String> datamap = new HashMap<>();
                datamap.put("title", generalFunc.getJsonValueStr("vTitle", obj_tmp));
                datamap.put("id", generalFunc.getJsonValueStr("iCancelReasonId", obj_tmp));
                list.add(datamap);
            }

            HashMap<String, String> othermap = new HashMap<>();
            othermap.put("title", generalFunc.retrieveLangLBl("", "LBL_OTHER_TXT"));
            othermap.put("id", "");
            list.add(othermap);

            AppCompatSpinner spinner = (AppCompatSpinner) dialogView.findViewById(R.id.declineReasonsSpinner);
            CustSpinnerAdapter adapter = new CustSpinnerAdapter(getActContext(), list);
            spinner.setAdapter(adapter);


            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    if (spinner.getSelectedItemPosition() == (list.size() - 1)) {
                        reasonBox.setVisibility(View.VISIBLE);
                        // dialog_declineOrder.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        dialog_declineOrder.getButton(AlertDialog.BUTTON_POSITIVE).setClickable(true);
                        dialog_declineOrder.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getActContext().getResources().getColor(R.color.black));
                    } else if (spinner.getSelectedItemPosition() == 0) {
                        // dialog_declineOrder.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                        dialog_declineOrder.getButton(AlertDialog.BUTTON_POSITIVE).setClickable(false);
                        dialog_declineOrder.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getActContext().getResources().getColor(R.color.gray));
                        reasonBox.setVisibility(View.GONE);
                    } else {
                        //dialog_declineOrder.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        dialog_declineOrder.getButton(AlertDialog.BUTTON_POSITIVE).setClickable(true);
                        dialog_declineOrder.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getActContext().getResources().getColor(R.color.black));
                        reasonBox.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });


            dialog_declineOrder = builder.create();
            dialog_declineOrder.show();

            // dialog_declineOrder.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            dialog_declineOrder.getButton(AlertDialog.BUTTON_POSITIVE).setClickable(false);
            dialog_declineOrder.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getActContext().getResources().getColor(R.color.gray));

            dialog_declineOrder.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utils.hideKeyboard(getActContext());

                    if (spinner.getSelectedItemPosition() == 0) {
                        return;
                    }

                    if (Utils.checkText(reasonBox) == false && spinner.getSelectedItemPosition() == (list.size() - 1)) {
                        reasonBox.setError(generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD_ERROR_TXT"));
                        return;
                    }

                    // declineOrder(arrListIDs.get(spinner.getSelectedItemPosition()), Utils.getText(reasonBox));


                    /* new CancelTripDialog(getActContext(), data_trip, generalFunc, list.get(spinner.getSelectedItemPosition()).get("id"), Utils.getText(reasonBox), false, reasonBox.getText().toString().trim());*/

                    new CancelTripDialog(getActContext(), data_trip, generalFunc, list.get(spinner.getSelectedItemPosition()).get("id"), Utils.getText(reasonBox), false, reasonBox.getText().toString().trim(), userLocation != null ? userLocation : GetLocationUpdates.getInstance().getLastLocation());


                    dialog_declineOrder.dismiss();
                }
            });

            dialog_declineOrder.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utils.hideKeyboard(getActContext());
                    dialog_declineOrder.dismiss();
                }
            });
        } else {
            generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_NO_DATA_AVAIL"));
        }
    }

    public void sinchCall() {

        if (!generalFunc.isCallPermissionGranted(false)) {
            generalFunc.isCallPermissionGranted(true);
        } else {



            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("Id", generalFunc.getMemberId());
            hashMap.put("Name", generalFunc.getJsonValueStr("vName", userProfileJsonObj));
            hashMap.put("PImage", generalFunc.getJsonValueStr("vImage", userProfileJsonObj));
            hashMap.put("type", Utils.userType);

        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_passenger_detail:
                if (REQUEST_TYPE.equals("Deliver")) {
                    Bundle bn = new Bundle();
                    bn.putString("TripId", data_trip.get("TripId"));
                    bn.putSerializable("data_trip", data_trip);

                } else {
                    new OpenPassengerDetailDialog(getActContext(), data_trip, generalFunc, false);
                }
                return true;
            case R.id.menu_cancel_trip:
                getDeclineReasonsList();
                return true;
            case R.id.menu_waybill_trip:
                Bundle bn = new Bundle();
                bn.putSerializable("data_trip", data_trip);

                return true;
            case R.id.menu_call:
                if (REQUEST_TYPE.equals("Deliver")) {
                    Bundle bn1 = new Bundle();
                    bn1.putString("TripId", data_trip.get("TripId"));
                    bn1.putSerializable("data_trip", data_trip);

                } else {
                    try {

//                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
//                        // callIntent.setData(Uri.parse("tel:" + data_trip.get("PPhoneC") + "" + data_trip.get("PPhone")));
//                        String phoneCode = data_trip.get("PPhoneC") != null && Utils.checkText(data_trip.get("PPhoneC")) ? "+" + data_trip.get("PPhoneC") : "";
//                        callIntent.setData(Uri.parse("tel:" + data_trip.get("PPhone")));
//                        getActContext().startActivity(callIntent);
                        // getMaskNumber();
                        if (generalFunc.getJsonValue("RIDE_DRIVER_CALLING_METHOD", userProfileJsonObj).equals("Voip")) {
                            sinchCall();
                        } else {
                            getMaskNumber();
                        }
                    } catch (Exception e) {
                    }
                }
                return true;
            case R.id.menu_message:
                if (REQUEST_TYPE.equals("Deliver")) {
                    Bundle bn2 = new Bundle();
                    bn2.putString("TripId", data_trip.get("TripId"));
                    bn2.putSerializable("data_trip", data_trip);

                } else {
                    Bundle bnChat = new Bundle();
                    bnChat.putString("iFromMemberId", data_trip.get("PassengerId"));
                    bnChat.putString("FromMemberImageName", data_trip.get("PPicName"));
                    bnChat.putString("iTripId", data_trip.get("iTripId"));
                    bnChat.putString("FromMemberName", data_trip.get("PName"));
                    bnChat.putString("vBookingNo", data_trip.get("vRideNo"));
                    new StartActProcess(getActContext()).startActWithData(ChatActivity.class, bnChat);
                }
                return true;
            case R.id.menu_specialInstruction:
                String last_trip_data = generalFunc.getJsonValue("TripDetails", userProfileJsonObj.toString());
                if (!generalFunc.getJsonValue("moreServices", last_trip_data).equalsIgnoreCase("") && generalFunc.getJsonValue("moreServices", last_trip_data).equalsIgnoreCase("Yes")) {
                    Bundle bundle = new Bundle();
                    bundle.putString("iTripId", data_trip.get("iTripId"));

                } else {
                    if (data_trip.get("tUserComment") != null && !data_trip.get("tUserComment").equals("")) {
                        generalFunc.showGeneralMessage(generalFunc.retrieveLangLBl("Special Instruction", "LBL_SPECIAL_INSTRUCTION_TXT"), data_trip.get("tUserComment"));
                    } else {
                        generalFunc.showGeneralMessage(generalFunc.retrieveLangLBl("Special Instruction", "LBL_SPECIAL_INSTRUCTION_TXT"), generalFunc.retrieveLangLBl("", "LBL_NO_SPECIAL_INSTRUCTION"));
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        stopProcess();
        super.onDestroy();
    }

    public void stopProcess() {
        if (updateDirections != null) {
            updateDirections.releaseTask();
            updateDirections = null;
        }

        if (GetLocationUpdates.retrieveInstance() != null) {
            GetLocationUpdates.retrieveInstance().stopLocationUpdates(this);
        }
    }

    public Context getActContext() {
        return DriverArrivedActivity.this; // Must be context of activity not application
    }

    public void openNavigationDialog(final String passenger_lat, final String passenger_lon) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());

        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_selectnavigation_view, null);

        final MTextView NavigationTitleTxt = (MTextView) dialogView.findViewById(R.id.NavigationTitleTxt);
        final MTextView wazemapTxtView = (MTextView) dialogView.findViewById(R.id.wazemapTxtView);
        final MTextView googlemmapTxtView = (MTextView) dialogView.findViewById(R.id.googlemmapTxtView);
        final RadioButton radiogmap = (RadioButton) dialogView.findViewById(R.id.radiogmap);
        final RadioButton radiowazemap = (RadioButton) dialogView.findViewById(R.id.radiowazemap);

        radiogmap.setOnClickListener(v -> {
            radiogmap.setChecked(true);
            radiowazemap.setChecked(false);
            googlemmapTxtView.performClick();

        });
        radiowazemap.setOnClickListener(v -> {
            radiogmap.setChecked(false);
            radiowazemap.setChecked(true);
            wazemapTxtView.performClick();

        });

        builder.setView(dialogView);
        NavigationTitleTxt.setText(generalFunc.retrieveLangLBl("Choose Option", "LBL_CHOOSE_OPTION"));
        googlemmapTxtView.setText(generalFunc.retrieveLangLBl("Google map navigation", "LBL_NAVIGATION_GOOGLE_MAP"));
        wazemapTxtView.setText(generalFunc.retrieveLangLBl("Waze navigation", "LBL_NAVIGATION_WAZE"));


        googlemmapTxtView.setOnClickListener(v -> {

            try {
                String url_view = "http://maps.google.com/maps?daddr=" + passenger_lat + "," + passenger_lon;
                (new StartActProcess(getActContext())).openURL(url_view, "com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                list_navigation.dismiss();
            } catch (Exception e) {
                generalFunc.showMessage(wazemapTxtView, generalFunc.retrieveLangLBl("Please install Google Maps in your device.", "LBL_INSTALL_GOOGLE_MAPS"));
            }

        });

        wazemapTxtView.setOnClickListener(v -> {
            try {
                String uri = "waze://?ll=" + passenger_lat + "," + passenger_lon + "&navigate=yes";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent);
                list_navigation.dismiss();
            } catch (Exception e) {
                generalFunc.showMessage(wazemapTxtView, generalFunc.retrieveLangLBl("Please install Waze navigation app in your device.", "LBL_INSTALL_WAZE"));
            }
        });


        list_navigation = builder.create();
        if (generalFunc.isRTLmode()) {
            generalFunc.forceRTLIfSupported(list_navigation);
        }
        list_navigation.show();
        list_navigation.setOnCancelListener(dialogInterface -> Utils.hideKeyboard(getActContext()));
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {


            if (view.getId() == emeTapImgView.getId()) {
                Bundle bn = new Bundle();

                bn.putString("TripId", tripId);
                new StartActProcess(getActContext()).startActWithData(ConfirmEmergencyTapActivity.class, bn);
            } else if (view.getId() == viewDetailsImgView.getId()) {
                Bundle bn = new Bundle();
                bn.putString("TripId", data_trip.get("TripId"));
                bn.putString("Status", "driverArrived");
                bn.putSerializable("TRIP_DATA", data_trip);

            } else if (view.getId() == subTitleTxt.getId()) {
                getDeclineReasonsList();
            }

        }
    }

    public class setOnClickAct implements View.OnClickListener {

        String passenger_lat = "";
        String passenger_lon = "";

        public setOnClickAct() {
        }

        public setOnClickAct(String passenger_lat, String passenger_lon) {
            this.passenger_lat = passenger_lat;
            this.passenger_lon = passenger_lon;
        }

        @Override
        public void onClick(View view) {
            int i = view.getId();
            Utils.hideKeyboard(DriverArrivedActivity.this);
            if (i == btn_type2.getId()) {
                btn_type2.setEnabled(false);
                buildMsgOnArrivedBtn();
            } else if (i == R.id.navigateArea) {
                openNavigationDialog(passenger_lat, passenger_lon);
            }
        }
    }

}
