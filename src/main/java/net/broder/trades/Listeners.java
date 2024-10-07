package net.broder.trades;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Logger;

public interface Listeners {
    public class PlayerFirstJoinListener implements Listener {
        Logger logger;
        TreeMap<UUID, net.broder.trades.Balance> player_balances;
        public PlayerFirstJoinListener(Logger logger, TreeMap<UUID, Balance> player_balances) {
            this.logger = logger;
            this.player_balances = player_balances;
        }
        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            boolean new_player = !player.hasPlayedBefore();
            if (new_player)
                player_balances.put(player.getUniqueId(), new Balance());
        }
    }
}
