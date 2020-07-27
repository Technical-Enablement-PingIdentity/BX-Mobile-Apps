//
// Class Name : AuthenticationActivity
// App name : Moderno
//
// This activity is used for MFA user authentication. It can be modified to prompt the user for a PIN-Code,
// fingerprint or any other way to verify the user's identity
//
// See LICENSE.txt for this sample’s licensing information and LICENSE_SDK.txt for the PingID SDK library licensing information.
// Created by Ping Identity on 3/23/17.
// Copyright © 2017 Ping Identity. All rights reserved.
//

package pingidsdk.pingidentity.com.simpledemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import org.jose4j.json.internal.json_simple.JSONObject;
import org.jose4j.json.internal.json_simple.parser.JSONParser;
import org.jose4j.json.internal.json_simple.parser.ParseException;

import pingidsdk.pingidentity.com.simpledemo.beans.AuthenticationData;
import pingidsdkclient.PIDUserSelectionObject;
import pingidsdkclient.PingID;

import static pingidsdk.pingidentity.com.simpledemo.beans.AuthenticationData.TRANSACTION_TYPE_AUTHENTICATION;

public class AuthenticationActivity extends BaseActivity {

    public static final String TAG = AuthenticationActivity.class.getName();

    private static final String KEY_TIMEOUT = "timeout";
    public static final String KEY_CLIENT_CONTEXT = "client_context";
    private boolean isTransaction = false; //regular authentication or transaction approval
    CountDownTimer countDownTimer;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        TextView textViewCaption1 =  findViewById(R.id.transactionApprovalCaption);
        TextView textViewCaption2 =  findViewById(R.id.textCaption2);
        TextView textViewCaption3 =  findViewById(R.id.textCaption3);
        TextView textViewCaption4 =  findViewById(R.id.textCaption4);
        AuthenticationData authenticationData;
        String clientContext = getIntent().getExtras().containsKey(KEY_CLIENT_CONTEXT) ? getIntent().getStringExtra(KEY_CLIENT_CONTEXT) : "{\"msg\" : \"Please Authenticate\"}";
        if(clientContext.isEmpty()){
            clientContext = "{\"msg\" : \"Please Authenticate\"}";
        }
        //if there are text extras for the captions - get them and display them
        if(getIntent().getExtras()!=null && getIntent().getStringExtra("authentication_token_status")!=null){
            //show dialog instead of activity screen
            LinearLayout authLayout = (LinearLayout) findViewById(R.id.viewAuth);
            authLayout.setVisibility(View.INVISIBLE);
            isTransaction=false;
            authenticationData = new Gson().fromJson(clientContext, AuthenticationData.class);
            String userString = getIntent().getStringExtra("user");
            String username = null;
            if(userString!=null) {
                try {
                    JSONParser parser = new JSONParser();
                    final JSONObject user = (JSONObject) parser.parse(userString);
                    username = (String) user.get("username");
                }catch (ParseException e) {
                    e.printStackTrace();
                }

            }else{
                 username = getIntent().getExtras().getString("username");
            }

            showUserApprovalDialog(authenticationData, username);

        }else if (getIntent()!=null && getIntent().getExtras()!=null && getIntent().getExtras().containsKey(KEY_CLIENT_CONTEXT) && getIntent().getExtras().getString(KEY_CLIENT_CONTEXT)!=null) {

            //get the authentication data (which will be used later)
            authenticationData = new Gson().fromJson(clientContext, AuthenticationData.class);
            if (TRANSACTION_TYPE_AUTHENTICATION.equals(authenticationData.getTransactionType())){
                //regular authentication
                isTransaction=false;
                textViewCaption3.setText(getString(R.string.prompt_username));
                textViewCaption4.setText(authenticationData.getMsg());
                textViewCaption3.setVisibility(View.VISIBLE);
                textViewCaption4.setVisibility(View.VISIBLE);
            }else{
                //step up
                isTransaction=true;
                textViewCaption1.setText(getString(R.string.auth_transaction_authorization));
                textViewCaption4.setText(authenticationData.getMsg());
                textViewCaption2.setVisibility(View.VISIBLE);
                textViewCaption3.setVisibility(View.VISIBLE);
                textViewCaption4.setVisibility(View.VISIBLE);
            }

            //store the clientContext data in the global has hmap for future use
            if (PingIDSdkDemoApplication.globalData.containsKey(KEY_CLIENT_CONTEXT)){
                PingIDSdkDemoApplication.globalData.remove(KEY_CLIENT_CONTEXT);
            }
            PingIDSdkDemoApplication.globalData.put(KEY_CLIENT_CONTEXT, clientContext);
        }else{
            textViewCaption1.setText(getString(R.string.auth_please_authenticate));
        }

