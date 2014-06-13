package nz.co.noirland.bankofnoir;

import org.bukkit.inventory.Inventory;

public class BankInventory<T> {

    private final T owner;
    private final Inventory bank;
    private Double remainder;

    public BankInventory(T owner, Inventory bank, Double remainder) {
        this.owner = owner;
        this.bank = bank;
        this.remainder = remainder;
    }

    public T getOwner() {
        return owner;
    }

    public Inventory getBank() {
        return bank;
    }

    public Double getRemainder() {
        return remainder;
    }

    public void setRemainder(Double remainder) {
        this.remainder = remainder;
    }
}
