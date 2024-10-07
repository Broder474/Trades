package net.broder.trades;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

public interface Commands {
    class Balance implements CommandExecutor {
        public Balance(Logger logger, TreeMap<UUID, net.broder.trades.Balance> player_balances) {
            this.logger = logger;
            this.player_balances = player_balances;
        }
        Logger logger;
        TreeMap<UUID, net.broder.trades.Balance> player_balances;
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            if (sender instanceof Player player) {
                UUID uuid = player.getUniqueId();
                player.sendMessage("§l§aБаланс:§6 " + player_balances.get(uuid).getBalance() + "$");
            } else {
                sender.sendMessage("This command can only be used by players");
            }
            return true;
        }
    }
    class Earn implements CommandExecutor {
        Logger logger;
        TreeMap<UUID, net.broder.trades.Balance> player_balances;

        public Earn(Logger logger, TreeMap<UUID, net.broder.trades.Balance> player_balances) {
            this.logger = logger;
            this.player_balances = player_balances;
        }
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            if (sender instanceof Player player) {
                UUID uuid = player.getUniqueId();

                player_balances.get(uuid).addBalance(50);
                player.sendMessage("§l§aБаланс:§6 " + player_balances.get(uuid).getBalance() + "$");
            } else {
                sender.sendMessage("This command can only be used by players");
            }
            return true;
        }
    }
    class Shop implements CommandExecutor {
        Logger logger;
        TreeMap<UUID, net.broder.trades.Balance> player_balances;

        public Shop(Logger logger, TreeMap<UUID, net.broder.trades.Balance> player_balances) {
            this.logger = logger;
            this.player_balances = player_balances;
        }
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            if (sender instanceof Player player) {
                UUID uuid = player.getUniqueId();
            } else {
                sender.sendMessage("This command can only be used by players");
            }
            return true;
        }
    }
    class Baltop implements CommandExecutor {
        Logger logger;
        TreeMap<UUID, net.broder.trades.Balance> player_balances;
        Server server;
        public Baltop(Logger logger, TreeMap<UUID, net.broder.trades.Balance> player_balances, Server server) {
            this.logger = logger;
            this.player_balances = player_balances;
            this.server = server;
        }
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            if (sender instanceof Player player) {
                UUID uuid = player.getUniqueId();
                StringBuilder baltop = new StringBuilder("§lBALTOP\n");
                int number = 1;
                int balance;

                Map<UUID, net.broder.trades.Balance>sorted_player_balances = player_balances.entrySet().stream()
                        .sorted(Map.Entry.<UUID, net.broder.trades.Balance>comparingByValue(Comparator.comparingInt(net.broder.trades.Balance::getBalance).reversed())) // Sort by Balance value
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (e1, e2) -> e1,
                                LinkedHashMap::new // Collect to a LinkedHashMap to maintain order
                        ));

                for (Map.Entry<UUID, net.broder.trades.Balance> entry : sorted_player_balances.entrySet()) {
                    baltop.append("§r").append(number).append(". §6");
                    balance = entry.getValue().getBalance();
                    Player online_player = server.getPlayer(entry.getKey());
                    if (online_player != null)
                        baltop.append(online_player.getName()).append(" : §a").append(balance).append("$\n");
                    else
                        baltop.append(server.getOfflinePlayer(entry.getKey()).getName()).append(" : §a").append(balance).append("$\n");
                    number++;
                }
                player.sendMessage(baltop.toString());
            } else {
                sender.sendMessage("This command can only be used by players");
            }
            return true;
        }
    }
    class Pay implements CommandExecutor {
        Logger logger;
        TreeMap<UUID, net.broder.trades.Balance> player_balances;
        Server server;

        public Pay(Logger logger, TreeMap<UUID, net.broder.trades.Balance> player_balances, Server server) {
            this.logger = logger;
            this.player_balances = player_balances;
            this.server = server;
        }
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            if (sender instanceof Player player) {
                UUID uuid = player.getUniqueId();
                if (args.length != 2)
                    return false;
                else
                {
                    String receiver_name;
                    int amount_to_send = 0;
                    receiver_name = args[0];
                    try {
                        amount_to_send = Integer.parseInt(args[1]);
                    }
                    catch(Exception ex) {
                        {
                            player.sendMessage("§3Ошибка ввода");
                            return true;
                        }
                }
                    if (amount_to_send <= 0)
                    {
                        player.sendMessage("§3Сумма транзакции не может быть меньше 1$");
                        return true;
                    }
                    else if (amount_to_send > player_balances.get(uuid).getBalance())
                    {
                        player.sendMessage("§3Сумма транзакции превышает доступный баланс");
                        return true;
                    }
                    else
                    {
                        for (Player receiver : server.getOnlinePlayers()) {
                            if (receiver.getName().equals(receiver_name))
                            {
                                player_balances.get(uuid).addBalance(-amount_to_send);
                                player_balances.get(receiver.getUniqueId()).addBalance(amount_to_send);
                                player.sendMessage("Отправлено §a" + amount_to_send + "$§r игроку §e" + receiver_name);
                                receiver.sendMessage("Получено §a" + amount_to_send + "$§r от игрока §e" + player.getName());
                                return true;
                            }
                        }
                        player.sendMessage("§3Игрок не найден");
                        return true;
                    }
                }
            } else {
                sender.sendMessage("This command can only be used by players");
            }
            return true;
        }
    }
}
