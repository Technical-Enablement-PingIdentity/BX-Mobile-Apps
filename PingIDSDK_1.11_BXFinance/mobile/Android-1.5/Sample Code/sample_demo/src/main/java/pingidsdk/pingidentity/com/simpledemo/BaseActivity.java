//
// Class Name : BaseActivity
// App name : Moderno
//
// This base activity class contains the boiler plate code to
// support PingID for Customers functionality across an application
//
// See LICENSE.txt for this sample’s licensing information and LICENSE_SDK.txt for the PingID SDK library licensing information.
// Created by Ping Identity on 3/23/17.
// Copyright © 2017 Ping Identity. All rights reserved.
//
package pingidsdk.pingidentity.com.simpledemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.List;

import pingidsdk.pingidentity.com.simpledemo.fingerprint.FingerprintAuthenticationDialogFragment;
import pingidsdkclient.PIDUserSelectionObject;
import pingidsdkclient.PingID;

import static pingidsdk.pingidentity.com.simpledemo.PingIDSdkDemoApplication.addLogLine;


public abstract class BaseActivity extends Activity  {

    public final static String TAG = BaseActivity.class.getName();
    public static final int PLAY_SERVICES_UPDATE_REQUEST = 9001;
    public static final String TRUST_LEVELS = "TRUST_LEVELS";
    private static final int CAMERA_ACTIVITY_REQUEST_CODE = 112;
    private static final String FINGERPRINT_AUTH_DIALOG_TAG = "fingerprint_authentication_fragment";
    FingerprintAuthenticationDialogFragment fragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getIntent() !=null && getIntent().getData()!=null && getIntent().getData().getQueryParameter("authentication_token")!=null){
            Log.i(TAG, "Deep linking intent received on creation");
            PingID.getInstance().validateAuthenticationToken(getIntent().getData().getQueryParameter("authentication_token"));
            changeScreenAvailability(false);
        }
    }

    private RelativeLayout successLayout;
    /*
    Create a dialog that enables the user to approve or deny the addition of a new device
    to his network of trusted devices.
     */
    public void displayAddDeviceToNetworkDialog(final List<String> availableTrustLevels) {

        if (availableTrustLevels.contains(PingID.PIDTrustLevel.PIDTrustLevelPrimary.getName()) || availableTrustLevels.contains(PingID.PIDTrustLevel.PIDTrustLevelTrusted.getName())) {

            //ask the user for approval
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    try{
                        PingID.PIDTrustLevel trustLevel = availableTrustLevels.contains(PingID.PIDTrustLevel.PIDTrustLevelPrimary.getName()) ? PingID.PIDTrustLevel.PIDTrustLevelPrimary : PingID.PIDTrustLevel.PIDTrustLevelTrusted;
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                try {
                                    PIDUserSelectionObject pidUserSelectionObject = new PIDUserSelectionObject();
                                    pidUserSelectionObject.setPidActionType(PingID.PIDActionType.PIDActionTypeApprove);
                                    pidUserSelectionObject.setPidTrustLevel(trustLevel);
                                    PingID.getInstance().setUserSelection(pidUserSelectionObject);
                                    //display a message to the user with status
                                    if (availableTrustLevels.contains(PingID.PIDTrustLevel.PIDTrustLevelPrimary.getName())) {
                                        Toast.makeText(getApplicationContext(), R.string.pairing_pairing_now, Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), R.string.pairing_new_device_will_be_paired, Toast.LENGTH_LONG).show();
                                    }
                                    changeScreenAvailability(false);
                                } catch (Throwable throwable) {
                                    throwable.printStackTrace();
                                    Toast.makeText(getApplicationContext(), R.string.pairing_problem, Toast.LENGTH_LONG).show();
                                    //get reference to the progress because we need to activate it
                                    changeScreenAvailability(true);
                                }
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                try {
                                    PIDUserSelectionObject pidUserSelectionObject = new PIDUserSelectionObject();
                                    pidUserSelectionObject.setPidActionType(PingID.PIDActionType.PIDActionTypeDeny);
                                    PingID.getInstance().setUserSelection(pidUserSelectionObject);
                                    Toast.makeText(getApplicationContext(), R.string.pairing_denied, Toast.LENGTH_LONG).show();
                                    if (BaseActivity.this instanceof LoginActivity && availableTrustLevels.contains(PingID.PIDTrustLevel.PIDTrustLevelPrimary.getName())) {
                                        ((LoginActivity) BaseActivity.this).launchHomeActivity();
                                    }
                                } catch (Throwable throwable) {
                                    throwable.printStackTrace();
                                    Toast.makeText(getApplicationContext(), R.string.pairing_problem, Toast.LENGTH_LONG).show();

                                } finally {
                                    changeScreenAvailability(true);
                                }

                                break;
                        }

                    }catch(WindowManager.BadTokenException badTokenException){
                        //in case the problem is that the app is in the background and cannot display the dialog
                        Log.e(TAG,"displayAddDeviceToNetworkDialog : The application is in the background and therefore cannot display the dialog");
                        badTokenException.printStackTrace();
                    }catch(Throwable throwable){
                        throwable.printStackTrace();
                    }
                }
            };

            DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    //enable the login button if the dialog is cancelled
                    changeScreenAvailability(true);
                }
            };

            //get the text to display to the user according to the available that were received from the server
            String msg = availableTrustLevels.contains(PingID.PIDTrustLevel.PIDTrustLevelPrimary.getName()) ? getString(R.string.add_primary_device_prompt) : getString(R.string.add_trusted_device_prompt);
            //display the dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(PingIDSdkDemoApplication.getInstance().getCurrentActivity()); //BaseActivity.this
            try{
                builder.setMessage(msg)
                    .setTitle(R.string.add_device_title)
                    .setPositiveButton(getString(R.string.approve), dialogClickListener)
                    .setNegativeButton(getString(R.string.deny), dialogClickListener)
                    .setOnCancelListener(cancelListener)
                    .show();
            }catch(WindowManager.BadTokenException badTokenException){
                //in case the problem is that the app is in the background and cannot display the dialog
                Log.e(TAG,"displayAddDeviceToNetworkDialog : The application is in the background and therefore cannot display the dialog");
                badTokenException.printStackTrace();
            }catch(Throwable throwable){
                throwable.printStackTrace();
            }

        }else{
            addLogLine("No available trust level to prompt the user.");
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent !=null && intent.getData()!=null && intent.getData().getQueryParameter("authentication_token")!=null){
            Log.i(TAG, "Deep linking intent as new intent");
            PingID.getInstance().validateAuthenticationToken(intent.getData().getQueryParameter("authentication_token"));
            changeScreenAvailability(false);
        }
    }

    //display a general alert dialog
    protected void displayAlertDialog(String title, String msg, String buttonText, final Runnable runAfter) {

        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(PingIDSdkDemoApplication.getInstance().getCurrentActivity());

            builder.setTitle(title);
            builder.setMessage(msg);

            builder.setPositiveButton(buttonText, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (runAfter!=null){ //if we have a runnable to run - run it
                        runAfter.run();
                    }
                }
            });

            AlertDialog alert = builder.create();

            try{
                alert.show();
            }catch(WindowManager.BadTokenException badTokenException){
                //in case the problem is that the app is in the background and cannot display the dialog
                Log.e(TAG,"displayAlertDialog : The application is in the background and therefore cannot display the dialog");
            }catch(Throwable throwable){
                throwable.printStackTrace();
            }
        }catch(Throwable throwable){
            throwable.printStackTrace();
        }
    }

    protected void showSuccess(){
        successLayout = (RelativeLayout) findViewById(R.id.layoutSuccess);
        if(successLayout!=null){
            successLayout.setVisibility(View.VISIBLE);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    successLayout.setVisibility(View.GONE);
                    if(BaseActivity.this instanceof AuthenticationActivity){
                        finish();
                    }
                }
            }, 2000);
        }
    }

    //launch the authentication activity
    protected void launchAuthenticationActivity(Bundle data) {
        Intent intent = new Intent(this, AuthenticationActivity.class);
        intent.putExtras(data);
        startActivity(intent);
    }

    //launch the user selection activity
    private void launchUserChoiceActivity(Bundle bundle) {
        Intent intent = new Intent(this, UserChoiceActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    //launch the Login activity
    protected void launchLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    protected void launchScanActivity(){
        /*
         * Check if need to perform biometric authentication
         * before launching scanning activity
         */
        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.M){
            FingerprintManager fingerprintManager = getSystemService(FingerprintManager.class);
            /* Please note that now the protection level of USE_FINGERPRINT permission is normal
             * instead of dangerous.
             * See http://developer.android.com/reference/android/Manifest.permission.html#USE_FINGERPRINT
             * The line below prevents the false positive inspection from Android Studio
             */
            if(fingerprintManager!=null && fingerprintManager.isHardwareDetected() && fingerprintManager.hasEnrolledFingerprints()){
                fragment = new FingerprintAuthenticationDialogFragment();
                fragment.show(getFragmentManager(), FINGERPRINT_AUTH_DIALOG_TAG);
            }else{
                launchCameraActivity();
            }
        }else {
           launchCameraActivity();
        }

    }

    private void launchCameraActivity(){
        Intent intent = new Intent(this, CameraActivity.class);
        startActivityForResult(intent, CAMERA_ACTIVITY_REQUEST_CODE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onFingerPrintAuthenticatedCallback(){
        fragment.dismissAllowingStateLoss();
        launchCameraActivity();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CAMERA_ACTIVITY_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                String token = data.getStringExtra("token");
                token = token.substring(token.indexOf('=') + 1);
                PingID.getInstance().validateAuthenticationToken(token);
            }
        }
    }

    protected void requestUserApproval(Bundle bundle){
        launchAuthenticationActivity(bundle);
    }

    protected void requestUserSelection(Bundle bundle){
        launchUserChoiceActivity(bundle);
    }


    abstract void changeScreenAvailability(boolean available);

    abstract void hideScanButton(boolean hide);
}
