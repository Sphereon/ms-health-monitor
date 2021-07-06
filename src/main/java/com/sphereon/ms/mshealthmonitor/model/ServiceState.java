package com.sphereon.ms.mshealthmonitor.model;

import org.springframework.boot.actuate.health.Status;

public enum ServiceState {
    UP, DOWN, OUT_OF_SERVICE;

    public Status toStatus() {
        switch (this) {
            case UP:
                return Status.UP;
            case DOWN:
                return Status.DOWN;
            case OUT_OF_SERVICE:
                return Status.OUT_OF_SERVICE;
        }
        return null;
    }
}
