package com.orange.lo.assetdemo.model;

/**
 * Created by ZLGP6287 on 25/10/2016.
 */

import java.util.Map;

import lombok.Getter;

public class DeviceCommandResponse<T> {

    public DeviceCommandResponse() {
        this.res = null;
        this.cid = null;
    }

    public DeviceCommandResponse(Map<String, T> res, Long cid) {
        this.res = res;
        this.cid = cid;
    }

    @Getter
    private final Map<String, T> res;
    @Getter
    private final Long cid;

}