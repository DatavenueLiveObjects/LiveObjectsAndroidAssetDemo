package com.orange.lo.assetdemo.mqtt;

import com.orange.lo.assetdemo.BuildConfig;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

/**
 * Created by ZLGP6287 on 13/10/2016.
 */

public class ServerSettingsList {

    // MQTT_SERVER_ORANGE_ALT: This URL can support the WS protocol, in addition of TCP and SSL
    //private static final String MQTT_SERVER_ORANGE_LO = "liveobjects.orange-business.com";
    //private static final String MQTT_LOCAL_PROXY = "10.0.2.2";
    // MQTT_SERVER: This URL only support the TCL and SSL protocol but NOT SSL
    //private static final String MQTT_SERVER_ORANGE_M2M = "m2m.orange.com";
    //private static final String MQTT_SERVER_ORANGE_IP = "84.39.42.208";

    private static final String MQTT_SERVER = BuildConfig.MQTT_SERVER;


//    private static final String CA_CERT_ORANGE_SA = "ca_cert_orange_sa.crt";
//    private static final String CA_CERT_ORANGE_SA_SYMANTEC_VERISIGN = "ca_cert_orange_sa_symantec_verisign.crt";
//    private static final String CA_CERT_SELF_SIGNED = "ca_self_signed.crt";

    public static final String CA_CERT_SSL = BuildConfig.CA_CERT_SSL;
    public static final String CA_CERT_WSS = BuildConfig.CA_CERT_WSS;

    //public static final String CA_CERT_ORANGE_SA_PASSPHRASE = "demodemo";

    public static ServerSettingsList instance = new ServerSettingsList();

    public enum MQTTConnectionType {
        TCP (0),
        SSL(1);
     //   WS(2),
     //   WSS(3);

        @Getter
        private final int value;

        MQTTConnectionType(final int value) {
            this.value = value;
        }
        //From String method will return you the Enum for the provided input string
        public static MQTTConnectionType fromValue(int value) {
            for (MQTTConnectionType objType : MQTTConnectionType.values()) {
                if (value == objType.value) {
                    return objType;
                }
            }
            return null;
        }

    }

    private final List<ServerSettings> list;

    private ServerSettingsList() {
        list = new ArrayList<>();
        list.add(new ServerSettings("tcp", MQTT_SERVER,1883, false, null));
        list.add(new ServerSettings("ssl", MQTT_SERVER,8883, true, CA_CERT_SSL));
    //    list.add(new ServerSettings("ws", MQTT_SERVER,null, false, null, "mqtt")); // port 1885 not needed
        //list.add(new ServerSettings("ws",MQTT_SERVER,1885, false)); // port 1885 not needed
    //    list.add(new ServerSettings("wss", MQTT_SERVER,null, true, CA_CERT_WSS, "mqtt")); // port 1885 not needed
        //list.add(new ServerSettings("wss",MQTT_SERVER,????, true)); // port ???? not needed

        //list.add(new ServerSettings("tcp", "172.16.6.108",1883, false, null));
        //list.add(new ServerSettings("ssl", "172.16.6.108",8883, true, CA_CERT_SSL));
    }

    public ServerSettings getServerSettings(MQTTConnectionType connectionType) {
        return list.get(connectionType.getValue());
    }

}
