package com.orange.lo.assetdemo.model;

/**
 * Created by ZLGP6287 on 25/10/2016.
 */

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

/**
 * Structure of a "configuration message" that can be sent by a device into Live Objects.
 */
public class DeviceConfig {

    /**
     * Configuration parameter
     */
    @Data
    public static class CfgParameter {
        public CfgParameter(String type, Object value) {
            this.type = type;
            this.value = value;
        }
        /**
         * Configuration parameter type ("str", "bin", "f64", "u32" or "i32")
         */
        @SerializedName("t")
        private String type;

        /**
         * Configuration parameter value: must match the parameter type:
         *  str: String
         *  bin: Base64 encoded string
         *  f64: Double
         *  u32 : Long
         *  i32 : Integer
         */
        @SerializedName("v")
        private Object value;

    }

    /**
     * current device configuration
     */
    @SerializedName("cfg")
    public final Map<String, CfgParameter> current = new HashMap<>();

    @Getter
    private Long cid;


}