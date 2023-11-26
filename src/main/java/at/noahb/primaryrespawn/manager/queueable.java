package at.noahb.primaryrespawn.manager;

import java.util.UUID;

public interface queueable {
    void queueSetSecondaryAsPrimary(UUID uuid);
    boolean isQueued(UUID uuid);
    void removeQueued(UUID uuid);
}