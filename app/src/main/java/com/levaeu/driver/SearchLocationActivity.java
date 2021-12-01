package com.levaeu.driver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.adapter.files.PlacesAdapter;
import com.general.files.DividerItemDecoration;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.InternetConnection;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.general.files.UpdateFrequentTask;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.MTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

public class SearchLocationActivity extends AppCompatActivity implements PlacesAdapter.setRecentLocClickList {


    public boolean isAddressEnable;
    MTextView titleTxt;
    ImageView backImgView;
    GeneralFunctions generalFunc;
    JSONObject userProfileJsonObj;
    String whichLocation = "";
    MTextView cancelTxt;
    RecyclerView placesRecyclerView;
    EditText searchTxt;
    ArrayList<HashMap<String, String>> placelist;
    PlacesAdapter placesAdapter;
    ImageView imageCancel;
    MTextView noPlacedata;
    InternetConnection intCheck;
    ImageView googleimagearea;

    String session_token = "";
    int MIN_CHAR_REQ_GOOGLE_AUTO_COMPLETE = 2;
    String currentSearchQuery = "";
    UpdateFrequentTask sessionTokenFreqTask = null;

    LinearLayout mapLocArea, sourceLocationView, destLocationView;
    MTextView mapLocTxt, homePlaceTxt, homePlaceHTxt;
    LinearLayout homeLocArea;
    MTextView placesTxt, recentLocHTxtView;
    LinearLayout placearea,placesarea;
    LinearLayout placesInfoArea;
    ImageView homeActionImgView;

    JSONArray SourceLocations_arr;
    JSONArray DestinationLocations_arr;

