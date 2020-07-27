//
// Class Name : NotificationActionReceiver
// App name : Moderno
//
// This broadcast receiver handles intents from the notification buttons
//
// See LICENSE.txt for this sample’s licensing information and LICENSE_SDK.txt for the PingID SDK library licensing information.
// Created by Ping Identity on 3/23/17.
// Copyright © 2017 Ping Identity. All rights reserved.
//

package pingidsdk.pingidentity.com.simpledemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import pingidsdkclient.PIDUserSelectionObject;
import pingidsdkclient.PingID;

public class NotificationActionReceiver extends BroadcastReceiver {

    public static final String INTENT_FILTER_APPROVE_AUTHENTICATION = "pingidsdk.pingidentity.com.simpledemo.APPROVE_AUTHENTICATION";
    public static final String INTENT_FILTER_DENY_AUTHENTICATION = "pingidsdk.pingidentity.com.simpledemo.DENY_AUTHENTICATION";
    public static final String INTENT_FILTER_APPROVE_PAIRING = "pingidsdk.pingidentity.com.simpledemo.APPROVE_PAIRING";
    public static final String INTENT_FILTER_DENY_PAIRING = "pingidsdk.pingidentity.com.simpledemo.DENY_PAIRING";
    public static final String TRUST_LEVEL = "TRUST_LEVEL";

    @Override
    public void onReceive(Context context, Intent intent) {
        //
        try {
            String action = intent.getAction();
            if (action.equals(INTENT_FILTER_APPROVE_AUTHENTICATION)) {
                //if the user clicked on "approve" in the notification - approve the authentication
                PingID.getInstance().setAuthenticationUserSelection(PingID.PIDActionType.PIDActionTypeApprove);
            } else if (action.equals(INTENT_FILTER_DENY_AUTHENTICATION)) {
                //if the user clicked on "deny" in the notification - deny the authentication
                PingID.getInstance().setAuthenticationUserSelection(PingID.PIDActionType.PIDActionTypeDeny);
            } else if (action.equals(INTENT_FILTER_APPROVE_PAIRING)) {
                //if the user clicked on "deny" in the notification - approve the pairing
                PingID.PIDTrustLevel trustLevel;
                if (intent.getExtras().getString(TRUST_LEVEL).equals(PingID.PIDTrustLevel.PIDTrustLevelPrimary.getName())){
                    trustLevel = PingID.PIDTrustLevel.PIDTrustLevelPrimary;
                }else{
                    trustLevel = PingID.PIDTrustLevel.PIDTrustLevelTrusted;
                }
                PIDUserSelectionObject pidUserSelectionObject = new PIDUserSelectionObject();
                pidUserSelectionObject.setPidActionType(PingID.PIDActionType.PIDActionTypeApprove);
                pidUserSelectionObject.setPidTrustLevel(trustLevel);
                PingID.getInstance().setUserSelection(pidUserSelectionObject);
            } else if (action.equals(INTENT_FILTER_DENY_PAIRING)) {
                //if the user clicked on "deny" in the notification - deny the pairing
                PIDUserSelectionObject pidUserSelectionObject = new PIDUserSelectionObject();
                pidUserSelectionObject.setPidActionType(PingID.PIDActionType.PIDActionTypeDeny);
                PingID.getInstance().setUserSelection(pidUserSelectionObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
