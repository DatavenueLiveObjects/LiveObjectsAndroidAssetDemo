package com.orange.lo.assetdemo.model;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

/**
 * Created by ZLGP6287 on 04/11/2016.
 */
@Data
public class DeviceResourceUpdateResponse {
    public enum ResponseStatus {
        OK,
        UNKNOWN_RESOURCE,
        WRONG_SOURCE_VERSION,
        INVALID_RESOURCE,
        NOT_AUTHORIZED,
        INTERNAL_ERROR
    }

    @SerializedName("res")
    ResponseStatus responseStatus;
    String cid;

    public DeviceResourceUpdateResponse(ResponseStatus responseStatus, String cid) {
        this.responseStatus = responseStatus;
        this.cid = cid;
    }

}

