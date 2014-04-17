package nz.co.noirland.bankofnoir;

import org.bukkit.inventory.Inventory;

import java.util.UUID;

public class BankInventory {

    private final UUID owner;
    private final Inventory bank;
    private Double remainder;

    public BankInventory(UUID owner, Inventory bank, Double remainder) {
        this.owner = owner;
        this.bank = bank;
        this.remainder = remainder;
    }

    public UUID getOwner() {
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
