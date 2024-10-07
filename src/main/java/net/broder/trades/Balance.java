package net.broder.trades;

public class Balance {
    protected int balance = 0;
    public Balance() {
        this.balance = 0;
    }
    public Balance(int balance) {
        this.balance = balance;
    }
    public int getBalance() {return balance;}
    protected void setBalance(int balance) {this.balance = balance;}
    protected void addBalance(int value) {this.balance += value;}
    protected void resetBalance() {this.balance = 0;}

}
