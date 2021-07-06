package com.sphereon.ms.mshealthmonitor.indicators;

import com.sphereon.ms.mshealthmonitor.model.EasyBlockchainState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class EasyBlockchainVerifyIndicator implements HealthIndicator {

    private final EasyBlockchainState easyBlockchainState;

    @Autowired
    public EasyBlockchainVerifyIndicator(final EasyBlockchainState easyBlockchainState) {
        this.easyBlockchainState = easyBlockchainState;
    }

    @Override
    public Health health() {
        final Builder builder = Health
          .status(easyBlockchainState.getServiceState().toStatus())
          .withDetail("message", easyBlockchainState.getStateMessage());
        if (easyBlockchainState.getLastException() != null) {
            builder.withException(easyBlockchainState.getLastException());
        }
        return builder.build();
    }
}
