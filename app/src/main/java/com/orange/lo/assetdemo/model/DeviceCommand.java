package com.orange.lo.assetdemo.model;

import java.util.Map;

import lombok.Getter;

/**
 * Created by ZLGP6287 on 25/10/2016.
 */

public class DeviceCommand {

    /* Request : string identifying the method called on the device */
    @Getter
    private final String req;

    /**
     * name and value (any valid JSON value) of an argument passed to the request call */
    @Getter
    private final Map<String, Object> arg;

    /**
     * Correlation Id :  an identifier that must be returned in the command response to help
     *  Live Objects match the response and request */
    @Getter
    private final Long cid;

    public DeviceCommand(Long cid, String req, Map<String, Object> arg) {
        this.cid = cid;
        this.req = req;
        this.arg = arg;
    }

    @Override
    public String toString() {
        return "DeviceCommand{" +
                "req='" + req + '\'' +
                ", arg=" + arg +
                ", cid=" + cid +
                '}';
    }
}