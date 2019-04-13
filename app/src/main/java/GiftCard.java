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
}
