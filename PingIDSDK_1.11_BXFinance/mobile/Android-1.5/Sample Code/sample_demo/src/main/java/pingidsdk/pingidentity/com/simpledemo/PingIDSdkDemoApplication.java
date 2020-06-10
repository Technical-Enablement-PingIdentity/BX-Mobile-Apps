//
// Class Name : PingIDSdkDemoApplication
// App name : Moderno
//
// This is the customized application object. It implements the PingIDSDKEvents interface and handles all the events
// triggered by the library. It also handle activity life cycle events to get reference to the currently loaded activity
// in order to know if the events are to be presented to the user on the activity or in a notification.
//
// See LICENSE.txt for this sample’s licensing information and LICENSE_SDK.txt for the PingID SDK library licensing information.
// Created by Ping Identity on 3/23/17.
// Copyright © 2017 Ping Identity. All rights reserved.
//

package pingidsdk.pingidentity.com.simpledemo;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import androidx.multidex.MultiDexApplication;
import androidx.core.app.NotificationCompat;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import pingidsdk.pingidentity.com.simpledemo.beans.AuthenticationData;
import pingidsdkclient.DeviceDetails;
import pingidsdkclient.PingID;

import static pingidsdk.pingidentity.com.simpledemo.AuthenticationActivity.KEY_CLIENT_CONTEXT;
import static pingidsdk.pingidentity.com.simpledemo.BaseActivity.PLAY_SERVICES_UPDATE_REQUEST;
import static pingidsdk.pingidentity.com.simpledemo.BaseActivity.TRUST_LEVELS;
import static pingidsdk.pingidentity.com.simpledemo.NotificationActionReceiver.INTENT_FILTER_APPROVE_AUTHENTICATION;
import static pingidsdk.pingidentity.com.simpledemo.NotificationActionReceiver.INTENT_FILTER_APPROVE_PAIRING;
import static pingidsdk.pingidentity.com.simpledemo.NotificationActionReceiver.INTENT_FILTER_DENY_AUTHENTICATION;
import static pingidsdk.pingidentity.com.simpledemo.NotificationActionReceiver.INTENT_FILTER_DENY_PAIRING;
import static pingidsdk.pingidentity.com.simpledemo.beans.AuthenticationData.TRANSACTION_TYPE_AUTHENTICATION;
import static pingidsdkclient.PingID.PIDTrustLevel.PIDTrustLevelPrimary;
import static pingidsdkclient.PingID.PIDTrustLevel.PIDTrustLevelTrusted;

public class PingIDSdkDemoApplication extends MultiDexApplication implements Application.ActivityLifecycleCallbacks, PingID.PingIdSdkEvents {

    public static final String TAG=PingIDSdkDemoApplication.class.getName();
    private Activity _activity;
    public static final Map<String, String> globalData= new HashMap<>(); //global object for application wide data
    public static PingIDSdkDemoApplication _pingIDSdkDemoApplication;
    public static final int NOTIFICATION_ID = 1;
    public static final String NOTIFICATION_CHANNEL_ID = "pingidsdk_channel_id";
    private int googlePlayServicesAvailabilityCheckResult = -1;
    private boolean isApprovalDialogToDisplayFromBackground = false;
    private boolean isHomeActivityLoginFromAttemptLogin = false;
    private Bundle bundle;

    /*YjQ4ZTk3M2FmYjcxNTVlYw==
     * Various QR Token Statuses
     */
    public static final String RESPONSE_AUTH_TOKEN_STATUS_PARAM = "authentication_token_status";

    public static final String AUTH_TOKEN_PARAM_CLAIMED = "claimed";
    public static final String AUTH_TOKEN_CLAIMED_AND_PUSH_FAILED = "claimed_and_push_failed";
    public static final String AUTH_TOKEN_PARAM_CLAIMED_AND_PUSHLESS = "claimed_and_pushless";

