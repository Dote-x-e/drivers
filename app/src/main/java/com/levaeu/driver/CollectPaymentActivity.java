package com.levaeu.driver;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.GetLocationUpdates;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.ErrorView;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.editBox.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class CollectPaymentActivity extends AppCompatActivity {

    MTextView titleTxt;
    ImageView backImgView;

    GeneralFunctions generalFunc;

    ProgressBar loading;
    ErrorView errorView;
    MButton btn_type2;
    ImageView editCommentImgView;
    MTextView commentBox;
    MTextView generalCommentTxt;

    int submitBtnId;

    String appliedComment = "";
    LinearLayout container;
    LinearLayout fareDetailDisplayArea;

    RatingBar ratingBar;
    String iTripId_str;

    HashMap<String, String> data_trip;
    android.support.v7.app.AlertDialog collectPaymentFailedDialog = null;

    MTextView additionalchargeHTxt, matrialfeeHTxt, miscfeeHTxt, discountHTxt;
    MaterialEditText timatrialfeeVTxt, miscfeeVTxt, discountVTxt;
    MTextView matrialfeeCurrancyTxt, miscfeeCurrancyTxt, discountCurrancyTxt;
    ImageView discounteditImgView, miseeditImgView, matrialeditImgView;
    MTextView payTypeHTxt, dateVTxt;
    MTextView totalFareTxt, cartypeTxt;

    MTextView promoAppliedVTxt, promoAppliedTxt;
    MTextView walletNoteTxt;
    JSONObject userProfileJsonObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect_payment);


        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        userProfileJsonObj = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));


        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        loading = (ProgressBar) findViewById(R.id.loading);
        errorView = (ErrorView) findViewById(R.id.errorView);
        editCommentImgView = (ImageView) findViewById(R.id.editCommentImgView);
        btn_type2 = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();
        commentBox = (MTextView) findViewById(R.id.commentBox);
        generalCommentTxt = (MTextView) findViewById(R.id.generalCommentTxt);
        container = (LinearLayout) findViewById(R.id.container);
        fareDetailDisplayArea = (LinearLayout) findViewById(R.id.fareDetailDisplayArea);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        payTypeHTxt = (MTextView) findViewById(R.id.payTypeHTxt);
        dateVTxt = (MTextView) findViewById(R.id.dateVTxt);
        promoAppliedVTxt = (MTextView) findViewById(R.id.promoAppliedVTxt);
        walletNoteTxt = (MTextView) findViewById(R.id.walletNoteTxt);

        additionalchargeHTxt = (MTextView) findViewById(R.id.additionalchargeHTxt);
        matrialfeeHTxt = (MTextView) findViewById(R.id.matrialfeeHTxt);
        miscfeeHTxt = (MTextView) findViewById(R.id.miscfeeHTxt);
        discountHTxt = (MTextView) findViewById(R.id.discountHTxt);

        timatrialfeeVTxt = (MaterialEditText) findViewById(R.id.timatrialfeeVTxt);
        miscfeeVTxt = (MaterialEditText) findViewById(R.id.miscfeeVTxt);
        discountVTxt = (MaterialEditText) findViewById(R.id.discountVTxt);

        matrialfeeCurrancyTxt = (MTextView) findViewById(R.id.matrialfeeCurrancyTxt);
        miscfeeCurrancyTxt = (MTextView) findViewById(R.id.miscfeeCurrancyTxt);
        discountCurrancyTxt = (MTextView) findViewById(R.id.discountCurrancyTxt);

        discounteditImgView = (ImageView) findViewById(R.id.discounteditImgView);
        miseeditImgView = (ImageView) findViewById(R.id.miseeditImgView);
        matrialeditImgView = (ImageView) findViewById(R.id.matrialeditImgView);
        cartypeTxt = (MTextView) findViewById(R.id.cartypeTxt);

        totalFareTxt = (MTextView) findViewById(R.id.totalFareTxt);

        discounteditImgView.setOnClickListener(new setOnClickList());
        miscfeeCurrancyTxt.setOnClickListener(new setOnClickList());
        discountCurrancyTxt.setOnClickListener(new setOnClickList());


        timatrialfeeVTxt.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
        // timatrialfeeVTxt.setImeOptions(EditorInfo.IME_ACTION_DONE);

        miscfeeVTxt.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
        // miscfeeVTxt.setImeOptions(EditorInfo.IME_ACTION_DONE);

        discountVTxt.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
        // discountVTxt.setImeOptions(EditorInfo.IME_ACTION_DONE);

        discountVTxt.setShowClearButton(false);
        miscfeeVTxt.setShowClearButton(false);
        timatrialfeeVTxt.setShowClearButton(false);

        discountVTxt.addTextChangedListener(new setOnAddTextListner());
        miscfeeVTxt.addTextChangedListener(new setOnAddTextListner());
        timatrialfeeVTxt.addTextChangedListener(new setOnAddTextListner());

