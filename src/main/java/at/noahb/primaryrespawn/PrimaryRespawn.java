package at.noahb.primaryrespawn;

import at.noahb.primaryrespawn.command.CommandManager;
import at.noahb.primaryrespawn.event.BedEventListener;
import at.noahb.primaryrespawn.event.RespawnEventListener;
import at.noahb.primaryrespawn.manager.Manager;
import at.noahb.primaryrespawn.manager.SimpleRespawnManger;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class PrimaryRespawn extends JavaPlugin {

    private static ComponentLogger logger;
    private static PrimaryRespawn instance;
    private Manager manager;

    @Override
    public void onLoad() {
        super.onLoad();
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this));

    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        logger = getComponentLogger();
        manager = new SimpleRespawnManger();

        initEvents();
        new CommandManager(this).init();
        logger.info(Component.text("Plugin enabled!").color(NamedTextColor.DARK_GREEN));
    }

    private void initEvents() {
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new BedEventListener(manager), this);
        pluginManager.registerEvents(new RespawnEventListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        CommandAPI.onDisable();
        manager.saveConfig();
        logger.info(Component.text("Plugin disabled!").color(NamedTextColor.RED));
    }

    public static ComponentLogger logger() {
        return logger;
    }

    public static PrimaryRespawn getInstance() {
        return instance;
    }

    public Manager getManager() {
        return manager;
    }
}