    public static final String AUTH_TOKEN_PARAM_CANCELED = "canceled";
    public static final String AUTH_TOKEN_PARAM_DENIED = "denied";

    public static final String AUTH_TOKEN_PARAM_PENDING_USER_APPROVAL = "pending_user_approval";
    public static final String AUTH_TOKEN_PARAM_WEB_USER_SELECTION = "web_user_selection";
    public static final String AUTH_TOKEN_PARAM_PENDING_PUSH_VERIFICATION = "pending_push_verification";
    public static final String AUTH_TOKEN_PARAM_PENDING_USER_SELECTION = "mobile_user_selection";
    public static final String AUTH_TOKEN_PARAM_PENDING_USER_SELECTION_AND_APPROVAL = "mobile_user_selection_and_approval";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        _pingIDSdkDemoApplication = this;
        this.registerActivityLifecycleCallbacks(PingIDSdkDemoApplication.this);

        // first time, init the system variables
        if (AppPreferences.getCustomerServerBaseUrl(this).isEmpty() && AppPreferences.getApiId(this).isEmpty() &&
                AppPreferences.getOidcIssuer(this).isEmpty() && AppPreferences.getPushSenderId(this).isEmpty() &&
                AppPreferences.getDeviceVerificationApiKey(this).isEmpty()) {
            AppPreferences.setCustomerServerBaseUrl(this, BuildConfig.CUSTOMER_SERVER_BASE_URL);
            AppPreferences.setApiId(this, BuildConfig.APP_ID);
            AppPreferences.setOidcIssuer(this, BuildConfig.OIDC_ISSUER);
            AppPreferences.setPushSenderId(this, BuildConfig.PUSH_SENDER_ID);
            AppPreferences.setDeviceVerificationApiKey(this, BuildConfig.DEVICE_VERIFICATION_API_KEY);
            AppPreferences.setRootDetectionActive(this, BuildConfig.IS_ROOT_DETECTION_ACTIVE);
        }

