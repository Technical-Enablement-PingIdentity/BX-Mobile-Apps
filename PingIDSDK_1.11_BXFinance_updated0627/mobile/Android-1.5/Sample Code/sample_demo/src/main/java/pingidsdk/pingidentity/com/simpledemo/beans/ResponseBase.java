//
// Class Name : ResponseBase
// App name : Moderno
//
// Response base class
//
// See LICENSE.txt for this sample’s licensing information and LICENSE_SDK.txt for the PingID SDK library licensing information.
// Created by Ping Identity on 3/23/17.
// Copyright © 2017 Ping Identity. All rights reserved.
//
package pingidsdk.pingidentity.com.simpledemo.beans;

import com.google.gson.annotations.SerializedName;

public class ResponseBase {

    @SerializedName("status")
    private int status;

    @SerializedName("description")
    private String description;

    public int getStatus(){
        return status;
    }

    public String getDescription(){
        return description;
    }

}
