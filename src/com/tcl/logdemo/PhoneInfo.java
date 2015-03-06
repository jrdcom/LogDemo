package com.tcl.logdemo;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

public class PhoneInfo {

    public static final String SERIAL = "Serial";
    public static final String PRODUCT = "Product";
    public static final String IMEI = "IMEI";
    public static final String VERSION = "Version";
    public static final String DATE = "Date";
    public static final String TIME = "Time";
    public static final String REPORTER = "Reporter";
    public static final String DESCRIPTION = "Description";

    private static PhoneInfo mPhoneInfo = null;
    private Context mContext = null;

    public static PhoneInfo getInstance(Context context) {
        if (mPhoneInfo == null) {
            mPhoneInfo = new PhoneInfo(context);
        }
        return mPhoneInfo;
    }

    private PhoneInfo(Context context) {
        mContext = context;
    }

    public class BuildInfo extends Build {
    }

    public String getIMEI() {
        String imeiStr = ((TelephonyManager) mContext
                .getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        return (imeiStr != null && imeiStr.length() != 0) ? imeiStr: "";
    }
}
