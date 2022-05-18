package com.sphereon.ms.mshealthmonitor.services;

import com.sphereon.libs.authentication.api.TokenRequest;
import com.sphereon.ms.mshealthmonitor.config.ApiConfig;
import com.sphereon.ms.mshealthmonitor.model.EasyBlockchainState;
import com.sphereon.ms.mshealthmonitor.model.ServiceState;
import com.sphereon.ms.mshealthmonitor.persistence.StatePersistence;
import com.sphereon.sdk.blockchain.easy.api.AllApi;
import com.sphereon.sdk.blockchain.easy.model.AnchoredEntryResponse.AnchorStateEnum;
import com.sphereon.sdk.blockchain.easy.model.Chain;
import com.sphereon.sdk.blockchain.easy.model.Entry;
import com.sphereon.sdk.blockchain.easy.model.EntryData;
import com.sphereon.sdk.blockchain.easy.model.ExternalId;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class EasyBlockchainVerifyProber {

    private static Logger logger = LoggerFactory.getLogger(ApiConfig.class);

    public static final String CONTEXT_FACTOM = "factom";
    private static final AtomicInteger registrationCounter = new AtomicInteger();

    private final EasyBlockchainState easyBlockchainState;
    private final StatePersistence statePersistence;
    private final AllApi allApi;
    private final TokenRequest tokenRequester;
    private final ShutdownManager shutdownManager;
    private LocalDateTime postPoneChecksUntil;

    @Autowired
    public EasyBlockchainVerifyProber(final EasyBlockchainState easyBlockchainState,
      final StatePersistence statePersistence,
      final AllApi allApi,
      final TokenRequest tokenRequester,
      ShutdownManager shutdownManager) {
        this.easyBlockchainState = easyBlockchainState;
        this.statePersistence = statePersistence;
        this.allApi = allApi;
        this.tokenRequester = tokenRequester;
        this.shutdownManager = shutdownManager;
        checkState();
    }

    private void checkState() {
        if (StringUtils.isEmpty(easyBlockchainState.getChainId())) {
            logger.info("No persisted chain id found, creating a new one");
            tokenRequester.execute();
            final var idResponse = allApi.createChain(new Chain().firstEntry(buildFirstEntry()), "factom");
            easyBlockchainState.setChainId(idResponse.getChain().getId());
            statePersistence.saveState();
            logger.info("Created chain " + easyBlockchainState.getChainId());
            postPoneChecksUntil = LocalDateTime.now().plusMinutes(14);
            logger.info("Postponing monitor until " + postPoneChecksUntil);
            easyBlockchainState.setServiceState(ServiceState.OUT_OF_SERVICE);
            easyBlockchainState.setStateMessage("Waiting for chain to anchor.");
            easyBlockchainState.setLastException(null);
            statePersistence.saveState();

        }
    }


    @Scheduled(fixedRate = 600000L)
    void testVerifyShort() {
        if (needToPostpone()) {
            return;
        }
        tokenRequester.execute();
        try {
            if (!chainIsAnchored()) {
                easyBlockchainState.setServiceState(ServiceState.OUT_OF_SERVICE);
            } else {
                if (easyBlockchainState.getFirstEntryId() != null) {
                    upAllOk();
                }
            }
        } catch (Exception exception) {
            down(exception);
        }
    }

    @Scheduled(fixedRate = 1800000L)
    void testRegisterAndVerifyLong() {
        if (needToPostpone()) {
            return;
        }
        if (registrationCounter.incrementAndGet() > 250) {
            shutdownManager.initiateShutdown(-1);
        }
        tokenRequester.execute();
        try {
            if (!easyBlockchainState.isChainAnchored() && !chainIsAnchored()) {
                return;
            }

            easyBlockchainState.setLastEntryId(easyBlockchainState.getNextEntryId());
            easyBlockchainState.setNextEntryId(null);
            statePersistence.saveState();

            final var createResponse = allApi.createEntry(buildTestEntry(), CONTEXT_FACTOM, easyBlockchainState.getChainId(), null);
            easyBlockchainState.setNextEntryId(createResponse.getEntry().getEntryId());
            statePersistence.saveState();
            if (easyBlockchainState.getLastEntryId() != null) {
                final var getResponse = allApi.entryById(CONTEXT_FACTOM, easyBlockchainState.getChainId(),
                  easyBlockchainState.getLastEntryId(), null);
                if (getResponse.getAnchorState() == AnchorStateEnum.ANCHORED) {
                    upAllOk();
                } else {
                    down(String.format("Entry %s was not anchored.", easyBlockchainState.getLastEntryId()));
                }
            } else {
                upAllOk();
            }
        } catch (Exception exception) {
            down(exception);
        }
    }

    private void down(final String message) {
        easyBlockchainState.setServiceState(ServiceState.DOWN);
        easyBlockchainState.setStateMessage(message);
        statePersistence.saveState();
    }

    private void down(final Exception exception) {
        easyBlockchainState.setLastException(exception);
        down(exception.getMessage());
        logger.error("Down state detected", exception);
    }

    private void upAllOk() {
        easyBlockchainState.setServiceState(ServiceState.UP);
        easyBlockchainState.setStateMessage("All ok");
        easyBlockchainState.setLastException(null);
        statePersistence.saveState();
    }

    private boolean chainIsAnchored() {
        final var idResponse = allApi.firstEntry(CONTEXT_FACTOM, easyBlockchainState.getChainId());
        final boolean isAnchored = idResponse.getAnchorTimes() != null && !idResponse.getAnchorTimes().isEmpty();
        easyBlockchainState.setFirstEntryId(idResponse.getAnchoredEntry().getEntryId());
        if (!easyBlockchainState.isChainAnchored()) {
            easyBlockchainState.setChainAnchored(true);
            statePersistence.saveState();
        }
        return isAnchored;
    }

    private Entry buildFirstEntry() {
        try {
            final byte[] timestamp = OffsetDateTime.now().toString().getBytes(StandardCharsets.UTF_8);
            return new Entry()
              .entryData(new EntryData()
                .externalIds(List.of(new ExternalId()
                    .value("EasyBlockchainVerifyProber".getBytes(StandardCharsets.UTF_8)),
                  new ExternalId()
                    .value(InetAddress.getLocalHost().getHostName().getBytes(StandardCharsets.UTF_8)),
                  new ExternalId()
                    .value(timestamp))
                ).content(timestamp));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Entry buildTestEntry() {
        try {
            return new Entry()
              .entryData(new EntryData()
                .externalIds(List.of(new ExternalId()
                  .value("TestEntry".getBytes(StandardCharsets.UTF_8))))
                .content(OffsetDateTime.now().toString().getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean needToPostpone() {
        if (postPoneChecksUntil != null && LocalDateTime.now().isBefore(postPoneChecksUntil)) {
            return true;
        }
        return false;
    }
}
