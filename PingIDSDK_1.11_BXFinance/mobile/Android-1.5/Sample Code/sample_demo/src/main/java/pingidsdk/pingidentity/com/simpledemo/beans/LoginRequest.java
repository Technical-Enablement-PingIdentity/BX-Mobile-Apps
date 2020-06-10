//
// Class Name : LoginRequest
// App name : Moderno
//
// LoginRequest class
//
// See LICENSE.txt for this sample’s licensing information and LICENSE_SDK.txt for the PingID SDK library licensing information.
// Created by Ping Identity on 3/23/17.
// Copyright © 2017 Ping Identity. All rights reserved.
//
package pingidsdk.pingidentity.com.simpledemo.beans;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {

    @SerializedName("pingIdPayload")
    private String pingIdPayload=null;

    @SerializedName("operation")
    private String operation=null;

    public String getPingIdPayload() {
        return pingIdPayload;
    }

    public void setPingIdPayload(String pingIdPayload) {
        this.pingIdPayload = pingIdPayload;
    }

    public String getOrgAlias() {
        return orgAlias;
    }

    public void setOrgAlias(String orgAlias) {
        this.orgAlias = orgAlias;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }



    @SerializedName("orgAlias")
    private String orgAlias=null;

    @SerializedName("authType")
    private String authType = null;

    @SerializedName("user")
    private String user = null;

    @SerializedName("password")
    private String password = null;

    @SerializedName("otp")
    private String otp = null;

    @SerializedName("sessionId")
    private String sessionId = null;

    @SerializedName("appId")
    private String appId = null;

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}
