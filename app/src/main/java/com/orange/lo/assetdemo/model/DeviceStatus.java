package com.orange.lo.assetdemo.model;

import lombok.Data;
import lombok.Getter;

/**
 * Created by ZLGP6287 on 02/11/2016.
 */
@Data
public class DeviceStatus {
    public class DeviceStatusInfo {
        @Getter
        private String model;
        @Getter
        private String version;
    }

    public DeviceStatus(String model, String version) {
        this.info = new DeviceStatusInfo();
        this.info.model = model;
        this.info.version = version;
    }

    @Getter
    private DeviceStatusInfo info;
}
