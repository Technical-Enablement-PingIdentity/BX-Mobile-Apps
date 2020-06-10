//
// Class Name : AppPreferences
// App name : Moderno
//
// Application preferences class
//
// See LICENSE.txt for this sample’s licensing information and LICENSE_SDK.txt for the PingID SDK library licensing information.
// Created by Ping Identity on 3/23/17.
// Copyright © 2017 Ping Identity. All rights reserved.
//
package pingidsdk.pingidentity.com.simpledemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppPreferences {
    //in-app settings
    private final static String USERNAME = "USERNAME";
    private final static String PASSWORD = "PASSWORD";
    private final static String CUSTOMER_SERVER_BASE_URL = "CUSTOMER_SERVER_BASE_URL";
    private final static String APP_ID = "APP_ID";
    private final static String OIDC_ISSUER = "OIDC_ISSUER";
    private final static String PUSH_SENDER_ID = "PUSH_SENDER_ID";
    private final static String DEVICE_VERIFICATION_API_KEY = "DEVICE_VERIFICATION_API_KEY";
    private final static String IS_ROOT_DETECTION_ACTIVE = "IS_ROOT_DETECTION_ACTIVE";

    /*
     * Gets username.
     *
     * @param context the context
     * @return the username
     */
    public static String getUsername(Context context) {
        return getParameter(context, USERNAME);
    }

    /*
     * Sets username.
     *
     * @param context the context
     * @param text    the text
     */
    public static void setUsername(Context context, String text) {
        setParameter(context, USERNAME, text);
    }

    /*
     * Gets password.
     *
     * @param context the context
     * @return the password
     */
    public static String getPassword(Context context) {
        return getParameter(context, PASSWORD);
    }

    /*
     * Sets password.
     *
     * @param context the context
     * @param text    the text
     */
    public static void setPassword(Context context, String text) {
        setParameter(context, PASSWORD, text);
    }

    public static String getCustomerServerBaseUrl(Context context) {
        return getParameter(context, CUSTOMER_SERVER_BASE_URL);
    }
    public static void setCustomerServerBaseUrl(Context context, String text) {
        setParameter(context, CUSTOMER_SERVER_BASE_URL, text);
    }

    public static String getApiId(Context context) {
        return getParameter(context, APP_ID);
    }
    public static void setApiId(Context context, String text) {
        setParameter(context, APP_ID, text);
    }

    public static String getOidcIssuer(Context context) {
        return getParameter(context, OIDC_ISSUER);
    }
    public static void setOidcIssuer(Context context, String text) {
        setParameter(context, OIDC_ISSUER, text);
    }

    public static String getPushSenderId(Context context) {
        return getParameter(context, PUSH_SENDER_ID);
    }
    public static void setPushSenderId(Context context, String text) {
        setParameter(context, PUSH_SENDER_ID, text);
    }

    public static String getDeviceVerificationApiKey(Context context) {
        return getParameter(context, DEVICE_VERIFICATION_API_KEY);
    }
    public static void setDeviceVerificationApiKey(Context context, String text) {
        setParameter(context, DEVICE_VERIFICATION_API_KEY, text);
    }

    public static boolean isRootDetectionActive(Context context) {
        SharedPreferences _sharedPrefs;
        _sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return _sharedPrefs.getBoolean(IS_ROOT_DETECTION_ACTIVE, true);
    }
    public static void setRootDetectionActive(Context context, boolean isActive) {
        SharedPreferences _sharedPrefs;
        _sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor _prefsEditor;
        _prefsEditor = _sharedPrefs.edit();
        _prefsEditor.putBoolean(IS_ROOT_DETECTION_ACTIVE, isActive);
        _prefsEditor.apply();
    }

    /**
     * get / set the parameter at the preference
     * @param context the context
     * @param key the parameter key
     * @return the value
     */
    private static String getParameter(Context context, String key) {
        SharedPreferences _sharedPrefs;
        _sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return _sharedPrefs.getString(key, "");
    }
    private static void setParameter(Context context, String key, String text) {
        SharedPreferences _sharedPrefs;
        _sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor _prefsEditor;
        _prefsEditor = _sharedPrefs.edit();
        _prefsEditor.putString(key, text);
        _prefsEditor.apply();
    }

}