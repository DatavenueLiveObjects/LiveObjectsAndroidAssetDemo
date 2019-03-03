package com.orange.lo.assetdemo.model;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

/**
 * Created by ZLGP6287 on 27/10/2016.
 */
@Data
public class DeviceResourceUpdate {

    @Data
    public class Model {
        String size;
        String uri;
        String md5;
    }

    String id;

    @SerializedName("old")
    String oldVersion;
    @SerializedName("new")
    String newVersion;

    @SerializedName("m")
    Model model = new Model();

    String cid;




}
