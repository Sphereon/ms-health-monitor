package com.sphereon.ms.mshealthmonitor.config;

import com.sphereon.ms.mshealthmonitor.services.EasyBlockchainVerifyProber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Autowired
    private EasyBlockchainVerifyProber easyBlockchainVerifyProber;
}
