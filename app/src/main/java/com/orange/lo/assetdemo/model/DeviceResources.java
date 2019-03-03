package com.orange.lo.assetdemo.model;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;

/**
 * Created by ZLGP6287 on 27/10/2016.
 */
public class DeviceResources {


    public DeviceResources() {
        rsc = new LinkedHashMap<>();
    }

    @Getter
    private final Map<String, DeviceResourceVersion> rsc;

}
