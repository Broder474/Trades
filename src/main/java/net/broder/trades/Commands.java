package net.broder.trades;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
        Server server;
        HashMap<Integer, ShopItem>prices;
        static class ShopItem {
            public ShopItem(ItemStack item, int price_buy, int price_sell) {
                this.item = item;
                this.price_buy = price_buy;
                this.price_sell = price_sell;
                ItemMeta meta = item.getItemMeta();
                List<String>lore = Arrays.asList(new String("§3[ЛКМ] §aКупить: " + price_buy), new String("§3[ПКМ] §aПродать: " + price_sell));
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            public int getPriceBuy() {return price_buy;}
            public int getPriceSell() {return price_sell;}
            public ItemStack getItem() {return item;}
            ItemStack item;
            int price_buy;
            int price_sell;
        }

        public Shop(Logger logger, TreeMap<UUID, net.broder.trades.Balance> player_balances, Server server,
                    HashMap<Integer, Commands.Shop.ShopItem>prices) {
            this.logger = logger;
            this.player_balances = player_balances;
            this.server = server;
            this.prices = prices;
        }
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            if (sender instanceof Player player) {
                UUID uuid = player.getUniqueId();
                net.broder.trades.Balance balance = player_balances.get(uuid);
                balance.setShopAction(net.broder.trades.Balance.Shop_Action.DEFAULT);
                balance.setShopItemTradeMaterial(null);
                balance.setShopItemTradeCount(0);
                net.broder.trades.Balance.Shop_Action shop_action = player_balances.get(uuid).getShopAction();
                if (shop_action == net.broder.trades.Balance.Shop_Action.DEFAULT) {
                    Inventory shop_inventory = Bukkit.createInventory(null, 54, "Магазин");
                    ItemStack page_prev = new ItemStack(Material.LIME_STAINED_GLASS_PANE, 1);
                    ItemStack page_next = page_prev.clone();
                    ItemMeta meta = page_prev.getItemMeta();
                    meta.setDisplayName("Предыдущая страница");
                    page_prev.setItemMeta(meta);
                    meta.setDisplayName("Следующая страница");
                    page_next.setItemMeta(meta);

                    for (Map.Entry<Integer, ShopItem> entry : prices.entrySet())
                    {
                        shop_inventory.setItem((Integer) entry.getKey(), entry.getValue().getItem());
                    }
                    shop_inventory.setItem(45, page_prev);

                    shop_inventory.setItem(53, page_next);
                    player.openInventory(shop_inventory);

                    player_balances.get(uuid).setShopItemTradeCount(0);
                }
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
    class Jobs implements CommandExecutor {
        Logger logger;
        TreeMap<UUID, net.broder.trades.Balance> player_balances;
        Server server;

        public Jobs(Logger logger, TreeMap<UUID, net.broder.trades.Balance> player_balances, Server server) {
            this.logger = logger;
            this.player_balances = player_balances;
            this.server = server;
        }
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            return false;
        }
    }
}
