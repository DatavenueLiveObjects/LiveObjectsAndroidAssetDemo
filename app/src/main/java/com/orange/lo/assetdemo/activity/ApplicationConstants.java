/**
 *
 */
package com.orange.lo.assetdemo.activity;

public class ApplicationConstants {

    /** Bundle key for passing a connection around by it's name **/

    public static final String ConnectionStatusProperty = "connectionStatus";
    public static final String TelemetryProperty = "telemetry";
    public static final String LocationProperty = "location";
    public static final String ConfigurationProperty = "configuration";
    public static final String ResourceNewVersionProperty = "ResourceNewVersionProperty";

    public static final String PUBLISHED_MODEL = "demo";
    public static final String DEFAULT_DEVICE_NAME = "123456789012345";

    public static final int TEMPERATURE_MIN_PROGESS = -20;
    public static final int TEMPERATURE_MAX_PROGESS = 120;

    // The minimum distance to change Updates in meters
    public static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;

    // The minimum time between updates in milliseconds
    public static final long MIN_TIME_BW_UPDATES = 1000;

    public static final int DEFAULT_UPDATE_RATE = 4;
    public static final String DEFAULT_LOG_LEVEL = "Info";


    public static final int MQTT_KEEP_ALIVE_INTERVAL = 20;
    public static final int MQTT_CONNECTION_TIMEOUT = 30;

    // Mqtt topics
    public static final String MQTT_TOPIC_PUBLISH_STATUS = "dev/info";
    public static final String MQTT_TOPIC_PUBLISH_DATA = "dev/data";

    public static final String MQTT_TOPIC_PUBLISH_RESOURCE = "dev/rsc";
    public static final String MQTT_TOPIC_SUBSCRIBE_RESOURCE = "dev/rsc/upd";
    public static final String MQTT_TOPIC_RESPONSE_RESOURCE = "dev/rsc/upd/res";

    public static final String MQTT_TOPIC_SUBSCRIBE_COMMAND = "dev/cmd";
    public static final String MQTT_TOPIC_RESPONSE_COMMAND = "dev/cmd/res";

    public static final String MQTT_TOPIC_PUBLISH_CONFIG = "dev/cfg";
    public static final String MQTT_TOPIC_SUBSCRIBE_CONFIG = "dev/cfg/upd";
    public static final String MQTT_TOPIC_RESPONSE_CONFIG = "dev/cfg"; //"dev/cfg/upd/res";

}
