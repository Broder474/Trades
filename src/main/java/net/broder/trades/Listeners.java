package net.broder.trades;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Logger;

public interface Listeners {
    public class PlayerFirstJoinListener implements Listener {
        Logger logger;
        TreeMap<UUID, Balance> player_balances;
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
    public class ShopInventoryListener implements  Listener {
        Logger logger;
        TreeMap<UUID, Balance> player_balances;
        Server server;
        HashMap<Integer, Commands.Shop.ShopItem>prices;
        public ShopInventoryListener(Logger logger, TreeMap<UUID, Balance> player_balances, Server server,
                                     HashMap<Integer, Commands.Shop.ShopItem> prices) {
            this.logger = logger;
            this.player_balances = player_balances;
            this.server = server;
            this.prices = prices;
        }
        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            InventoryView view = event.getView();

            // cancel any action with item
            if (view.getTitle().equals("Магазин")) {
                event.setCancelled(true);
                int slot = event.getSlot();
                Player player = (Player) event.getWhoClicked();
                UUID uuid = player.getUniqueId();
                Balance balance = player_balances.get(uuid);
                Balance.Shop_Action shop_action = balance.getShopAction();
                if (balance.getShopItemTradeCount() == 0 && prices.get(slot) == null )
                    return;
                if (shop_action == Balance.Shop_Action.DEFAULT)
                {
                    if (event.isLeftClick())
                        balance.setShopAction(Balance.Shop_Action.BUY);
                    else if (event.isRightClick())
                        balance.setShopAction(Balance.Shop_Action.SELL);
                    shop_action = balance.getShopAction();
                }

                if (shop_action != Balance.Shop_Action.DEFAULT) {
                    if (balance.getShopItemTradeCount() == 0)
                        for (Map.Entry<Integer, Commands.Shop.ShopItem>entry : prices.entrySet())
                            if (entry.getKey() == slot) {
                                balance.setShopItemTradeMaterial(entry.getValue().getItem().getType());
                                balance.setShopItemTradeCount(1);
                                break;
                            }
                    Inventory shop_inventory = Bukkit.createInventory(null, 54, "Магазин");
                    ItemStack add_count_1 = new ItemStack(Material.GRASS_BLOCK, 1);
                    ItemMeta meta = add_count_1.getItemMeta();
                    meta.setDisplayName("Добавить 1");
                    add_count_1.setItemMeta(meta);
                    ItemStack add_count_4 = new ItemStack(Material.GRASS_BLOCK, 4);
                    meta.setDisplayName("Добавить 4");
                    add_count_4.setItemMeta(meta);
                    ItemStack add_count_16 = new ItemStack(Material.GRASS_BLOCK, 16);
                    meta.setDisplayName("Добавить 16");
                    add_count_16.setItemMeta(meta);
                    ItemStack add_count_64 = new ItemStack(Material.GRASS_BLOCK, 64);
                    meta.setDisplayName("Добавить 64");
                    add_count_64.setItemMeta(meta);

                    ItemStack rem_count_1 = new ItemStack(Material.GRASS_BLOCK, 1);
                    meta.setDisplayName("Убрать 1");
                    rem_count_1.setItemMeta(meta);
                    ItemStack rem_count_4 = new ItemStack(Material.GRASS_BLOCK, 4);
                    meta.setDisplayName("Убрать 4");
                    rem_count_4.setItemMeta(meta);
                    ItemStack rem_count_16 = new ItemStack(Material.GRASS_BLOCK, 16);
                    meta.setDisplayName("Убрать 16");
                    rem_count_16.setItemMeta(meta);
                    ItemStack rem_count_64 = new ItemStack(Material.GRASS_BLOCK, 64);
                    meta.setDisplayName("Убрать 64");
                    rem_count_64.setItemMeta(meta);


                    // buttons to increase/decrease selected amount
                    shop_inventory.setItem(45, rem_count_64);
                    shop_inventory.setItem(46, rem_count_16);
                    shop_inventory.setItem(47, rem_count_4);
                    shop_inventory.setItem(48, rem_count_1);

                    shop_inventory.setItem(50, add_count_1);
                    shop_inventory.setItem(51, add_count_4);
                    shop_inventory.setItem(52, add_count_16);
                    shop_inventory.setItem(53, add_count_64);

                    player.openInventory(shop_inventory);


                    int shop_item_trade_count = balance.getShopItemTradeCount();
                    if (slot == 45)
                        balance.addShopItemTradeCount(-64);
                    else if (slot == 46)
                        balance.addShopItemTradeCount(-16);
                    else if (slot == 47)
                        balance.addShopItemTradeCount(-4);
                    else if (slot == 48)
                        balance.addShopItemTradeCount(-1);
                    else if (slot == 50)
                        balance.addShopItemTradeCount(1);
                    else if (slot == 51)
                        balance.addShopItemTradeCount(4);
                    else if (slot == 52)
                        balance.addShopItemTradeCount(16);
                    else if (slot == 53)
                        balance.addShopItemTradeCount(64);

                    ItemStack items_to_buy = new ItemStack(balance.getShopItemTradeMaterial(), balance.getShopItemTradeCount());

                    int price = 0;
                    for (Map.Entry<Integer, Commands.Shop.ShopItem>entry : prices.entrySet())
                        if (entry.getValue().getItem().getType() == balance.getShopItemTradeMaterial()) {
                            if (shop_action == Balance.Shop_Action.BUY)
                                price = entry.getValue().price_buy;
                            else
                                price = entry.getValue().price_sell;
                            break;
                        }
                    if (shop_action == Balance.Shop_Action.BUY)
                        meta.setDisplayName("Купить за " + price * balance.getShopItemTradeCount());
                    else
                        meta.setDisplayName("Продать за " + price * balance.getShopItemTradeCount());
                    items_to_buy.setItemMeta(meta);

                    shop_inventory.setItem(40, items_to_buy);

                    ItemStack confirm_action = new ItemStack(Material.LIME_STAINED_GLASS_PANE, 1);
                    meta.setDisplayName("§aПодтвердить");
                    confirm_action.setItemMeta(meta);
                    ItemStack decline_action = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
                    meta.setDisplayName("§cОтменить");
                    decline_action.setItemMeta(meta);
                    shop_inventory.setItem(20, confirm_action);
                    shop_inventory.setItem(24, decline_action);

                    if (shop_action == Balance.Shop_Action.BUY) {
                        if (slot == 20) {
                            if (balance.getBalance() >= price * balance.getShopItemTradeCount())
                            {
                                player_balances.get(uuid).addBalance(-price * balance.getShopItemTradeCount());
                                ItemStack stack = new ItemStack(balance.getShopItemTradeMaterial(), balance.getShopItemTradeCount());
                                player.getInventory().addItem(stack);
                                player.sendMessage("Куплено§9 " + balance.getShopItemTradeCount() + " " + balance.getShopItemTradeMaterial() + "§r за §a" + price *
                                        balance.getShopItemTradeCount() + "$");
                            }
                            else
                                player.sendMessage("Недостаточного денег");
                        }
                        else if (slot == 24)
                            shop_inventory.close();
                    }
                    else if (shop_action == Balance.Shop_Action.SELL) {
                        int available_items = 0;
                        if (slot == 20) {
                            for (ItemStack item : player.getInventory().getContents())
                                if (item != null && item.getType() == balance.getShopItemTradeMaterial())
                                    available_items += item.getAmount();
                            int remaining = balance.getShopItemTradeCount();
                            if (available_items >= remaining)
                            {
                                for (ItemStack item : player.getInventory().getContents()) {
                                    if (item != null && item.getType() == balance.getShopItemTradeMaterial()) {
                                        int itemAmount = item.getAmount();
                                        if (itemAmount >= remaining) {
                                            item.setAmount(itemAmount - remaining);
                                            remaining = 0;
                                        } else {
                                            remaining -= itemAmount;
                                            item.setAmount(0);
                                        }
                                    }
                                }
                                player.sendMessage("Продано§9 " + balance.getShopItemTradeCount() + " " + balance.getShopItemTradeMaterial().toString() +
                                        "§r за §a" + price * balance.getShopItemTradeCount() + "$");
                                player_balances.get(uuid).addBalance(balance.getShopItemTradeCount() * price);
                            }
                            else
                                player.sendMessage("Недостаточно ресурсов для продажи");
                        }
                        else if (slot == 24)
                            shop_inventory.close();
                    }
                }
            }
        }
        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event) {}
    }
}
