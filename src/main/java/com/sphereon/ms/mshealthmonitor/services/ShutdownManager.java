package com.sphereon.ms.mshealthmonitor.services;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
class ShutdownManager {

    private final ApplicationContext appContext;

    public ShutdownManager(final ApplicationContext appContext) {
        this.appContext = appContext;
    }

    /*
     * Invoke with `0` to indicate no error or different code to indicate
     * abnormal exit. es: shutdownManager.initiateShutdown(0);
     **/
    public void initiateShutdown(int returnCode){
        SpringApplication.exit(appContext, () -> returnCode);
    }
}