package com.example.packhacks2019;

public class GiftCard {
    private String storeName;
    private int balance;

    public GiftCard(String storeName, int balance) {
        this.storeName = storeName;
        this.balance = balance;
    }

    public String getStoreName() {
        return this.storeName;
    }
    public int getBalance() {
        return this.balance;
    }
    public String toString() {
        return "Place: " + this.storeName + "\nBalance: " + this.balance;
    }
}
