//
// Class Name : AuthenticatingDeviceData
// App name : Moderno
//
// Contains the authenticating device details - name, id etc.
//
// See LICENSE.txt for this sample’s licensing information and LICENSE_SDK.txt for the PingID SDK library licensing information.
// Created by Ping Identity on 3/23/17.
// Copyright © 2017 Ping Identity. All rights reserved.
//
package pingidsdk.pingidentity.com.simpledemo.beans;

import com.google.gson.annotations.SerializedName;

public class AuthenticatingDeviceData {

    @SerializedName("id")
    private String id=null;

    @SerializedName("deviceType")
    private String deviceType=null;

    @SerializedName("name")
    private String name=null;

    public String getId() {
        return id;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getName() {
        return name;
    }
}