package com.general.files;

import android.content.Context;
import android.text.TextUtils;

import com.levaeu.driver.ActiveTripActivity;
import com.levaeu.driver.BuildConfig;
import com.levaeu.driver.MyProfileActivity;
import com.levaeu.driver.UploadDocActivity;
import com.rest.RestClient;
import com.utils.CommonUtilities;
import com.utils.Logger;
import com.utils.Utils;
import com.view.MyProgressDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Admin on 08-07-2016.
 */
public class UploadProfileImage /*extends AsyncTask<String, String, String>*/ {

    String selectedImagePath;
    String responseString = "";

    String temp_File_Name = "";
    ArrayList<String[]> paramsList;
    String type = "";
    Context actContext;
    MyProgressDialog myPDialog;
    GeneralFunctions generalFunc;

    public UploadProfileImage(Context actContext, String selectedImagePath, String temp_File_Name, ArrayList<String[]> paramsList, String type) {
        this.selectedImagePath = selectedImagePath;
        this.temp_File_Name = temp_File_Name;
        this.paramsList = paramsList;
        this.type = type;
        this.actContext = actContext;
        this.generalFunc = MyApp.getInstance().getGeneralFun(actContext);
    }


    public UploadProfileImage(Context actContext, String selectedImagePath, String temp_File_Name, ArrayList<String[]> paramsList) {
        this.selectedImagePath = selectedImagePath;
        this.temp_File_Name = temp_File_Name;
        this.paramsList = paramsList;
        this.actContext = actContext;
    }


    public void execute() {
        if (generalFunc == null) {
            generalFunc = MyApp.getInstance().getGeneralFun(actContext);
        }
        myPDialog = new MyProgressDialog(actContext, false, generalFunc.retrieveLangLBl("Loading", "LBL_LOADING_TXT"));
        try {
            myPDialog.show();
        } catch (Exception e) {

        }

        String filePath = selectedImagePath;
        if (TextUtils.isEmpty(type)) {
            filePath = generalFunc.decodeFile(selectedImagePath, Utils.ImageUpload_DESIREDWIDTH,
                    Utils.ImageUpload_DESIREDHEIGHT, temp_File_Name);
        }

        if (filePath.equals("")) {
            HashMap<String, String> dataParams = new HashMap<>();
            for (int i = 0; i < paramsList.size(); i++) {
                String[] arrData = paramsList.get(i);

                dataParams.put(arrData[0], arrData[1]);
            }

            if (dataParams != null) {
                dataParams.put("tSessionId", generalFunc.getMemberId().equals("") ? "" : generalFunc.retrieveValue(Utils.SESSION_ID_KEY));
                dataParams.put("deviceHeight", Utils.getScreenPixelHeight(actContext) + "");
                dataParams.put("deviceWidth", Utils.getScreenPixelWidth(actContext) + "");

                dataParams.put("GeneralUserType", Utils.app_type);
                dataParams.put("GeneralMemberId", generalFunc.getMemberId());
                dataParams.put("GeneralDeviceType", "" + Utils.deviceType);
                dataParams.put("GeneralAppVersion", BuildConfig.VERSION_NAME);
                dataParams.put("vTimeZone", generalFunc.getTimezone());
                dataParams.put("vUserDeviceCountry", Utils.getUserDeviceCountryCode(actContext));
                dataParams.put("APP_TYPE", ExecuteWebServerUrl.CUSTOM_APP_TYPE);
            }

            Call<Object> call = RestClient.getClient("POST", CommonUtilities.SERVER).getResponse(CommonUtilities.SERVER_WEBSERVICE_PATH, dataParams);
            call.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    if (response.isSuccessful()) {
                        // request successful (status code 200, 201)
                        responseString = RestClient.getGSONBuilder().toJson(response.body());

                        fireResponse();
                    } else {
                        responseString = "";
                        fireResponse();
                    }
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    Logger.d("DataError", "::" + t.getMessage());
                    responseString = "";
                    fireResponse();
                }

            });

            return;
        }
        File file = new File(filePath);

        MultipartBody.Part filePart = MultipartBody.Part.createFormData("vImage", temp_File_Name, RequestBody.create(MediaType.parse("multipart/form-data"), file));

        Logger.d("temp_File_Name", "::" + temp_File_Name);


        HashMap<String, RequestBody> dataParams = new HashMap<>();

        for (int i = 0; i < paramsList.size(); i++) {
            String[] arrData = paramsList.get(i);

            dataParams.put(arrData[0], RequestBody.create(MediaType.parse("text/plain"), arrData[1]));
        }

        if (dataParams != null) {
            dataParams.put("tSessionId", RequestBody.create(MediaType.parse("text/plain"), generalFunc.getMemberId().equals("") ? "" : generalFunc.retrieveValue(Utils.SESSION_ID_KEY)));
            dataParams.put("deviceHeight", RequestBody.create(MediaType.parse("text/plain"), Utils.getScreenPixelHeight(actContext) + ""));
            dataParams.put("deviceWidth", RequestBody.create(MediaType.parse("text/plain"), Utils.getScreenPixelWidth(actContext) + ""));

            dataParams.put("GeneralUserType", RequestBody.create(MediaType.parse("text/plain"), Utils.app_type));
            dataParams.put("GeneralMemberId", RequestBody.create(MediaType.parse("text/plain"), generalFunc.getMemberId()));
            dataParams.put("GeneralDeviceType", RequestBody.create(MediaType.parse("text/plain"), "" + Utils.deviceType));
            dataParams.put("GeneralAppVersion", RequestBody.create(MediaType.parse("text/plain"), BuildConfig.VERSION_NAME));
            dataParams.put("vTimeZone", RequestBody.create(MediaType.parse("text/plain"), generalFunc.getTimezone()));
            dataParams.put("vUserDeviceCountry", RequestBody.create(MediaType.parse("text/plain"), Utils.getUserDeviceCountryCode(actContext)));
            dataParams.put("APP_TYPE", RequestBody.create(MediaType.parse("text/plain"), ExecuteWebServerUrl.CUSTOM_APP_TYPE));
        }

        Call<Object> call = RestClient.getClient("POST", CommonUtilities.SERVER).uploadData(CommonUtilities.SERVER_WEBSERVICE_PATH, filePart, dataParams);

        call.enqueue(new Callback<Object>() {

            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    // request successful (status code 200, 201)

//                    Logger.d("Data", "response = " + new Gson().toJson(response.body()));

                    responseString = RestClient.getGSONBuilder().toJson(response.body());

                    fireResponse();
                } else {
                    responseString = "";
                    fireResponse();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Logger.d("DataError", "::" + t.getMessage());
                responseString = "";
                fireResponse();
            }

        });

    }

    public void fireResponse() {


        try {
            if (myPDialog != null) {
                myPDialog.close();
            }
        } catch (Exception e) {

        }

        if (actContext instanceof MyProfileActivity) {
            ((MyProfileActivity) actContext).handleImgUploadResponse(responseString);
        } else if (actContext instanceof ActiveTripActivity) {
            ((ActiveTripActivity) actContext).handleImgUploadResponse(responseString, type);
        } else if (actContext instanceof UploadDocActivity) {
            ((UploadDocActivity) actContext).handleImgUploadResponse(responseString);
        }
    }
}
