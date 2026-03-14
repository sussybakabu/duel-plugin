package com.example.duelplugin;

import org.bukkit.plugin.java.JavaPlugin;

public final class DuelPlugin extends JavaPlugin {
    private DuelManager duelManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.duelManager = new DuelManager();

        DuelCommand duelCommand = new DuelCommand(duelManager);
        getCommand("duel").setExecutor(duelCommand);
        getCommand("duel").setTabCompleter(duelCommand);

        AcceptCommand acceptCommand = new AcceptCommand(duelManager);
        getCommand("accept").setExecutor(acceptCommand);

        DenyCommand denyCommand = new DenyCommand(duelManager);
        getCommand("deny").setExecutor(denyCommand);

        getServer().getPluginManager().registerEvents(new DuelListener(duelManager), this);

        getLogger().info("DuelPlugin enabled.");
    }

    @Override
    public void onDisable() {
        if (duelManager != null) {
            duelManager.clearAll();
        }
        getLogger().info("DuelPlugin disabled.");
    }
}
