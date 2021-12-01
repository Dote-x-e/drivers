package com.levaeu.driver;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import com.general.files.CustomLinearLayoutManager;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.general.files.SetOnTouchList;
import com.general.files.StartActProcess;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.editBox.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class AddVehicleActivity extends AppCompatActivity{

    MTextView titleTxt;
    ImageView backImgView;

    GeneralFunctions generalFunc;
    String[] vCarTypes = null;
    MButton submitVehicleBtn;
    MaterialEditText makeBox, modelBox, yearBox, licencePlateBox, colorPlateBox, vehicleTypeBox;

    ArrayList<String> dataList = new ArrayList<>();
    android.support.v7.app.AlertDialog list_make;
    android.support.v7.app.AlertDialog list_model;
    android.support.v7.app.AlertDialog list_year;
    android.support.v7.app.AlertDialog list_vehicleType;

    LinearLayout serviceSelectArea;

    String iSelectedMakeId = "";
    String iSelectedModelId = "";


    int iSelectedMakePosition = 0;

    JSONArray year_arr;
    JSONArray vehicletypelist;
    JSONArray vehicletypelist_DeliverAll = new JSONArray();

    ArrayList<Boolean> carTypesStatusArr;
    ArrayList<Boolean> rentalcarTypesStatusArr;

    String iDriverVehicleId = "";
    String tempiDriverVehicleId = "";
    CheckBox checkboxHandicap, checkboxChildSeat, checkboxWheelChair;
    boolean ishandicapavilabel = false;
    boolean ischildseatavilabel = false;
    boolean iswheelchairavilabel = false;
    String app_type = "";
    JSONObject userProfileJsonObj;
//    String selectedtype = "";

    FrameLayout vehicleTypeArea;

    String ENABLE_EDIT_DRIVER_VEHICLE = "";
    String vRentalCarType = "";
    RecyclerView serviceSelectRecyclerView;

    /*Deliver All*/

    String selectedCarTypes_DeliverAll = "";

    ProgressBar loadingBar;
    View contentArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vehicle);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());

        userProfileJsonObj = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));

        ENABLE_EDIT_DRIVER_VEHICLE = generalFunc.getJsonValueStr("ENABLE_EDIT_DRIVER_VEHICLE", userProfileJsonObj);

        backImgView = (ImageView) findViewById(R.id.backImgView);
        checkboxHandicap = (CheckBox) findViewById(R.id.checkboxHandicap);
        checkboxChildSeat = (CheckBox) findViewById(R.id.checkboxChildSeat);
        checkboxWheelChair = (CheckBox) findViewById(R.id.checkboxWheelChair);
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        serviceSelectArea = (LinearLayout) findViewById(R.id.serviceSelectArea);
        serviceSelectRecyclerView = (RecyclerView) findViewById(R.id.serviceSelectRecyclerView);

        loadingBar = findViewById(R.id.loadingBar);
        contentArea = findViewById(R.id.contentArea);

        submitVehicleBtn = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();

        makeBox = (MaterialEditText) findViewById(R.id.makeBox);
        modelBox = (MaterialEditText) findViewById(R.id.modelBox);
        yearBox = (MaterialEditText) findViewById(R.id.yearBox);
        licencePlateBox = (MaterialEditText) findViewById(R.id.licencePlateBox);
        colorPlateBox = (MaterialEditText) findViewById(R.id.colorPlateBox);
        vehicleTypeArea = (FrameLayout) findViewById(R.id.vehicleTypeArea);
        vehicleTypeBox = (MaterialEditText) findViewById(R.id.vehicleTypeBox);
        app_type = generalFunc.getJsonValueStr("APP_TYPE", userProfileJsonObj);

        serviceSelectRecyclerView.setNestedScrollingEnabled(false);

        if (app_type.equalsIgnoreCase(Utils.CabGeneralType_Deliver) || app_type.equals(Utils.CabGeneralType_UberX) || generalFunc.isDeliverOnlyEnabled()) {
            checkboxHandicap.setVisibility(View.GONE);
            checkboxChildSeat.setVisibility(View.GONE);
            checkboxWheelChair.setVisibility(View.GONE);
        } else {

            String isHadicap = generalFunc.getJsonValueStr("HANDICAP_ACCESSIBILITY_OPTION", userProfileJsonObj);
            String isChildSeatAvail = generalFunc.getJsonValueStr("CHILD_SEAT_ACCESSIBILITY_OPTION", userProfileJsonObj);
            String isWeelChairAvail = generalFunc.getJsonValueStr("WHEEL_CHAIR_ACCESSIBILITY_OPTION", userProfileJsonObj);

            if (isHadicap == null || !isHadicap.equalsIgnoreCase("Yes")) {
                checkboxHandicap.setVisibility(View.GONE);
            } else {
                checkboxHandicap.setVisibility(View.VISIBLE);
            }

            if (isChildSeatAvail == null || !isChildSeatAvail.equalsIgnoreCase("Yes")) {
                checkboxChildSeat.setVisibility(View.GONE);
            } else {
                checkboxChildSeat.setVisibility(View.VISIBLE);
            }

            if (isWeelChairAvail == null || !isWeelChairAvail.equalsIgnoreCase("Yes")) {
                checkboxWheelChair.setVisibility(View.GONE);
            } else {
                checkboxWheelChair.setVisibility(View.VISIBLE);
            }
        }

        backImgView.setOnClickListener(new setOnClickList());

        setLabels();

        String iDriverVehicleId_=getIntent().getStringExtra("iDriverVehicleId");
        if (iDriverVehicleId_ != null && !iDriverVehicleId_.equalsIgnoreCase("")) {
            iDriverVehicleId = iDriverVehicleId_;
            iSelectedMakeId = getIntent().getStringExtra("iMakeId");
            iSelectedModelId = getIntent().getStringExtra("iModelId");
            String vLicencePlate = getIntent().getStringExtra("vLicencePlate");
            String vColour = getIntent().getStringExtra("vColour");
            String iYear = getIntent().getStringExtra("iYear");
            String hadicap = generalFunc.isDeliverOnlyEnabled() ? "No" : getIntent().getStringExtra("eHandiCapAccessibility");
            String childseat = generalFunc.isDeliverOnlyEnabled() ? "No" : getIntent().getStringExtra("eChildAccessibility");
            String wheelchair = generalFunc.isDeliverOnlyEnabled() ? "No" : getIntent().getStringExtra("eWheelChairAccessibility");

            if (hadicap.equalsIgnoreCase("yes")) {
                checkboxHandicap.setChecked(true);
            }
            if (childseat.equalsIgnoreCase("yes")) {
                checkboxChildSeat.setChecked(true);
            }
            if (wheelchair.equalsIgnoreCase("yes")) {
                checkboxWheelChair.setChecked(true);
            }

            licencePlateBox.setText(vLicencePlate.trim());
            colorPlateBox.setText(vColour);
            yearBox.setText(iYear);
        }

        vehicleTypeArea.setVisibility(View.GONE);
    }

    public void setLabels() {
        if (getIntent().getStringExtra("isfrom") != null && getIntent().getStringExtra("isfrom").equalsIgnoreCase("edit")) {
            titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_EDIT_VEHICLE"));
        } else {
            titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_VEHICLE"));
        }

        submitVehicleBtn.setId(Utils.generateViewId());
        submitVehicleBtn.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_SUBMIT_TXT"));

        makeBox.setBothText(generalFunc.retrieveLangLBl("Make", "LBL_MAKE"));
        modelBox.setBothText(generalFunc.retrieveLangLBl("Model", "LBL_MODEL"));
        yearBox.setBothText(generalFunc.retrieveLangLBl("Year", "LBL_YEAR"));
        licencePlateBox.setBothText(generalFunc.retrieveLangLBl("Licence", "LBL_LICENCE_PLATE_TXT"));
        colorPlateBox.setBothText(generalFunc.retrieveLangLBl("Color", "LBL_COLOR_TXT"));

        vehicleTypeBox.setBothText(generalFunc.retrieveLangLBl("Vehicle Type", "LBL_VEHICLE_TYPE_SMALL_TXT"));
        checkboxHandicap.setText(generalFunc.retrieveLangLBl("Handicap accessibility available?", "LBL_HANDICAP_QUESTION"));
        checkboxChildSeat.setText(generalFunc.retrieveLangLBl("", "LBL_CHILD_SEAT_QUESTION"));
        checkboxWheelChair.setText(generalFunc.retrieveLangLBl("", "LBL_WHEEL_CHAIR_ADD_VEHICLES"));

        backImgView.setOnClickListener(new setOnClickList());
        submitVehicleBtn.setOnClickListener(new setOnClickList());


        removeInput();
        buildMakeList();
    }

    private void removeInput() {
        Utils.removeInput(makeBox);
        Utils.removeInput(modelBox);
        Utils.removeInput(yearBox);
        Utils.removeInput(vehicleTypeBox);

        makeBox.setOnTouchListener(new SetOnTouchList());
        modelBox.setOnTouchListener(new SetOnTouchList());
        yearBox.setOnTouchListener(new SetOnTouchList());

        makeBox.setOnClickListener(new setOnClickList());
        modelBox.setOnClickListener(new setOnClickList());
        yearBox.setOnClickListener(new setOnClickList());
        vehicleTypeBox.setOnClickListener(new setOnClickList());
        vehicleTypeBox.setOnTouchListener(new SetOnTouchList());

    }

    public void buildMakeList() {
        dataList.clear();

        loadingBar.setVisibility(View.VISIBLE);
        contentArea.setVisibility(View.GONE);

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getUserVehicleDetails");
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("UserType", Utils.app_type);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), false, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObject=generalFunc.getJsonObject(responseString);

            if (responseStringObject != null && !responseStringObject.equals("")) {
                dataList.clear();
                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObject);

                if (isDataAvail) {

                    JSONObject message_obj = generalFunc.getJsonObject("message", responseStringObject);
                    year_arr = generalFunc.getJsonArray("year", message_obj.toString());

                    vehicletypelist = generalFunc.getJsonArray("vehicletypelist", message_obj.toString());

                    if (vehicletypelist.length() == 0) {
                        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
                        generateAlert.setCancelable(false);
                        generateAlert.setBtnClickList(btn_id -> {
                            if (btn_id == 0) {
                                generateAlert.closeAlertBox();
                                Bundle bn = new Bundle();
                                bn.putBoolean("isContactus", false);
                                new StartActProcess(getActContext()).setOkResult(bn);
                                backImgView.performClick();
                            } else if (btn_id == 1) {
                                Bundle bn = new Bundle();
                                bn.putBoolean("isContactus", true);
                                new StartActProcess(getActContext()).setOkResult(bn);
                                backImgView.performClick();
                            }
                        });

                        generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str_one, responseStringObject)));
                        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
                        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_CONTACT_US_TXT"));

                        generateAlert.showAlertBox();
                    }

                    JSONArray carList_arr;
                    dataList.clear();
                    if (message_obj != null && message_obj.length() > 0) {
                        carList_arr = generalFunc.getJsonArray("carlist", message_obj.toString());

                        if (carList_arr != null) {
                            for (int i = 0; i < carList_arr.length(); i++) {
                                JSONObject obj = generalFunc.getJsonObject(carList_arr, i);
                                dataList.add(obj.toString());
                            }
                        }
                    }

                    buildMake();

                    buildServices(generalFunc.getJsonValueStr("IS_SHOW_VEHICLE_TYPE", message_obj));

                } else {
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObject)));
                }
            } else {
                generalFunc.showError();
            }


            loadingBar.setVisibility(View.GONE);
            contentArea.setVisibility(View.VISIBLE);
        });
        exeWebServer.execute();
    }

    public void buildMake() {
        ArrayList<String> items = new ArrayList<String>();

        for (int i = 0; i < dataList.size(); i++) {
            items.add(generalFunc.getJsonValue("vMake", dataList.get(i)));

            String iMakeId = generalFunc.getJsonValue("iMakeId", dataList.get(i));
            if (!iSelectedMakeId.equals("") && iSelectedMakeId.equals(iMakeId)) {
                iSelectedMakePosition = i;
                makeBox.setText(generalFunc.getJsonValue("vMake", dataList.get(i)));

                buildModelList(false);
            }
        }

        CharSequence[] cs_currency_txt = items.toArray(new CharSequence[items.size()]);


        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());
        builder.setTitle(generalFunc.retrieveLangLBl("Select Make", "LBL_SELECT_MAKE"));

        builder.setItems(cs_currency_txt, (dialog, item) -> {

            if (list_make != null) {
                list_make.dismiss();
            }

            modelBox.setText("");
            iSelectedModelId = "";

            modelBox.setBothText(generalFunc.retrieveLangLBl("Model", "LBL_MODEL"));

            makeBox.setText(generalFunc.getJsonValue("vMake", dataList.get(item)));
            iSelectedMakeId = generalFunc.getJsonValue("iMakeId", dataList.get(item));
            iSelectedMakePosition = item;

        });

        list_make = builder.create();

        if (generalFunc.isRTLmode()) {
            generalFunc.forceRTLIfSupported(list_make);
        }
    }

    private void buildYear() {
        if (year_arr == null || year_arr.length() == 0) {
            return;
        }

        ArrayList<String> items = new ArrayList<String>();

        for (int i = 0; i < year_arr.length(); i++) {
            items.add((String) generalFunc.getValueFromJsonArr(year_arr, i));
        }

        CharSequence[] cs_currency_txt = items.toArray(new CharSequence[items.size()]);

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());
        builder.setTitle(generalFunc.retrieveLangLBl("Select Year", "LBL_SELECT_YEAR"));

        builder.setItems(cs_currency_txt, (dialog, item) -> {
            if (list_year != null) {
                list_year.dismiss();
            }
            yearBox.setText((String) generalFunc.getValueFromJsonArr(year_arr, item));
        });

        list_year = builder.create();

        if (generalFunc.isRTLmode()) {
            generalFunc.forceRTLIfSupported(list_make);
        }

        list_year.show();
    }

    private void buildModelList(final boolean isShow) {

        ArrayList<String> items = new ArrayList<String>();

        JSONArray vModellistArr = generalFunc.getJsonArray("vModellist", dataList.get(iSelectedMakePosition));
        if (vModellistArr != null) {
            for (int i = 0; i < vModellistArr.length(); i++) {
                JSONObject obj_temp = generalFunc.getJsonObject(vModellistArr, i);

                items.add(generalFunc.getJsonValueStr("vTitle", obj_temp));

                String iModelId = generalFunc.getJsonValueStr("iModelId", obj_temp);
                if (!iSelectedModelId.equals("") && iSelectedModelId.equals(iModelId)) {
                    modelBox.setText(generalFunc.getJsonValueStr("vTitle", obj_temp));
                }
            }

            CharSequence[] cs_currency_txt = items.toArray(new CharSequence[items.size()]);

            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());
            builder.setTitle(generalFunc.retrieveLangLBl("Select Models", "LBL_SELECT_MODEL"));

            builder.setItems(cs_currency_txt, (dialog, item) -> {

                if (list_make != null) {
                    list_make.dismiss();
                }
                JSONArray vModellistArr1 = generalFunc.getJsonArray("vModellist", dataList.get(iSelectedMakePosition));
                JSONObject obj_temp = generalFunc.getJsonObject(vModellistArr1, item);

                modelBox.setText(generalFunc.getJsonValueStr("vTitle", obj_temp));
                iSelectedModelId = generalFunc.getJsonValueStr("iModelId", obj_temp);

                if (!isShow) {
                    Utils.removeInput(modelBox);
                }
            });

            list_model = builder.create();

            if (generalFunc.isRTLmode()) {
                generalFunc.forceRTLIfSupported(list_model);
            }

            if (isShow) {
                list_model.show();
            }
        }

    }

    public void buildServices(String IS_SHOW_VEHICLE_TYPE) {

        if (serviceSelectArea.getChildCount() > 0) {
            serviceSelectArea.removeAllViewsInLayout();
        }

        carTypesStatusArr = new ArrayList<>();
        rentalcarTypesStatusArr = new ArrayList<>();
        vehicletypelist_DeliverAll = new JSONArray();
        int position = 0;

        String[] vCarTypes = {};
        String[] vRentalCarType = {};


        String vCarType=getIntent().getStringExtra("vCarType");
        if (vCarType != null && !vCarType.equals("")) {
            vCarTypes = vCarType.split(",");
        }

        String vRentalCarType_=getIntent().getStringExtra("vRentalCarType");
        if (vRentalCarType_ != null && !vRentalCarType_.equals("")) {
            vRentalCarType = vRentalCarType_.split(",");
        }

        String LBL_DELIVERY=generalFunc.retrieveLangLBl("", "LBL_DELIVERY");
        String LBL_RIDE=generalFunc.retrieveLangLBl("", "LBL_RIDE");
        String LBL_HEADER_RDU_FLY_RIDE=generalFunc.retrieveLangLBl("", "LBL_HEADER_RDU_FLY_RIDE");
        if (!generalFunc.isDeliverOnlyEnabled()) {
            for (int i = 0; i < vehicletypelist.length(); i++) {
                JSONObject obj = generalFunc.getJsonObject(vehicletypelist, i);

                if (generalFunc.getJsonValueStr("eType", obj).equalsIgnoreCase(Utils.eSystem_Type)) {
                    try {

                        obj.put("LBL_DELIVERALL", generalFunc.retrieveLangLBl("", "LBL_DELIVERALL"));
                        obj.put("showTag", "Yes");

                        vehicletypelist_DeliverAll.put(position, obj);
                        carTypesStatusArr.add(false);
                        rentalcarTypesStatusArr.add(false);
                        position++;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View view = inflater.inflate(R.layout.item_select_service_ride_del_design, null);

                    MTextView serviceNameTxtView = (MTextView) view.findViewById(R.id.serviceNameTxtView);
                    MTextView serviceTypeNameTxtView = (MTextView) view.findViewById(R.id.serviceTypeNameTxtView);
                    MTextView apptypeTxtView = (MTextView) view.findViewById(R.id.apptypeTxtView);

                    LinearLayout rentalArea = (LinearLayout) view.findViewById(R.id.rentalArea);
                    CheckBox rentalchkBox = (CheckBox) view.findViewById(R.id.rentalchkBox);
                    MTextView rentalTxtView = (MTextView) view.findViewById(R.id.rentalTxtView);
                    rentalTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_AVAILABLE_FOR_RENTAL"));


                    LinearLayout editarea = (LinearLayout) view.findViewById(R.id.editarea);
                    editarea.setVisibility(View.GONE);
                    serviceNameTxtView.setText(generalFunc.getJsonValueStr("vVehicleType", obj));
                    serviceTypeNameTxtView.setText(generalFunc.getJsonValueStr("SubTitle", obj));
                    if (IS_SHOW_VEHICLE_TYPE.equalsIgnoreCase("Yes")) {
                        apptypeTxtView.setVisibility(View.VISIBLE);
                        String eType = generalFunc.getJsonValueStr("eType", obj);
                        String eTypeLable = (eType.equalsIgnoreCase("Delivery") || eType.equalsIgnoreCase("Deliver")) ? LBL_DELIVERY : eType.equalsIgnoreCase("Fly")?LBL_HEADER_RDU_FLY_RIDE:LBL_RIDE;
                        apptypeTxtView.setText("(" + eTypeLable + ")");
                    } else {
                        apptypeTxtView.setVisibility(View.GONE);
                    }


                    final AppCompatCheckBox chkBox = (AppCompatCheckBox) view.findViewById(R.id.chkBox);

                    String eRental=generalFunc.getJsonValueStr("eRental", obj);

                    if (vCarTypes != null && vCarTypes.length > 0) {

                        String ischeck = generalFunc.getJsonValueStr("VehicleServiceStatus", obj);
                        if (ischeck.equalsIgnoreCase("true") || Arrays.asList(vCarTypes).contains(generalFunc.getJsonValue("iVehicleTypeId", obj))) {
                            chkBox.setChecked(true);
                            carTypesStatusArr.add(true);
                            if (eRental != null && eRental.equalsIgnoreCase("yes")) {
                                rentalArea.setVisibility(View.VISIBLE);
                            }

                        } else {
                            carTypesStatusArr.add(false);
                        }
                    } else {
                        carTypesStatusArr.add(false);
                    }


                    if (vRentalCarType != null && vRentalCarType.length > 0) {

                        if (Arrays.asList(vRentalCarType).contains(generalFunc.getJsonValue("iVehicleTypeId", obj))) {

                            rentalchkBox.setChecked(true);
                            rentalcarTypesStatusArr.add(true);
                        } else {
                            rentalcarTypesStatusArr.add(false);
                        }
                    } else {
                        rentalcarTypesStatusArr.add(false);
                    }


                    final int finalI = i;
                    chkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        carTypesStatusArr.set(finalI, isChecked);

                        if (eRental != null && eRental.equalsIgnoreCase("yes")) {

                            if (isChecked) {
                                rentalArea.setVisibility(View.VISIBLE);
                            } else {
                                rentalArea.setVisibility(View.GONE);
                                rentalchkBox.setChecked(false);
                            }
                        } else {
                            rentalArea.setVisibility(View.GONE);
                        }
                    });
                    rentalchkBox.setOnCheckedChangeListener((buttonView, isChecked) -> rentalcarTypesStatusArr.set(finalI, isChecked));
                    serviceSelectArea.addView(view);
                }

            }
        } else {
            String LBL_DELIVERALL=generalFunc.retrieveLangLBl("", "LBL_DELIVERALL");

            for (int i = 0; i < vehicletypelist.length(); i++) {
                JSONObject obj = generalFunc.getJsonObject(vehicletypelist, i);

                if (generalFunc.getJsonValueStr("eType", obj).equalsIgnoreCase(Utils.eSystem_Type)) {
                    try {
                        obj.put("LBL_DELIVERALL", LBL_DELIVERALL);
                        obj.put("showTag", "No");
                        vehicletypelist_DeliverAll.put(position, obj);
                        position++;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }


        if (vehicletypelist_DeliverAll.length() > 0 && generalFunc.isAnyDeliverOptionEnabled()) {

        }
    }

    public Context getActContext() {
        return AddVehicleActivity.this;
    }

    public void checkData() {

        if (iSelectedMakeId.equals("")) {
            generalFunc.showMessage(generalFunc.getCurrentView((Activity) getActContext()), generalFunc.retrieveLangLBl("", "LBL_CHOOSE_MAKE"));
            return;
        }
        if (iSelectedModelId.equals("")) {
            generalFunc.showMessage(generalFunc.getCurrentView((Activity) getActContext()), generalFunc.retrieveLangLBl("", "LBL_CHOOSE_VEHICLE_MODEL"));
            return;
        }

        if (Utils.getText(yearBox).equals("")) {
            generalFunc.showMessage(generalFunc.getCurrentView((Activity) getActContext()), generalFunc.retrieveLangLBl("", "LBL_CHOOSE_YEAR"));
            return;
        }
        if (Utils.getText(licencePlateBox).equals("")) {
            generalFunc.showMessage(generalFunc.getCurrentView((Activity) getActContext()), generalFunc.retrieveLangLBl("Please add your car's licence plate no.", "LBL_ADD_LICENCE_PLATE"));
            return;
        }


        boolean isCarTypeSelected = false;

        String carTypes = "";

        /*Set Deliver All Types as selected*/

        carTypes = selectedCarTypes_DeliverAll;

        if (Utils.checkText(carTypes)) {
            isCarTypeSelected = true;
        }

        if (app_type.equals(Utils.CabGeneralType_UberX)) {

            for (int i = 0; i < carTypesStatusArr.size(); i++) {
                if (carTypesStatusArr.get(i)) {
                    isCarTypeSelected = true;

                    JSONObject obj = generalFunc.getJsonObject(vehicletypelist, i);

                    String iVehicleTypeId = generalFunc.getJsonValueStr("iVehicleTypeId", obj);

                    carTypes = carTypes.equals("") ? iVehicleTypeId : (carTypes + "," + iVehicleTypeId);
                }
            }

            if (!isCarTypeSelected) {
                generalFunc.showMessage(generalFunc.getCurrentView((Activity) getActContext()), generalFunc.retrieveLangLBl(".", "LBL_SELECT_CAR_TYPE"));
                return;
            }
        } else {
            for (int i = 0; i < carTypesStatusArr.size(); i++) {
                if (carTypesStatusArr.get(i)) {
                    isCarTypeSelected = true;

                    JSONObject obj = generalFunc.getJsonObject(vehicletypelist, i);

                    String iVehicleTypeId = generalFunc.getJsonValueStr("iVehicleTypeId", obj);
                    carTypes = carTypes.equals("") ? iVehicleTypeId : (carTypes + "," + iVehicleTypeId);
                }
            }


            if (!isCarTypeSelected) {
                generalFunc.showMessage(generalFunc.getCurrentView((Activity) getActContext()), generalFunc.retrieveLangLBl(".", "LBL_SELECT_CAR_TYPE"));
                return;
            }
        }

        if (checkboxHandicap.isChecked() && !generalFunc.isDeliverOnlyEnabled()) {
            ishandicapavilabel = true;
        } else {
            ishandicapavilabel = false;
        }

        if (checkboxChildSeat.isChecked() && !generalFunc.isDeliverOnlyEnabled()) {
            ischildseatavilabel = true;
        } else {
            ischildseatavilabel = false;
        }

        if (checkboxWheelChair.isChecked() && !generalFunc.isDeliverOnlyEnabled()) {
            iswheelchairavilabel = true;
        } else {
            iswheelchairavilabel = false;
        }

        if (iDriverVehicleId.equals("")) {
            if (ENABLE_EDIT_DRIVER_VEHICLE != null && ENABLE_EDIT_DRIVER_VEHICLE.equalsIgnoreCase("No")) {

                try {

                    GenerateAlertBox editVehicleConfirmDialog = new GenerateAlertBox(getActContext());
                    editVehicleConfirmDialog.setContentMessage("", generalFunc.retrieveLangLBl("", "LBL_COMFIRM_ADD_VEHICLE"));
                    editVehicleConfirmDialog.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"));
                    editVehicleConfirmDialog.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_CONFIRM_TXT"));
                    editVehicleConfirmDialog.setCancelable(false);
                    String finalCarTypes = carTypes;
                    editVehicleConfirmDialog.setBtnClickList(btn_id -> {
                        if (btn_id == 0) {
                            editVehicleConfirmDialog.closeAlertBox();
                        } else {
                            addVehicle(iSelectedMakeId, iSelectedModelId, finalCarTypes);
                        }
                    });
                    editVehicleConfirmDialog.showAlertBox();
                } catch (Exception e) {
                    addVehicle(iSelectedMakeId, iSelectedModelId, carTypes);
                }
            } else {
                addVehicle(iSelectedMakeId, iSelectedModelId, carTypes);
            }
        } else {
            addVehicle(iSelectedMakeId, iSelectedModelId, carTypes);
        }
    }

    public void addVehicle(String iMakeId, String iModelId, String vCarType) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "UpdateDriverVehicle");
        parameters.put("iDriverId", generalFunc.getMemberId());
        parameters.put("UserType", Utils.app_type);
        parameters.put("iMakeId", iMakeId);
        parameters.put("iModelId", iModelId);
        parameters.put("iYear", Utils.getText(yearBox));
        parameters.put("vLicencePlate", Utils.getText(licencePlateBox));
        parameters.put("vCarType", vCarType);
        parameters.put("eAddedDeliverVehicle", generalFunc.isAnyDeliverOptionEnabled() && Utils.checkText(selectedCarTypes_DeliverAll) ? "Yes" : "No");

        for (int i = 0; i < rentalcarTypesStatusArr.size(); i++) {
            if (rentalcarTypesStatusArr.get(i)) {

                JSONObject obj = generalFunc.getJsonObject(vehicletypelist, i);

                String iVehicleTypeId = generalFunc.getJsonValueStr("iVehicleTypeId", obj);

                vRentalCarType = vRentalCarType.equals("") ? iVehicleTypeId : (vRentalCarType + "," + iVehicleTypeId);
            }
        }
        parameters.put("vRentalCarType", vRentalCarType);
        parameters.put("iDriverVehicleId", iDriverVehicleId);
        parameters.put("vColor", Utils.getText(colorPlateBox));

        parameters.put("HandiCap", ishandicapavilabel ? "Yes" : "No");
        parameters.put("ChildAccess", ischildseatavilabel ? "Yes" : "No");
        parameters.put("WheelChair", iswheelchairavilabel ? "Yes" : "No");

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObject=generalFunc.getJsonObject(responseString);

            if (responseStringObject != null && !responseStringObject.equals("")) {
                dataList.clear();
                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObject);

                if (isDataAvail) {

                    try {
                        if (generalFunc.getJsonValue("VehicleInsertId", responseStringObject) != null) {
                            tempiDriverVehicleId = generalFunc.getJsonValueStr("VehicleInsertId", responseStringObject);
                        }
                    } catch (Exception e) {

                    }

                    final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
                    generateAlert.setCancelable(false);
                    generateAlert.setBtnClickList(btn_id -> {
                        if (iDriverVehicleId.equals("")) {
                            if (btn_id == 0) {
                                generateAlert.closeAlertBox();
                                Bundle bn = new Bundle();
                                bn.putBoolean("isUploadDoc", false);
                                bn.putString("iDriverVehicleId", tempiDriverVehicleId);
                                new StartActProcess(getActContext()).setOkResult(bn);
                                backImgView.performClick();
                            } else if (btn_id == 1) {
                                Bundle bn = new Bundle();
                                bn.putString("PAGE_TYPE", "vehicle");
                                bn.putString("vLicencePlate", Utils.getText(licencePlateBox));
                                bn.putString("eStatus", generalFunc.getJsonValueStr("VehicleStatus", responseStringObject));
                                bn.putString("vMake", Utils.getText(makeBox));
                                bn.putString("iDriverVehicleId", generalFunc.getJsonValueStr("VehicleInsertId", responseStringObject));
                                bn.putString("vCarType", vCarType);
                                bn.putString("iMakeId", iMakeId);
                                bn.putString("iYear", Utils.getText(yearBox));
                                bn.putString("iModelId", iModelId);
                                bn.putString("vColour", Utils.getText(colorPlateBox));
                                Bundle passBn = new Bundle();
                                passBn.putBoolean("isUploadDoc", false);
                                passBn.putString("iDriverVehicleId", tempiDriverVehicleId);
                                new StartActProcess(getActContext()).setOkResult(passBn);
                                new StartActProcess(getApplicationContext()).startActWithDataNewTask(ListOfDocumentActivity.class, bn);
                                finish();

                                iDriverVehicleId = tempiDriverVehicleId;
                            }
                        } else {
                            if (btn_id == 0) {
                                generateAlert.closeAlertBox();
                                Bundle bn = new Bundle();
                                bn.putBoolean("isUploadDoc", false);
                                bn.putString("iDriverVehicleId", tempiDriverVehicleId);
                                new StartActProcess(getActContext()).setOkResult(bn);
                                backImgView.performClick();
                            } else if (btn_id == 1) {
                                generateAlert.closeAlertBox();
                                Bundle bn = new Bundle();
                                bn.putBoolean("isUploadDoc", false);
                                bn.putString("iDriverVehicleId", tempiDriverVehicleId);
                                new StartActProcess(getActContext()).setOkResult(bn);
                                backImgView.performClick();
                            }
                        }
                    });
                    if (iDriverVehicleId.equals("")) {
                        generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObject)));
                        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_SKIP_TXT"));
                        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_UPLOAD_DOC"));
                    } else {
                        generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObject)));
                        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
                    }

                    generateAlert.showAlertBox();

                } else {
                    String message = generalFunc.getJsonValueStr(Utils.message_str, responseStringObject);
                    if (!iDriverVehicleId.equals("") && message.equalsIgnoreCase("LBL_EDIT_VEHICLE_DISABLED")) {
                        GenerateAlertBox alertBox = new GenerateAlertBox(getActContext());
                        alertBox.setContentMessage("", generalFunc.retrieveLangLBl("", message));
                        alertBox.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
                        alertBox.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_CONTACT_US_TXT"));
                        alertBox.setBtnClickList(btn_id -> {
                            if (btn_id == 0) {
                                alertBox.closeAlertBox();
                                new StartActProcess(getActContext()).startAct(ContactUsActivity.class);
                            } else {
                                alertBox.closeAlertBox();
                            }
                        });
                        alertBox.showAlertBox();
                    } else {
                        generalFunc.showGeneralMessage("",
                                generalFunc.retrieveLangLBl("", message));
                    }
                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }



    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            Utils.hideKeyboard(AddVehicleActivity.this);
            if (i == R.id.backImgView) {
                AddVehicleActivity.super.onBackPressed();

            } else if (i == R.id.makeBox) {
                if (list_make == null) {
                    buildMake();
                    list_make.show();
                } else {
                    list_make.show();
                }
            } else if (i == R.id.modelBox) {

                if (iSelectedMakeId.equals("")) {
                    generalFunc.showMessage(generalFunc.getCurrentView((Activity) getActContext()), generalFunc.retrieveLangLBl("", "LBL_CHOOSE_MAKE"));
                } else {
                    buildModelList(true);
                }

            } else if (i == R.id.yearBox) {
                if (list_year == null) {
                    buildYear();
                } else {
                    list_year.show();
                }
            } else if (i == submitVehicleBtn.getId()) {
                checkData();
            }
        }
    }


}
