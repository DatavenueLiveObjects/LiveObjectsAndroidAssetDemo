package com.orange.lo.assetdemo.model;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Date;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class ReceivedMessage {
    public ReceivedMessage(String topic, MqttMessage message) {
        this.topic = topic;
        this.message = message;
        this.timestamp = new Date();
    }

    @Getter @Setter(value = AccessLevel.NONE)
    String topic;
    @Getter @Setter(value = AccessLevel.NONE)
    MqttMessage message;
    @Getter @Setter(value = AccessLevel.NONE)
    Date timestamp;

}
