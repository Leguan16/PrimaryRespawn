package at.noahb.primaryrespawn.domain;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class PlayerSpawns {

    private final UUID uuid;
    private Location primary;
    private Location secondary;

    public PlayerSpawns(UUID uuid, Location primary, Location secondary) {
        this.uuid = uuid;
        this.primary = primary;
        this.secondary = secondary;
    }

    public PlayerSpawns(UUID uuid) {
        this.uuid = uuid;
    }

    public Location getPrimary() {
        return primary;
    }

    public Location getSecondary() {
        return secondary;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public void setPrimary(Location primary) {
        this.primary = primary;
    }

    public void setSecondary(Location secondary) {
        this.secondary = secondary;
    }

    public void sendMessage(Component message) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (player.isOnline() && player.getPlayer() != null) {
            player.getPlayer().sendMessage(message);
        } else {
            // TODO: 26/11/2023 save message to be sent when player logs in
        }
    }
}
