package at.noahb.primaryrespawn.manager;

import java.util.UUID;

public interface Queueble {
    void queueSetSecondaryAsPrimary(UUID uuid);

    boolean isQueued(UUID uuid);

    void removeQueued(UUID uuid);
}