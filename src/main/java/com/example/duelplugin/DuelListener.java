package com.example.duelplugin;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class DuelListener implements Listener {
    private final DuelManager duelManager;

    public DuelListener(DuelManager duelManager) {
        this.duelManager = duelManager;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        duelManager.onPlayerDefeated(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        duelManager.onPlayerQuit(event.getPlayer());
    }
}
