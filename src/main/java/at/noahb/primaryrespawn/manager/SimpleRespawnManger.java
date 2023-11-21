package at.noahb.primaryrespawn.manager;

import at.noahb.primaryrespawn.PrimaryRespawn;
import at.noahb.primaryrespawn.domain.PlayerSpawns;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class SimpleRespawnManger implements Manager {

    private final YamlConfiguration config;
    private final Map<UUID, LocalDateTime> setSecondaryAsPrimary = new HashMap<>();

    public SimpleRespawnManger() {
        this.config = YamlConfiguration.loadConfiguration(new File(PrimaryRespawn.getInstance().getDataFolder(), "data.yml"));
        PrimaryRespawn.getInstance().getServer().getAsyncScheduler().runAtFixedRate(PrimaryRespawn.getInstance(), new SaveDataTask(), 5, 5, TimeUnit.MINUTES);
    }

    @Override
    public Optional<PlayerSpawns> loadDataForPlayer(UUID uniqueId) {
        ConfigurationSection root = config.getConfigurationSection(uniqueId.toString());
        if (root == null) {
            return Optional.empty();
        }

        Location primary = root.getLocation(LocationType.PRIMARY.name());
        Location secondary = root.getLocation(LocationType.SECONDARY.name());

        return Optional.of(new PlayerSpawns(uniqueId, primary, secondary));
    }

    @Override
    public boolean addLocation(UUID uniqueId, Location location, boolean isPrimary) {
        PlayerSpawns playerSpawns = getSpawnsForPlayer(uniqueId);

        if (isPrimary) {
            playerSpawns.setPrimary(location);
        } else {
            if (Objects.equals(playerSpawns.getPrimary(), location)) {
                return false;
            }
            playerSpawns.setSecondary(location);
        }
        saveDataForPlayer(playerSpawns);
        return true;
    }

    @Override
    public boolean removeLocation(UUID uniqueId, LocationType type) {
        PlayerSpawns playerSpawns = getSpawnsForPlayer(uniqueId);
        if (LocationType.PRIMARY.equals(type)) {
            setSecondaryAsPrimary(playerSpawns);
        } else {
            playerSpawns.setSecondary(null);
        }
        saveDataForPlayer(playerSpawns);
        return true;
    }

    @Override
    public Optional<Location> getPrimaryLocation(UUID uniqueId) {
        return Optional.ofNullable(getSpawnsForPlayer(uniqueId).getPrimary());
    }

    @Override
    public Optional<Location> getSecondaryLocation(UUID uniqueId) {
        return Optional.ofNullable(getSpawnsForPlayer(uniqueId).getSecondary());
    }

    @Override
    public Optional<Location> getBestLocation(UUID uniqueId) {
        if (hasSecondaryLocation(uniqueId)) {
            return getSecondaryLocation(uniqueId);
        }
        return getPrimaryLocation(uniqueId);
    }

    @Override
    public boolean hasPrimaryLocation(UUID uniqueId) {
        return getSpawnsForPlayer(uniqueId).getPrimary() != null;
    }

    @Override
    public boolean hasSecondaryLocation(UUID uniqueId) {
        return getSpawnsForPlayer(uniqueId).getSecondary() != null;
    }

    @Override
    public PlayerSpawns getSpawnsForPlayer(UUID uniqueId) {
        return loadDataForPlayer(uniqueId).orElse(new PlayerSpawns(uniqueId));
    }

    @Override
    public Optional<LocationType> getLocationType(UUID uniqueId, Location location) {
        PlayerSpawns playerSpawns = getSpawnsForPlayer(uniqueId);

        return Objects.equals(playerSpawns.getPrimary(), location)
                ? Optional.of(LocationType.PRIMARY)
                : Objects.equals(playerSpawns.getSecondary(), location)
                ? Optional.of(LocationType.SECONDARY)
                : Optional.empty();
    }

    @Override
    public void setSecondaryAsPrimary(PlayerSpawns playerSpawns) {
        playerSpawns.setPrimary(playerSpawns.getSecondary());
        playerSpawns.setSecondary(null);

        Player player = Bukkit.getPlayer(playerSpawns.getUniqueId());
        if (player != null) {
            player.sendRichMessage("<green>Secondary respawn point set as primary.<green>");
        }

        removeQueued(playerSpawns.getUniqueId());
        saveDataForPlayer(playerSpawns);
    }

    @Override
    public void setSecondaryAsPrimary(UUID uuid) {
        setSecondaryAsPrimary(getSpawnsForPlayer(uuid));
    }

    @Override
    public void resetRespawns(UUID uniqueId) {
        removeLocation(uniqueId, LocationType.PRIMARY);
        removeLocation(uniqueId, LocationType.SECONDARY);
    }

    @Override
    public CompletableFuture<List<PlayerSpawns>> getPlayersAtLocation(Location location) {
        return CompletableFuture.supplyAsync(() -> Arrays.stream(Bukkit.getOfflinePlayers())
                .map(offlinePlayer -> getSpawnsForPlayer(offlinePlayer.getUniqueId()))
                .filter(playerSpawns -> location.equals(playerSpawns.getPrimary()) || location.equals(playerSpawns.getSecondary()))
                .toList()
        );
    }

    @Override
    public void saveDataForPlayer(PlayerSpawns playerSpawns) {
        ConfigurationSection root = config.getConfigurationSection(playerSpawns.getUniqueId().toString());
        if (root == null) {
            root = config.createSection(playerSpawns.getUniqueId().toString());
        }

        root.set(LocationType.PRIMARY.name(), playerSpawns.getPrimary());
        root.set(LocationType.SECONDARY.name(), playerSpawns.getSecondary());

        config.set(playerSpawns.getUniqueId().toString(), root);
    }

    @Override
    public void saveConfig() {
        try {
            config.save(new File(PrimaryRespawn.getInstance().getDataFolder(), "data.yml"));
        } catch (IOException e) {
            PrimaryRespawn.logger().error("Failed to save data.yml", e);
        }
    }

    @Override
    public void queueSetSecondaryAsPrimary(UUID uniqueId) {
        if (isQueued(uniqueId)) {
            return;
        }
        setSecondaryAsPrimary.put(uniqueId, LocalDateTime.now());
    }

    @Override
    public boolean isQueued(UUID uniqueId) {
        return setSecondaryAsPrimary.containsKey(uniqueId) && setSecondaryAsPrimary.get(uniqueId).isAfter(LocalDateTime.now().minusSeconds(5));
    }

    @Override
    public void removeQueued(UUID uniqueId) {
        setSecondaryAsPrimary.remove(uniqueId);
    }

    private class SaveDataTask implements Consumer<ScheduledTask> {

        @Override
        public void accept(ScheduledTask scheduledTask) {
            saveConfig();
        }
    }
}
