package com.orange.lo.assetdemo.model;

/**
 * Created by ZLGP6287 on 25/10/2016.
 */

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.Getter;

/**
 * Structure of a "data message" that can be sent by a device into Live Objects.
 */
@Data
public class DeviceData {

    /**
     * Stream identifier : identifier of the timeseries this message belongs to
     */
    @SerializedName("s")
    private String streamId;

    /**
     * timestamp (ISO8601 format)
     */
    @SerializedName("ts")
    private  String timestamp;

    /**
     * Data "model" : a string identifying the schema used for the "value" part of the message,
     *   to avoid conflict at data indexing
     */
    @SerializedName("m")
    private String model;

    /**
     * Value : a free JSON object describing the collected information
     */
    @SerializedName("v")
    private Object value;

    /**
     * Tags : list of strings associated to the message to convey extra-information
     */
    @SerializedName("t")
    private List<String> tags;

    /**
     * Location [latitude, longitude]
     */
    @SerializedName("loc")
    private Double[] location;

}
