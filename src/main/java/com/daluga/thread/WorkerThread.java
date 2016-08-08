package com.daluga.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.Callable;

// We need a scope of prototype so that a new instance is created each time. The default scope is singleton so you would always get the same instance
// if we did not specify a scope of prototype.

@Component
@Scope("prototype")
public class WorkerThread implements Callable<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerThread.class);

    private String request;

    public WorkerThread(String request) {
        this.request = request;
    }

    @Override
    public String call() throws Exception {
        LOGGER.debug("Thread started [" + request + "]");
        return doWork();
    }

    private String doWork() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return "Request [" + request + "] " + createUUID();
    }

    private String createUUID() {
        UUID id = UUID.randomUUID();
        return id.toString();
    }

}
