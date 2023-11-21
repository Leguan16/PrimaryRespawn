package at.noahb.primaryrespawn.event;

import at.noahb.primaryrespawn.manager.Manager;
import com.destroystokyo.paper.MaterialTags;
import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public record BedEventListener(Manager manager) implements Listener {

    private static final Collection<Material> VALID_BEDS = Set.of(Material.BLACK_BED, Material.BLUE_BED, Material.BROWN_BED,
            Material.CYAN_BED, Material.GRAY_BED, Material.GREEN_BED, Material.LIGHT_BLUE_BED, Material.LIGHT_GRAY_BED,
            Material.LIME_BED, Material.MAGENTA_BED, Material.ORANGE_BED, Material.PINK_BED, Material.PURPLE_BED,
            Material.RED_BED, Material.WHITE_BED, Material.YELLOW_BED
    );

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event) {
        Location location = event.getBed().getLocation();
        Player player = event.getPlayer();
        UUID uniqueId = player.getUniqueId();

        if (manager.hasPrimaryLocation(uniqueId) && manager.hasSecondaryLocation(uniqueId)) {
            var type = manager.getLocationType(uniqueId, location);

            if (type.isPresent() && !Manager.LocationType.SECONDARY.equals(type.get())) {
                return;
            }

            if (manager.isQueued(uniqueId)) {
                manager.setSecondaryAsPrimary(uniqueId);
                return;
            }

            manager.queueSetSecondaryAsPrimary(uniqueId);
            player.sendMessage(Component.text("This bed is already set as secondary respawn point. If you want to set it as primary, click it again."));
            return;
        }

        if (manager.hasPrimaryLocation(uniqueId)) {
            if (manager.addLocation(uniqueId, location, false)) {
                player.sendMessage(Component.text("Secondary respawn point set."));
            }
            return;
        }

        if (manager.addLocation(uniqueId, location, true)) {
            player.sendMessage(Component.text("Respawn point set."));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!VALID_BEDS.contains(event.getBlock().getType())) {
            return;
        }

        Bed bed = ((Bed) event.getBlock().getBlockData());
        Location location = bed.getPart() == Bed.Part.HEAD
                ? event.getBlock().getLocation()
                : event.getBlock().getLocation().add(bed.getFacing().getDirection());
        Player player = event.getPlayer();
        UUID uniqueId = player.getUniqueId();

        if (manager.hasPrimaryLocation(uniqueId) && location.equals(manager.getPrimaryLocation(uniqueId).orElse(null))) {
            boolean hasSecondary = manager.hasSecondaryLocation(uniqueId);
            if (manager.removeLocation(uniqueId, Manager.LocationType.PRIMARY)) {
                player.sendMessage(Component.text("Respawn point removed."));
                if (hasSecondary) {
                    player.sendMessage(Component.text("Previous secondary respawn point set as primary."));
                }
            }
        } else if (manager.hasSecondaryLocation(uniqueId) && location.equals(manager.getSecondaryLocation(uniqueId).orElse(null))) {
            if (manager.removeLocation(uniqueId, Manager.LocationType.SECONDARY)) {
                player.sendMessage(Component.text("Secondary respawn point removed."));
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        var location = manager.getBestLocation(event.getPlayer().getUniqueId());

        if (location.isEmpty()) {
            event.getPlayer().sendMessage(Component.text("No respawn point set. Spawning at world spawn."));
            return;
        }

        event.setRespawnLocation(location.get());
    }

    @EventHandler
    public void onBlockDestroy(BlockDestroyEvent event) {
        if (!MaterialTags.BEDS.isTagged(event.getBlock())) {
            return;
        }

        Bed bed = ((Bed) event.getBlock().getBlockData());
        Location location = bed.getPart() == Bed.Part.HEAD
                ? event.getBlock().getLocation()
                : event.getBlock().getLocation().add(bed.getFacing().getDirection());
        manager.getPlayersAtLocation(location).thenAccept(playerSpawns -> playerSpawns.forEach(playerSpawn -> {
            manager.getLocationType(playerSpawn.getUniqueId(), location).ifPresent(type -> {
                if (Manager.LocationType.PRIMARY.equals(type)) {
                    playerSpawn.sendMessage(Component.text("Your respawn point has been destroyed."));
                    manager.resetRespawns(playerSpawn.getUniqueId());
                } else {
                    playerSpawn.sendMessage(Component.text("Your secondary respawn point has been destroyed."));
                    manager.removeLocation(playerSpawn.getUniqueId(), Manager.LocationType.SECONDARY);
                }
            });
        }));
    }

}
