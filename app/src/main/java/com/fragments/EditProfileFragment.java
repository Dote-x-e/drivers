package com.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;

import com.levaeu.driver.MyProfileActivity;
import com.levaeu.driver.R;
import com.levaeu.driver.SelectCountryActivity;
import com.levaeu.driver.VerifyInfoActivity;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.SetUserData;
import com.general.files.StartActProcess;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MaterialRippleLayout;
import com.view.anim.loader.AVLoadingIndicatorView;
import com.view.editBox.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class EditProfileFragment extends Fragment {

    MyProfileActivity myProfileAct;
    View view;

    GeneralFunctions generalFunc;

    JSONObject userProfileJsonObj;

    MaterialEditText fNameBox;
    MaterialEditText lNameBox;
    MaterialEditText emailBox;
    MaterialEditText profileDescriptionEditBox;
    MaterialEditText countryBox;
    MaterialEditText mobileBox;
    MaterialEditText langBox;
    MaterialEditText currencyBox;

    AVLoadingIndicatorView loaderView;
    FrameLayout langSelectArea, currencySelectArea;

    ArrayList<String> items_txt_language = new ArrayList<String>();
    ArrayList<String> items_language_code = new ArrayList<String>();

    String selected_language_code = "";
    android.support.v7.app.AlertDialog list_language;

    ArrayList<String> items_txt_currency = new ArrayList<String>();
    ArrayList<String> items_currency_symbol = new ArrayList<String>();


    String selected_currency = "";
    String selected_currency_symbol = "";
    // String selected_work_location = "";
    android.support.v7.app.AlertDialog list_currency;
    android.support.v7.app.AlertDialog list_work_location;

    MButton btn_type2;
    int submitBtnId;

    String required_str = "";
    String error_email_str = "";

    String vCountryCode = "";
    String vPhoneCode = "";
    boolean isCountrySelected = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        myProfileAct = (MyProfileActivity) getActivity();

        generalFunc = myProfileAct.generalFunc;
        userProfileJsonObj = myProfileAct.userProfileJsonObj;

        fNameBox = (MaterialEditText) view.findViewById(R.id.fNameBox);
        lNameBox = (MaterialEditText) view.findViewById(R.id.lNameBox);
        emailBox = (MaterialEditText) view.findViewById(R.id.emailBox);
        countryBox = (MaterialEditText) view.findViewById(R.id.countryBox);
        mobileBox = (MaterialEditText) view.findViewById(R.id.mobileBox);
        langBox = (MaterialEditText) view.findViewById(R.id.langBox);
        currencyBox = (MaterialEditText) view.findViewById(R.id.currencyBox);
        loaderView = (AVLoadingIndicatorView) view.findViewById(R.id.loaderView);
        profileDescriptionEditBox = (MaterialEditText) view.findViewById(R.id.profileDescriptionEditBox);
        btn_type2 = ((MaterialRippleLayout) view.findViewById(R.id.btn_type2)).getChildView();

        currencySelectArea = (FrameLayout) view.findViewById(R.id.currencySelectArea);

        langSelectArea = (FrameLayout) view.findViewById(R.id.langSelectArea);

        submitBtnId = Utils.generateViewId();
        btn_type2.setId(submitBtnId);

        btn_type2.setOnClickListener(new setOnClickList());
        mobileBox.setInputType(InputType.TYPE_CLASS_NUMBER);
        emailBox.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);


        setLabels();

        removeInput();

        buildLanguageList();

        setData();
        myProfileAct.changePageTitle(generalFunc.retrieveLangLBl("", "LBL_EDIT_PROFILE_TXT"));

        if (generalFunc.retrieveValue("ENABLE_EDIT_DRIVER_PROFILE").equalsIgnoreCase("No")) {
            generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_PROFILE_EDIT_BLOCK_TXT"));
        }

        if (myProfileAct.isEmail) {
            emailBox.requestFocus();
        }

        if (myProfileAct.isMobile) {
            mobileBox.requestFocus();
        }

        return view;
    }

    public void setLabels() {
        fNameBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_FIRST_NAME_HEADER_TXT"));
        lNameBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_LAST_NAME_HEADER_TXT"));
        emailBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_EMAIL_LBL_TXT"));
        countryBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_COUNTRY_TXT"));
        mobileBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_MOBILE_NUMBER_HEADER_TXT"));
        langBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_LANGUAGE_TXT"));
        currencyBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_CURRENCY_TXT"));
        profileDescriptionEditBox.setBothText(generalFunc.retrieveLangLBl("Service Description", "LBL_SERVICE_DESCRIPTION"));
        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_PROFILE_UPDATE_PAGE_TXT"));

        fNameBox.getLabelFocusAnimator().start();
        lNameBox.getLabelFocusAnimator().start();
        emailBox.getLabelFocusAnimator().start();
        countryBox.getLabelFocusAnimator().start();
        mobileBox.getLabelFocusAnimator().start();
        langBox.getLabelFocusAnimator().start();
        currencyBox.getLabelFocusAnimator().start();
        profileDescriptionEditBox.getLabelFocusAnimator().start();

        mobileBox.setImeOptions(EditorInfo.IME_ACTION_DONE);

        required_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD_ERROR_TXT");
        error_email_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_EMAIL_ERROR_TXT");

        String APP_TYPE=generalFunc.getJsonValueStr("APP_TYPE", userProfileJsonObj);

        if ((APP_TYPE.equalsIgnoreCase("UberX") || (APP_TYPE.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX) && myProfileAct.isUfxServicesEnabled)) && !generalFunc.isDeliverOnlyEnabled()) {
            profileDescriptionEditBox.setVisibility(View.VISIBLE);
        }
    }

    public void removeInput() {
        Utils.removeInput(countryBox);
        Utils.removeInput(langBox);
        Utils.removeInput(currencyBox);

        if (generalFunc.retrieveValue("showCountryList").equalsIgnoreCase("Yes")) {
            view.findViewById(R.id.imageView2).setVisibility(View.VISIBLE);
            countryBox.setOnTouchListener(new setOnTouchList());
            countryBox.setOnClickListener(new setOnClickList());
        }

        langBox.setOnTouchListener(new setOnTouchList());
        currencyBox.setOnTouchListener(new setOnTouchList());


        langBox.setOnClickListener(new setOnClickList());
        currencyBox.setOnClickListener(new setOnClickList());
    }

    public class setOnTouchList implements View.OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP && !view.hasFocus()) {
                view.performClick();
            }
            return true;
        }
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            Utils.hideKeyboard(getActivity());
            if (i == R.id.langBox) {
                showLanguageList();

            } else if (i == R.id.currencyBox) {
                showCurrencyList();

            } else if (i == submitBtnId) {

                checkValues();
            } else if (i == R.id.countryBox) {
                new StartActProcess(getActContext()).startActForResult(myProfileAct.getEditProfileFrag(),
                        SelectCountryActivity.class, Utils.SELECT_COUNTRY_REQ_CODE);
            }
        }
    }

    public void setData() {
        fNameBox.setText(generalFunc.getJsonValueStr("vName", userProfileJsonObj));
        lNameBox.setText(generalFunc.getJsonValueStr("vLastName", userProfileJsonObj));

        if (generalFunc.retrieveValue("ENABLE_EDIT_DRIVER_PROFILE").equalsIgnoreCase("No")) {
            Utils.removeInput(fNameBox) ;
            Utils.removeInput(lNameBox) ;
        }

        emailBox.setText(generalFunc.getJsonValueStr("vEmail", userProfileJsonObj));
        countryBox.setText(generalFunc.getJsonValueStr("vCode", userProfileJsonObj));
        mobileBox.setText(generalFunc.getJsonValueStr("vPhone", userProfileJsonObj));
        currencyBox.setText(generalFunc.getJsonValueStr("vCurrencyDriver", userProfileJsonObj));
        profileDescriptionEditBox.setText(generalFunc.getJsonValueStr("tProfileDescription", userProfileJsonObj));

        String vCode=generalFunc.getJsonValueStr("vCode", userProfileJsonObj);
        if (!vCode.equals("")) {
            isCountrySelected = true;
            vPhoneCode = vCode;
            vCountryCode = generalFunc.getJsonValueStr("vCountry", userProfileJsonObj);
        }

        selected_currency = generalFunc.getJsonValueStr("vCurrencyDriver", userProfileJsonObj);
    }

    public void buildLanguageList() {

        HashMap<String,String> data=new HashMap<>();
        data.put(Utils.LANGUAGE_LIST_KEY,"");
        data.put(Utils.LANGUAGE_CODE_KEY,"");
        data=generalFunc.retrieveValue(data);

        JSONArray languageList_arr = generalFunc.getJsonArray(data.get(Utils.LANGUAGE_LIST_KEY));

        for (int i = 0; i < languageList_arr.length(); i++) {
            JSONObject obj_temp = generalFunc.getJsonObject(languageList_arr, i);

            String vTitle=generalFunc.getJsonValueStr("vTitle", obj_temp);
            String vCode=generalFunc.getJsonValueStr("vCode", obj_temp);


            items_txt_language.add(vTitle);
            items_language_code.add(vCode);

            if ((data.get(Utils.LANGUAGE_CODE_KEY)).equals(vCode)) {
                selected_language_code = vCode;

                langBox.setText(vTitle);
            }
        }

        CharSequence[] cs_languages_txt = items_txt_language.toArray(new CharSequence[items_txt_language.size()]);

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());
        builder.setTitle(getSelectLangText());

        builder.setItems(cs_languages_txt, (dialog, item) -> {
            if (list_language != null) {
                list_language.dismiss();
            }
            selected_language_code = items_language_code.get(item);
            generalFunc.storeData(Utils.DEFAULT_LANGUAGE_VALUE, items_txt_language.get(item));
            langBox.setText(items_txt_language.get(item));

        });
        // builder.setItems(cs_languages_txt, null);

        list_language = builder.create();

        if (generalFunc.isRTLmode()) {
            generalFunc.forceRTLIfSupported(list_language);
        }

        if (items_txt_language.size() < 2) {
            langSelectArea.setVisibility(View.GONE);

        }

        buildCurrencyList();
    }


    public void buildCurrencyList() {
        JSONArray currencyList_arr = generalFunc.getJsonArray(generalFunc.retrieveValue(Utils.CURRENCY_LIST_KEY));

        for (int i = 0; i < currencyList_arr.length(); i++) {
            JSONObject obj_temp = generalFunc.getJsonObject(currencyList_arr, i);

            items_txt_currency.add(generalFunc.getJsonValueStr("vName", obj_temp));
            items_currency_symbol.add(generalFunc.getJsonValueStr("vSymbol", obj_temp));
        }

        CharSequence[] cs_currency_txt = items_txt_currency.toArray(new CharSequence[items_txt_currency.size()]);

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());
        builder.setTitle(generalFunc.retrieveLangLBl("", "LBL_SELECT_CURRENCY"));

        builder.setItems(cs_currency_txt, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                // Do something with the selection

                if (list_currency != null) {
                    list_currency.dismiss();
                }

                selected_currency = items_txt_currency.get(item);
                currencyBox.setText(items_txt_currency.get(item));

            }
        });

        list_currency = builder.create();

        if (generalFunc.isRTLmode()) {
            generalFunc.forceRTLIfSupported(list_currency);
        }
        if (items_txt_currency.size() < 2) {
            currencySelectArea.setVisibility(View.GONE);
        }
    }

    public void showLanguageList() {
        list_language.show();
    }

    public void showCurrencyList() {
        list_currency.show();
    }

    public String getSelectLangText() {
        return ("" + generalFunc.retrieveLangLBl("Select", "LBL_SELECT_LANGUAGE_HINT_TXT"));
    }

    public void checkValues() {


        boolean fNameEntered = Utils.checkText(fNameBox) ? true : Utils.setErrorFields(fNameBox, required_str);
        boolean lNameEntered = Utils.checkText(lNameBox) ? true : Utils.setErrorFields(lNameBox, required_str);
        boolean emailEntered = Utils.checkText(emailBox) ?
                (generalFunc.isEmailValid(Utils.getText(emailBox)) ? true : Utils.setErrorFields(emailBox, error_email_str))
                : Utils.setErrorFields(emailBox, required_str);
        boolean mobileEntered = Utils.checkText(mobileBox) ? true : Utils.setErrorFields(mobileBox, required_str);
        boolean countryEntered = isCountrySelected ? true : Utils.setErrorFields(countryBox, required_str);
        boolean currencyEntered = !selected_currency.equals("") ? true : Utils.setErrorFields(currencyBox, required_str);


        if (mobileEntered) {
            mobileEntered = mobileBox.length() >= 3 ? true : Utils.setErrorFields(mobileBox, generalFunc.retrieveLangLBl("", "LBL_INVALID_MOBILE_NO"));
        }
        if (fNameEntered == false || lNameEntered == false || emailEntered == false || mobileEntered == false
                || countryEntered == false || currencyEntered == false) {
            return;
        }

        String currentMobileNum = generalFunc.getJsonValueStr("vPhone", userProfileJsonObj);
        String currentPhoneCode = generalFunc.getJsonValueStr("vCode", userProfileJsonObj);

        if (!currentPhoneCode.equals(vPhoneCode) || !currentMobileNum.equals(Utils.getText(mobileBox))) {
            if (generalFunc.retrieveValue(Utils.MOBILE_VERIFICATION_ENABLE_KEY).equals("Yes")) {
                notifyVerifyMobile();

                return;
            }
        }

        updateProfile();
    }

    public void notifyVerifyMobile() {
        Bundle bn = new Bundle();
        bn.putString("MOBILE", vPhoneCode + Utils.getText(mobileBox));
        bn.putString("msg", "DO_PHONE_VERIFY");
        generalFunc.verifyMobile(bn, myProfileAct.getEditProfileFrag(), VerifyInfoActivity.class);
    }

    public void updateProfile() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "updateUserProfileDetail");
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("vName", Utils.getText(fNameBox));
        parameters.put("vLastName", Utils.getText(lNameBox));
        parameters.put("vPhone", Utils.getText(mobileBox));
        parameters.put("vEmail", Utils.getText(emailBox));
        parameters.put("tProfileDescription", Utils.getText(profileDescriptionEditBox));
        parameters.put("vPhoneCode", vPhoneCode);
        parameters.put("vCountry", vCountryCode);
        parameters.put("CurrencyCode", selected_currency);
        parameters.put("LanguageCode", selected_language_code);
        parameters.put("UserType", Utils.app_type);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {
                JSONObject responseStringObject=generalFunc.getJsonObject(responseString);
                if (responseStringObject != null && !responseStringObject.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObject);

                    if (isDataAvail) {

                        String currentLangCode = generalFunc.retrieveValue(Utils.LANGUAGE_CODE_KEY);
                        String vCurrencyPassenger = generalFunc.getJsonValueStr("vCurrencyDriver", userProfileJsonObj);

                        try {
                            String messgeJson = generalFunc.getJsonValueStr(Utils.message_str, responseStringObject);
                            generalFunc.storeData(Utils.USER_PROFILE_JSON, messgeJson);
                            responseString = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);

                        } catch (Exception e) {

                        }

                        new SetUserData(responseString, generalFunc, getActContext(), false);

                        if (!currentLangCode.equals(selected_language_code) || !selected_currency.equals(vCurrencyPassenger)) {

                            GenerateAlertBox alertBox = generalFunc.notifyRestartApp();
                            alertBox.setCancelable(false);
                            alertBox.setBtnClickList(btn_id -> {
                                if (btn_id == 1) {
                                    generalFunc.storeData(Utils.LANGUAGE_CODE_KEY, selected_language_code);
                                    generalFunc.storeData(Utils.DEFAULT_CURRENCY_VALUE, selected_currency);
                                    loaderView.setVisibility(View.VISIBLE);
                                    changeLanguagedata(selected_language_code);
                                }
                            });
                        } else {
                            myProfileAct.changeUserProfileJson(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
                        }

                    } else {
                        generalFunc.showGeneralMessage("",
                                generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObject)));
                    }
                } else {
                    generalFunc.showError();
                }
            }
        });
        exeWebServer.execute();
    }

    public void changeLanguagedata(String langcode) {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "changelanguagelabel");
        parameters.put("vLang", langcode);
        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            JSONObject responseStringObj=generalFunc.getJsonObject(responseString);
            if (responseStringObj != null && !responseStringObj.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObj);

                if (isDataAvail) {
                    loaderView.setVisibility(View.GONE);
                    generalFunc.storeData(Utils.languageLabelsKey, generalFunc.getJsonValueStr(Utils.message_str, responseStringObj));
                    generalFunc.storeData(Utils.LANGUAGE_IS_RTL_KEY, generalFunc.getJsonValueStr("eType", responseStringObj));
                    generalFunc.storeData(Utils.GOOGLE_MAP_LANGUAGE_CODE_KEY, generalFunc.getJsonValueStr("vGMapLangCode", responseStringObj));
                    new Handler().postDelayed(() -> generalFunc.restartApp(), 100);
                } else {
                    loaderView.setVisibility(View.GONE);
                }
            } else {
                loaderView.setVisibility(View.GONE);
            }

        });
        exeWebServer.execute();
    }

    public Context getActContext() {
        return myProfileAct.getActContext();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.SELECT_COUNTRY_REQ_CODE && resultCode == Activity.RESULT_OK && data != null) {
            vCountryCode = data.getStringExtra("vCountryCode");
            vPhoneCode = data.getStringExtra("vPhoneCode");
            isCountrySelected = true;

            countryBox.setText("+" + vPhoneCode);
        } else if (requestCode == Utils.VERIFY_MOBILE_REQ_CODE && resultCode == Activity.RESULT_OK) {
            updateProfile();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Utils.hideKeyboard(getActivity());
    }
}
