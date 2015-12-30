package com.daluga.messaging.producer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.*;
import java.util.UUID;

// We need a scope of prototype so that a new instance is created each time. The default scope is singleton so you would always get the same instance
// if we did not specify a scope of prototype.

@Component
@Scope("prototype")
public class Producer implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Producer.class);

    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private Queue queue;

    private String payload;

    private String reply;

    public void send() {
        LOGGER.debug("Sending message: " + payload);
        doSimulatedWork();
    }

    private void doSimulatedWork() {
        try {
            setReply(createUUID());
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        send();
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    private String createUUID() {
        UUID id = UUID.randomUUID();
        return id.toString();
    }
}