        //prepare the "approve" button
        final Button approveButton = (Button)findViewById(R.id.approveButton);
        approveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                approveButton.setEnabled(false);
                PIDUserSelectionObject pidUserSelectionObject = new PIDUserSelectionObject();
                pidUserSelectionObject.setPidActionType(PingID.PIDActionType.PIDActionTypeApprove);
                if(getIntent().getExtras().getString("username") != null) {
                    pidUserSelectionObject.setPidUsername(getIntent().getExtras().getString("username"));
                }
                setAuthenticationStatus(pidUserSelectionObject);

            }
        });

        //prepare the "deny" button
        final Button denyButton = (Button)findViewById(R.id.denyButton);
        denyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                denyButton.setEnabled(false);
                approveButton.setEnabled(false);
                setAuthenticationStatus(PingID.PIDActionType.PIDActionTypeDeny);

            }
        });

        //set up a countdown timer for the screen. it will close after the specified interval (default is 40 seconds)
        int timeout = 40000; //default value is 40 seconds
        if (getIntent()!=null && getIntent().getExtras()!=null && getIntent().getExtras().containsKey(KEY_TIMEOUT)) {
            timeout = getIntent().getIntExtra(KEY_TIMEOUT, 40000);
        }
        countDownTimer = new CountDownTimer(timeout, timeout) {

            @Override
            public void onTick(long millisUntilFinished) {
                // do nothing
            }

            @Override
            public void onFinish() {
                Log.i(TAG, "Authentication timeout. Closing the activity");
                displayStatus(true);
            }

        }.start();

    }

    private void showUserApprovalDialog(AuthenticationData authenticationData, final String username) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(username + ", " + authenticationData.getMsg())
                .setTitle(R.string.sign_on);
        // Add the buttons
        builder.setPositiveButton(R.string.approve, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                PIDUserSelectionObject pidUserSelectionObject = new PIDUserSelectionObject();
                pidUserSelectionObject.setPidActionType(PingID.PIDActionType.PIDActionTypeApprove);
                pidUserSelectionObject.setPidTrustLevel(PingID.PIDTrustLevel.PIDTrustLevelTrusted);
                pidUserSelectionObject.setPidUsername(username);
                setAuthenticationStatus(pidUserSelectionObject);
            }
        });
        builder.setNegativeButton(R.string.deny, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                PIDUserSelectionObject pidUserSelectionObject = new PIDUserSelectionObject();
                pidUserSelectionObject.setPidActionType(PingID.PIDActionType.PIDActionTypeDeny);
                pidUserSelectionObject.setPidTrustLevel(PingID.PIDTrustLevel.PIDTrustLevelTrusted);
                pidUserSelectionObject.setPidUsername(username);
                setAuthenticationStatus(pidUserSelectionObject);
            }
        });
        dialog = builder.create();
        dialog.show();
    }

    //This function will be triggered after the user has completed the authentication (approving or denying or timeout)
    //so that the UI will be able display the result to the user
    public void onAuthenticationCompleted(final PingID.PIDActionStatus status, final PingID.PIDActionType actionType){
        if (actionType.equals(PingID.PIDActionType.PIDActionTypeApprove)){
            if (status.equals(PingID.PIDActionStatus.SUCCESS)) {
                //if the flow was completed successfully - display success msg
                displayStatus(false);
            }else{
                displayAlertDialog("", getString(R.string.error_authentication_failed), getString(R.string.ok), new Runnable() {
                    @Override
                    public void run() {
                        AuthenticationActivity.this.finish();
                    }
                });
            }
        }else{
            //this close block will be execute in case the user clicked on "Deny"
            //if the user clicked "deny" - display a msg
            if (isTransaction) {
                displayAlertDialog("", getString(R.string.transaction_denied), getString(R.string.ok), new Runnable() {
                    @Override
                    public void run() {
                        AuthenticationActivity.this.finish();
                    }
                });
            }else{
                displayAlertDialog("", getString(R.string.authentication_denied), getString(R.string.ok), new Runnable() {
                    @Override
                    public void run() {
                        AuthenticationActivity.this.finish();
                    }
                });
            }
        }
    }

    //Display the status of an authentication
    public void displayStatus(boolean isTimeout){
        LinearLayout viewSuccess = (LinearLayout)this.findViewById(R.id.viewSuccess);
        LinearLayout viewAuth = (LinearLayout)this.findViewById(R.id.viewAuth);
        ImageView approvedView = (ImageView)findViewById(R.id.approvedImage);
        ImageView transferredView = (ImageView)findViewById(R.id.transferredImage);
        ImageView timeoutView = (ImageView)findViewById(R.id.timeoutImage);
        if(dialog!=null && dialog.isShowing()){
            dialog.dismiss();
        }
        if (isTimeout){
            timeoutView.setVisibility(View.VISIBLE);
        }else {
            if (isTransaction) {
                transferredView.setVisibility(View.VISIBLE);
            } else {
                approvedView.setVisibility(View.VISIBLE);
            }
        }
        viewSuccess.setVisibility(View.VISIBLE);
        viewAuth.setVisibility(View.GONE);
        waitAndCloseActivity();
    }


    //finalize the authentication status based on the button the user clicked - approve/deny
    private void setAuthenticationStatus(PingID.PIDActionType actionType) {

        try {
            Log.i(TAG, "setAuthenticationStatus triggered. actionType=" + actionType.name());
            countDownTimer.cancel();
            PingID.getInstance().setAuthenticationUserSelection(actionType);

            //waitAndCloseActivity();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //finalize the authentication status based on the button the user clicked - approve/deny
    private void setAuthenticationStatus(PIDUserSelectionObject userSelectionObject) {

        try {
            Log.i(TAG, "setAuthenticationStatus triggered. actionType=" + userSelectionObject.getPidActionType().name());
            countDownTimer.cancel();
            PingID.getInstance().setAuthenticationUserSelection(userSelectionObject);

            //waitAndCloseActivity();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Wait for 3 seconds and them close the activity
    private void waitAndCloseActivity(){
        //wait 3 seconds before closing the activity
        new CountDownTimer(4000, 4000) {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                finish();
            }

        }.start();
    }

    @Override
    void changeScreenAvailability(boolean available) {
        //nothing to implement here yet
    }

    @Override
    void hideScanButton(boolean hide) {

    }
}
