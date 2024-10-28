package net.broder.trades;

import org.bukkit.Material;

public class Balance {
    protected int balance = 0;
    int shop_item_trade_count = 0;
    int shop_page = 0;
    public enum Shop_Action {DEFAULT, BUY, SELL};
    Shop_Action shop_action = Shop_Action.DEFAULT;
    Material shop_item_trade_material = null;
    public int getShopPage() {return shop_page;}
    public void addShopPage(int n) {shop_page += n; if (shop_page < 0) shop_page = 0;}
    public Material getShopItemTradeMaterial() {return shop_item_trade_material;}
    public void setShopItemTradeMaterial(Material material) {this.shop_item_trade_material = material;}
    public Balance() {
        this.balance = 0;
    }
    public Balance(int balance) {
        this.balance = balance;
    }
    public int getBalance() {return balance;}
    public Shop_Action getShopAction() {return shop_action;}
    public void setShopAction(Shop_Action action) {this.shop_action = action;}
    public int getShopItemTradeCount() {return shop_item_trade_count;}
    public void addShopItemTradeCount(int count) {
        shop_item_trade_count += count;
        if (shop_item_trade_count > 64)
            shop_item_trade_count = 64;
        else if (shop_item_trade_count <= 0)
            shop_item_trade_count = 1;
    }
    public void setShopItemTradeCount(int count) {shop_item_trade_count = count;}
    protected void setBalance(int balance) {this.balance = balance;}
    protected void addBalance(int value) {this.balance += value;}
    protected void resetBalance() {this.balance = 0;}

}
