package com.sphereon.ms.mshealthmonitor.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sphereon.ms.mshealthmonitor.model.EasyBlockchainState;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class StatePersistence {

    private final File stateFile;
    private ObjectMapper objectMapper = new ObjectMapper();
    private EasyBlockchainState easyBlockchainState;


    @Autowired
    public StatePersistence(final EasyBlockchainState easyBlockchainState) {
        this.easyBlockchainState = easyBlockchainState;

        try {
            final String envStateDir = System.getenv("STATE_DIRECTORY");
            File stateDir;
            if (StringUtils.isEmpty(envStateDir)) {
                final File jarDir = new File(getRunDir());
                stateDir = new File(jarDir, "state");
            } else {
                stateDir = new File(envStateDir);
            }
            stateDir.mkdir();
            this.stateFile = new File(stateDir, "EasyBlockchainState.json");
            loadState();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private String getRunDir() throws URISyntaxException {
        String path = StatePersistence.class.getProtectionDomain().getCodeSource().getLocation().toURI().toString();
        int pos = path.indexOf(".jar!");
        if (pos > -1) {
            path = path.substring(0, pos);
            pos = path.lastIndexOf('/');
            if (pos == -1) {
                throw new RuntimeException("Can't extract directory from " + path);
            }
            path = path.substring(0, pos);
        }
        return StringUtils.replace(path, "file:/", "");
    }

    public void saveState() {
        try {
            objectMapper.writeValue(stateFile, easyBlockchainState);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadState() {
        if (!stateFile.exists()) {
            return;
        }

        try {
            final var objectReader = objectMapper.readerForUpdating(easyBlockchainState);
            objectReader.readValue(stateFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
