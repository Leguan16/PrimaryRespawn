package at.noahb.primaryrespawn.manager;

import at.noahb.primaryrespawn.domain.PlayerSpawns;
import org.bukkit.Location;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Manager extends Queueble {

    Optional<PlayerSpawns> loadDataForPlayer(UUID uniqueId);
    boolean addLocation(UUID uniqueId, Location location, boolean isPrimary);
    boolean removeLocation(UUID uniqueId, LocationType type);
    Optional<Location> getPrimaryLocation(UUID uniqueId);
    Optional<Location> getSecondaryLocation(UUID uniqueId);
    Optional<Location> getBestLocation(UUID uniqueId);
    boolean hasPrimaryLocation(UUID uniqueId);
    boolean hasSecondaryLocation(UUID uniqueId);
    PlayerSpawns getSpawnsForPlayer(UUID uniqueId);
    Optional<LocationType> getLocationType(UUID uniqueId, Location location);

    void setSecondaryAsPrimary(PlayerSpawns playerSpawns);
    void setSecondaryAsPrimary(UUID uuid);

    void resetRespawns(UUID uniqueId);

    CompletableFuture<List<PlayerSpawns>> getPlayersAtLocation(Location location);
    void saveDataForPlayer(PlayerSpawns playerSpawns);

    void saveConfig();

    enum LocationType {
        PRIMARY,
        SECONDARY
    }
}
