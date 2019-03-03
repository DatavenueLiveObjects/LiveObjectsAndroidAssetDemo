package com.orange.lo.assetdemo.mqtt;

import android.text.TextUtils;

import lombok.Data;

/**
 * Created by ZLGP6287 on 13/10/2016.
 */

@Data
public class ServerSettings {


    private String caCertFile;

    String protocol; // tcp / ssl / ws / wss
    String host;
    Integer port;
    Boolean tlsConnection;
    String uriExtension;

    public ServerSettings(String protocol, String host, Integer port, Boolean tlsConnection, String caCertFile, String uriExtension) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.uriExtension = uriExtension;
        this.tlsConnection = tlsConnection;
        this.caCertFile = caCertFile;
    }

    public ServerSettings(String protocol, String host, Integer port, Boolean tlsConnection, String caCertFile) {
        this(protocol, host, port, tlsConnection, caCertFile, null);
    }


    public String getURI() {
        String uri = protocol + "://" + host;
        if (port != null && port > 0) {
            uri += ":" + String.valueOf(port);
        }
        if (uriExtension != null && !TextUtils.isEmpty(uriExtension)) {
            uri += "/" + this.uriExtension;
        }

        return uri;
    }



}

