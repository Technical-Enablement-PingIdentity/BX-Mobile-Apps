//
// Class Name : HomeActivity
// App name : Moderno
//
// This activity is the main activity.
//
// See LICENSE.txt for this sample’s licensing information and LICENSE_SDK.txt for the PingID SDK library licensing information.
// Created by Ping Identity on 3/23/17.
// Copyright © 2017 Ping Identity. All rights reserved.
//

package pingidsdk.pingidentity.com.simpledemo;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import pingidsdkclient.PingID;

public class HomeActivity extends BaseActivity {

    private TextView mOneTimePasscodeView;
    private Button scanButton;
    private Button newOtpButton;
    public static final String KEY_SUM = "KEY_SUM";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLight.ttf");

        TextView accountBalanceTextView = findViewById(R.id.accountBalanceTextView);
        if (accountBalanceTextView != null) {
            if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().getString(KEY_SUM) != null && getIntent().getExtras().getString(KEY_SUM).length() > 0) {
                accountBalanceTextView.setText(getIntent().getExtras().getString(KEY_SUM));
            } else {
                accountBalanceTextView.setText("4,977.00");
            }
        }

        newOtpButton = findViewById(R.id.new_passcode_button);
        newOtpButton.setTypeface(font);
        newOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newOtpButton.setEnabled(false);
                try {
                    PingID.getInstance().getRestrictiveOneTimePasscode(new PingID.GetRestrictiveOneTimePasscodeCallback() {
                        @Override
                        public void onRestrictivePasscodeGenerated(Pair<String, PingID.PIDOneTimePasscodeStatus> pair) {
                            String otp = null;
                            if (PingID.PIDOneTimePasscodeStatus.PIDOneTimePasscodeOK != pair.second){
                                Log.i(TAG, "Error getting OneTimePasscode " + pair.second.name());
                            }else{
                                otp = pair.first;
                            }
                            final String finalOtp = otp;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    newOtpButton.setEnabled(true);
                                    if(finalOtp!=null) {
                                        mOneTimePasscodeView.setText(finalOtp);
                                    }
                                }
                            });
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    newOtpButton.setEnabled(true);
                }
            }
        });

        scanButton = findViewById(R.id.buttonScan);
        scanButton.setTypeface(font);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchScanActivity();
            }
        });

        Button mMenuButton = (Button) findViewById(R.id.buttonMenu);
        mMenuButton.setTypeface(font);
        mMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                launchLoginActivity();
            }
        });

        mOneTimePasscodeView = (TextView)findViewById(R.id.OneTimePasscodeTextView);
        if(PingID.getInstance().isDeviceTrusted()) {
            hideScanButton(false);
        } else {
            hideScanButton(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        newOtpButton.setEnabled(false);
        try {
            PingID.getInstance().getRestrictiveOneTimePasscode(new PingID.GetRestrictiveOneTimePasscodeCallback() {
                @Override
                public void onRestrictivePasscodeGenerated(Pair<String, PingID.PIDOneTimePasscodeStatus> pair) {
                    String otp = null;
                    if (PingID.PIDOneTimePasscodeStatus.PIDOneTimePasscodeOK != pair.second){
                        Log.i(TAG, "Error getting OneTimePasscode " + pair.second.name());
                    }else{
                        otp = pair.first;
                    }
                    final String finalOtp = otp;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            newOtpButton.setEnabled(true);
                            if(finalOtp!=null) {
                                mOneTimePasscodeView.setText(finalOtp);
                            }
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            newOtpButton.setEnabled(true);
        }
    }

    public void setOneTimePasscodeViewText(String newOneTimePasscode){
        if(mOneTimePasscodeView != null) {
            mOneTimePasscodeView.setText(newOneTimePasscode);
        }
    }
    @Override
    void changeScreenAvailability(boolean available) {
        //nothing to implement here yet
    }

    @Override
    void hideScanButton(boolean hide) {
        if(scanButton != null) {
            if(hide) {
                scanButton.setVisibility(View.INVISIBLE);
            } else {
                scanButton.setVisibility(View.VISIBLE);
            }
        }
    }


}
