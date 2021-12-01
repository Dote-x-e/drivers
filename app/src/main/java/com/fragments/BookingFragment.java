package com.fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import com.adapter.files.CustSpinnerAdapter;
import com.adapter.files.MyBookingsRecycleAdapter;
import com.levaeu.driver.HistoryActivity;
import com.levaeu.driver.R;
import com.general.files.CancelTripDialog;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.GetLocationUpdates;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.utils.CommonUtilities;
import com.utils.Logger;
import com.utils.Utils;
import com.view.ErrorView;
import com.view.GenerateAlertBox;
import com.view.MTextView;
import com.view.editBox.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class BookingFragment extends Fragment implements MyBookingsRecycleAdapter.OnItemClickListener {


    View view;

    ProgressBar loading_my_bookings;
    MTextView noRidesTxt;

    RecyclerView myBookingsRecyclerView;
    ErrorView errorView;

    MyBookingsRecycleAdapter myBookingsRecyclerAdapter;

    ArrayList<HashMap<String, String>> list;

    boolean mIsLoading = false;
    boolean isNextPageAvailable = false;

    String next_page_str = "";

    GeneralFunctions generalFunc;

    HistoryActivity myBookingAct;
    String type = "";
    JSONObject userProfileJsonObj;
    String APP_TYPE = "";
    ArrayList<HashMap<String, String>> filterlist;
    AlertDialog dialog_declineOrder;
    String selectedItemId = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_booking, container, false);

        loading_my_bookings = (ProgressBar) view.findViewById(R.id.loading_my_bookings);
        noRidesTxt = (MTextView) view.findViewById(R.id.noRidesTxt);
        myBookingsRecyclerView = (RecyclerView) view.findViewById(R.id.myBookingsRecyclerView);
        errorView = (ErrorView) view.findViewById(R.id.errorView);
        myBookingAct = (HistoryActivity) getActivity();
        generalFunc = myBookingAct.generalFunc;
        type = getArguments().getString("type");
        list = new ArrayList<>();
        myBookingsRecyclerAdapter = new MyBookingsRecycleAdapter(getActContext(), list, type, generalFunc, false);
        myBookingsRecyclerView.setAdapter(myBookingsRecyclerAdapter);
        myBookingsRecyclerAdapter.setOnItemClickListener(this);
        userProfileJsonObj = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
        APP_TYPE = generalFunc.getJsonValueStr("APP_TYPE", userProfileJsonObj);


        myBookingsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = recyclerView.getLayoutManager().getChildCount();
                int totalItemCount = recyclerView.getLayoutManager().getItemCount();
                int firstVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

                int lastInScreen = firstVisibleItemPosition + visibleItemCount;
                if ((lastInScreen == totalItemCount) && !(mIsLoading) && isNextPageAvailable == true) {

                    mIsLoading = true;
                    myBookingsRecyclerAdapter.addFooterView();


                    getBookingsHistory(true);


                } else if (isNextPageAvailable == false) {
                    myBookingsRecyclerAdapter.removeFooterView();
                }
            }
        });

        getBookingsHistory(false);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getBookingsHistory(false);
    }

    public boolean isDeliver() {
        if (getArguments().getString("BOOKING_TYPE").equals(Utils.CabGeneralType_Deliver)) {
            return true;
        }
        return false;
    }

    @Override
    public void onCancelBookingClickList(View v, int position) {


        confirmCancelBooking(list.get(position).get("iCabBookingId"), list.get(position));

    }

    @Override
    public void onTripStartClickList(View v, int position) {
        String contentMsg = "";

        String eTypeVal = list.get(position).get("eTypeVal");

        if (eTypeVal.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
            contentMsg = generalFunc.retrieveLangLBl("", "LBL_CONFIRM_START_JOB");
        } else if (eTypeVal.equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
            contentMsg = generalFunc.retrieveLangLBl("", "LBL_CONFIRM_START_TRIP_TXT");
        } else {
            contentMsg = generalFunc.retrieveLangLBl("", "LBL_CONFIRM_START_DELIVERY");

        }

        buildMsgOnStartTripBtn(list.get(position).get("iCabBookingId"), contentMsg);
    }

    @Override
    public void onViewServiceClickList(View v, int position) {

        Bundle bundle = new Bundle();
        bundle.putString("iCabBookingId", list.get(position).get("iCabBookingId"));

    }

    public void confirmCancelBooking(final String iCabBookingId, HashMap<String, String> list) {


        getDeclineReasonsList(iCabBookingId, list);
//        final android.support.v7.app.AlertDialog alertDialog;
//        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());
//        if (type.equalsIgnoreCase("Pending")) {
//
//            builder.setTitle(generalFunc.retrieveLangLBl("Decline Job", "LBL_DECLINE_BOOKING"));
//
//        } else {
//            if (list.get("eTypeVal").equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
//                builder.setTitle(generalFunc.retrieveLangLBl("Cancel Booking", "LBL_CANCEL_TRIP"));
//            } else if (list.get("eTypeVal").equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
//                builder.setTitle(generalFunc.retrieveLangLBl("", "LBL_CANCEL_JOB"));
//            } else {
//                builder.setTitle(generalFunc.retrieveLangLBl("", "LBL_CANCEL_DELIVERY"));
//            }
//
//        }
//
//        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View dialogView = inflater.inflate(R.layout.input_box_view, null);
//
//
//        final MaterialEditText reasonBox = (MaterialEditText) dialogView.findViewById(R.id.editBox);
//
//        reasonBox.setSingleLine(false);
//        reasonBox.setMaxLines(5);
//
//        reasonBox.setBothText(generalFunc.retrieveLangLBl("Reason", "LBL_REASON"), generalFunc.retrieveLangLBl("Enter your reason", "LBL_ENTER_REASON"));
//
//
//        builder.setView(dialogView);
//        builder.setPositiveButton(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"), (dialog, which) -> {
//
//        });
//        builder.setNegativeButton(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"), (dialog, which) -> {
//        });
//
//        alertDialog = builder.create();
//        alertDialog.show();
//
//        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
//
//            if (!Utils.checkText(reasonBox)) {
//                reasonBox.setError(generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD_ERROR_TXT"));
//                return;
//            }
//
//            alertDialog.dismiss();
//
//            if (list.get("eTypeVal").equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
//                if (type.equalsIgnoreCase("Pending")) {
//                    declineBooking(iCabBookingId, Utils.getText(reasonBox));
//                } else {
//                    cancelBooking(iCabBookingId, Utils.getText(reasonBox), true);
//                }
//            } else {
//                cancelBooking(iCabBookingId, Utils.getText(reasonBox), false);
//
//            }
//
//        });
//
//        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(view -> alertDialog.dismiss());
    }

    public void getDeclineReasonsList(String iCabBookingId, HashMap<String, String> list) {
        HashMap<String, String> parameters = new HashMap<>();

        parameters.put("type", "GetCancelReasons");
        // parameters.put("iTripId", iCabBookingId);
        parameters.put("iCabBookingId", iCabBookingId);
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("eUserType", list.get("eTypeVal"));

        ExecuteWebServerUrl exeServerTask = new ExecuteWebServerUrl(getActContext(), parameters);
        exeServerTask.setLoaderConfig(getActContext(), true, generalFunc);
        exeServerTask.setDataResponseListener(responseString -> {
            JSONObject responseStringObj = generalFunc.getJsonObject(responseString);

            if (!responseStringObj.equals("")) {

                boolean isDataAvail = generalFunc.checkDataAvail(Utils.action_str, responseStringObj);

                if (isDataAvail) {
                    showDeclineReasonsAlert(responseStringObj, list);
                } else {
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObj)));
                }

            } else {
                generalFunc.showError();
            }

        });
        exeServerTask.execute();
    }


    public void showDeclineReasonsAlert(JSONObject responseString, HashMap<String, String> listdata) {


        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());
        if (type.equalsIgnoreCase("Pending")) {

            builder.setTitle(generalFunc.retrieveLangLBl("Decline Job", "LBL_DECLINE_BOOKING"));

        } else {
            if (listdata.get("eTypeVal").equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
                builder.setTitle(generalFunc.retrieveLangLBl("Cancel Booking", "LBL_CANCEL_TRIP"));
            } else if (listdata.get("eTypeVal").equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
                builder.setTitle(generalFunc.retrieveLangLBl("", "LBL_CANCEL_JOB"));
            } else {
                builder.setTitle(generalFunc.retrieveLangLBl("", "LBL_CANCEL_DELIVERY"));
            }

        }

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.decline_order_dialog_design, null);
        builder.setView(dialogView);

        MaterialEditText reasonBox = (MaterialEditText) dialogView.findViewById(R.id.inputBox);
        reasonBox.setVisibility(View.GONE);

        reasonBox.setBothText("", generalFunc.retrieveLangLBl("", "LBL_ENTER_REASON"));

        builder.setPositiveButton(generalFunc.retrieveLangLBl("", "LBL_YES"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setNegativeButton(generalFunc.retrieveLangLBl("", "LBL_NO"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
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
                        //  dialog_declineOrder.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled
                        // (true);
                        dialog_declineOrder.getButton(AlertDialog.BUTTON_POSITIVE).setClickable(true);
                        dialog_declineOrder.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getActContext().getResources().getColor(R.color.black));
                    } else if (spinner.getSelectedItemPosition() == 0) {
                        // dialog_declineOrder.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                        dialog_declineOrder.getButton(AlertDialog.BUTTON_POSITIVE).setClickable(false);
                        dialog_declineOrder.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getActContext().getResources().getColor(R.color.gray));
                        reasonBox.setVisibility(View.GONE);
                    } else {
                        // dialog_declineOrder.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
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

            dialog_declineOrder.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                Utils.hideKeyboard(getActContext());
                selectedItemId = list.get(spinner.getSelectedItemPosition()).get("id");

                if (spinner.getSelectedItemPosition() == 0) {
                    return;
                }

                if (Utils.checkText(reasonBox) == false && spinner.getSelectedItemPosition() == (list.size() - 1)) {
                    reasonBox.setError(generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD_ERROR_TXT"));
                    return;
                }

                //new CancelTripDialog(getActContext(), listdata, generalFunc, list.get(spinner.getSelectedItemPosition()).get("id"), Utils.getText(reasonBox), false, reasonBox.getText().toString().trim());

                if (type.equalsIgnoreCase("Pending")) {
                    declineBooking(list.get(spinner.getSelectedItemPosition()).get("id"), Utils.getText(reasonBox), reasonBox.getText().toString().trim(), listdata);
                } else {
                    cancelTrip(list.get(spinner.getSelectedItemPosition()).get("id"), Utils.getText(reasonBox), reasonBox.getText().toString().trim(), listdata);
                }

                dialog_declineOrder.dismiss();
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


    public void acceptBooking(String iCabBookingId, String eConfirmByProvider) {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "UpdateBookingStatus");
        parameters.put("UserType", Utils.app_type);
        parameters.put("iDriverId", generalFunc.getMemberId());
        parameters.put("iCabBookingId", iCabBookingId);
        parameters.put("eStatus", "Accepted");
        parameters.put("eConfirmByProvider", eConfirmByProvider);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            JSONObject responseStringObj = generalFunc.getJsonObject(responseString);

            if (responseStringObj != null && !responseStringObj.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObj);

                if (isDataAvail) {
                    list.clear();
                    myBookingsRecyclerAdapter.notifyDataSetChanged();

                    final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
                    generateAlert.setCancelable(false);
                    generateAlert.setBtnClickList(btn_id -> {
                        generateAlert.closeAlertBox();

                        getBookingsHistory(false);
                    });
                    generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObj)));
                    generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));

                    generateAlert.showAlertBox();
                } else {

                    String BookingFound = generalFunc.getJsonValueStr("BookingFound", responseStringObj);

                    if (BookingFound.equalsIgnoreCase("Yes")) {

                        GenerateAlertBox alertBox = new GenerateAlertBox(getActContext());
                        alertBox.setCancelable(false);
                        alertBox.setContentMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObj)));
                        alertBox.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
                        alertBox.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"));
                        alertBox.setBtnClickList(btn_id -> {
                            if (btn_id == 0) {
                                alertBox.closeAlertBox();
                            } else if (btn_id == 1) {
                                acceptBooking(iCabBookingId, "Yes");
                                alertBox.closeAlertBox();
                            }
                        });
                        alertBox.showAlertBox();
                    } else {
                        generalFunc.showGeneralMessage("",
                                generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObj)));
                    }
                }

            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

    public void declineBooking(String iCancelReasonId, String comment, String reason, HashMap<String, String> data_trip) {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "UpdateBookingStatus");
        parameters.put("UserType", Utils.app_type);
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("iCabBookingId", data_trip.get("iCabBookingId"));
        parameters.put("vCancelReason", reason);
        parameters.put("eStatus", "Declined");
        parameters.put("iCancelReasonId", iCancelReasonId);
        parameters.put("Reason", reason);
        parameters.put("Comment", comment);


        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObj = generalFunc.getJsonObject(responseString);
            if (responseStringObj != null && !responseStringObj.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObj);

                if (isDataAvail) {
                    list.clear();
                    myBookingsRecyclerAdapter.notifyDataSetChanged();


                    final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
                    generateAlert.setCancelable(false);
                    generateAlert.setBtnClickList(btn_id -> {
                        generateAlert.closeAlertBox();
                        getBookingsHistory(false);
                    });
                    generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObj)));
                    generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));

                    generateAlert.showAlertBox();
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


    public void cancelTrip(String iCancelReasonId, String comment, String reason, HashMap<String, String> data_trip) {


        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "cancelBooking");
        parameters.put("UserType", Utils.app_type);
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("iCabBookingId", data_trip.get("iCabBookingId"));
        parameters.put("Comment", comment);
        parameters.put("iCancelReasonId", iCancelReasonId);
        parameters.put("Reason", reason);


        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObj = generalFunc.getJsonObject(responseString);

            if (responseStringObj != null && !responseStringObj.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObj);

                if (isDataAvail) {
                    list.clear();
                    myBookingsRecyclerAdapter.notifyDataSetChanged();
                    getBookingsHistory(false);
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

    public void cancelBooking(String iCabBookingId, String reason, boolean isUfx) {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "cancelBooking");
        parameters.put("UserType", Utils.app_type);
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("iCabBookingId", iCabBookingId);
        parameters.put("Reason", reason);
        if (!isUfx) {
            parameters.put("DataType", "PENDING");

        }

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObj = generalFunc.getJsonObject(responseString);
            if (responseStringObj != null && !responseStringObj.equals("")) {
                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObj);

                if (isDataAvail) {
                    list.clear();
                    myBookingsRecyclerAdapter.notifyDataSetChanged();
                    getBookingsHistory(false);
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

    public void buildMsgOnStartTripBtn(final String iCabBookingId, String contentMsg) {
        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(btn_id -> {
            if (btn_id == 0) {
                generateAlert.closeAlertBox();
            } else {
                if (type.equalsIgnoreCase("Pending")) {
                    acceptBooking(iCabBookingId, "No");
                } else {
                    startTrip(iCabBookingId);
                }
            }

        });
        if (type.equalsIgnoreCase("Pending")) {
            generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("Are you sure? You want to accept this job.", "LBL_CONFIRM_ACCEPT_JOB"));
        } else {
            generateAlert.setContentMessage("", contentMsg);
        }
        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_YES_TXT"));
        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_NO_TXT"));
        generateAlert.showAlertBox();
    }

    public void startTrip(String iCabBookingId) {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "GenerateTrip");
        parameters.put("UserType", Utils.app_type);
        parameters.put("DriverID", generalFunc.getMemberId());
        parameters.put("iCabBookingId", iCabBookingId);
        parameters.put("GoogleServerKey", generalFunc.retrieveValue(Utils.GOOGLE_SERVER_ANDROID_DRIVER_APP_KEY));

        if (myBookingAct != null && myBookingAct.userLocation != null) {
            parameters.put("vLatitude", "" + myBookingAct.userLocation.getLatitude());
            parameters.put("vLongitude", "" + myBookingAct.userLocation.getLongitude());
        } else if (GetLocationUpdates.getInstance() != null && GetLocationUpdates.getInstance().getLastLocation() != null) {
            Location lastLocation = GetLocationUpdates.getInstance().getLastLocation();

            parameters.put("vLatitude", "" + lastLocation.getLatitude());
            parameters.put("vLongitude", "" + lastLocation.getLongitude());
        }


        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObj = generalFunc.getJsonObject(responseString);

            if (responseStringObj != null && !responseStringObj.equals("")) {
                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObj);

                if (isDataAvail) {
                    myBookingAct.stopLocUpdates();
                    MyApp.getInstance().restartWithGetDataApp();
                } else {
                    String message = generalFunc.getJsonValueStr(Utils.message_str, responseStringObj);

                    if (message.equalsIgnoreCase("DO_RESTART")) {
                        MyApp.getInstance().restartWithGetDataApp();
                        return;
                    }

                    if (generalFunc.getJsonValueStr("DO_RELOAD", responseStringObj).equalsIgnoreCase("YES")) {
                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", message), generalFunc.retrieveLangLBl("Ok", "LBL_BTN_OK_TXT"), "", buttonId -> {

                            list.clear();
                            myBookingsRecyclerAdapter.notifyDataSetChanged();
                            getBookingsHistory(false);

                        });
                        return;
                    }

                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", message));
                }

            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }


    public void getBookingsHistory(final boolean isLoadMore) {
        if (errorView != null) {
            if (errorView.getVisibility() == View.VISIBLE) {
                errorView.setVisibility(View.GONE);
            }
        }

        if (loading_my_bookings != null) {
            if (loading_my_bookings.getVisibility() != View.VISIBLE && !isLoadMore) {
                loading_my_bookings.setVisibility(View.VISIBLE);
            }
        }

        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "checkBookings");
        parameters.put("iDriverId", generalFunc.getMemberId());
        parameters.put("UserType", Utils.app_type);
        parameters.put("bookingType", getArguments().getString("BOOKING_TYPE"));
        parameters.put("DataType", type);
        parameters.put("vFilterParam", myBookingAct.selFilterType);
        if (isLoadMore) {
            parameters.put("page", next_page_str);
        }

        noRidesTxt.setVisibility(View.GONE);

        final ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setDataResponseListener(responseString -> {

            JSONObject responseStringObj = generalFunc.getJsonObject(responseString);
            noRidesTxt.setVisibility(View.GONE);

            if (responseStringObj != null && !responseStringObj.equals("")) {
                closeLoader();

                if (GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObj)) {

                    list.clear();
                    String nextPage = generalFunc.getJsonValueStr("NextPage", responseStringObj);
                    JSONArray arr_rides = generalFunc.getJsonArray(Utils.message_str, responseStringObj);

                    if (arr_rides != null && arr_rides.length() > 0) {
                        for (int i = 0; i < arr_rides.length(); i++) {
                            JSONObject obj_temp = generalFunc.getJsonObject(arr_rides, i);
                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put("dBooking_date", generalFunc.getJsonValueStr("dBooking_date", obj_temp));
                            map.put("vSourceAddresss", generalFunc.getJsonValueStr("vSourceAddresss", obj_temp));
                            map.put("tDestAddress", generalFunc.getJsonValueStr("tDestAddress", obj_temp));
                            String vBookingNo = generalFunc.getJsonValueStr("vBookingNo", obj_temp);
                            map.put("vBookingNo", vBookingNo);
                            map.put("formattedVBookingNo", generalFunc.convertNumberWithRTL(vBookingNo));
                            map.put("eStatus", generalFunc.getJsonValueStr("eStatus", obj_temp));
                            map.put("iCabBookingId", generalFunc.getJsonValueStr("iCabBookingId", obj_temp));
                            map.put("dBooking_dateOrig", generalFunc.getJsonValueStr("dBooking_dateOrig", obj_temp));
                            map.put("PassengerId", generalFunc.getJsonValueStr("PassengerId", obj_temp));

                            if (generalFunc.getJsonValueStr("selectedtime", obj_temp) != null) {
                                map.put("selectedtime", generalFunc.getJsonValueStr("selectedtime", obj_temp));
                            }

                            map.put("eTypeVal", generalFunc.getJsonValueStr("eType", obj_temp));
                            if (generalFunc.getJsonValueStr("eType", obj_temp).equalsIgnoreCase("deliver")) {
                                map.put("eType", generalFunc.retrieveLangLBl("Delivery", "LBL_DELIVERY"));
                            } else if (generalFunc.getJsonValueStr("eType", obj_temp).equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
                                map.put("eType", generalFunc.retrieveLangLBl("Delivery", "LBL_RIDE"));
                            } else {
                                map.put("eType", generalFunc.retrieveLangLBl("", "LBL_SERVICES"));
                            }

                            map.put("eFareType", generalFunc.getJsonValueStr("eFareType", obj_temp));
                            map.put("appType", APP_TYPE);

                            if (map.get("eStatus").equals("Completed")) {
                                map.put("eStatus", generalFunc.retrieveLangLBl("", "LBL_ASSIGNED"));
                            } else if (map.get("eStatus").equals("Cancel")) {
                                map.put("eStatus", generalFunc.retrieveLangLBl("", "LBL_CANCELLED"));
                            }

                            if (generalFunc.getJsonValueStr("eCancelBy", obj_temp).equals("Driver")) {
                                map.put("eStatus", generalFunc.retrieveLangLBl("", "LBL_CANCELLED_BY_DRIVER"));
                            }

                            if (generalFunc.getJsonValueStr("eType", obj_temp).equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
                                map.put("LBL_BOOKING_NO", generalFunc.retrieveLangLBl("", "LBL_BOOKING"));
                                map.put("LBL_START_TRIP", generalFunc.retrieveLangLBl("", "LBL_BEGIN_TRIP"));
                                map.put("LBL_CANCEL_TRIP", generalFunc.retrieveLangLBl("", "LBL_CANCEL_TRIP"));
                                map.put("LBL_PICK_UP_LOCATION", generalFunc.retrieveLangLBl("", "LBL_PICK_UP_LOCATION"));
                                map.put("LBL_DEST_LOCATION", generalFunc.retrieveLangLBl("", "LBL_DEST_LOCATION"));
                            } else {
                                map.put("LBL_BOOKING_NO", generalFunc.retrieveLangLBl("Delivery No", "LBL_DELIVERY_NO"));
                                map.put("LBL_START_TRIP", generalFunc.retrieveLangLBl("Start Delivery", "LBL_BEGIN_DELIVERY"));
                                map.put("LBL_CANCEL_TRIP", generalFunc.retrieveLangLBl("Cancel Delivery", "LBL_CANCEL_DELIVERY"));
                                map.put("LBL_PICK_UP_LOCATION", generalFunc.retrieveLangLBl("Sender Location", "LBL_SENDER_LOCATION"));
                                map.put("LBL_DEST_LOCATION", generalFunc.retrieveLangLBl("Receiver's Location", "LBL_RECEIVER_LOCATION"));
                            }


                            if (generalFunc.getJsonValueStr("eType", obj_temp).equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
                                map.put("LBL_ACCEPT_JOB", generalFunc.retrieveLangLBl("Accept Job", "LBL_ACCEPT_JOB"));
                                map.put("LBL_DECLINE_JOB", generalFunc.retrieveLangLBl("Decline job", "LBL_DECLINE_JOB"));
                                map.put("LBL_START_TRIP", generalFunc.retrieveLangLBl("", "LBL_BEGIN_JOB"));
                                map.put("LBL_CANCEL_TRIP", generalFunc.retrieveLangLBl("Cancel job", "LBL_CANCEL_JOB"));

                                map.put("SelectedCategory", generalFunc.getJsonValueStr("SelectedCategory", obj_temp));
                                map.put("SelectedVehicle", generalFunc.getJsonValueStr("SelectedVehicle", obj_temp));
                                map.put("LBL_PICK_UP_LOCATION", generalFunc.retrieveLangLBl("", "LBL_JOB_LOCATION_TXT"));
                                map.put("LBL_DEST_LOCATION", generalFunc.retrieveLangLBl("", "LBL_DEST_LOCATION"));
                            } else {
                                map.put("SelectedCategory", generalFunc.getJsonValueStr("vVehicleType", obj_temp));
                            }

                            map.put("moreServices", generalFunc.getJsonValueStr("moreServices", obj_temp));
                            map.put("vServiceTitle", generalFunc.getJsonValueStr("vServiceTitle", obj_temp));
                            map.put("vServiceDetailTitle", generalFunc.getJsonValueStr("vServiceDetailTitle", obj_temp));

                            if (generalFunc.getJsonValueStr("eFareType", obj_temp).equalsIgnoreCase(Utils.CabFaretypeFixed) && generalFunc.getJsonValueStr("moreServices", obj_temp).equalsIgnoreCase("No")) {
                                map.put("SelectedCategory", generalFunc.getJsonValueStr("vCategory", obj_temp));

                            }

                            map.put("LBL_Status", generalFunc.retrieveLangLBl("", "LBL_Status"));
                            map.put("LBL_VIEW_REQUESTED_SERVICES", generalFunc.retrieveLangLBl("", "LBL_VIEW_REQUESTED_SERVICES"));
                            map.put("JSON", obj_temp.toString());

                            map.put("LBL_JOB_LOCATION_TXT", generalFunc.retrieveLangLBl("", "LBL_JOB_LOCATION_TXT"));

                            map.put("listingFormattedDate", generalFunc.getDateFormatedType(generalFunc.getJsonValueStr("dBooking_dateOrig", obj_temp), Utils.OriginalDateFormate, Utils.getDetailDateFormat(getActContext())));


                            list.add(map);

                        }

                        JSONArray arr_type_filter = generalFunc.getJsonArray("AppTypeFilterArr", responseStringObj);

                        if (arr_type_filter != null && arr_type_filter.length() > 0) {
                            filterlist = new ArrayList<>();
                            for (int i = 0; i < arr_type_filter.length(); i++) {
                                JSONObject obj_temp = generalFunc.getJsonObject(arr_type_filter, i);

                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put("vTitle", generalFunc.getJsonValueStr("vTitle", obj_temp));
                                map.put("vFilterParam", generalFunc.getJsonValueStr("vFilterParam", obj_temp));
                                filterlist.add(map);
                            }
                            myBookingAct.filterManage(filterlist);
                        }
                    }


                    if (!nextPage.equals("") && !nextPage.equals("0")) {
                        next_page_str = nextPage;
                        isNextPageAvailable = true;
                    } else {
                        removeNextPageConfig();
                    }

                    myBookingsRecyclerAdapter.notifyDataSetChanged();

                } else {
                    list.clear();
                    if (list.size() == 0) {
                        removeNextPageConfig();
                        noRidesTxt.setText(generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObj)));
                        noRidesTxt.setVisibility(View.VISIBLE);
                        myBookingsRecyclerAdapter.notifyDataSetChanged();
                    }
                }
            } else {
                if (isLoadMore == false) {
                    removeNextPageConfig();
                    generateErrorView();
                }

            }

            mIsLoading = false;
        });
        exeWebServer.execute();
    }

    public void removeNextPageConfig() {
        next_page_str = "";
        isNextPageAvailable = false;
        mIsLoading = false;
        myBookingsRecyclerAdapter.removeFooterView();
    }

    public void closeLoader() {
        if (loading_my_bookings.getVisibility() == View.VISIBLE) {
            loading_my_bookings.setVisibility(View.GONE);
        }
    }

    public void generateErrorView() {

        closeLoader();

        generalFunc.generateErrorView(errorView, "LBL_ERROR_TXT", "LBL_NO_INTERNET_TXT");

        if (errorView.getVisibility() != View.VISIBLE) {
            errorView.setVisibility(View.VISIBLE);
        }
        errorView.setOnRetryListener(new ErrorView.RetryListener() {
            @Override
            public void onRetry() {
                getBookingsHistory(false);
            }
        });
    }

    public Context getActContext() {
        return myBookingAct.getActContext();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Utils.hideKeyboard(getActivity());
    }
}