        //initialize PingID
        try {
            PingID.init(this, AppPreferences.getApiId(getApplicationContext()), PingIDSdkDemoApplication.getInstance(), AppPreferences.getPushSenderId(getApplicationContext()), PingID.PIDSupportedMfaType.PIDSupportedMfaTypeAutomatic);
            PingID.getInstance().setRootDetection(AppPreferences.isRootDetectionActive(getApplicationContext()), PingID.PIDDataCenterType.PIDDataCenterTypeNA, AppPreferences.getDeviceVerificationApiKey(getApplicationContext()));
            PingIDSdkDemoApplication.addLogLine("PingID Consumer lib initialization succeeded");
        } catch (Exception e) {
            e.printStackTrace();
            addLogLine("PingID Consumer lib initialization failed : " + e.getMessage());
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static PingIDSdkDemoApplication getInstance(){
        return _pingIDSdkDemoApplication;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Log.w(TAG, "Activity resumed:" + activity.getLocalClassName());
        _activity = activity;
        //if we have a googlePlayServicesAvailabilityCheckResult value we didn't handle bacause there
        //was not activity loaded - handle it now.
        if (googlePlayServicesAvailabilityCheckResult > -1) {
            handleGooglePlayServicesAvailabilityCheckResult(googlePlayServicesAvailabilityCheckResult);
            googlePlayServicesAvailabilityCheckResult = -1 ;//reset the value
        }

        if(isApprovalDialogToDisplayFromBackground) {
            isApprovalDialogToDisplayFromBackground = false;
            ((BaseActivity) getCurrentActivity()).requestUserApproval(bundle);
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Log.w(TAG, "Activity paused:" + activity.getLocalClassName());
        if (_activity == activity){
            _activity = null;
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        //Log.w(TAG, "Activity destroyed:" + activity.getLocalClassName());
    }

    public Activity getCurrentActivity() {
        return _activity;
    }

    @Override
    public void onPairingOptionsRequired(final List<String> availableTrustLevels, DeviceDetails deviceDetails) {
        Log.i(TAG, "onPairingOptionsRequired triggered");
        if (getCurrentActivity() != null) {
            getCurrentActivity().runOnUiThread(new Runnable() {
                public void run() {
                    //this will display the "add device" dialog to the user
                    //the device type will be auto selected according to the
                    //options coming from the server - primary/trusted
                    addLogLine("onPairingOptionsRequired request triggered by PingID");
                    ((BaseActivity) getCurrentActivity()).displayAddDeviceToNetworkDialog(availableTrustLevels);
                }
            });
        }else{
            createPairingNotification(availableTrustLevels);
        }
    }



    @Override
    public void onPairingOptionsRequiredWithPasscode(List<String> list, String s) {
        if (getCurrentActivity() instanceof LoginActivity) {
            ((LoginActivity) getCurrentActivity()).launchOtpActivity();
        }
    }

    @Override
    public void onPairingCompleted(final PingID.PIDActionStatus status, final PingID.PIDErrorDomain pidErrorDomain) {
        Log.i(TAG, "onPairingCompleted triggered with status " + status.name() + (pidErrorDomain!=null ? ", error=" + pidErrorDomain.getResultCode() + " " + pidErrorDomain.getResultDescription() : ""));
        final String msg = "Pairing completed with status " + status + (pidErrorDomain != null ? ", error=" + pidErrorDomain.getResultCode() + " " + pidErrorDomain.getResultDescription() : "");
        if (getCurrentActivity()!=null) {
            getCurrentActivity().runOnUiThread(new Runnable() {
                public void run() {
                    //display the results of the pairing operation
                    showStatus(msg);

                    //if (getCurrentActivity() instanceof BaseActivity) {
                    ((BaseActivity) getCurrentActivity()).changeScreenAvailability(true);

                        //((BaseActivity) getCurrentActivity()).changeScreenAvailability(true, null);
                    //}

                    if (status.name().equalsIgnoreCase(PingID.PIDActionStatus.SUCCESS.name()) || status.name().equalsIgnoreCase(PingID.PIDActionStatus.RE_PAIR_SUCCESS.name())) {
                        if (getCurrentActivity() instanceof LoginActivity && !isHomeActivityLoginFromAttemptLogin) { // if isHomeActivityLoginFromAttemptLogin might login and logout - should not login again
                            ((LoginActivity) getCurrentActivity()).launchHomeActivity();
                        }

                    } else {
                        ((BaseActivity) getCurrentActivity()).displayAlertDialog("Pairing", "Pairing failed, please review the log for details.", "OK", null);
                    }
                }
            });
        }else{
            Log.i(TAG, msg);
            removeNotification();
        }
    }

    @Override
    public void onIgnoreDeviceCompleted(final PingID.PIDActionStatus status, PingID.PIDErrorDomain pidErrorDomain) {
        //not supported in this demo
    }

    @Override
    public void onAuthenticationCompleted(final PingID.PIDActionStatus status, final PingID.PIDActionType actionType, final PingID.PIDErrorDomain pidErrorDomain) {
        String msg = "Authentication completed with " + (actionType != null ? "actionType " + actionType.name() : null) + ", status=" + " " + status.name() + (pidErrorDomain != null ? ", error=" + pidErrorDomain.getResultCode() + " " + pidErrorDomain.getResultDescription() : "");
        if (PingIDSdkDemoApplication.globalData.containsKey(KEY_CLIENT_CONTEXT) &&  PingIDSdkDemoApplication.globalData.get(KEY_CLIENT_CONTEXT).toLowerCase().contains("transfer")) {
            msg = "Transaction completed with " + (actionType != null ? "actionType " + actionType.name() : null) + ", status=" + " " + status.name() + (pidErrorDomain != null ? ", error=" + pidErrorDomain.getResultCode() + " " + pidErrorDomain.getResultDescription() : "");
        }
        final String authenticationStatus = msg;
        Log.i(TAG, authenticationStatus);
        if (getCurrentActivity() != null) {
            getCurrentActivity().runOnUiThread(new Runnable() {
                public void run() {
                    showStatus(authenticationStatus);
                    if (getCurrentActivity() instanceof LoginActivity || getCurrentActivity() instanceof HomeActivity) {
                        ((BaseActivity) getCurrentActivity()).changeScreenAvailability(true);
                    } else if (getCurrentActivity() instanceof AuthenticationActivity) {
                        ((AuthenticationActivity) getCurrentActivity()).onAuthenticationCompleted(status, actionType);
                    }

                }
            });
        }else{
            Log.e(TAG, msg);
        }
        removeNotification();
    }

    @Override
    public void onGeneralMessage(final String msg) {
        Log.i(TAG, "onGeneralMessage triggered. Msg=" + msg);
    }

    @Override
    public void onPairingProgress(String msg) {
        addLogLine("onPairingProgress : " + msg);
    }

    @Override
    public void onAuthenticationRequired(final Bundle data) {
        Log.i(TAG, "onAuthenticationRequired triggered. Bundle size=" + data.keySet().size());
        if (getCurrentActivity() != null) { //if there is an active activity
            getCurrentActivity().runOnUiThread(new Runnable() {
                public void run() {
                    //incoming authentication - launch the authentication activity
                    ((BaseActivity) getCurrentActivity()).launchAuthenticationActivity(data);
                }
            });
        }else{
            createAuthenticationNotification(data);
        }

    }



    @Override
    public void onError(final Throwable throwable, final String description) {
        getCurrentActivity().runOnUiThread(new Runnable() {
            public void run() {
                //display the error
                showStatus("onError msg: " + description + "; " + throwable.getMessage());
            }
        });
    }

    @Override
    public void onLogsSentToServer(final PingID.PIDActionStatus status, final String supportId) {
        Log.i(TAG, "onLogsSentToServer triggered. supportId=" + supportId);
        //bringAppToFront();
        getCurrentActivity().runOnUiThread(new Runnable() {
            public void run() {
                //display the results of this operation
                if (status.equals(PingID.PIDActionStatus.SUCCESS)) {
                    addLogLine("Logs sent to PingID SDK server : supportId=" + supportId);
                    Toast.makeText(getApplicationContext(), "Log upload succeeded, supportId=" + supportId, Toast.LENGTH_LONG).show();
                } else {
                    addLogLine("Log upload failed");
                    Toast.makeText(getApplicationContext(), "Log upload failed", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onAuthenticationCancelled() {
        //not supported in this demo
    }

    @Override
    public void onServicePayloadReceivedWithStatusDone() {
        addLogLine("onServicePayloadReceivedWithStatusDone triggered");
        if (getCurrentActivity() != null && getCurrentActivity() instanceof LoginActivity) {
            getCurrentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((LoginActivity)getCurrentActivity()).launchHomeActivityPingFed();
                }
            });
        }
    }

    /*
     * This event is triggered when the the authentication token status is sent from
     * the PingID SDK as a response to QR Code Authorization flow. Please read the documentation
     * about various authentication token statuses.
     */
    @Override
    public void authenticationTokenStatus(final Bundle bundle, final PingID.PIDErrorDomain pidErrorDomain) {
        if (pidErrorDomain==null) {
            String authTokenStatus = "";
            if (bundle != null && bundle.getString(RESPONSE_AUTH_TOKEN_STATUS_PARAM) != null) {
                authTokenStatus = bundle.getString(RESPONSE_AUTH_TOKEN_STATUS_PARAM);
            }
            switch (authTokenStatus.toLowerCase()) {
                case "":
                    addLogLine("Received no authentication token status");
                    break;
                case AUTH_TOKEN_PARAM_CLAIMED:
                case AUTH_TOKEN_PARAM_CLAIMED_AND_PUSHLESS:
                case AUTH_TOKEN_CLAIMED_AND_PUSH_FAILED:
                    final String finalAuthTokenStatus = authTokenStatus;
                    getCurrentActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            addLogLine("Received authentication token status: " + finalAuthTokenStatus);
                            ((BaseActivity)getCurrentActivity()).showSuccess();
                        }
                    });

                    break;
                case AUTH_TOKEN_PARAM_WEB_USER_SELECTION:
                case AUTH_TOKEN_PARAM_CANCELED:
                case AUTH_TOKEN_PARAM_DENIED:
                case AUTH_TOKEN_PARAM_PENDING_PUSH_VERIFICATION:
                    final String finalAuthTokenStatus1 = authTokenStatus;
                    getCurrentActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            addLogLine("Received authentication token status: " + finalAuthTokenStatus1);
                            Toast.makeText(getApplicationContext(), "Received authentication token status: " + finalAuthTokenStatus1, Toast.LENGTH_LONG).show();
                            if(getCurrentActivity() instanceof AuthenticationActivity){
                                getCurrentActivity().finish();
                            }
                        }
                    });

                    break;
                case AUTH_TOKEN_PARAM_PENDING_USER_SELECTION:
                case AUTH_TOKEN_PARAM_PENDING_USER_SELECTION_AND_APPROVAL:
                    getCurrentActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((BaseActivity) getCurrentActivity()).requestUserSelection(bundle);
                        }
                    });
                    break;
                case AUTH_TOKEN_PARAM_PENDING_USER_APPROVAL:
                    if (bundle != null && bundle.getString("user") != null) {
                        getCurrentActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((BaseActivity) getCurrentActivity()).requestUserApproval(bundle);
                            }
                        });

                        // if at the background
                        if (_activity == null) {
                            isApprovalDialogToDisplayFromBackground = true;
                            this.bundle = bundle;
                        }
                    }
                    break;
                default:
                    final String finalAuthTokenStatus2 = authTokenStatus;
                    getCurrentActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            addLogLine("Received unexpected authentication token status: " + finalAuthTokenStatus2);
                            Toast.makeText(getApplicationContext(), "Received unexpected authentication token status: " + finalAuthTokenStatus2, Toast.LENGTH_LONG).show();
                        }
                    });


            }
            System.out.println("RECEIVED RESPONSE FROM SDK");
            for (String s : bundle.keySet()) {
                System.out.println("KEY = " + s);
                System.out.println("VALUE = " + bundle.get(s));
            }
        }else{
            getCurrentActivity().runOnUiThread(new Runnable() {
                public void run() {
                    addLogLine(String.format("An error happened in claimng auth token flow, eMsg=\"%s\")", pidErrorDomain.getResultDescription()));
                    Toast.makeText(getApplicationContext(), "Error: " + pidErrorDomain.getResultDescription(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void didUntrustDevice() {
        getCurrentActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                addLogLine("The device was untrusted at the server");
                if(getCurrentActivity() instanceof LoginActivity) {
                    ((LoginActivity) getCurrentActivity()).hideScanButton(true);
                }
                if(getCurrentActivity() instanceof HomeActivity) {
                    ((LoginActivity) getCurrentActivity()).hideScanButton(true);
                }
            }
        });
    }

    @Override
    public void onGooglePlayServicesStatusReceived(int status) {

        //if we have an activity loaded when we get the result of the Google Play Services availability check
        //we can display the dialog . if not - we'll have to wait until an activity is loaded
        if (getCurrentActivity() != null) {
            handleGooglePlayServicesAvailabilityCheckResult(status);
        } else {
            googlePlayServicesAvailabilityCheckResult=status;
        }


    }

    @Override
    public void onOneTimePasscodeChanged(final String newOneTimePasscode) {
        addLogLine("One time passcode changed. new value : " + newOneTimePasscode);
        if(getCurrentActivity() != null && (getCurrentActivity() instanceof HomeActivity)) {
            getCurrentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((HomeActivity)getCurrentActivity()).setOneTimePasscodeViewText(newOneTimePasscode);
                }
            });

        }
    }


    private void createAuthenticationNotification(Bundle data) {

        final int PRIORITY_MAX=2;

        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        Intent resultIntent = new Intent(getApplicationContext(), AuthenticationActivity.class);
        resultIntent.putExtras(data);
        resultIntent.setAction("auth");

        PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AuthenticationData authenticationData = new Gson().fromJson(data.getString(KEY_CLIENT_CONTEXT), AuthenticationData.class);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle(builder);
        if (data.getString("body") == null) {
            inboxStyle.addLine(getString(R.string.step_up_accounts));
        }
        if (authenticationData != null) {
            inboxStyle.addLine(authenticationData.getMsg());
        }
        inboxStyle.setSummaryText("");

        String notificationText;
        String notificationTitle;
        if (authenticationData!=null && authenticationData.getTransactionType().equals(TRANSACTION_TYPE_AUTHENTICATION)) {
            //regular authentication
            if (data.getString("body") != null) {
                notificationText = data.getString("body");
                inboxStyle.addLine(data.getString("body"));
            }
            else {
                notificationText = getString(R.string.new_auth_notification_body);
                inboxStyle.addLine(getString(R.string.new_auth_notification_body));
            }
            if (data.getString("title") != null) {
                notificationTitle = data.getString("title");
            }
            else {
                notificationTitle = getString(R.string.new_auth_notification_title);
            }
        } else {
            //step up
            if (data.getString("body") != null) {
                notificationText = data.getString("body");
                inboxStyle.addLine(data.getString("body"));
            }
            else {
                notificationText = getString(R.string.step_up_accounts) + "\n" + (authenticationData != null ? authenticationData.getMsg() : "");
            }

            if (data.getString("title") != null) {
                notificationTitle = data.getString("title");
            }
            else {
                notificationTitle = getString(R.string.step_up_transfer_between_accounts);
            }

        }

        inboxStyle.setBigContentTitle(notificationTitle);

        //prepare notification buttons
        Intent approveIntent = new Intent(this, NotificationActionReceiver.class);
        approveIntent.setAction(INTENT_FILTER_APPROVE_AUTHENTICATION); //
        approveIntent.putExtras(data);
        PendingIntent pendingApproveIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, approveIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Action approveAction = new NotificationCompat.Action.Builder(0, getString(R.string.approve), pendingApproveIntent).build();

        Intent denyIntent = new Intent(this, NotificationActionReceiver.class);
        denyIntent.setAction(INTENT_FILTER_DENY_AUTHENTICATION);
        denyIntent.putExtras(data);
        PendingIntent pendingDenyIntent = PendingIntent.getBroadcast(getApplicationContext(), 2, denyIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Action denyAction = new NotificationCompat.Action.Builder(0, getString(R.string.deny), pendingDenyIntent).build();

        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        builder.setStyle(inboxStyle)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setDefaults(Notification.DEFAULT_ALL | Notification.FLAG_AUTO_CANCEL)
                .setPriority(PRIORITY_MAX)
                .setSmallIcon(R.drawable.app_icon_moderno_1024)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.app_icon_moderno_1024))
                .setContentIntent(resultPendingIntent)
                .addAction(approveAction)
                .addAction(denyAction)
                .setExtras(data)
                .setAutoCancel(true);

        mNotifyMgr.notify(NOTIFICATION_ID, builder.build());

        wakeupDevice();

    }

    private void removeNotification() {

        Log.i(TAG, "removeNotification");
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.cancel(NOTIFICATION_ID);
    }

    private void wakeupDevice() {
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "moderno:wakelocktag");
        wakeLock.acquire();
    }

    private void createPairingNotification(List<String> availableTrustLevels) {
        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        try {
            Intent resultIntent = new Intent(getApplicationContext(), LoginActivity.class);
            resultIntent.setAction("pairing");
            resultIntent.putStringArrayListExtra(TRUST_LEVELS, new ArrayList<>(availableTrustLevels));

            PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            String notificationText = availableTrustLevels.contains(PIDTrustLevelPrimary.getName()) ? getString(R.string.add_primary_device_prompt) : getString(R.string.add_trusted_device_prompt);
            String trustLevel = availableTrustLevels.contains(PIDTrustLevelPrimary.getName()) ? PIDTrustLevelPrimary.getName() : PIDTrustLevelTrusted.getName();

            //prepare notification style
            NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
            bigText.bigText(notificationText);
            bigText.setBigContentTitle(getString(R.string.app_name));

            //prepare notification buttons
            Intent approveIntent = new Intent(this, NotificationActionReceiver.class);
            approveIntent.setAction(INTENT_FILTER_APPROVE_PAIRING);
            approveIntent.putExtra(NotificationActionReceiver.TRUST_LEVEL, trustLevel);
            PendingIntent pendingApproveIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, approveIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            NotificationCompat.Action approveAction = new NotificationCompat.Action.Builder(0, getString(R.string.approve), pendingApproveIntent).build();

            Intent denyIntent = new Intent(this, NotificationActionReceiver.class);
            denyIntent.setAction(INTENT_FILTER_DENY_PAIRING);
            PendingIntent pendingDenyIntent = PendingIntent.getBroadcast(getApplicationContext(), 2, denyIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            NotificationCompat.Action denyAction = new NotificationCompat.Action.Builder(0, getString(R.string.deny), pendingDenyIntent).build();

            //prepare notification builder
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                            .setSmallIcon(R.drawable.app_icon_moderno_1024)
                            .setContentTitle(getString(R.string.app_name))
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.app_icon_moderno_1024))
                            .setContentIntent(resultPendingIntent)
                            .setContentText(notificationText)
                            .setDefaults(Notification.DEFAULT_ALL | Notification.FLAG_AUTO_CANCEL)
                            .setPriority(Notification.PRIORITY_MAX)
                            .setStyle(bigText)
                            .addAction(approveAction)
                            .addAction(denyAction)
                            .setAutoCancel(true);

            // Gets an instance of the NotificationManager service
            NotificationManager mNotifyMgr =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Builds the notification and issues it.
            mNotifyMgr.notify(NOTIFICATION_ID, mBuilder.build());

            wakeupDevice();
        } catch(Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public static void addLogLine(String line) {
        String logLine = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()) + "  - " + line + "\n";
        Log.i(TAG, logLine);
    }

    protected static void showStatus(final String info) {
        if (info == null) {
            return;
        }
        if (getInstance().getCurrentActivity() != null) {
            getInstance().getCurrentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getInstance().getCurrentActivity(), info, Toast.LENGTH_LONG).show();
                }
            });
        }
        Log.i(TAG, info);
    }

    protected void handleGooglePlayServicesAvailabilityCheckResult(int status){
        if (status == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
            //Google Play services needs to be updated - display a dialog tha requests the user to upgrade
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(getCurrentActivity(), ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED, PLAY_SERVICES_UPDATE_REQUEST, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    addLogLine("Google Play services upgrade dialog has been dismissed");
                }
            });

            try {
                dialog.show();
            } catch (WindowManager.BadTokenException badTokenException) {
                //in case the problem is that the app is in the background and cannot display the dialog
                Log.e(TAG, "onGooglePlayServicesStatusReceived : The application is in the background and therefore cannot display the dialog");
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

        } else {
            addLogLine("Google Play Services unavailable on this device. Authentication will work in offline mode only");
            ((BaseActivity) getCurrentActivity()).displayAlertDialog("Google Play Services", "Google Play Services unavailable on this device. Authentication will work in offline mode only", "OK", null);
        }

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.notification_channel_name);
            String description = getString(R.string.notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if(notificationManager!=null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public void setHomeActivityLoginFromAttemptLogin(boolean homeActivityLogout) {
        isHomeActivityLoginFromAttemptLogin = homeActivityLogout;
    }
}