    ArrayList<HashMap<String, String>> recentLocList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_location);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        userProfileJsonObj = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));

        intCheck = new InternetConnection(getActContext());

        googleimagearea = (ImageView) findViewById(R.id.googleimagearea);
        cancelTxt = (MTextView) findViewById(R.id.cancelTxt);
        cancelTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"));

        placesRecyclerView = (RecyclerView) findViewById(R.id.placesRecyclerView);
        placesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Utils.hideKeyboard(getActContext());
            }
        });
        searchTxt = (EditText) findViewById(R.id.searchTxt);
        searchTxt.setHint(generalFunc.retrieveLangLBl("Search", "LBL_Search"));

        cancelTxt.setOnClickListener(new setOnClickList());
        imageCancel = (ImageView) findViewById(R.id.imageCancel);
        noPlacedata = (MTextView) findViewById(R.id.noPlacedata);
        imageCancel.setOnClickListener(new setOnClickList());

        homeLocArea = (LinearLayout) findViewById(R.id.homeLocArea);
        placesInfoArea = (LinearLayout) findViewById(R.id.placesInfoArea);
        placearea = (LinearLayout) findViewById(R.id.placearea);
        placesarea = (LinearLayout) findViewById(R.id.placesarea);
        homeActionImgView = (ImageView) findViewById(R.id.homeActionImgView);
        placesTxt = (MTextView) findViewById(R.id.locPlacesTxt);
        homePlaceTxt = (MTextView) findViewById(R.id.homePlaceTxt);
        homePlaceHTxt = (MTextView) findViewById(R.id.homePlaceHTxt);
        recentLocHTxtView = (MTextView) findViewById(R.id.recentLocHTxtView);
        mapLocArea = (LinearLayout) findViewById(R.id.mapLocArea);
        mapLocArea.setOnClickListener(new setOnClickList());
        mapLocTxt = (MTextView) findViewById(R.id.mapLocTxt);
        destLocationView = (LinearLayout) findViewById(R.id.destLocationView);
        sourceLocationView = (LinearLayout) findViewById(R.id.sourceLocationView);

        homeLocArea.setOnClickListener(new setOnClickList());
        placesTxt.setOnClickListener(new setOnClickList());
        homeActionImgView.setOnClickListener(new setOnClickList());

        setLables();

        showAddHomeAddressArea();
        placelist = new ArrayList<>();
        MIN_CHAR_REQ_GOOGLE_AUTO_COMPLETE = GeneralFunctions.parseIntegerValue(2, generalFunc.getJsonValueStr("MIN_CHAR_REQ_GOOGLE_AUTO_COMPLETE", userProfileJsonObj));



        searchTxt.setOnFocusChangeListener((v, hasFocus) -> {

            if (!hasFocus) {
                Utils.hideSoftKeyboard((Activity) getActContext(), searchTxt);
            } else {
                Utils.showSoftKeyboard((Activity) getActContext(), searchTxt);
            }
        });

        searchTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (currentSearchQuery.equals(s.toString().trim())) {
                    return;
                }

                currentSearchQuery = searchTxt.getText().toString();

                if (s.length() >= MIN_CHAR_REQ_GOOGLE_AUTO_COMPLETE) {
                    if (session_token.trim().equalsIgnoreCase("")) {
                        session_token = Utils.userType + "_" + generalFunc.getMemberId() + "_" + System.currentTimeMillis();
                        initializeSessionRegeneration();
                    }

                    placesRecyclerView.setVisibility(View.VISIBLE);

                    if (getIntent().hasExtra("eSystem")) {
                        googleimagearea.setVisibility(View.VISIBLE);
                    }
                    placesarea.setVisibility(View.GONE);
                    getGooglePlaces(currentSearchQuery);
                } else {
                    if (getIntent().getBooleanExtra("isPlaceAreaShow", true)) {
                        placesarea.setVisibility(View.VISIBLE);
                    }
                    googleimagearea.setVisibility(View.GONE);
                    placesRecyclerView.setVisibility(View.GONE);
                    noPlacedata.setVisibility(View.GONE);
                }
            }
        });

        searchTxt.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                getSearchGooglePlace(v.getText().toString());
                return true;
            }
            return false;
        });

        if (getIntent().hasExtra("hideSetMapLoc")) {
            mapLocArea.setVisibility(View.GONE);
            placesarea.setVisibility(View.GONE);
        } else {
            mapLocArea.setVisibility(View.VISIBLE);
        }

        if (getIntent().hasExtra("eSystem")) {
            mapLocArea.setVisibility(View.GONE);
        }

        placesRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        placesRecyclerView.setLayoutManager(mLayoutManager);
        placesRecyclerView.setItemAnimator(new DefaultItemAnimator());

        if (getCallingActivity() != null && getCallingActivity().getClassName().equals(AddAddressActivity.class.getName())) {
            (findViewById(R.id.recentScrollView)).setVisibility(View.GONE);
            (findViewById(R.id.recentLocHTxtView)).setVisibility(View.GONE);
        }

    }


    private void showAddHomeAddressArea() {
        if (getIntent().hasExtra("requestType")) {
            placesarea.setVisibility(View.VISIBLE);
            placesRecyclerView.setVisibility(View.GONE);
            googleimagearea.setVisibility(View.GONE);
            placesInfoArea.setVisibility(View.VISIBLE);
            setWhichLocationAreaSelected(getIntent().getStringExtra("locationArea"));
        }
        else {
            placesarea.setVisibility(View.GONE);
            placesRecyclerView.setVisibility(View.VISIBLE);
            placesInfoArea.setVisibility(View.GONE);
            googleimagearea.setVisibility(View.VISIBLE);
        }
    }

    private void setLables() {
        homePlaceTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_HOME_PLACE_TXT"));
        homePlaceHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_HOME_PLACE"));
        mapLocTxt.setText(generalFunc.retrieveLangLBl("Set location on map", "LBL_SET_LOC_ON_MAP"));

        placesTxt.setText(generalFunc.retrieveLangLBl("", "LBL_FAV_LOCATIONS"));
        recentLocHTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_RECENT_LOCATIONS"));

    }



    public void checkPlaces(final String whichLocationArea) {

        String home_address_str = generalFunc.retrieveValue("userHomeLocationAddress");
//        if(home_address_str.equalsIgnoreCase("")){
//            home_address_str = "----";
//        }
//        String work_address_str = mpref_place.getString("userWorkLocationAddress", "");
//        if(work_address_str.equalsIgnoreCase("")){
//            work_address_str = "----";
//        }

        if (home_address_str != null && !home_address_str.equalsIgnoreCase("")) {

            homePlaceTxt.setText(generalFunc.retrieveLangLBl("", "LBL_HOME_PLACE"));
            homePlaceHTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            homePlaceTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            homePlaceTxt.setTextColor(Color.parseColor("#909090"));
            homePlaceHTxt.setText("" + home_address_str);
            homePlaceHTxt.setVisibility(View.VISIBLE);
            homePlaceHTxt.setTextColor(getResources().getColor(R.color.black));
            homeActionImgView.setImageResource(R.mipmap.ic_edit);

        } else {
            homePlaceHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_HOME_PLACE"));
            homePlaceHTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            homePlaceTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            homePlaceTxt.setText("" + generalFunc.retrieveLangLBl("", "LBL_ADD_HOME_PLACE_TXT"));
            homePlaceTxt.setTextColor(Color.parseColor("#909090"));
            homeActionImgView.setImageResource(R.mipmap.ic_pluse);
        }


        if (home_address_str != null && home_address_str.equalsIgnoreCase("")) {
            homePlaceHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_HOME_PLACE_TXT"));
            homePlaceTxt.setText("----");

            homePlaceHTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            homePlaceHTxt.setTextColor(getResources().getColor(R.color.black));

            homePlaceTxt.setTextColor(Color.parseColor("#909090"));
            homePlaceTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

            homePlaceHTxt.setVisibility(View.VISIBLE);
            homeActionImgView.setImageResource(R.mipmap.ic_pluse);
        }

    }

    private void getRecentLocations(final String whichView) {
        final LayoutInflater mInflater = (LayoutInflater)
                getActContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        DestinationLocations_arr = generalFunc.getJsonArray("DestinationLocations", userProfileJsonObj);
        SourceLocations_arr = generalFunc.getJsonArray("SourceLocations", userProfileJsonObj);

        if (DestinationLocations_arr != null || SourceLocations_arr != null) {

            if (whichView.equals("dest")) {

                if (destLocationView != null) {
                    destLocationView.removeAllViews();
                    recentLocList.clear();
                }
                for (int i = 0; i < DestinationLocations_arr.length(); i++) {

                    JSONObject destLoc_obj = generalFunc.getJsonObject(DestinationLocations_arr, i);


                    final String tEndLat = generalFunc.getJsonValueStr("tDestLatitude", destLoc_obj);
                    final String tEndLong = generalFunc.getJsonValueStr("tDestLongitude", destLoc_obj);
                    final String tDaddress = generalFunc.getJsonValueStr("tDaddress", destLoc_obj);



                    HashMap<String, String> map = new HashMap<>();
                    map.put("tLat", tEndLat);
                    map.put("tLong", tEndLong);
                    map.put("taddress", tDaddress);

                    recentLocList.add(map);

                }
            } else {
                if (sourceLocationView != null) {
                    sourceLocationView.removeAllViews();
                    recentLocList.clear();
                }
                for (int i = 0; i < SourceLocations_arr.length(); i++) {


                    JSONObject loc_obj = generalFunc.getJsonObject(SourceLocations_arr, i);
                    final String tStartLat = generalFunc.getJsonValueStr("tStartLat", loc_obj);
                    final String tStartLong = generalFunc.getJsonValueStr("tStartLong", loc_obj);
                    final String tSaddress = generalFunc.getJsonValueStr("tSaddress", loc_obj);


                    HashMap<String, String> map = new HashMap<>();
                    map.put("tLat", tStartLat);
                    map.put("tLong", tStartLong);
                    map.put("taddress", tSaddress);

                    recentLocList.add(map);

                }
            }

        } else {
            destLocationView.setVisibility(View.GONE);
            sourceLocationView.setVisibility(View.GONE);
            recentLocHTxtView.setVisibility(View.GONE);
        }
    }

    public void getSearchGooglePlace(String input) {
        noPlacedata.setVisibility(View.GONE);

        String serverKey = generalFunc.retrieveValue(Utils.GOOGLE_SERVER_ANDROID_DRIVER_APP_KEY);

        String url = null;
        //   URLEncoder.encode(input.replace(" ", "%20"), "UTF-8")
        try {


            String s = input.trim();
            String[] split = s.split("\\s+");


            /* url = "https://maps.googleapis.com/maps/api/place/queryautocomplete/json?input=" + *//*input.replace(" ", "%20")*//*URLEncoder.encode(input*//*.replace(" ", "%20")*//*, "utf8") + "&key=" + serverKey +
                    "&language=" + generalFunc.retrieveValue(CommonUtilities.GOOGLE_MAP_LANGUAGE_CODE_KEY) + "&sensor=true";
*/
            url = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input=" + /*input.replace(" ", "%20")*/URLEncoder.encode(input/*.replace(" ", "%20")*/, "utf8") + "&key=" + serverKey + "&inputtype=" + "textquery" + "&fields=" + "photos,formatted_address,name,rating,geometry" +
                    "&language=" + generalFunc.retrieveValue(Utils.GOOGLE_MAP_LANGUAGE_CODE_KEY) + "&sensor=true";


            if (getIntent().getDoubleExtra("long", 0.0) != 0.0) {

                url = url + "&location=" + getIntent().getDoubleExtra("lat", 0.0) + "," + getIntent().getDoubleExtra("long", 0.0) + "&radius=20";

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (url == null) {
            return;
        }
        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), url, true);

        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObj=generalFunc.getJsonObject(responseString);

            if (generalFunc.getJsonValue("status", responseStringObj).equals("OK")) {
                JSONArray candidatesArr = generalFunc.getJsonArray("candidates", responseStringObj);

                if (searchTxt.getText().toString().length() == 0) {
                    placesRecyclerView.setVisibility(View.GONE);
                    noPlacedata.setVisibility(View.GONE);
                    googleimagearea.setVisibility(View.GONE);
                    if (getIntent().getBooleanExtra("isPlaceAreaShow", true)) {
                        placesarea.setVisibility(View.VISIBLE);
                    }
                    return;
                }

                placelist.clear();
                for (int i = 0; i < candidatesArr.length(); i++) {
                    JSONObject item = generalFunc.getJsonObject(candidatesArr, i);

                    if (!generalFunc.getJsonValue("formatted_address", item.toString()).equals("")) {

                        HashMap<String, String> map = new HashMap<String, String>();

                        map.put("main_text", generalFunc.getJsonValueStr("formatted_address", item));
                        map.put("secondary_text", "");
                        map.put("place_id", "");
                        map.put("description", generalFunc.getJsonValueStr("name", item));

                        JSONObject obj=generalFunc.getJsonObject("geometry", item);
                        map.put("lat", generalFunc.getJsonValueStr("lat", generalFunc.getJsonObject("location", obj)));
                        map.put("lng", generalFunc.getJsonValueStr("lng", generalFunc.getJsonObject("location", obj)));


                        placelist.add(map);
                    }

                }

                if (placelist.size() > 0) {
                    placesarea.setVisibility(View.GONE);
                    placesRecyclerView.setVisibility(View.VISIBLE);
                    googleimagearea.setVisibility(View.VISIBLE);
                    noPlacedata.setVisibility(View.GONE);

                    if (placesAdapter == null) {
                        placesAdapter = new PlacesAdapter(getActContext(), placelist);
                        placesRecyclerView.setAdapter(placesAdapter);
                        placesAdapter.itemRecentLocClick(SearchLocationActivity.this);

                    } else {
                        placesAdapter.notifyDataSetChanged();
                    }
                }
            } else if (generalFunc.getJsonValueStr("status", responseStringObj).equals("ZERO_RESULTS")) {
                placelist.clear();
                if (placesAdapter != null) {
                    placesAdapter.notifyDataSetChanged();
                }

                String msg = generalFunc.retrieveLangLBl("We didn't find any places matched to your entered place. Please try again with another text.", "LBL_NO_PLACES_FOUND");
                noPlacedata.setText(msg);
                placesarea.setVisibility(View.GONE);
                placesRecyclerView.setVisibility(View.GONE);
                googleimagearea.setVisibility(View.GONE);
                noPlacedata.setVisibility(View.VISIBLE);


            } else {
                placelist.clear();
                if (placesAdapter != null) {
                    placesAdapter.notifyDataSetChanged();
                }
                String msg = "";
                if (!intCheck.isNetworkConnected() && !intCheck.check_int()) {
                    msg = generalFunc.retrieveLangLBl("No Internet Connection", "LBL_NO_INTERNET_TXT");

                } else {
                    msg = generalFunc.retrieveLangLBl("Error occurred while searching nearest places. Please try again later.", "LBL_PLACE_SEARCH_ERROR");

                }

                noPlacedata.setText(msg);
                placesarea.setVisibility(View.GONE);
                placesRecyclerView.setVisibility(View.GONE);
                noPlacedata.setVisibility(View.VISIBLE);

            }


        });
        exeWebServer.execute();
    }

    private void searchResult(JSONObject responseStringObj) {
        if (generalFunc.getJsonValueStr("status", responseStringObj).equals("OK")) {
            JSONArray predictionsArr = generalFunc.getJsonArray("predictions", responseStringObj);

            if (searchTxt.getText().toString().length() == 0) {
                placesRecyclerView.setVisibility(View.GONE);
                noPlacedata.setVisibility(View.GONE);
                return;
            }

            placelist.clear();
            for (int i = 0; i < predictionsArr.length(); i++) {
                JSONObject item = generalFunc.getJsonObject(predictionsArr, i);

                if (!generalFunc.getJsonValue("place_id", item.toString()).equals("")) {

                    HashMap<String, String> map = new HashMap<String, String>();

                    JSONObject structured_formatting = generalFunc.getJsonObject("structured_formatting", item);
                    map.put("main_text", generalFunc.getJsonValueStr("main_text", structured_formatting));
                    map.put("secondary_text", generalFunc.getJsonValueStr("secondary_text", structured_formatting));
                    map.put("place_id", generalFunc.getJsonValueStr("place_id", item));
                    map.put("description", generalFunc.getJsonValueStr("description", item));
                    map.put("session_token", session_token);

                    placelist.add(map);

                }
            }
            if (placelist.size() > 0) {
                placesRecyclerView.setVisibility(View.VISIBLE);

                if (placesAdapter == null) {
                    placesAdapter = new PlacesAdapter(getActContext(), placelist);
                    placesRecyclerView.setAdapter(placesAdapter);
                    placesAdapter.itemRecentLocClick(SearchLocationActivity.this);

                } else {
                    placesAdapter.notifyDataSetChanged();
                }
            }
        } else if (generalFunc.getJsonValueStr("status", responseStringObj).equals("ZERO_RESULTS")) {
            placelist.clear();
            if (placesAdapter != null) {
                placesAdapter.notifyDataSetChanged();
            }

            String msg = generalFunc.retrieveLangLBl("We didn't find any places matched to your entered place. Please try again with another text.", "LBL_NO_PLACES_FOUND");
            noPlacedata.setText(msg);
            placesRecyclerView.setVisibility(View.VISIBLE);

            noPlacedata.setVisibility(View.VISIBLE);

        } else {
            placelist.clear();
            if (placesAdapter != null) {
                placesAdapter.notifyDataSetChanged();
            }
            String msg = "";
            if (!intCheck.isNetworkConnected() && !intCheck.check_int()) {
                msg = generalFunc.retrieveLangLBl("No Internet Connection", "LBL_NO_INTERNET_TXT");

            } else {
                msg = generalFunc.retrieveLangLBl("Error occurred while searching nearest places. Please try again later.", "LBL_PLACE_SEARCH_ERROR");

            }

            noPlacedata.setText(msg);
            placesRecyclerView.setVisibility(View.VISIBLE);
            noPlacedata.setVisibility(View.VISIBLE);
        }
    }

    public void initializeSessionRegeneration() {

        if (sessionTokenFreqTask != null) {
            sessionTokenFreqTask.stopRepeatingTask();
        }
        sessionTokenFreqTask = new UpdateFrequentTask(170000);
        sessionTokenFreqTask.setTaskRunListener(() -> session_token = Utils.userType + "_" + generalFunc.getMemberId() + "_" + System.currentTimeMillis());

        sessionTokenFreqTask.startRepeatingTask();
    }


    @Override
    public void itemRecentLocClick(int position) {

        //getSelectAddresLatLong(placelist.get(position).get("place_id"), placelist.get(position).get("description"));
        getSelectAddresLatLong(placelist.get(position).get("place_id"), placelist.get(position).get("description"), placelist.get(position).get("session_token"), placelist.get(position).get("lat"), placelist.get(position).get("lng"));

    }

    public void setWhichLocationAreaSelected(String locationArea) {
        this.whichLocation = locationArea;

        if (locationArea.equals("dest")) {
            destLocationView.setVisibility(View.VISIBLE);
            sourceLocationView.setVisibility(View.GONE);
            getRecentLocations("dest");
            checkPlaces(locationArea);

        } else if (locationArea.equals("source")) {
            destLocationView.setVisibility(View.GONE);
            sourceLocationView.setVisibility(View.VISIBLE);
            getRecentLocations("source");
            checkPlaces(locationArea);
        }

    }

    public Context getActContext() {
        return SearchLocationActivity.this;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.ADD_HOME_LOC_REQ_CODE && resultCode == RESULT_OK && data != null) {
            HashMap<String,String> storeData=new HashMap<>();
            storeData.put("userHomeLocationLatitude", "" + data.getStringExtra("Latitude"));
            storeData.put("userHomeLocationLongitude", "" + data.getStringExtra("Longitude"));
            storeData.put("userHomeLocationAddress", "" + data.getStringExtra("Address"));
            generalFunc.storeData(storeData);

            homePlaceTxt.setText(data.getStringExtra("Address"));
            checkPlaces(whichLocation);


            Bundle bn = new Bundle();
            bn.putString("Latitude", data.getStringExtra("Latitude"));
            bn.putString("Longitude", "" + data.getStringExtra("Longitude"));
            bn.putString("Address", "" + data.getStringExtra("Address"));
            new StartActProcess(getActContext()).setOkResult(bn);
            finish();

        }else if (requestCode == Utils.ADD_MAP_LOC_REQ_CODE && resultCode == RESULT_OK && data != null) {

            Bundle bn = new Bundle();
            bn.putString("Latitude", data.getStringExtra("Latitude"));
            bn.putString("Longitude", "" + data.getStringExtra("Longitude"));
            bn.putString("Address", "" + data.getStringExtra("Address"));


            new StartActProcess(getActContext()).setOkResult(bn);
            finish();

        }
    }

    public void getGooglePlaces(String input) {

        noPlacedata.setVisibility(View.GONE);

        String serverKey = generalFunc.retrieveValue(Utils.GOOGLE_SERVER_ANDROID_DRIVER_APP_KEY);

        String url = null;
        //   URLEncoder.encode(input.replace(" ", "%20"), "UTF-8")
        String session_token = this.session_token;

        try {
            url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=" + URLEncoder.encode(input, "UTF-8") + "&key=" + serverKey +
                    "&language=" + generalFunc.retrieveValue(Utils.GOOGLE_MAP_LANGUAGE_CODE_KEY) + "&sensor=true&sessiontoken=" + session_token;

            if (getIntent().getDoubleExtra("long", 0.0) != 0.0) {
                url = url + "&location=" + getIntent().getDoubleExtra("lat", 0.0) + "," + getIntent().getDoubleExtra("long", 0.0) + "&radius=20";
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


        if (url == null) {
            return;
        }
        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), url, true);

        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                JSONObject responseStringObj=generalFunc.getJsonObject(responseString);

                if (!currentSearchQuery.equals(input)) {
                    return;
                }

                searchResult(responseStringObj);

            }
        });
        exeWebServer.execute();
    }

    public void getSelectAddresLatLong(String Place_id, final String address, String session_token, String lat, String lng) {


        if (lat == null || lat.equalsIgnoreCase("") || lng
                == null || lng.equalsIgnoreCase("")) {
            String serverKey = generalFunc.retrieveValue(Utils.GOOGLE_SERVER_ANDROID_DRIVER_APP_KEY);


            String url = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + Place_id + "&key=" + serverKey +
                    "&language=" + generalFunc.retrieveValue(Utils.GOOGLE_MAP_LANGUAGE_CODE_KEY) + "&sensor=true&fields=formatted_address,name,geometry&sessiontoken=" + session_token;

            ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), url, true);
            exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
            exeWebServer.setDataResponseListener(responseString -> {
                JSONObject responseStringObj=generalFunc.getJsonObject(responseString);

                if (generalFunc.getJsonValueStr("status", responseStringObj).equals("OK")) {
                    String resultObj = generalFunc.getJsonValueStr("result", responseStringObj);
                    String geometryObj = generalFunc.getJsonValue("geometry", resultObj);
                    String locationObj = generalFunc.getJsonValue("location", geometryObj);
                    String latitude = generalFunc.getJsonValue("lat", locationObj);
                    String longitude = generalFunc.getJsonValue("lng", locationObj);

                    Bundle bn = new Bundle();
                    bn.putString("Address", address);
                    bn.putString("Latitude", "" + latitude);
                    bn.putString("Longitude", "" + longitude);
                    new StartActProcess(getActContext()).setOkResult(bn);
                    finish();


                }


            });
            exeWebServer.execute();
        } else if (!lat.equals("") && !lng.equalsIgnoreCase("")) {
            Bundle bn = new Bundle();
            bn.putString("Address", address);
            bn.putString("Latitude", "" + lat);
            bn.putString("Longitude", "" + lng);
            bn.putBoolean("isSkip", false);
            new StartActProcess(getActContext()).setOkResult(bn);
            finish();

        }

    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            Bundle bndl = new Bundle();

            if (i == R.id.cancelTxt) {
                finish();

            } else if (i == R.id.imageCancel) {
                placesRecyclerView.setVisibility(View.GONE);
                searchTxt.setText("");
                noPlacedata.setVisibility(View.GONE);
            } else if (i == R.id.homeLocArea) {

//                if (mpref_place != null) {

                    final String home_address_str = generalFunc.retrieveValue("userHomeLocationAddress");
                    final String home_addr_latitude = generalFunc.retrieveValue("userHomeLocationLatitude");
                    final String home_addr_longitude = generalFunc.retrieveValue("userHomeLocationLongitude");

                    if (home_address_str != null && !home_address_str.equalsIgnoreCase("")) {

                        if (whichLocation.equals("dest")) {


                            LatLng placeLocation = new LatLng(generalFunc.parseDoubleValue(0.0, home_addr_latitude), generalFunc.parseDoubleValue(0.0, home_addr_longitude));


                            Bundle bn = new Bundle();
                            bn.putString("Address", home_address_str);
                            bn.putString("Latitude", "" + placeLocation.latitude);
                            bn.putString("Longitude", "" + placeLocation.longitude);

                            bn.putBoolean("isSkip", false);
                            new StartActProcess(getActContext()).setOkResult(bn);
                            finish();
                        } else {

                            LatLng placeLocation = new LatLng(generalFunc.parseDoubleValue(0.0, home_addr_latitude), generalFunc.parseDoubleValue(0.0, home_addr_longitude));

                            Bundle bn = new Bundle();
                            bn.putString("Address", home_address_str);
                            bn.putString("Latitude", "" + placeLocation.latitude);
                            bn.putString("Longitude", "" + placeLocation.longitude);
                            bn.putBoolean("isSkip", false);
                            new StartActProcess(getActContext()).setOkResult(bn);
                            finish();
                        }
                    } else {
                        bndl.putString("isHome", "true");
                        new StartActProcess(getActContext()).startActForResult(SearchPickupLocationActivity.class,
                                bndl, Utils.ADD_HOME_LOC_REQ_CODE);
                    }
               /* }else {
                    bndl.putString("isHome", "true");
                    new StartActProcess(getActContext()).startActForResult(SearchPickupLocationActivity.class,
                            bndl, Utils.ADD_HOME_LOC_REQ_CODE);
                }*/

            }else if (i == R.id.homeActionImgView) {
                if (intCheck.isNetworkConnected()) {
                    Bundle bn = new Bundle();
                    bn.putString("isHome", "true");
                    new StartActProcess(getActContext()).startActForResult(SearchPickupLocationActivity.class,
                            bn, Utils.ADD_HOME_LOC_REQ_CODE);
                } else {
                    generalFunc.showMessage(mapLocArea, generalFunc.retrieveLangLBl("", "LBL_NO_INTERNET_TXT"));
                }
            }else if (i == R.id.mapLocArea) {
                bndl.putString("locationArea", getIntent().getStringExtra("locationArea"));
                String from = !whichLocation.equals("dest") ? "isPickUpLoc" : "isDestLoc";
                String lati = !whichLocation.equals("dest") ? "PickUpLatitude" : "DestLatitude";
                String longi = !whichLocation.equals("dest") ? "PickUpLongitude" : "DestLongitude";
                String address = !whichLocation.equals("dest") ? "PickUpAddress" : "DestAddress";


                bndl.putString(from, "true");
                if (getIntent().getDoubleExtra("lat", 0.0) != 0.0 && getIntent().getDoubleExtra("long", 0.0) != 0.0) {
                    bndl.putString(lati, "" + getIntent().getDoubleExtra("lat", 0.0));
                    bndl.putString(longi, "" + getIntent().getDoubleExtra("long", 0.0));
                    if (getIntent().hasExtra("address") && Utils.checkText(getIntent().getStringExtra("address"))) {
                        bndl.putString(address, "" + getIntent().getStringExtra("address"));
                    } else {
                        bndl.putString(address, "");
                    }

                }

                bndl.putString("IS_FROM_SELECT_LOC", "Yes");

                new StartActProcess(getActContext()).startActForResult(SearchPickupLocationActivity.class,
                        bndl, Utils.ADD_MAP_LOC_REQ_CODE);


            }

        }
    }

    @Override
    protected void onDestroy() {
        if (sessionTokenFreqTask != null) {
            sessionTokenFreqTask.stopRepeatingTask();
        }
        super.onDestroy();
    }
}
