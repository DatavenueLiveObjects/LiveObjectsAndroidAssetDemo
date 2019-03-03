package com.orange.lo.assetdemo.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * Created by ZLGP6287 on 04/11/2016.
 */
@Data
public class DeviceResourceVersion {
    @SerializedName("v")
    String version;
    @SerializedName("m")
    Object meta;

    private transient String  url;

    private transient String  newVersion;

    public DeviceResourceVersion(String version, Object meta) {
        this.version = version;
        this.meta = meta;
    }

    public DeviceResourceVersion(String version) {
        this(version, new Object());

    }

}
