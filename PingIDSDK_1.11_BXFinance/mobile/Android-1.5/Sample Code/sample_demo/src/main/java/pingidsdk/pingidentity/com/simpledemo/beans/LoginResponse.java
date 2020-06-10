//
// Class Name : LoginResponse
// App name : Moderno
//
// LoginResponse class
//
// See LICENSE.txt for this sample’s licensing information and LICENSE_SDK.txt for the PingID SDK library licensing information.
// Created by Ping Identity on 3/23/17.
// Copyright © 2017 Ping Identity. All rights reserved.
//
package pingidsdk.pingidentity.com.simpledemo.beans;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LoginResponse extends pingidsdk.pingidentity.com.simpledemo.beans.ResponseBase {

    @SerializedName("pingIdPayload")
    private String pingIdPayload=null;

    @SerializedName("authSessionID")
    private String authSessionID=null;

    @SerializedName("sum")
    private String sum=null;

    public String getAuthSessionID() {
        return authSessionID;
    }

    @SerializedName("isPingIdAuthenticated")
    private boolean isPingIdAuthenticated=false;

    public String getServerPayload() {
        return pingIdPayload;
    }

    public boolean getIsPingIdAuthenticated() {
        return isPingIdAuthenticated;
    }

    @SerializedName("currentAuthenticatingDeviceData")
    private pingidsdk.pingidentity.com.simpledemo.beans.AuthenticatingDeviceData currentAuthenticatingDeviceData=null;

    public pingidsdk.pingidentity.com.simpledemo.beans.AuthenticatingDeviceData getCurrentAuthenticatingDeviceData(){
        return currentAuthenticatingDeviceData;
    }

    @SerializedName("availableDevicesForAuthentication")
    private List<pingidsdk.pingidentity.com.simpledemo.beans.AuthenticatingDeviceData> availableDevicesForAuthentication=null;

    public List<pingidsdk.pingidentity.com.simpledemo.beans.AuthenticatingDeviceData> getAvailableDevicesForAuthentication() {
        return availableDevicesForAuthentication;
    }


    public boolean isPingIdAuthenticated() {
        return isPingIdAuthenticated;
    }

    public void setPingIdAuthenticated(boolean pingIdAuthenticated) {
        isPingIdAuthenticated = pingIdAuthenticated;
    }

    public String getSum() {
        return sum;
    }
}
