package com.levaeu.driver;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.utils.Utils;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.editBox.MaterialEditText;

public class AddAddressActivity extends AppCompatActivity {


    GeneralFunctions generalFunc;
    ImageView backImgView;
    MTextView titleTxt;

    MaterialEditText buildingBox;
    MaterialEditText landmarkBox;
    MaterialEditText addrtypeBox;
    MaterialEditText apartmentLocNameBox;
    MaterialEditText companyBox;
    MaterialEditText postCodeBox;
    MaterialEditText addr2Box;
    MaterialEditText deliveryIntructionBox;
    MaterialEditText vContryBox;

    ImageView locationImage;
    String addresslatitude="";
    String addresslongitude;
    String address="";
    MTextView locAddrTxtView;
    MTextView serviceAddrHederTxtView;
    MTextView AddrareaTxtView;
    MButton btn_type2;
    LinearLayout loc_area;

    String required_str = "";
    String type = "";
    String iUserAddressId;
    boolean isclick = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());


        backImgView = (ImageView) findViewById(R.id.backImgView);
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        loc_area = (LinearLayout) findViewById(R.id.loc_area);

        buildingBox = (MaterialEditText) findViewById(R.id.buildingBox);
        landmarkBox = (MaterialEditText) findViewById(R.id.landmarkBox);
        addrtypeBox = (MaterialEditText) findViewById(R.id.addrtypeBox);
        apartmentLocNameBox = (MaterialEditText) findViewById(R.id.apartmentLocNameBox);
        companyBox = (MaterialEditText) findViewById(R.id.companyBox);
        postCodeBox = (MaterialEditText) findViewById(R.id.postCodeBox);
        addr2Box = (MaterialEditText) findViewById(R.id.addr2Box);
        deliveryIntructionBox = (MaterialEditText) findViewById(R.id.deliveryIntructionBox);
        vContryBox = (MaterialEditText) findViewById(R.id.vContryBox);
        locationImage = (ImageView) findViewById(R.id.locationImage);
        locAddrTxtView = (MTextView) findViewById(R.id.locAddrTxtView);
        serviceAddrHederTxtView = (MTextView) findViewById(R.id.serviceAddrHederTxtView);
        AddrareaTxtView = (MTextView) findViewById(R.id.AddrareaTxtView);
        btn_type2 = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();
        btn_type2.setOnClickListener(new setOnClick());
        loc_area.setOnClickListener(new setOnClick());



            addresslatitude = getIntent().getStringExtra("latitude");
            addresslongitude = getIntent().getStringExtra("longitude");
         //   address = getIntent().getStringExtra("address");
//            type = getIntent().getStringExtra("type");

        locAddrTxtView.setText(generalFunc.retrieveLangLBl("","LBL_SELECT_ADDRESS_TITLE_TXT"));

        backImgView.setOnClickListener(new setOnClick());
        locationImage.setOnClickListener(new setOnClick());
        setLabel();
    }

    private void setLabel() {
        titleTxt.setText(generalFunc.retrieveLangLBl("Add New Address", "LBL_WORKLOCATION"));
        buildingBox.setBothText(generalFunc.retrieveLangLBl("Building/House/Flat No.", "LBL_JOB_LOCATION_HINT_INFO"));
        landmarkBox.setBothText(generalFunc.retrieveLangLBl("Landmark(e.g hospital,park etc.)", "LBL_LANDMARK_HINT_INFO"));
        addrtypeBox.setBothText(generalFunc.retrieveLangLBl("Nickname(optional-home,office etc.)", " LBL_ADDRESSTYPE_HINT_INFO"));
        serviceAddrHederTxtView.setText(generalFunc.retrieveLangLBl("Service address", "LBL_SERVICE_ADDRESS_HINT_INFO"));
        AddrareaTxtView.setText(generalFunc.retrieveLangLBl("Area of service", "LBL_AREA_SERVICE_HINT_INFO"));
        btn_type2.setText(generalFunc.retrieveLangLBl("Save", "LBL_BTN_SUBMIT_TXT"));
        required_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD_ERROR_TXT");
    }

    public void checkValues() {
        boolean buildingDataenterd = Utils.checkText(buildingBox) ? true
                : Utils.setErrorFields(buildingBox, required_str);
        boolean landmarkDataenterd = Utils.checkText(landmarkBox) ? true
                : Utils.setErrorFields(landmarkBox, required_str);

        if (buildingDataenterd == false || landmarkDataenterd == false) {
            return;

        }


        Bundle bn = new Bundle();
        bn.putString("Latitude", addresslatitude);
        bn.putString("Longitude", addresslongitude);
        if(Utils.checkText(addrtypeBox))
        {
            address = Utils.getText(buildingBox) + ", " +  Utils.getText(landmarkBox) + ", " + Utils.getText(addrtypeBox)+", "+ address;
        }
        else {
            address =  Utils.getText(buildingBox) + ", " +  Utils.getText(landmarkBox) + ", " + address;
        }
        bn.putString("Address", address);


        (new StartActProcess(getActContext())).setOkResult(bn);
        finish();

    }



    public Context getActContext() {
        return AddAddressActivity.this;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.SEARCH_PICKUP_LOC_REQ_CODE && resultCode == RESULT_OK && data != null) {

//            mainAct.configPickUpDrag(true, false, false);

            if (resultCode == RESULT_OK) {


                Place place = PlaceAutocomplete.getPlace(getActContext(), data);
                LatLng placeLocation = place.getLatLng();
                locAddrTxtView.setText(place.getAddress().toString());
                addresslatitude = placeLocation.latitude + "";
                addresslongitude = placeLocation.longitude + "";
                address = place.getAddress().toString();


            }

        } else if (requestCode == Utils.UBER_X_SEARCH_PICKUP_LOC_REQ_CODE && resultCode == RESULT_OK && data != null) {
            address = data.getStringExtra("Address");
            locAddrTxtView.setText(address);

            String Latitude=data.getStringExtra("Latitude");
            String Longitude=data.getStringExtra("Longitude");

            addresslatitude = Latitude == null ? "0.0" : Latitude;
            addresslongitude = Longitude == null ? "0.0" : Longitude;
        }
    }

    public class setOnClick implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == R.id.backImgView) {
                AddAddressActivity.super.onBackPressed();
            } else if (i == R.id.loc_area) {

                if (generalFunc.isLocationEnabled()) {
                    Bundle bn = new Bundle();
                    bn.putString("locationArea", "source");
                    bn.putBoolean("isaddressview", true);
                    if (getIntent().hasExtra("iCompanyId")) {
                        bn.putString("eSystem", Utils.eSystem_Type);
                    }
                    new StartActProcess(getActContext()).startActForResult(SearchLocationActivity.class,
                            bn, Utils.UBER_X_SEARCH_PICKUP_LOC_REQ_CODE);
                } else {
                    try {
                        LatLngBounds bounds = null;


                        Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                .setBoundsBias(bounds)
                                .build(AddAddressActivity.this);
                        startActivityForResult(intent, Utils.SEARCH_PICKUP_LOC_REQ_CODE);
                    } catch (Exception e) {

                    }
                }


            } else if (i == locationImage.getId()) {
                loc_area.performClick();
            } else if (i == btn_type2.getId()) {
                if(Utils.checkText(address))
                {
                    checkValues();

                }
                else
                {
                    generalFunc.showMessage(backImgView,generalFunc.retrieveLangLBl("","LBL_SELECT_ADDRESS_TITLE_TXT"));

                }

            }
        }
    }
}