//        discountVTxt.setEnabled(false);
//        miscfeeVTxt.setEnabled(false);
//        timatrialfeeVTxt.setEnabled(false);


        submitBtnId = Utils.generateViewId();
        btn_type2.setId(submitBtnId);

        btn_type2.setOnClickListener(new setOnClickList());
        editCommentImgView.setOnClickListener(new setOnClickList());
        backImgView.setVisibility(View.GONE);
        setLabels();

        getFare();

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) titleTxt.getLayoutParams();
        params.setMargins(Utils.dipToPixels(getActContext(), 15), 0, 0, 0);
        titleTxt.setLayoutParams(params);

        data_trip = (HashMap<String, String>) getIntent().getSerializableExtra("TRIP_DATA");

        if (savedInstanceState != null) {
            // Restore value of members from saved state
            String restratValue_str = savedInstanceState.getString("RESTART_STATE");

            if (restratValue_str != null && !restratValue_str.equals("") && restratValue_str.trim().equals("true")) {
                generalFunc.restartApp();
            }
        }

        GetLocationUpdates.getInstance().setTripStartValue(false, false, "");

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putString("RESTART_STATE", "true");
        super.onSaveInstanceState(outState);
    }

    public Context getActContext() {
        return CollectPaymentActivity.this;
    }

    public void setLabels() {
        titleTxt.setText(generalFunc.retrieveLangLBl("Your Trip", "LBL_PAY_SUMMARY"));
        commentBox.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_COMMENT_TXT"));
        promoAppliedVTxt.setText(generalFunc.retrieveLangLBl("", "LBL_DIS_APPLIED"));
        btn_type2.setText(generalFunc.retrieveLangLBl("COLLECT PAYMENT", "LBL_COLLECT_PAYMENT"));
        ((MTextView) findViewById(R.id.detailsTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_DETAILS"));


        additionalchargeHTxt.setText(generalFunc.retrieveLangLBl("ADDITIONAL CHARGES", "LBL_ADDITONAL_CHARGE_HINT"));
        matrialfeeHTxt.setText(generalFunc.retrieveLangLBl("Material fee", "LBL_MATERIAL_FEE"));
        miscfeeHTxt.setText(generalFunc.retrieveLangLBl("Misc fee", "LBL_MISC_FEE"));
        discountHTxt.setText(generalFunc.retrieveLangLBl("Provider Discount", "LBL_PROVIDER_DISCOUNT"));
        payTypeHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_PAYMENT_TYPE_TXT") + " : ");
        dateVTxt.setText(generalFunc.retrieveLangLBl("", "LBL_MYTRIP_Trip_Date"));
        totalFareTxt.setText(generalFunc.retrieveLangLBl("", "LBL_Total_Fare"));
        discountVTxt.setText("0.0");
        miscfeeVTxt.setText("0.0");
        timatrialfeeVTxt.setText("0.0");


    }

    public void showCommentBox() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());
        builder.setTitle(generalFunc.retrieveLangLBl("", "LBL_ADD_COMMENT_HEADER_TXT"));

        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.input_box_view, null);
        builder.setView(dialogView);

        final MaterialEditText input = (MaterialEditText) dialogView.findViewById(R.id.editBox);

        input.setSingleLine(false);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setMaxLines(5);
        if (!appliedComment.equals("")) {
            input.setText(appliedComment);
        }
        builder.setPositiveButton("OK", (dialog, which) -> {
            if (Utils.getText(input).trim().equals("") && appliedComment.equals("")) {
                generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_ENTER_PROMO"));
            } else if (Utils.getText(input).trim().equals("") && !appliedComment.equals("")) {
                appliedComment = "";
                commentBox.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_COMMENT_TXT"));
                generalFunc.showGeneralMessage("", "Your comment has been removed.");
            } else {
                appliedComment = Utils.getText(input);
                commentBox.setText(appliedComment);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public void getFare() {
        if (errorView.getVisibility() == View.VISIBLE) {
            errorView.setVisibility(View.GONE);
        }
        if (container.getVisibility() == View.VISIBLE) {
            container.setVisibility(View.GONE);
        }
        if (loading.getVisibility() != View.VISIBLE) {
            loading.setVisibility(View.VISIBLE);
        }

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "displayFare");
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("UserType", Utils.app_type);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObj = generalFunc.getJsonObject(responseString);

            if (responseStringObj != null && !responseStringObj.equals("")) {

                closeLoader();


                if (GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObj)) {

                    String message = generalFunc.getJsonValueStr(Utils.message_str, responseStringObj);

                    String SYSTEM_PAYMENT_FLOW = generalFunc.getJsonValue("SYSTEM_PAYMENT_FLOW", message);

                    String FormattedTripDate = generalFunc.getJsonValue("tTripRequestDateOrig", message);
                    String FareSubTotal = generalFunc.getJsonValue("FareSubTotal", message);
                    String eCancelled = generalFunc.getJsonValue("eCancelled", message);
                    String vCancelReason = generalFunc.getJsonValue("vCancelReason", message);
                    String vTripPaymentMode = generalFunc.getJsonValue("vTripPaymentMode", message);
                    String fDiscount = generalFunc.getJsonValue("fDiscount", message);
                    String CurrencySymbol = generalFunc.getJsonValue("CurrencySymbol", message);
                    String PaymentPerson = generalFunc.getJsonValue("PaymentPerson", message);
                    String ePaymentBy = generalFunc.getJsonValue("ePaymentBy", message);
                    String button_lbl = generalFunc.getJsonValue("OutstandingLabel", message);
                    String OutstandingDescDriver = generalFunc.getJsonValue("OutstandingDescDriver", message);

                    String vServiceDetailTitle = generalFunc.getJsonValue("vServiceDetailTitle", message);

                    if (!OutstandingDescDriver.equalsIgnoreCase("")) {
                        generalCommentTxt.setText(generalFunc.retrieveLangLBl("", OutstandingDescDriver));
                        generalCommentTxt.setVisibility(View.VISIBLE);
                    }

                    if (!button_lbl.equalsIgnoreCase("")) {
                        btn_type2.setText(generalFunc.retrieveLangLBl("", button_lbl));
                    }


                    cartypeTxt.setText(vServiceDetailTitle);
                    cartypeTxt.setVisibility(View.VISIBLE);

                    String iTripId = generalFunc.getJsonValue("iTripId", message);

                    iTripId_str = iTripId;

                    if (generalFunc.getJsonValue("eWalletAmtAdjusted", message).equalsIgnoreCase("Yes")) {
                        walletNoteTxt.setVisibility(View.VISIBLE);
                        walletNoteTxt.setText(generalFunc.retrieveLangLBl("", "LBL_WALLET_AMT_ADJUSTED") + " " + generalFunc.getJsonValue("fWalletAmountAdjusted", message));
                    }

                    if (!fDiscount.equals("") && !fDiscount.equals("0") && !fDiscount.equals("0.00")) {
                        ((MTextView) findViewById(R.id.promoAppliedTxt)).setText(CurrencySymbol + generalFunc.convertNumberWithRTL(fDiscount));
                        (findViewById(R.id.promoView)).setVisibility(View.VISIBLE);
                    } else {
                        ((MTextView) findViewById(R.id.promoAppliedTxt)).setText("--");
                    }


                    String collectMoneytxt = "";
                    String deductedcard = "";

                    String eType = generalFunc.getJsonValue("eType", message);

                    if (eType.equals(Utils.CabGeneralType_UberX)) {
                        dateVTxt.setText(generalFunc.retrieveLangLBl("", "LBL_JOB_REQ_DATE"));
                        collectMoneytxt = generalFunc.retrieveLangLBl("Please collect money from rider", "LBL_COLLECT_MONEY_FRM_USER");
                        deductedcard = generalFunc.retrieveLangLBl("", "LBL_DEDUCTED_USER_CARD");
                        if (!SYSTEM_PAYMENT_FLOW.equalsIgnoreCase("Method-1")) {
                            deductedcard = generalFunc.retrieveLangLBl("", "LBL_DEDUCTED_USER_WALLET");
                        }
                    } else if (eType.equals("Deliver") || eType.equals(Utils.eType_Multi_Delivery)) {
                        dateVTxt.setText(generalFunc.retrieveLangLBl("", "LBL_DELIVERY_DATE_TXT"));
                        collectMoneytxt = generalFunc.retrieveLangLBl("Please collect money from rider", "LBL_COLLECT_MONEY_FRM_RECIPIENT");

                        if (eType.equals(Utils.eType_Multi_Delivery)) {
                            if (Utils.checkText(PaymentPerson)) {
                                collectMoneytxt = generalFunc.retrieveLangLBl("Paid By", "LBL_PAID_BY_TXT") + " : " + PaymentPerson;
                            }
                            btn_type2.setText(generalFunc.retrieveLangLBl("Confirm Delivery", "LBL_CONFIRM_DELIVERY_TXT"));
                        }

                        deductedcard = generalFunc.retrieveLangLBl("", "LBL_DEDUCTED_SENDER_CARD");
                        if (!SYSTEM_PAYMENT_FLOW.equalsIgnoreCase("Method-1")) {
                            deductedcard = generalFunc.retrieveLangLBl("", "LBL_DEDUCTED_SENDER_WALLET");
                        }

                    } else {
                        dateVTxt.setText(generalFunc.retrieveLangLBl("", "LBL_TRIP_DATE_TXT"));
                        collectMoneytxt = generalFunc.retrieveLangLBl("Please collect money from rider", "LBL_COLLECT_MONEY_FRM_RIDER");
                        deductedcard = generalFunc.retrieveLangLBl("", "LBL_DEDUCTED_RIDER_CARD");
                        if (!SYSTEM_PAYMENT_FLOW.equalsIgnoreCase("Method-1")) {
                            deductedcard = generalFunc.retrieveLangLBl("", "LBL_DEDUCTED_RIDER_WALLET");
                        }
                    }

                    if (vTripPaymentMode.equals("Cash")) {
                        ((MTextView) findViewById(R.id.payTypeTxt)).setText(
                                generalFunc.retrieveLangLBl("", "LBL_CASH_TXT"));

                        String pay_str = "";
                        if (Utils.getText(generalCommentTxt).length() > 0) {
                            pay_str = generalCommentTxt.getText().toString() + "\n" + collectMoneytxt;
                        } else {
                            pay_str = collectMoneytxt;
                        }

                        if (eType.equals(Utils.eType_Multi_Delivery)) {
                            if (Utils.checkText(PaymentPerson)) {
                                pay_str = generalFunc.retrieveLangLBl("Paid By", "LBL_PAID_BY_TXT") + " : " + PaymentPerson;
                            }
                        }

                        generalCommentTxt.setText(pay_str);
                        generalCommentTxt.setVisibility(View.VISIBLE);

                    } else {


                        if (SYSTEM_PAYMENT_FLOW.equalsIgnoreCase("Method-1")) {
                            ((MTextView) findViewById(R.id.payTypeTxt)).setText(
                                    generalFunc.retrieveLangLBl("", "LBL_CARD"));
                            generalCommentTxt.setText(deductedcard);
                            generalCommentTxt.setVisibility(View.VISIBLE);
                        } else if (!SYSTEM_PAYMENT_FLOW.equalsIgnoreCase("Method-1")) {
                            ((MTextView) findViewById(R.id.payTypeTxt)).setText(
                                    generalFunc.retrieveLangLBl("Pay by Wallet", "LBL_PAY_BY_WALLET_TXT"));
                            generalCommentTxt.setText(deductedcard);
                            generalCommentTxt.setVisibility(View.VISIBLE);
                        }
                    }

                    if (ePaymentBy.equals("Organization")) {
                        ((MTextView) findViewById(R.id.payTypeTxt)).setText(
                                generalFunc.retrieveLangLBl("", "LBL_ORGANIZATION"));
                        payTypeHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_PAYMENT_BY_TXT") + " : ");
                        generalCommentTxt.setText(generalFunc.retrieveLangLBl("", "LBL_MONEY_PAID_ORGANIZATION"));
                        generalCommentTxt.setVisibility(View.VISIBLE);
                        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_NEXT_TXT"));

                    }


//                        ((MTextView) findViewById(R.id.dateTxt)).setText(generalFunc.getDateFormatedType(FormattedTripDate, Utils.OriginalDateFormate, Utils.dateFormateInList));
                    ((MTextView) findViewById(R.id.dateTxt)).setText(generalFunc.getDateFormatedType(FormattedTripDate, Utils.OriginalDateFormate, Utils.getDetailDateFormat(getActContext())));
                    ((MTextView) findViewById(R.id.fareTxt)).setText(generalFunc.convertNumberWithRTL(FareSubTotal));

                    container.setVisibility(View.VISIBLE);
                    boolean FareDetailsArrNew = generalFunc.isJSONkeyAvail("FareDetailsNewArr", responseString);

                    JSONArray FareDetailsArrNewObj = null;
                    if (FareDetailsArrNew) {
                        FareDetailsArrNewObj = generalFunc.getJsonArray("FareDetailsNewArr", responseStringObj);
                    }
                    if (FareDetailsArrNewObj != null)
                        addFareDetailLayout(FareDetailsArrNewObj);

                } else {
                    generateErrorView();
                }
            } else {
                generateErrorView();
            }
        });
        exeWebServer.execute();
    }

    private void addFareDetailLayout(JSONArray jobjArray) {

        if (fareDetailDisplayArea.getChildCount() > 0) {
            fareDetailDisplayArea.removeAllViewsInLayout();
        }

        for (int i = 0; i < jobjArray.length(); i++) {
            JSONObject jobject = generalFunc.getJsonObject(jobjArray, i);
            try {
                String data = jobject.names().getString(0);
                addFareDetailRow(data, jobject.get(data).toString(), false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private void addFareDetailRow(String row_name, String row_value, boolean isLast) {
        View convertView = null;
        if (row_name.equalsIgnoreCase("eDisplaySeperator")) {
            convertView = new View(getActContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dipToPixels(getActContext(), 1));
            params.setMarginStart(Utils.dipToPixels(getActContext(), 10));
            params.setMarginEnd(Utils.dipToPixels(getActContext(), 10));
            convertView.setBackgroundColor(Color.parseColor("#dedede"));
            convertView.setLayoutParams(params);
        } else {
            LayoutInflater infalInflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.design_fare_deatil_row, null);

            convertView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            convertView.setMinimumHeight(Utils.dipToPixels(getActContext(), 40));

            MTextView titleHTxt = (MTextView) convertView.findViewById(R.id.titleHTxt);
            MTextView titleVTxt = (MTextView) convertView.findViewById(R.id.titleVTxt);

            titleHTxt.setText(generalFunc.convertNumberWithRTL(row_name));
            titleVTxt.setText(generalFunc.convertNumberWithRTL(row_value));

            titleHTxt.setTextColor(Color.parseColor("#303030"));
            titleVTxt.setTextColor(Color.parseColor("#111111"));
        }

        if (convertView != null)
            fareDetailDisplayArea.addView(convertView);
    }

    public void collectPayment(String isCollectCash) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "CollectPayment");
        parameters.put("iTripId", iTripId_str);
        if (!isCollectCash.equals("")) {
            parameters.put("isCollectCash", isCollectCash);
        }

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObj = generalFunc.getJsonObject(responseString);

            if (responseStringObj != null && !responseStringObj.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObj);

                if (isDataAvail == true) {

                    Bundle bn = new Bundle();
                    bn.putSerializable("TRIP_DATA", data_trip);
                    try {
                        if (data_trip.get("eHailTrip").equalsIgnoreCase("Yes")) {
                            generalFunc.saveGoOnlineInfo();
                            MyApp.getInstance().restartWithGetDataApp();

                        } else if (data_trip.get("eBookingFrom").equalsIgnoreCase(Utils.eSystem_Type_KIOSK)) {
                            generalFunc.saveGoOnlineInfo();
                            MyApp.getInstance().restartWithGetDataApp();
                        } else {
                            new StartActProcess(getActContext()).startActWithData(TripRatingActivity.class, bn);
                        }
                    } catch (Exception e) {
                        new StartActProcess(getActContext()).startActWithData(TripRatingActivity.class, bn);
                    }

                } else {

                    buildPaymentCollectFailedMessage(generalFunc.retrieveLangLBl("",
                            generalFunc.getJsonValueStr(Utils.message_str, responseStringObj)), generalFunc.retrieveLangLBl("",
                            generalFunc.getJsonValueStr(Utils.message_str_one, responseStringObj)));

                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }


    public void buildPaymentCollectFailedMessage(String msg, String btnStr) {

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext(), R.style.StackedAlertDialogStyle);
        builder.setTitle("");
        builder.setCancelable(false);

        builder.setMessage(msg);

        builder.setPositiveButton(generalFunc.retrieveLangLBl("", "LBL_RETRY_TXT"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                collectPaymentFailedDialog.dismiss();
                collectPayment("");
            }
        });
        builder.setNegativeButton(btnStr, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                collectPaymentFailedDialog.dismiss();
                collectPayment("true");
            }
        });

        collectPaymentFailedDialog = builder.create();
        collectPaymentFailedDialog.setCancelable(false);
        collectPaymentFailedDialog.setCanceledOnTouchOutside(false);
        collectPaymentFailedDialog.show();
    }

    public void closeLoader() {
        if (loading.getVisibility() == View.VISIBLE) {
            loading.setVisibility(View.GONE);
        }
    }

    public void generateErrorView() {

        closeLoader();

        generalFunc.generateErrorView(errorView, "LBL_ERROR_TXT", "LBL_NO_INTERNET_TXT");

        if (errorView.getVisibility() != View.VISIBLE) {
            errorView.setVisibility(View.VISIBLE);
        }
        errorView.setOnRetryListener(() -> getFare());
    }

    @Override
    public void onBackPressed() {
        return;
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            Utils.hideKeyboard(CollectPaymentActivity.this);
            if (i == submitBtnId) {
                collectPayment("");
            } else if (i == editCommentImgView.getId()) {
                showCommentBox();
            } else if (i == discounteditImgView.getId()) {
                discountVTxt.setEnabled(true);

            } else if (i == miscfeeCurrancyTxt.getId()) {
                miscfeeVTxt.setEnabled(true);
            } else if (i == discountCurrancyTxt.getId()) {
                timatrialfeeVTxt.setEnabled(true);

            }


        }
    }


    public class setOnAddTextListner implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}
