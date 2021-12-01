package com.levaeu.driver;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.GetDeviceToken;
import com.general.files.MyApp;
import com.general.files.OpenMainProfile;
import com.general.files.SetOnTouchList;
import com.general.files.SetUserData;
import com.general.files.StartActProcess;
import com.utils.Utils;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.editBox.MaterialEditText;

import org.json.JSONObject;

import java.util.HashMap;

public class AccountverificationActivity extends BaseActivity {


    LinearLayout emailarea, mobileNoArea;
    MaterialEditText emailBox;
    MaterialEditText countryBox;
    MaterialEditText mobileBox;
    GeneralFunctions generalFunc;
    String vCountryCode = "";
    String vPhoneCode = "";
    boolean isCountrySelected = false;
    JSONObject userProfileJsonObj;
    String required_str = "";
    String error_email_str = "";
    MTextView accountverifyHint;
    MButton btn_type2;
    int submitBtnId;
    MTextView titleTxt;
    ImageView backImgView, logoutImageview;

    ImageView imageView2, imageView1;
    MaterialEditText invitecodeBox;
    ImageView inviteQueryImg;
    CheckBox checkboxTermsCond;
    MTextView txtTermsCond;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accountverification);

        initView();
        setLabel();
        removeInput();
    }

    public Context getActContext() {
        return AccountverificationActivity.this;
    }

    private void initView() {
        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());

        userProfileJsonObj = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
        emailBox = (MaterialEditText) findViewById(R.id.emailBox);
        countryBox = (MaterialEditText) findViewById(R.id.countryBox);

        mobileBox = (MaterialEditText) findViewById(R.id.mobileBox);
        emailarea = (LinearLayout) findViewById(R.id.emailarea);
        mobileNoArea = (LinearLayout) findViewById(R.id.mobileNoArea);
        accountverifyHint = (MTextView) findViewById(R.id.accountverifyHint);
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        logoutImageview = (ImageView) findViewById(R.id.logoutImageview);
        imageView2 = (ImageView) findViewById(R.id.imageView2);
        imageView1 = (ImageView) findViewById(R.id.imageView1);
        invitecodeBox = (MaterialEditText) findViewById(R.id.invitecodeBox);
        inviteQueryImg = (ImageView) findViewById(R.id.inviteQueryImg);
        checkboxTermsCond = (CheckBox) findViewById(R.id.checkboxTermsCond);
        txtTermsCond = (MTextView) findViewById(R.id.txtTermsCond);
        txtTermsCond.setOnClickListener(new setOnClickList());
        inviteQueryImg.setColorFilter(Color.parseColor("#CECECE"));
        inviteQueryImg.setOnClickListener(new setOnClickList());


        logoutImageview.setVisibility(View.VISIBLE);
        logoutImageview.setOnClickListener(new setOnClickList());
        backImgView.setVisibility(View.GONE);
        HashMap<String,String> data=new HashMap<>();
        data.put(Utils.DefaultCountryCode,"");
        data.put(Utils.DefaultPhoneCode,"");
        data=generalFunc.retrieveValue(data);

        vCountryCode = data.get(Utils.DefaultCountryCode);
        vPhoneCode = data.get(Utils.DefaultPhoneCode);

        if (!vPhoneCode.equalsIgnoreCase("")) {
            countryBox.setText("+" + vPhoneCode);
            isCountrySelected = true;
        }

        btn_type2 = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();

        submitBtnId = Utils.generateViewId();
        btn_type2.setId(submitBtnId);

        btn_type2.setOnClickListener(new setOnClickList());


        emailBox.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        mobileBox.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mobileBox.setInputType(InputType.TYPE_CLASS_NUMBER);

        if (generalFunc.getJsonValueStr("vPhone", userProfileJsonObj).equals("")) {
            mobileNoArea.setVisibility(View.VISIBLE);
        } else {
            mobileNoArea.setVisibility(View.GONE);

        }

        String vEmail=generalFunc.getJsonValueStr("vEmail", userProfileJsonObj);
        if (!Utils.checkText(vEmail)) {
            emailarea.setVisibility(View.VISIBLE);
        } else {
            emailBox.setText(vEmail);
            emailarea.setVisibility(View.GONE);
        }

        countryBox.setShowClearButton(false);

    }

    public void removeInput() {
        Utils.removeInput(countryBox);

        if (generalFunc.retrieveValue("showCountryList").equalsIgnoreCase("Yes")) {
            imageView2.setVisibility(View.VISIBLE);

            countryBox.setOnTouchListener(new SetOnTouchList());

            countryBox.setOnClickListener(new setOnClickList());
        }
    }

    private void setLabel() {

        emailBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_EMAIL_LBL_TXT"));
        countryBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_COUNTRY_TXT"));
        mobileBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_MOBILE_NUMBER_HEADER_TXT"));
        required_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD_ERROR_TXT");
        error_email_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_EMAIL_ERROR_TXT");
        accountverifyHint.setText(generalFunc.retrieveLangLBl("", "LBL_ACC_SUB_INFO"));
        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_ARRIVED_DIALOG_BTN_CONTINUE_TXT"));
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ACC_INFO"));
        String LBL_INVITE_CODE_HINT=generalFunc.retrieveLangLBl("", "LBL_INVITE_CODE_HINT");
        invitecodeBox.setBothText(LBL_INVITE_CODE_HINT, LBL_INVITE_CODE_HINT);

        String attrString1 = generalFunc.retrieveLangLBl("", "LBL_TERMS_CONDITION_PREFIX");
        String attrString2 = generalFunc.retrieveLangLBl("", "LBL_TERMS_PRIVACY");
        String htmlString = "<u><font color=" + getActContext().getResources().getColor(R.color.appThemeColor_1) + ">" + attrString2 + "</font></u>";
        txtTermsCond.setText(Html.fromHtml(attrString1 + " " + htmlString));


        emailBox.getLabelFocusAnimator().start();
        countryBox.getLabelFocusAnimator().start();
        mobileBox.getLabelFocusAnimator().start();

    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {

            int i = view.getId();
            Utils.hideKeyboard(AccountverificationActivity.this);

            if (i == R.id.countryBox) {
                new StartActProcess(getActContext()).startActForResult(SelectCountryActivity.class, Utils.SELECT_COUNTRY_REQ_CODE);
            } else if (i == submitBtnId) {
                Utils.hideKeyboard(AccountverificationActivity.this);
                checkValues();
            } else if (i == logoutImageview.getId()) {

                MyApp.getInstance().logOutFromDevice(false);

            } else if (i == inviteQueryImg.getId()) {
                generalFunc.showGeneralMessage(generalFunc.retrieveLangLBl(" What is Referral / Invite Code ?", "LBL_REFERAL_SCHEME_TXT"),
                        generalFunc.retrieveLangLBl("", "LBL_REFERAL_SCHEME"));
            } else if (i == txtTermsCond.getId()) {

                Bundle bn = new Bundle();
                bn.putBoolean("islogin", true);
                new StartActProcess(getActContext()).startActWithData(SupportActivity.class, bn);

            }

        }
    }

    public void checkValues() {


        boolean emailEntered = Utils.checkText(emailBox) ?
                (generalFunc.isEmailValid(Utils.getText(emailBox)) ? true : Utils.setErrorFields(emailBox, error_email_str))
                : Utils.setErrorFields(emailBox, required_str);
        boolean mobileEntered = Utils.checkText(mobileBox) ? true : Utils.setErrorFields(mobileBox, required_str);
        boolean countryEntered = isCountrySelected ? true : Utils.setErrorFields(countryBox, required_str);

        if (generalFunc.retrieveValue("showCountryList").equalsIgnoreCase("Yes")) {
            if (countryEntered == false) {
                imageView2.setVisibility(View.GONE);
                imageView1.setVisibility(View.VISIBLE);
            } else {
                imageView2.setVisibility(View.VISIBLE);
                imageView1.setVisibility(View.GONE);
            }
        }

        if (mobileNoArea.getVisibility() == View.GONE) {
            mobileEntered = true;
            countryEntered = true;
        }
        if (emailarea.getVisibility() == View.GONE) {
            emailEntered = true;
        }

        if (mobileEntered) {
            mobileEntered = mobileBox.length() >= 3 ? true : Utils.setErrorFields(mobileBox, generalFunc.retrieveLangLBl("", "LBL_INVALID_MOBILE_NO"));
        }

        if (!checkboxTermsCond.isChecked()) {
            generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_ACCEPT_TERMS_PRIVACY_ALERT"));
            return;
        }


        if (emailEntered == false || mobileEntered == false || countryEntered == false) {
            return;
        }


        updateProfile();
    }

    public void updateProfile() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "updateUserProfileDetail");
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("vName", generalFunc.getJsonValueStr("vName", userProfileJsonObj));
        parameters.put("vLastName", generalFunc.getJsonValueStr("vLastName", userProfileJsonObj));
        parameters.put("vPhone", Utils.getText(mobileBox));
        parameters.put("vPhoneCode", vPhoneCode);
        parameters.put("vCountry", vCountryCode);
        parameters.put("vEmail", Utils.getText(emailBox));


        HashMap<String,String> data=new HashMap<>();
        data.put(Utils.DEFAULT_CURRENCY_VALUE,"");
        data.put(Utils.LANGUAGE_CODE_KEY,"");
        data=generalFunc.retrieveValue(data);

        parameters.put("CurrencyCode", data.get(Utils.DEFAULT_CURRENCY_VALUE));
        parameters.put("LanguageCode", data.get(Utils.LANGUAGE_CODE_KEY));
        parameters.put("UserType", Utils.app_type);
        parameters.put("vInviteCode", Utils.getText(invitecodeBox));

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObj=generalFunc.getJsonObject(responseString);

            if (responseStringObj != null && !responseStringObj.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObj);

                if (isDataAvail) {

                    new SetUserData(responseString, generalFunc, getActContext(), true);

                    manageSinchClient();

                    generalFunc.storeData(Utils.USER_PROFILE_JSON, generalFunc.getJsonValueStr(Utils.message_str, responseStringObj));
                    new OpenMainProfile(getActContext(),
                            generalFunc.getJsonValueStr(Utils.message_str, responseStringObj), false, generalFunc).startProcess();

                } else {
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObj)));
                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == Utils.SELECT_COUNTRY_REQ_CODE && resultCode == RESULT_OK && data != null) {
            vCountryCode = data.getStringExtra("vCountryCode");
            vPhoneCode = data.getStringExtra("vPhoneCode");
            isCountrySelected = true;

            countryBox.setText("+" + vPhoneCode);
            imageView2.setVisibility(View.VISIBLE);
            imageView1.setVisibility(View.GONE);
        }
    }

    public void manageSinchClient() {

    }
}
