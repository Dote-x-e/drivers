package com.general.files;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.levaeu.driver.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.utils.CommonUtilities;
import com.utils.Logger;
import com.utils.Utils;
import com.view.SelectableRoundedImageView;

import org.json.JSONObject;

public class AppFunctions {
    Context mContext;
    GeneralFunctions generalFunc;

    public AppFunctions(Context mContext) {
        this.mContext = mContext;
        generalFunc = new GeneralFunctions(mContext);
    }

    public void checkProfileImage(SelectableRoundedImageView userProfileImgView, String userProfileJson, String imageKey) {
        String vImgName_str = generalFunc.getJsonValue(imageKey, userProfileJson);

        Picasso.get().load(CommonUtilities.PROVIDER_PHOTO_PATH + generalFunc.getMemberId() + "/" + vImgName_str).placeholder(R.mipmap.ic_no_pic_user).error(R.mipmap.ic_no_pic_user).into(userProfileImgView);
    }

    public void checkProfileImage(SelectableRoundedImageView userProfileImgView, JSONObject userProfileJsonObj, String imageKey, ImageView profilebackimage) {
        String vImgName_str = generalFunc.getJsonValueStr(imageKey, userProfileJsonObj);

        Picasso.get().load(CommonUtilities.PROVIDER_PHOTO_PATH + generalFunc.getMemberId() + "/" + vImgName_str).placeholder(R.mipmap.ic_no_pic_user).error(R.mipmap.ic_no_pic_user).into(userProfileImgView);

        Picasso.get().load(CommonUtilities.PROVIDER_PHOTO_PATH + generalFunc.getMemberId() + "/" + vImgName_str).placeholder(R.mipmap.ic_no_pic_user).error(R.mipmap.ic_no_pic_user).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Utils.setBlurImage(bitmap, profilebackimage);
            }

            @Override
            public void onBitmapFailed(Exception e,Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }



}
