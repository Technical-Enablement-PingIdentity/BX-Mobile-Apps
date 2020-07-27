//
// Class Name : AuthenticationData
// App name : Moderno
//
// Class containing data about the authentication
//
// See LICENSE.txt for this sample’s licensing information and LICENSE_SDK.txt for the PingID SDK library licensing information.
// Created by Ping Identity on 3/23/17.
// Copyright © 2017 Ping Identity. All rights reserved.
//
package pingidsdk.pingidentity.com.simpledemo.beans;

import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

public class AuthenticationData {

    public static final String TRANSACTION_TYPE_AUTHENTICATION = "AUTHENTICATION";

    @SerializedName("msg")
    private String msg=null;

    @SerializedName("transactionType")
    private String transactionType=null;

    public String getMsg() {
        return msg;
    }

    public String getTransactionType() {
        return transactionType;
    }
}
