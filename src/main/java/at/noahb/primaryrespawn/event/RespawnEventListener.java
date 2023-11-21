package at.noahb.primaryrespawn.event;

import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RespawnEventListener implements Listener {


    @EventHandler
    public void onRespawnSet(PlayerSetSpawnEvent event) {
        event.setCancelled(true);
    }
}
