package com.sphereon.ms.mshealthmonitor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class EasyBlockchainState {

    private String chainId;
    private boolean chainAnchored;
    private String lastEntryId;
    private String firstEntryId;
    private String nextEntryId;
    private ServiceState serviceState;
    private String stateMessage;

    @JsonIgnore
    private transient Exception lastException;

    public String getChainId() {
        return chainId;
    }

    public void setChainId(final String chainId) {
        this.chainId = chainId;
    }


    public boolean isChainAnchored() {
        return chainAnchored;
    }

    public void setChainAnchored(final boolean chainAnchored) {
        this.chainAnchored = chainAnchored;
    }

    public String getLastEntryId() {
        return lastEntryId;
    }

    public void setLastEntryId(final String lastEntryId) {
        this.lastEntryId = lastEntryId;
    }

    public String getFirstEntryId() {
        return firstEntryId;
    }

    public void setFirstEntryId(final String firstEntry) {
        this.firstEntryId = firstEntry;
    }

    public String getNextEntryId() {
        return nextEntryId;
    }

    public void setNextEntryId(final String nextEntryId) {
        this.nextEntryId = nextEntryId;
    }

    public ServiceState getServiceState() {
        return serviceState;
    }

    public void setServiceState(final ServiceState serviceState) {
        this.serviceState = serviceState;
    }

    public String getStateMessage() {
        return stateMessage;
    }

    public void setStateMessage(final String stateMessage) {
        this.stateMessage = stateMessage;
    }

    public void setLastException(final Exception lastException) {
        this.lastException = lastException;
    }

    public Exception getLastException() {
        return lastException;
    }
}
