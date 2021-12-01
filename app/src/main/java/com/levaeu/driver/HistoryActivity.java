package com.levaeu.driver;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.adapter.files.ViewPagerAdapter;
import com.fragments.BookingFragment;
import com.fragments.RideHistoryFragment;
import com.general.files.GeneralFunctions;
import com.general.files.GetLocationUpdates;
import com.general.files.MyApp;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.MTextView;
import com.view.MaterialTabs;

import java.util.ArrayList;
import java.util.HashMap;

public class HistoryActivity extends AppCompatActivity implements GetLocationUpdates.LocationUpdatesListener {

    public GeneralFunctions generalFunc;
    MTextView titleTxt;
    ImageView backImgView;
    public String userProfileJson;
    CharSequence[] titles;
    String app_type = "Ride";
    boolean ispending = false;
    boolean isupcoming = false;
    public String selFilterType = "";
    ArrayList<Fragment> fragmentList = new ArrayList<>();
    ArrayList<HashMap<String, String>> filterlist;
    android.support.v7.app.AlertDialog list_type;
    int selTabPos = 0;
    ImageView filterImageview;
    public Location userLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());

        userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
        isupcoming = getIntent().getBooleanExtra("isupcoming", false);
        ispending = getIntent().getBooleanExtra("ispending", false);

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        filterImageview = (ImageView) findViewById(R.id.filterImageview);
        filterImageview.setOnClickListener(new setOnClickList());
        backImgView.setOnClickListener(new setOnClickList());

        app_type = generalFunc.getJsonValue("APP_TYPE", userProfileJson);
        GetLocationUpdates.getInstance().startLocationUpdates(this, this);

        setLabels();

        ViewPager appLogin_view_pager = (ViewPager) findViewById(R.id.appLogin_view_pager);
        MaterialTabs material_tabs = (MaterialTabs) findViewById(R.id.material_tabs);

        String LBL_PAST = generalFunc.retrieveLangLBl("", "LBL_PAST");
        String LBL_UPCOMING = generalFunc.retrieveLangLBl("", "LBL_UPCOMING");
        boolean isRIDE_LATER_BOOKING_ENABLED = generalFunc.getJsonValue("RIDE_LATER_BOOKING_ENABLED", userProfileJson).equalsIgnoreCase("Yes");

        if ((app_type.equalsIgnoreCase(Utils.CabGeneralType_UberX) || app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX)) && generalFunc.getJsonValue("UFX_SERVICE_AVAILABLE", userProfileJson).equalsIgnoreCase("Yes")) {
            if (isRIDE_LATER_BOOKING_ENABLED) {
                titles = new CharSequence[]{generalFunc.retrieveLangLBl("Pending", "LBL_PENDING"), LBL_UPCOMING, LBL_PAST};
                material_tabs.setVisibility(View.VISIBLE);
                fragmentList.add(generateBookingFragPendiing(Utils.Upcoming));
                fragmentList.add(generateBookingFrag(Utils.Upcoming));
                fragmentList.add(generateBookingFragHistory(Utils.Past));
            } else {
                titles = new CharSequence[]{LBL_PAST};
                material_tabs.setVisibility(View.GONE);
                fragmentList.add(generateBookingFragHistory(Utils.Past));

            }
        } else {

            if (isRIDE_LATER_BOOKING_ENABLED) {
                titles = new CharSequence[]{LBL_PAST, LBL_UPCOMING,};
                material_tabs.setVisibility(View.VISIBLE);
                fragmentList.add(generateBookingFragHistory(Utils.Past));
                fragmentList.add(generateBookingFrag(Utils.Upcoming));
            } else {
                titles = new CharSequence[]{LBL_PAST};
                material_tabs.setVisibility(View.GONE);
                fragmentList.add(generateBookingFragHistory(Utils.Past));

            }


        }


        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), titles, fragmentList);
        appLogin_view_pager.setAdapter(adapter);
        material_tabs.setViewPager(appLogin_view_pager);

        if (ispending) {
            appLogin_view_pager.setCurrentItem(0);
        }
        if (isupcoming) {
            appLogin_view_pager.setCurrentItem(1);
        }

        appLogin_view_pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                selTabPos = position;
                selFilterType = "";
                fragmentList.get(position).onResume();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });


    }

    public void finishScreens() {
        ActivityCompat.finishAffinity(HistoryActivity.this);
    }

    public void filterManage(ArrayList<HashMap<String, String>> filterlist) {

        this.filterlist = filterlist;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                filterImageview.setVisibility(View.VISIBLE);
            }
        });

    }

    public void setLabels() {


        String menuMsgYourTrips = "";
        if (app_type.equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
            menuMsgYourTrips = generalFunc.retrieveLangLBl("", "LBL_YOUR_TRIPS");
        } else if (app_type.equalsIgnoreCase("Delivery")) {
            menuMsgYourTrips = generalFunc.retrieveLangLBl("", "LBL_YOUR_DELIVERY");
        } else if (app_type.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
            menuMsgYourTrips = generalFunc.retrieveLangLBl("", "LBL_YOUR_JOB");
        } else {
            menuMsgYourTrips = generalFunc.retrieveLangLBl("", "LBL_YOUR_BOOKING");
        }
        titleTxt.setText(menuMsgYourTrips);

    }


    public void BuildType() {

        ArrayList<String> typeNameList = new ArrayList<>();
        for (int i = 0; i < filterlist.size(); i++) {
            typeNameList.add((filterlist.get(i).get("vTitle")));
        }
        CharSequence[] cs_currency_txt = typeNameList.toArray(new CharSequence[typeNameList.size()]);
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());
        builder.setTitle(generalFunc.retrieveLangLBl("Select Type", "LBL_SELECT_TYPE"));
        builder.setItems(cs_currency_txt, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                // Do something with the selection
                if (list_type != null) {
                    list_type.dismiss();
                }
                selFilterType = filterlist.get(item).get("vFilterParam");
                fragmentList.get(selTabPos).onResume();
            }
        });

        list_type = builder.create();

        if (generalFunc.isRTLmode()) {
            generalFunc.forceRTLIfSupported(list_type);
        }

        list_type.show();


    }


    @Override
    protected void onResume() {

        app_type = generalFunc.getJsonValue("APP_TYPE", userProfileJson);
        super.onResume();

    }

    public Context getActContext() {
        return HistoryActivity.this;
    }

    @Override
    public void onLocationUpdate(Location location) {
        this.userLocation = location;
    }

    public void stopLocUpdates() {
        if (GetLocationUpdates.retrieveInstance() != null) {
            GetLocationUpdates.getInstance().stopLocationUpdates(this);
        }
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(HistoryActivity.this);
            switch (view.getId()) {
                case R.id.backImgView:
                    HistoryActivity.super.onBackPressed();
                    break;
                case R.id.filterImageview:

                    BuildType();
                    break;

            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            ViewPager appLogin_view_pager = (ViewPager) findViewById(R.id.appLogin_view_pager);
            MaterialTabs material_tabs = (MaterialTabs) findViewById(R.id.material_tabs);

            userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
            app_type = generalFunc.getJsonValue("APP_TYPE", userProfileJson);
            ArrayList<Fragment> fragmentList = new ArrayList<>();

            ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), titles, fragmentList);
            appLogin_view_pager.setAdapter(adapter);
            material_tabs.setViewPager(appLogin_view_pager);


        }
    }

    public BookingFragment generateBookingFrag(String bookingType) {
        BookingFragment frag = new BookingFragment();
        Bundle bn = new Bundle();
        bn.putString("BOOKING_TYPE", bookingType);
        bn.putString("type", "LATER");
        frag.setArguments(bn);
        return frag;
    }

    public BookingFragment generateBookingFragPendiing(String bookingType) {
        BookingFragment frag = new BookingFragment();
        Bundle bn = new Bundle();
        bn.putString("BOOKING_TYPE", bookingType);
        bn.putString("type", "PENDING");
        frag.setArguments(bn);
        return frag;
    }


    public RideHistoryFragment generateBookingFragHistory(String bookingType) {
        RideHistoryFragment frag = new RideHistoryFragment();
        Bundle bn = new Bundle();
        bn.putString("BOOKING_TYPE", bookingType);
        bn.putString("type", "PAST");
        frag.setArguments(bn);
        return frag;
    }

    @Override
    protected void onDestroy() {
        stopLocUpdates();
        super.onDestroy();
    }
}
