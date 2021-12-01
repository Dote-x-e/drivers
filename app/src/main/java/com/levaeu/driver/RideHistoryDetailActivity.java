package com.levaeu.driver;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.MTextView;
import com.view.SelectableRoundedImageView;
import com.view.simpleratingbar.SimpleRatingBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class RideHistoryDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    MTextView titleTxt;
    MTextView subTitleTxt;
    ImageView backImgView;

    public GeneralFunctions generalFunc;

    GoogleMap gMap;

    LinearLayout fareDetailDisplayArea;
    private View convertView = null;

    LinearLayout beforeServiceArea, afterServiceArea;
    String before_serviceImg_url = "";
    String after_serviceImg_url = "";
    MTextView cartypeTxt;
    MTextView tipHTxt, tipamtTxt, tipmsgTxt;
    CardView tiparea;
    LinearLayout profilearea;
    ImageView tipPluseImage;
    MTextView vReasonTitleTxt;
    /*Multi Delivery Rlated fields*/
    private Dialog signatureImageDialog;
    private String senderImage;
    private View signatureArea;
    private String tripData;
    private String userProfileJson;

    MTextView viewReqServicesTxtView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_history_detail);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        profilearea = (LinearLayout) findViewById(R.id.profilearea);
        subTitleTxt = (MTextView) findViewById(R.id.subTitleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        fareDetailDisplayArea = (LinearLayout) findViewById(R.id.fareDetailDisplayArea);
        afterServiceArea = (LinearLayout) findViewById(R.id.afterServiceArea);
        beforeServiceArea = (LinearLayout) findViewById(R.id.beforeServiceArea);
        signatureArea = (LinearLayout) findViewById(R.id.signatureArea);
        tipPluseImage = (ImageView) findViewById(R.id.tipPluseImage);
        cartypeTxt = (MTextView) findViewById(R.id.cartypeTxt);
        vReasonTitleTxt = (MTextView) findViewById(R.id.vReasonTitleTxt);
        viewReqServicesTxtView = (MTextView) findViewById(R.id.viewReqServicesTxtView);

        tipHTxt = (MTextView) findViewById(R.id.tipHTxt);
        tipamtTxt = (MTextView) findViewById(R.id.tipamtTxt);
        tipmsgTxt = (MTextView) findViewById(R.id.tipmsgTxt);

        tiparea = (CardView) findViewById(R.id.tiparea);
        setLabels();
        setData();

        backImgView.setOnClickListener(new setOnClickList());
        subTitleTxt.setOnClickListener(new setOnClickList());
        afterServiceArea.setOnClickListener(new setOnClickList());
        beforeServiceArea.setOnClickListener(new setOnClickList());
        viewReqServicesTxtView.setOnClickListener(new setOnClickList());
    }

    public void setLabels() {
        /*Multi related new lable*/
        ((MTextView) findViewById(R.id.passengerSignTxt)).setText(generalFunc.retrieveLangLBl("View Signature", "LBL_VIEW_MULTI_SENDER_SIGN"));


        tripData = getIntent().getStringExtra("TripData");

        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_RECEIPT_HEADER_TXT"));
        subTitleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_GET_RECEIPT_TXT"));
        viewReqServicesTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_VIEW_REQUESTED_SERVICES"));


        String headerLable = "", noVal = "", driverhVal = "";
        boolean eFly=generalFunc.getJsonValue("eFly", tripData).equalsIgnoreCase("Yes");

        if (generalFunc.getJsonValue("eType", tripData).equals(Utils.CabGeneralType_UberX)) {
            headerLable = generalFunc.retrieveLangLBl("", "LBL_THANKS_UFX_DRIVER");
            noVal = generalFunc.retrieveLangLBl("", "LBL_SERVICES") + "#";
            driverhVal = generalFunc.retrieveLangLBl("", "LBL_USER");
        } else if (generalFunc.getJsonValue("eType", tripData).equals("Deliver") || generalFunc.getJsonValue("eType", tripData).equals(Utils.eType_Multi_Delivery)) {
            headerLable = generalFunc.retrieveLangLBl("", "LBL_THANKS_DELIVERY_DRIVER");
            noVal = generalFunc.retrieveLangLBl("", "LBL_DELIVERY") + "#";
            driverhVal = generalFunc.retrieveLangLBl("", "LBL_SENDER");
        } else {
            headerLable = generalFunc.retrieveLangLBl("", "LBL_THANKS_RIDING_DRIVER");
            noVal = generalFunc.retrieveLangLBl("", eFly?"LBL_HEADER_RDU_FLY_RIDE":"LBL_RIDE") + "#";
            driverhVal = generalFunc.retrieveLangLBl("", "LBL_PASSENGER_TXT");
        }

        ((MTextView) findViewById(R.id.headerTxt)).setText(generalFunc.retrieveLangLBl("", headerLable));

        ((MTextView) findViewById(R.id.rideNoHTxt)).setText(noVal);
        ((MTextView) findViewById(R.id.ratingDriverHTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_RATING"));
        ((MTextView) findViewById(R.id.passengerHTxt)).setText(driverhVal);
        String dateLable = "";
        String pickupHval = "";

        if (generalFunc.getJsonValue("eType", tripData).equals(Utils.CabGeneralType_UberX)) {
            dateLable = generalFunc.retrieveLangLBl("", "LBL_JOB_REQ_DATE");
            pickupHval = generalFunc.retrieveLangLBl("", "LBL_JOB_LOCATION_TXT");
            tipmsgTxt.setText(generalFunc.retrieveLangLBl("Congratulation! You got a tip from the passenger for this trip.", "LBL_TIP_INFO_SHOW_PROVIDER"));
        } else if (generalFunc.getJsonValue("eType", tripData).equals("Deliver") || generalFunc.getJsonValue("eType", tripData).equals(Utils.eType_Multi_Delivery)) {
            dateLable = generalFunc.retrieveLangLBl("", "LBL_DELIVERY_REQUEST_DATE");
            pickupHval = generalFunc.retrieveLangLBl("", "LBL_SENDER_LOCATION");
            tipmsgTxt.setText(generalFunc.retrieveLangLBl("Congratulation! You got a tip from the passenger for this trip.", "LBL_TIP_INFO_SHOW_CARRIER"));
        } else {
            dateLable = generalFunc.retrieveLangLBl("", "LBL_TRIP_REQUEST_DATE_TXT");
            pickupHval = generalFunc.retrieveLangLBl("", "LBL_PICKUP_LOCATION_TXT");
            tipmsgTxt.setText(generalFunc.retrieveLangLBl("Congratulation! You got a tip from the passenger for this trip.", "LBL_TIP_INFO_SHOW_DRIVER"));
        }

        ((MTextView) findViewById(R.id.tripdateHTxt)).setText(generalFunc.retrieveLangLBl("", dateLable));
        ((MTextView) findViewById(R.id.pickUpHTxt)).setText(pickupHval);
        if (generalFunc.getJsonValue("eType", tripData).equals("Deliver") || generalFunc.getJsonValue("eType", tripData).equals(Utils.eType_Multi_Delivery)) {
            ((MTextView) findViewById(R.id.dropOffHTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_DELIVERY_DETAILS_TXT"));
        } else if (generalFunc.getJsonValue("eType", tripData).equals(Utils.CabGeneralType_Ride)) {
            ((MTextView) findViewById(R.id.dropOffHTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_DEST_LOCATION"));
        } else {
            ((MTextView) findViewById(R.id.dropOffHTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_DEST_LOCATION"));

        }
        ((MTextView) findViewById(R.id.chargesHTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_CHARGES_TXT"));
        ((MTextView) findViewById(R.id.serviceHTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_SERVICE_TXT"));

        tipHTxt.setText(generalFunc.retrieveLangLBl("Tip Amount", "LBL_TIP_AMOUNT"));


    }

    public void setData() {
        String tripData = getIntent().getStringExtra("TripData");


        if (generalFunc.getJsonValue("vReasonTitle", tripData) != null && !generalFunc.getJsonValue("vReasonTitle", tripData).equalsIgnoreCase("")) {
            vReasonTitleTxt.setVisibility(View.VISIBLE);
            vReasonTitleTxt.setText(generalFunc.getJsonValue("vReasonTitle", tripData));
        }

        if (generalFunc.getJsonValue("eHailTrip", tripData).equalsIgnoreCase("yes")) {
            profilearea.setVisibility(View.GONE);
        } else {
            profilearea.setVisibility(View.VISIBLE);
        }

        ((MTextView) findViewById(R.id.rideNoVTxt)).setText(generalFunc.convertNumberWithRTL(generalFunc.getJsonValue("vRideNo", tripData)));
        ((MTextView) findViewById(R.id.namePassengerVTxt)).setText(generalFunc.getJsonValue("vName", tripData) + " " +
                generalFunc.getJsonValue("vLastName", tripData));
        ((MTextView) findViewById(R.id.tripdateVTxt)).setText(generalFunc.getDateFormatedType(generalFunc.getJsonValue("tTripRequestDateOrig", tripData), Utils.OriginalDateFormate, Utils.getDetailDateFormat(getActContext())));
        ((MTextView) findViewById(R.id.pickUpVTxt)).setText(generalFunc.getJsonValue("tSaddress", tripData));
        if (generalFunc.getJsonValue("eType", tripData).equals("Deliver")) {
            ((MTextView) findViewById(R.id.dropOffVTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_RECEIVER_NAME") + ": " + generalFunc.getJsonValue("vReceiverName", tripData) + "\n\n" +
                    generalFunc.retrieveLangLBl("", "LBL_RECEIVER_LOCATION") + ": " + generalFunc.getJsonValue("tDaddress", tripData) + "\n\n" +
                    generalFunc.retrieveLangLBl("", "LBL_PACKAGE_TYPE_TXT") + ": " + generalFunc.getJsonValue("PackageType", tripData) + "\n\n" +
                    generalFunc.retrieveLangLBl("", "LBL_PACKAGE_DETAILS") + ": " + generalFunc.getJsonValue("tPackageDetails", tripData)
            );
        } else {
            ((MTextView) findViewById(R.id.dropOffVTxt)).setText(generalFunc.getJsonValue("tDaddress", tripData));
        }


        cartypeTxt.setText(generalFunc.getJsonValue("vServiceDetailTitle", tripData));

        if (generalFunc.getJsonValue("tDaddress", tripData).equals("")) {
            ((MTextView) findViewById(R.id.dropOffVTxt)).setVisibility(View.GONE);
            ((MTextView) findViewById(R.id.dropOffHTxt)).setVisibility(View.GONE);
        }

        if (!generalFunc.getJsonValue("fTipPrice", tripData).equals("0") && !generalFunc.getJsonValue("fTipPrice", tripData).equals("0.0") &&
                !generalFunc.getJsonValue("fTipPrice", tripData).equals("0.00") &&
                !generalFunc.getJsonValue("fTipPrice", tripData).equals("")) {
            tiparea.setVisibility(View.VISIBLE);
            tipPluseImage.setVisibility(View.VISIBLE);

            tipamtTxt.setText(generalFunc.getJsonValue("fTipPrice", tripData));
        } else {
            tiparea.setVisibility(View.GONE);
            tipPluseImage.setVisibility(View.GONE);
        }

        String trip_status_str = generalFunc.getJsonValue("iActive", tripData);
        if (trip_status_str.contains("Canceled")) {
            String cancelLable = "";
            String cancelableReason = generalFunc.getJsonValue("vCancelReason", tripData);

            if (generalFunc.getJsonValue("eCancelledBy", tripData).equalsIgnoreCase("DRIVER")) {
                if (generalFunc.getJsonValue("eType", tripData).equals(Utils.CabGeneralType_UberX)) {
                    cancelLable = generalFunc.retrieveLangLBl("", "LBL_CANCELED_JOB");
                } else if (generalFunc.getJsonValue("eType", tripData).equals("Deliver") || generalFunc.getJsonValue("eType", tripData).equals(Utils.eType_Multi_Delivery)) {
                    cancelLable = generalFunc.retrieveLangLBl("", "LBL_CANCELED_DELIVERY_TXT");
                } else {
                    cancelLable = generalFunc.retrieveLangLBl("", "LBL_CANCELED_TRIP_TXT");
                }
            } else {
                if (generalFunc.getJsonValue("eType", tripData).equals(Utils.CabGeneralType_UberX)) {
                    cancelLable = generalFunc.retrieveLangLBl("", "LBL_USER_CANCEL_JOB_TXT");
                } else if (generalFunc.getJsonValue("eType", tripData).equals("Deliver") || generalFunc.getJsonValue("eType", tripData).equals(Utils.eType_Multi_Delivery)) {
                    cancelLable = generalFunc.retrieveLangLBl("", "LBL_SENDER_CANCEL_DELIVERY_TXT");
                } else {
                    cancelLable = generalFunc.retrieveLangLBl("", "LBL_PASSENGER_CANCEL_TRIP_TXT");
                }

            }

            ((MTextView) findViewById(R.id.tripStatusTxt)).setText(generalFunc.retrieveLangLBl("", cancelLable));
            (findViewById(R.id.tripDetailArea)).setVisibility(View.VISIBLE);

        } else if (trip_status_str.contains("Finished")) {
            String finishLable = "";
            if (generalFunc.getJsonValue("eType", tripData).equals(Utils.CabGeneralType_UberX)) {
                finishLable = generalFunc.retrieveLangLBl("", "LBL_FINISHED_JOB_TXT");
            } else if (generalFunc.getJsonValue("eType", tripData).equals("Deliver") || generalFunc.getJsonValue("eType", tripData).equals(Utils.eType_Multi_Delivery)) {
                finishLable = generalFunc.retrieveLangLBl("", "LBL_FINISHED_DELIVERY_TXT");
            } else {
                finishLable = generalFunc.retrieveLangLBl("", "LBL_FINISHED_TRIP_TXT");
            }

            ((MTextView) findViewById(R.id.tripStatusTxt)).setText(generalFunc.retrieveLangLBl("", finishLable));

            (findViewById(R.id.tripDetailArea)).setVisibility(View.VISIBLE);
            subTitleTxt.setVisibility(View.VISIBLE);
        } else {
            ((MTextView) findViewById(R.id.tripStatusTxt)).setText(trip_status_str);
        }

        if (generalFunc.getJsonValue("vTripPaymentMode", tripData).equals("Cash")) {
            ((MTextView) findViewById(R.id.paymentTypeTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_CASH_PAYMENT_TXT"));
        } else {
            ((MTextView) findViewById(R.id.paymentTypeTxt)).setText(generalFunc.retrieveLangLBl("Card Payment", "LBL_CARD_PAYMENT"));
            ((ImageView) findViewById(R.id.paymentTypeImgeView)).setImageResource(R.mipmap.ic_card_new);
        }

        if (generalFunc.getJsonValue("vTripPaymentMode", tripData).equalsIgnoreCase("Organization")) {
            ((MTextView) findViewById(R.id.paymentTypeTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_PAYMENT_BY_TXT") + " " + generalFunc.getJsonValue("OrganizationName", tripData));
            ((ImageView) findViewById(R.id.paymentTypeImgeView)).setImageResource(R.drawable.ic_business_pay);
            ((ImageView) findViewById(R.id.paymentTypeImgeView)).setColorFilter(getResources().getColor(R.color.appThemeColor_1), PorterDuff.Mode.SRC_IN);
        }

        if (generalFunc.getJsonValue("ePayWallet", tripData).equals("Yes")) {
            ((MTextView) findViewById(R.id.paymentTypeTxt)).setText(generalFunc.retrieveLangLBl("Paid By Wallet", "LBL_PAID_VIA_WALLET"));
            ((ImageView) findViewById(R.id.paymentTypeImgeView)).setImageResource(R.mipmap.ic_menu_wallet);
        }


        if (generalFunc.getJsonValue("eCancelled", tripData).equals("Yes")) {
            subTitleTxt.setVisibility(View.GONE);
            String cancelledLable = "";
            String cancelableReason = generalFunc.getJsonValue("vCancelReason", tripData);

            if (generalFunc.getJsonValue("eCancelledBy", tripData).equalsIgnoreCase("DRIVER")) {
                if (generalFunc.getJsonValue("eType", tripData).equals(Utils.CabGeneralType_UberX)) {
                    cancelledLable = generalFunc.retrieveLangLBl("", "LBL_PREFIX_JOB_CANCEL_YOU") + " " + cancelableReason;
                } else if (generalFunc.getJsonValue("eType", tripData).equals("Deliver") || generalFunc.getJsonValue("eType", tripData).equals(Utils.eType_Multi_Delivery)) {
                    cancelledLable = generalFunc.retrieveLangLBl("", "LBL_PREFIX_DELIVERY_CANCEL_YOU") + " " + cancelableReason;
                } else {
                    cancelledLable = generalFunc.retrieveLangLBl("", "LBL_PREFIX_TRIP_CANCEL_YOU") + " " + cancelableReason;
                }
            } else {
                if (generalFunc.getJsonValue("eType", tripData).equals(Utils.CabGeneralType_UberX)) {
                    cancelledLable = generalFunc.retrieveLangLBl("", "LBL_USER_CANCEL_JOB_TXT");
                } else if (generalFunc.getJsonValue("eType", tripData).equals("Deliver") || generalFunc.getJsonValue("eType", tripData).equals(Utils.eType_Multi_Delivery)) {
                    cancelledLable = generalFunc.retrieveLangLBl("", "LBL_SENDER_CANCEL_DELIVERY_TXT");
                } else {
                    cancelledLable = generalFunc.retrieveLangLBl("", "LBL_PASSENGER_CANCEL_TRIP_TXT");
                }
            }

            ((MTextView) findViewById(R.id.tripStatusTxt)).setText(generalFunc.retrieveLangLBl("", cancelledLable));


        }

        ((SimpleRatingBar) findViewById(R.id.ratingBar)).setRating(generalFunc.parseFloatValue(0, generalFunc.getJsonValue("TripRating", tripData)));

        String driverImageName = generalFunc.getJsonValue("vImage", tripData);
        final ImageView profilebackImage = (ImageView) findViewById(R.id.profileimageback);
        final ImageView driverImageview = (SelectableRoundedImageView) findViewById(R.id.driverImgView);

        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                if (profilebackImage != null) {
                    Utils.setBlurImage(bitmap, profilebackImage);
                }
                driverImageview.setImageBitmap(bitmap);
            }

            @Override
            public void onBitmapFailed(Exception e,Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };


//        if (driverImageName == null || driverImageName.equals("") || driverImageName.equals("NONE")) {
//            ((SelectableRoundedImageView) findViewById(R.id.driverImgView)).setImageResource(R.mipmap.ic_no_pic_user);
//        } else {
        driverImageview.setTag(target);
        String image_url = CommonUtilities.USER_PHOTO_PATH + generalFunc.getJsonValue("iUserId", tripData) + "/"
                + driverImageName;
        Picasso.get()
                .load(image_url)
                .placeholder(R.mipmap.ic_no_pic_user)
                .error(R.mipmap.ic_no_pic_user)
                .into(target);
        // }


        if (generalFunc.getJsonValue("eType", tripData).equalsIgnoreCase("UberX") && generalFunc.getJsonValue("SERVICE_PROVIDER_FLOW", userProfileJson).equalsIgnoreCase("Provider")) {
            viewReqServicesTxtView.setVisibility(View.VISIBLE);
        }

        if (generalFunc.getJsonValue("eType", tripData).equalsIgnoreCase("UberX") || generalFunc.getJsonValue("eFareType", tripData).equalsIgnoreCase("Fixed")) {
            findViewById(R.id.card_service_area).setVisibility(View.VISIBLE);
            findViewById(R.id.serviceHTxt).setVisibility(View.GONE);
            findViewById(R.id.photoArea).setVisibility(View.VISIBLE);

            ((MTextView) findViewById(R.id.beforeImgHTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_BEFORE_SERVICE"));
            ((MTextView) findViewById(R.id.afterImgHTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_AFTER_SERVICE"));

            if (!TextUtils.isEmpty(generalFunc.getJsonValue("vBeforeImage", tripData))) {
                findViewById(R.id.beforeServiceArea).setVisibility(View.VISIBLE);
                before_serviceImg_url = generalFunc.getJsonValue("vBeforeImage", tripData);

                String vBeforeImage = Utils.getResizeImgURL(getActContext(), before_serviceImg_url, getResources().getDimensionPixelSize(R.dimen.before_after_img_size), getResources().getDimensionPixelSize(R.dimen.before_after_img_size));

                displayPic(vBeforeImage, (ImageView) findViewById(R.id.iv_before_img), "before");

                findViewById(R.id.iv_before_img).setOnClickListener(v -> (new StartActProcess(getActContext())).openURL(before_serviceImg_url));
            } else {
                findViewById(R.id.beforeServiceArea).setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(generalFunc.getJsonValue("vAfterImage", tripData))) {
                findViewById(R.id.afterServiceArea).setVisibility(View.VISIBLE);
                after_serviceImg_url = generalFunc.getJsonValue("vAfterImage", tripData);
                String vAfterImage = Utils.getResizeImgURL(getActContext(), after_serviceImg_url, getResources().getDimensionPixelSize(R.dimen.before_after_img_size), getResources().getDimensionPixelSize(R.dimen.before_after_img_size));
                displayPic(vAfterImage, (ImageView) findViewById(R.id.iv_after_img), "after");
                findViewById(R.id.iv_after_img).setOnClickListener(v -> (new StartActProcess(getActContext())).openURL(after_serviceImg_url));
            } else {
                findViewById(R.id.afterServiceArea).setVisibility(View.GONE);
            }

            if (TextUtils.isEmpty(generalFunc.getJsonValue("vBeforeImage", tripData)) && TextUtils.isEmpty(generalFunc.getJsonValue("vAfterImage", tripData))) {
                findViewById(R.id.photoArea).setVisibility(View.GONE);
            }


            ((MTextView) findViewById(R.id.pickUpVTxt)).setText(generalFunc.getJsonValue("tSaddress", tripData));

           /* if (generalFunc.getJsonValue("SERVICE_PROVIDER_FLOW", userProfileJson).equalsIgnoreCase("Provider") &&
                    generalFunc.getJsonValue("eFareType", tripData).equalsIgnoreCase("Fixed")) {
                ((MTextView) findViewById(R.id.serviceTypeVTxt)).setText(generalFunc.getJsonValue("vVehicleCategory", tripData));
            } else {
                ((MTextView) findViewById(R.id.serviceTypeVTxt)).setText(generalFunc.getJsonValue("vVehicleCategory", tripData) + " - " + generalFunc.getJsonValue("vVehicleType", tripData));
            }

            ((MTextView) findViewById(R.id.serviceTypeHTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_Car_Type"));*/


        } else {
            findViewById(R.id.tripDetailArea).setVisibility(View.VISIBLE);
            findViewById(R.id.service_area).setVisibility(View.GONE);
            findViewById(R.id.card_service_area).setVisibility(View.GONE);
            findViewById(R.id.serviceHTxt).setVisibility(View.GONE);
            findViewById(R.id.photoArea).setVisibility(View.GONE);
        }


        /*Show Multi Delivery Details*/
        if (generalFunc.getJsonValue("eType", tripData).equals(Utils.eType_Multi_Delivery)) {
            (findViewById(R.id.tripDetailArea)).setVisibility(View.VISIBLE);
            (findViewById(R.id.dropOffVTxt)).setVisibility(View.GONE);
            getTripDeliveryLocations(tripData);

            if (trip_status_str.contains("Canceled") ) {
              /*  (findViewById(R.id.tripDetailArea)).setVisibility(View.GONE);
                (findViewById(R.id.dropOffHTxt)).setVisibility(View.GONE);
                (findViewById(R.id.dropOffVTxt)).setVisibility(View.GONE);*/
            }
        }


        boolean FareDetailsArrNew = generalFunc.isJSONkeyAvail("HistoryFareDetailsNewArr", tripData);

        JSONArray FareDetailsArrNewObj = null;
        if (FareDetailsArrNew) {
            FareDetailsArrNewObj = generalFunc.getJsonArray("HistoryFareDetailsNewArr", tripData);
        }
        if (FareDetailsArrNewObj != null)
            addFareDetailLayout(FareDetailsArrNewObj);

        subTitleTxt.setVisibility(View.GONE);
    }

    /*Start of set delivery details*/
    public void getTripDeliveryLocations(String tripData) {

        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getTripDeliveryDetails");
        parameters.put("iTripId", generalFunc.getJsonValue("iTripId", tripData));
        parameters.put("iCabBookingId", "");
        parameters.put("userType", Utils.app_type);

        final ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(this, parameters);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObject = generalFunc.getJsonObject(responseString);

            if (responseStringObject != null && !responseStringObject.equals("")) {

                String msg_str = generalFunc.getJsonValueStr(Utils.message_str, responseStringObject);

                if (GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObject)) {

                    String paymentDoneByDetail = generalFunc.getJsonValueStr("PaymentPerson", responseStringObject);

                    if (Utils.checkText(paymentDoneByDetail)) {
                        if (generalFunc.getJsonValue("ePayWallet", tripData).equals("Yes")) {
                            ((MTextView) findViewById(R.id.paymentTypeTxt)).setText(generalFunc.retrieveLangLBl("Paid By Wallet", "LBL_PAY_BY_WALLET_TXT"));
                            ((ImageView) findViewById(R.id.paymentTypeImgeView)).setImageResource(R.mipmap.ic_menu_wallet);
                        }

                        ((MTextView) findViewById(R.id.paymentTypeTxt)).append(" " + generalFunc.retrieveLangLBl("Paid By", "LBL_PAID_BY_TXT") + " " + paymentDoneByDetail);

                    }

                    if (Utils.checkText(msg_str)) {
                        JSONObject jobject = generalFunc.getJsonObject("MemberDetails", msg_str);

                        if (jobject != null) {
                            senderImage = generalFunc.getJsonValue("Sender_Signature", jobject.toString());
                        }

                        JSONArray tripLocations = generalFunc.getJsonArray("Deliveries", msg_str);
                        if (tripLocations != null) {

                            String LBL_RECIPIENT = "",LBL_Status="",LBL_CANCELED_TRIP_TXT="",LBL_FINISHED_TXT="",LBL_DROP_OFF_LOCATION_TXT="",LBL_MULTI_AMOUNT_COLLECT_TXT="",LBL_PICK_UP_INS="",LBL_DELIVERY_INS="",LBL_PACKAGE_DETAILS="",LBL_CALL_TXT="",LBL_VIEW_SIGN_TXT="",LBL_MESSAGE_ACTIVE_TRIP="",LBL_RESPONSIBLE_FOR_PAYMENT_TXT="",LBL_DELIVERY_STATUS_TXT="";

                            if (tripLocations.length() > 0) {
                                LBL_RECIPIENT = generalFunc.retrieveLangLBl("", "LBL_RECIPIENT");
                                LBL_Status = generalFunc.retrieveLangLBl("", "LBL_Status");
                                LBL_CANCELED_TRIP_TXT = generalFunc.retrieveLangLBl("", "LBL_CANCELED_TRIP_TXT");
                                LBL_FINISHED_TXT = generalFunc.retrieveLangLBl("", "LBL_FINISHED_TXT");
                                LBL_DROP_OFF_LOCATION_TXT = generalFunc.retrieveLangLBl("", "LBL_DROP_OFF_LOCATION_TXT");
                                LBL_MULTI_AMOUNT_COLLECT_TXT = generalFunc.retrieveLangLBl("", "LBL_MULTI_AMOUNT_COLLECT_TXT");
                                LBL_PICK_UP_INS = generalFunc.retrieveLangLBl("", "LBL_PICK_UP_INS");
                                LBL_DELIVERY_INS = generalFunc.retrieveLangLBl("", "LBL_DELIVERY_INS");
                                LBL_PACKAGE_DETAILS = generalFunc.retrieveLangLBl("", "LBL_PACKAGE_DETAILS");
                                LBL_CALL_TXT = generalFunc.retrieveLangLBl("", "LBL_CALL_TXT");
                                LBL_VIEW_SIGN_TXT = generalFunc.retrieveLangLBl("", "LBL_VIEW_SIGN_TXT");
                                LBL_MESSAGE_ACTIVE_TRIP = generalFunc.retrieveLangLBl("", LBL_MESSAGE_ACTIVE_TRIP);
                                LBL_RESPONSIBLE_FOR_PAYMENT_TXT=generalFunc.retrieveLangLBl("Responsible for payment","LBL_RESPONSIBLE_FOR_PAYMENT_TXT");
                                LBL_DELIVERY_STATUS_TXT = generalFunc.retrieveLangLBl("", LBL_DELIVERY_STATUS_TXT);
                            }


                            for (int i = 0; i < tripLocations.length(); i++) {

                                JSONArray jsonArray1 = generalFunc.getJsonArray(tripLocations, i);



                                for (int j = 0; j < jsonArray1.length(); j++) {
                                    JSONObject jobject1 = generalFunc.getJsonObject(jsonArray1, j);


                                    String vValue=generalFunc.getJsonValueStr("vValue", jobject1);
                                    String vFieldName = generalFunc.getJsonValueStr("vFieldName", jobject1);




                                    if (vFieldName.equalsIgnoreCase("Recepient Name") || (generalFunc.getJsonValueStr("iDeliveryFieldId", jobject1).equalsIgnoreCase("2"))) {

                                    } else if (vFieldName.equalsIgnoreCase("Mobile Number") || (generalFunc.getJsonValueStr("iDeliveryFieldId", jobject1).equalsIgnoreCase("3"))) {


                                    } else if (vFieldName.equalsIgnoreCase("Address")) {


                                    }



                                    if ((!vFieldName.equalsIgnoreCase("Mobile Number") && !(generalFunc.getJsonValueStr("iDeliveryFieldId", jobject1).equalsIgnoreCase("3"))) && (!vFieldName.equalsIgnoreCase("Recepient Name") && !(generalFunc.getJsonValueStr("iDeliveryFieldId", jobject1).equalsIgnoreCase("2")))/*&& Utils.checkText(generalFunc.getJsonValue("vValue", jobject1))*/) {

                                    }
                                }

                                String status = getIntent().hasExtra("Status") ? getIntent().getStringExtra("Status") : "";

                                if (status.equalsIgnoreCase("activeTrip")) {

                                } else {

                                }
                                if (status.equalsIgnoreCase("cabRequestScreen")) {

                                } else {

                                }


                                setRecyclerView();
                            }
                        }
                    } else {
                        generalFunc.showGeneralMessage(generalFunc.retrieveLangLBl("Error", "LBL_ERROR_TXT"),
                                generalFunc.retrieveLangLBl("", msg_str));

                    }
                }
            }
        });
        exeWebServer.execute();

    }

    private void setRecyclerView() {

        if (((LinearLayout) findViewById(R.id.deliveryArea)).getChildCount() > 0) {
            ((LinearLayout) findViewById(R.id.deliveryArea)).removeAllViewsInLayout();
        }

        if (Utils.checkText(senderImage)) {
            signatureArea.setVisibility(View.VISIBLE);
            signatureArea.setOnClickListener(new setOnClickList());
        }



        ((LinearLayout) findViewById(R.id.deliveryArea)).setVisibility(View.VISIBLE);

        findViewById(R.id.deliveryItemListRecycleview).setVisibility(View.GONE);

    }


    public void showSignatureImage(String Name, String image_url, boolean isSender) {
        signatureImageDialog = new Dialog(getActContext(), R.style.Theme_Dialog);
        signatureImageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        signatureImageDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        signatureImageDialog.setContentView(R.layout.multi_show_sign_design);

        final ProgressBar LoadingProgressBar = ((ProgressBar) signatureImageDialog.findViewById(R.id.LoadingProgressBar));

        ((MTextView) signatureImageDialog.findViewById(R.id.nameTxt)).setText(" " + Name);

        if (isSender) {
            ((MTextView) signatureImageDialog.findViewById(R.id.passengerDTxt)).setText(generalFunc.retrieveLangLBl("Sender Signature", "LBL_SENDER_SIGN"));
            ((MTextView) signatureImageDialog.findViewById(R.id.nameTxt)).setVisibility(View.GONE);

        } else {
            ((MTextView) signatureImageDialog.findViewById(R.id.passengerDTxt)).setText(generalFunc.retrieveLangLBl("Receiver Signature", "LBL_RECEIVER_SIGN"));
            ((MTextView) signatureImageDialog.findViewById(R.id.nameTxt)).setVisibility(View.VISIBLE);

        }

        if (Utils.checkText(image_url)) {

            Picasso.get()
                    .load(image_url)
                    .into(((ImageView) signatureImageDialog.findViewById(R.id.passengerImgView)), new Callback() {
                        @Override
                        public void onSuccess() {
                            LoadingProgressBar.setVisibility(View.GONE);
                            ((ImageView) signatureImageDialog.findViewById(R.id.passengerImgView)).setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(Exception e) {
                            LoadingProgressBar.setVisibility(View.VISIBLE);
                            ((ImageView) signatureImageDialog.findViewById(R.id.passengerImgView)).setVisibility(View.GONE);

                        }
                    });
        } else {
            LoadingProgressBar.setVisibility(View.VISIBLE);
            ((ImageView) signatureImageDialog.findViewById(R.id.passengerImgView)).setVisibility(View.GONE);

        }
        (signatureImageDialog.findViewById(R.id.closeImg)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (signatureImageDialog != null) {
                    signatureImageDialog.dismiss();
                }
            }
        });

        signatureImageDialog.setCancelable(false);
        signatureImageDialog.setCanceledOnTouchOutside(false);

        if (generalFunc.isRTLmode()) {
            generalFunc.forceRTLIfSupported(signatureImageDialog);
        }
        signatureImageDialog.show();

    }

    /*End of set delivery details*/

    public void displayPic(String image_url, ImageView view, final String imgType) {

        Picasso.get()
                .load(image_url)
                .placeholder(R.mipmap.ic_no_icon)
                .into(view, new Callback() {
                    @Override
                    public void onSuccess() {
                        if (imgType.equalsIgnoreCase("before")) {
                            findViewById(R.id.before_loading).setVisibility(View.GONE);
                            findViewById(R.id.iv_before_img).setVisibility(View.VISIBLE);
                        } else if (imgType.equalsIgnoreCase("after")) {
                            findViewById(R.id.after_loading).setVisibility(View.GONE);
                            findViewById(R.id.iv_after_img).setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        if (imgType.equalsIgnoreCase("before")) {
                            findViewById(R.id.before_loading).setVisibility(View.VISIBLE);
                            findViewById(R.id.iv_before_img).setVisibility(View.GONE);
                        } else if (imgType.equalsIgnoreCase("after")) {
                            findViewById(R.id.after_loading).setVisibility(View.VISIBLE);
                            findViewById(R.id.iv_after_img).setVisibility(View.GONE);
                        }
                    }
                });

    }

    private void addFareDetailLayout(JSONArray jobjArray) {

        if (fareDetailDisplayArea.getChildCount() > 0) {
            fareDetailDisplayArea.removeAllViewsInLayout();
        }

        for (int i = 0; i < jobjArray.length(); i++) {
            JSONObject jobject = generalFunc.getJsonObject(jobjArray, i);
            try {
                String data = jobject.names().getString(0);

                addFareDetailRow(data, jobject.get(data).toString(), jobjArray.length() - 1 == i ? true : false);
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
            convertView.setPaddingRelative(Utils.dipToPixels(getActContext(), 10), 0, Utils.dipToPixels(getActContext(), 10), 0);

            convertView.setMinimumHeight(Utils.dipToPixels(getActContext(), 40));

            MTextView titleHTxt = (MTextView) convertView.findViewById(R.id.titleHTxt);
            MTextView titleVTxt = (MTextView) convertView.findViewById(R.id.titleVTxt);

            titleHTxt.setText(generalFunc.convertNumberWithRTL(row_name));
            titleVTxt.setText(generalFunc.convertNumberWithRTL(row_value));

            titleHTxt.setTextColor(Color.parseColor("#303030"));
            titleVTxt.setTextColor(Color.parseColor("#111111"));
        }

        fareDetailDisplayArea.addView(convertView);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.gMap = googleMap;

        String tripData = getIntent().getStringExtra("TripData");

        String tStartLat = generalFunc.getJsonValue("tStartLat", tripData);
        String tStartLong = generalFunc.getJsonValue("tStartLong", tripData);
        String tEndLat = generalFunc.getJsonValue("tEndLat", tripData);
        String tEndLong = generalFunc.getJsonValue("tEndLong", tripData);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        Marker pickUpMarker = null;
        Marker destMarker = null;
        if (!tStartLat.equals("") && !tStartLat.equals("0.0") && !tStartLong.equals("") && !tStartLong.equals("0.0")) {
            LatLng pickUpLoc = new LatLng(GeneralFunctions.parseDoubleValue(0.0, tStartLat), GeneralFunctions.parseDoubleValue(0.0, tStartLong));
            MarkerOptions marker_opt = new MarkerOptions().position(pickUpLoc);
            marker_opt.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_source_marker)).anchor(0.5f, 0.5f);
            pickUpMarker = this.gMap.addMarker(marker_opt);

            builder.include(pickUpLoc);

            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickUpLoc, Utils.defaultZomLevel));
        }

        if (generalFunc.getJsonValue("iActive", tripData).equals("Finished")) {
            if (!tEndLat.equals("") && !tEndLat.equals("0.0") && !tEndLong.equals("") && !tEndLong.equals("0.0")) {
                LatLng destLoc = new LatLng(GeneralFunctions.parseDoubleValue(0.0, tEndLat), GeneralFunctions.parseDoubleValue(0.0, tEndLong));
                MarkerOptions marker_opt = new MarkerOptions().position(destLoc);
                marker_opt.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_dest_marker)).anchor(0.5f, 0.5f);
                destMarker = this.gMap.addMarker(marker_opt);

                builder.include(destLoc);

                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destLoc, 10));
            }
        }
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;

        gMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), width, Utils.dipToPixels(getActContext(), 200), 100));
        gMap.setOnMarkerClickListener(marker -> {

            marker.hideInfoWindow();
            return true;
        });

        if (pickUpMarker != null && destMarker != null) {
            drawRoute(pickUpMarker.getPosition(), destMarker.getPosition());
        }

    }

    public void drawRoute(LatLng pickUpLoc, LatLng destinationLoc) {
        HashMap<String,String> data=new HashMap<>();
        data.put(Utils.GOOGLE_SERVER_ANDROID_DRIVER_APP_KEY,"");
        data.put(Utils.GOOGLE_MAP_LANGUAGE_CODE_KEY,"");
        data=generalFunc.retrieveValue(data);

        String originLoc = pickUpLoc.latitude + "," + pickUpLoc.longitude;
        String destLoc = destinationLoc.latitude + "," + destinationLoc.longitude;
        String serverKey = data.get(Utils.GOOGLE_SERVER_ANDROID_DRIVER_APP_KEY);
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + originLoc + "&destination=" + destLoc + "&sensor=true&key=" + serverKey + "&language=" + data.get(Utils.GOOGLE_MAP_LANGUAGE_CODE_KEY) + "&sensor=true";

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), url, true);

        exeWebServer.setDataResponseListener(responseString -> {

            if (responseString != null && !responseString.equals("")) {

                String status = generalFunc.getJsonValue("status", responseString);

                if (status.equals("OK")) {

                    JSONArray obj_routes = generalFunc.getJsonArray("routes", responseString);
                    if (obj_routes != null && obj_routes.length() > 0) {

                        PolylineOptions lineOptions = generalFunc.getGoogleRouteOptions(responseString, Utils.dipToPixels(getActContext(), 5), getActContext().getResources().getColor(R.color.black));

                        if (lineOptions != null) {
                            gMap.addPolyline(lineOptions);
                        }
                    }

                }

            }
        });
        exeWebServer.execute();
    }

    public void sendReceipt() {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getReceipt");
        parameters.put("UserType", Utils.app_type);
        parameters.put("iTripId", generalFunc.getJsonValue("iTripId", getIntent().getStringExtra("TripData")));

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObj = generalFunc.getJsonObject(responseString);

            if (responseStringObj != null && !responseStringObj.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObj);

                if (isDataAvail) {
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObj)));
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

    public Context getActContext() {
        return RideHistoryDetailActivity.this;
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(RideHistoryDetailActivity.this);
            switch (view.getId()) {
                case R.id.backImgView:
                    RideHistoryDetailActivity.super.onBackPressed();
                    break;
                case R.id.viewReqServicesTxtView:
                    Bundle bundle = new Bundle();
                    bundle.putString("iTripId", generalFunc.getJsonValue("iTripId", tripData));

                    break;
                case R.id.subTitleTxt:
                    sendReceipt();
                    break;
                case R.id.signatureArea:
                    showSignatureImage(generalFunc.getJsonValue("vName", tripData) + " " +
                            generalFunc.getJsonValue("vLastName", tripData), senderImage, true);
                    break;
            }
        }
    }
}
