package com.orange.lo.assetdemo.model;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

/**
 * Created by ZLGP6287 on 25/10/2016.
 */

/**
 * This Class implement the Data Model for the android demo simulator of the Live Object Demo
 */
@Data
public class DeviceDataTelemetry {

    private Integer temperature;
    @SerializedName("hygrometry") // typo
    private Integer hydrometry;
    /** Revolution per min **/
    private Integer revmin;
    private Integer CO2;
    private Integer pressure;
    private boolean doorOpen;


    DeviceDataTelemetry() {
        temperature = 0;
        hydrometry = 0;
        revmin = 0;
        CO2 = 400;
        pressure = 1000;
        doorOpen = false;
    }
    public static DeviceDataTelemetry clone(DeviceDataTelemetry value) {
        DeviceDataTelemetry clone = new DeviceDataTelemetry();
        clone.temperature = value.temperature;
        clone.hydrometry = value.hydrometry;
        clone.revmin = value.revmin;
        clone.CO2 = value.CO2;
        clone.pressure = value.pressure;
        clone.doorOpen= value.doorOpen;
        return clone;

    }
}
