package com.example.duelplugin;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class DuelManager {
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    private final Map<UUID, DuelRequest> pendingRequestsByTarget = new HashMap<>();
    private final Map<UUID, UUID> duelOpponents = new HashMap<>();

    public DuelManager() {
    }

    public boolean sendRequest(Player challenger, Player target) {
        cleanupExpiredRequests();

        if (challenger.getUniqueId().equals(target.getUniqueId())) {
            challenger.sendMessage("§cYou cannot duel yourself.");
            return false;
        }

        if (isInDuel(challenger) || isInDuel(target)) {
            challenger.sendMessage("§cYou or that player is already in a duel.");
            return false;
        }

        DuelRequest existing = pendingRequestsByTarget.get(target.getUniqueId());
        if (existing != null) {
            challenger.sendMessage("§cThat player already has a pending duel request.");
            return false;
        }

        DuelRequest request = new DuelRequest(challenger.getUniqueId(), target.getUniqueId(), Instant.now());
        pendingRequestsByTarget.put(target.getUniqueId(), request);

        challenger.sendMessage("§aDuel request sent to §f" + target.getName() + "§a.");
        target.sendMessage("§e" + challenger.getName() + " has challenged you to a duel!");
        target.sendMessage("§eType §f/accept §eto fight or §f/deny §eto decline.");
        return true;
    }

    public Optional<DuelRequest> getRequestForTarget(UUID targetId) {
        cleanupExpiredRequests();
        return Optional.ofNullable(pendingRequestsByTarget.get(targetId));
    }

    public void denyRequest(Player target) {
        DuelRequest request = pendingRequestsByTarget.remove(target.getUniqueId());
        if (request == null) {
            target.sendMessage("§cYou do not have any pending duel requests.");
            return;
        }

        Player challenger = Bukkit.getPlayer(request.challengerId());
        if (challenger != null && challenger.isOnline()) {
            challenger.sendMessage("§c" + target.getName() + " denied your duel request.");
        }
        target.sendMessage("§eDuel request denied.");
    }

    public void acceptRequest(Player target) {
        DuelRequest request = pendingRequestsByTarget.remove(target.getUniqueId());
        if (request == null) {
            target.sendMessage("§cYou do not have any pending duel requests.");
            return;
        }

        Player challenger = Bukkit.getPlayer(request.challengerId());
        if (challenger == null || !challenger.isOnline()) {
            target.sendMessage("§cThe challenger is no longer online.");
            return;
        }

        if (isInDuel(challenger) || isInDuel(target)) {
            target.sendMessage("§cA player is already in a duel.");
            return;
        }

        startDuel(challenger, target);
    }

    public void onPlayerDefeated(Player defeated) {
        UUID opponentId = duelOpponents.remove(defeated.getUniqueId());
        if (opponentId == null) {
            return;
        }

        duelOpponents.remove(opponentId);
        Player opponent = Bukkit.getPlayer(opponentId);
        if (opponent != null && opponent.isOnline()) {
            opponent.sendMessage("§aYou won the duel against §f" + defeated.getName() + "§a!");
        }
    }

    public void onPlayerQuit(Player player) {
        pendingRequestsByTarget.remove(player.getUniqueId());
        pendingRequestsByTarget.entrySet().removeIf(entry -> entry.getValue().challengerId().equals(player.getUniqueId()));

        UUID opponentId = duelOpponents.remove(player.getUniqueId());
        if (opponentId != null) {
            duelOpponents.remove(opponentId);
            Player opponent = Bukkit.getPlayer(opponentId);
            if (opponent != null && opponent.isOnline()) {
                opponent.sendMessage("§aYou won the duel because your opponent disconnected.");
            }
        }
    }

    public boolean isInDuel(Player player) {
        return duelOpponents.containsKey(player.getUniqueId());
    }

    public void clearAll() {
        pendingRequestsByTarget.clear();
        duelOpponents.clear();
    }

    private void startDuel(Player challenger, Player target) {
        duelOpponents.put(challenger.getUniqueId(), target.getUniqueId());
        duelOpponents.put(target.getUniqueId(), challenger.getUniqueId());

        Location spawn = challenger.getWorld().getSpawnLocation();
        challenger.teleport(spawn.clone().add(2, 0, 0));
        target.teleport(spawn.clone().add(-2, 0, 0));

        challenger.sendMessage("§aDuel started with §f" + target.getName() + "§a!");
        target.sendMessage("§aDuel started with §f" + challenger.getName() + "§a!");
    }

    private void cleanupExpiredRequests() {
        Instant now = Instant.now();
        pendingRequestsByTarget.entrySet().removeIf(entry -> {
            boolean expired = Duration.between(entry.getValue().createdAt(), now).compareTo(REQUEST_TIMEOUT) > 0;
            if (expired) {
                Player challenger = Bukkit.getPlayer(entry.getValue().challengerId());
                Player target = Bukkit.getPlayer(entry.getValue().targetId());
                if (challenger != null && challenger.isOnline()) {
                    challenger.sendMessage("§eYour duel request expired.");
                }
                if (target != null && target.isOnline()) {
                    target.sendMessage("§eDuel request expired.");
                }
            }
            return expired;
        });
    }

    public record DuelRequest(UUID challengerId, UUID targetId, Instant createdAt) {
    }
}
