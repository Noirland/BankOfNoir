package nz.co.noirland.bankofnoir;

import com.google.common.collect.Ordering;
import nz.co.noirland.zephcore.UpdateInventoryTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

/**
 * Abstract class for any bank managers. This will create banks, update their contents,
 * listen for closing, etc.
 * @param <T> The type for the owner of the Bank. This can be any unique object (eg a String, Player)
 */
public abstract class AbstractBankManager<T> implements Listener {

    protected EcoManager eco = EcoManager.inst();

    protected final Map<T, BankInventory<T>> openBanks = new HashMap<T, BankInventory<T>>();

    /**
     * Either gives the bank owned by owner, or creates a new one with their balance.
     * @param owner The owner of bank requested
     * @return Bank object for owner
     */
    public BankInventory<T> getBank(T owner) {
        BankInventory<T> bank;
        if(openBanks.containsKey(owner)) {
            bank = openBanks.get(owner);
        } else {
            bank = createBank(owner);
            openBanks.put(owner, bank);
        }
        return bank;
    }

    /**
     * Check if specified {@link org.bukkit.inventory.Inventory} is an open bank inventory owned by
     * someone from this Manager.
     * @param inv The Inventory to check
     * @return The BankInventory, or null if the inventory is not an open bank
     */
    public BankInventory<T> getOpenBank(Inventory inv) {
        for(BankInventory<T> entry : openBanks.values()) {
            if(entry.getBank().equals(inv)) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Removes the BankInventory from the list of inventories, as long as nobody else has it open.
     * @param bank Bank to try and remove
     */
    public void removeOpenBank(BankInventory<T> bank) {
        if(bank.getBank().getViewers().size() > 1) return;
        openBanks.remove(bank.getOwner());
    }

    /**
     * Creates a bank Inventory that can be shown to a player.
     * @param owner The owner of the bank. This is not necessarily the person viewing it.
     * @return a fully functional BankInventory
     */
    public BankInventory<T> createBank(T owner) {
        Inventory bank = Bukkit.createInventory(null, EcoManager.BANK_SIZE, "Bank: " + ChatColor.GOLD + getName(owner));

        Double remainder = setBankContents(bank, getBalance(owner));

        return new BankInventory<T>(owner, bank, remainder);
    }

    /**
     * If a balance is changed while a bank is open, the contents of that bank needs to be updates to avoid inconsistencies.
     * This takes the diff, and then adjusts the bank's inventory/remainder accordingly.
     * @param owner The owner of bank to update
     * @param diff The difference in balance, either positive or negative, to adjust
     */
    public void updateBank(T owner, double diff) {
        if(!openBanks.containsKey(owner)) return;
        BankInventory<T> bank = openBanks.get(owner);
        Double bankBal = itemsToBalance(bank.getBank().getContents()) + bank.getRemainder();
        bank.setRemainder(setBankContents(bank.getBank(), bankBal + diff));
    }

    /**
     * Called when a Player closes an inventory. This is used to update the owners balance once the viewer
     * is done with the bank. This also handles updating other viewers of the bank.
     * @param event Event being handled
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onCloseChest(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        Player player = (Player) event.getPlayer();

        BankInventory<T> bank = getOpenBank(inv);
        if (bank == null) {
            return;
        }
        T owner = bank.getOwner();
        removeOpenBank(bank);

        double balance = getBalance(owner);
        double newBalance = itemsToBalance(inv.getContents()) + bank.getRemainder();

        for (ItemStack item : inv.getContents()) {
            if (item == null) continue;
            if (eco.isDenomination(item.getType())) continue;

            PlayerInventory pInv = player.getInventory();
            if (pInv.firstEmpty() != -1) {
                pInv.addItem(item);
                new UpdateInventoryTask(player);
            } else {
                player.getWorld().dropItem(player.getLocation(), item);
            }
        }

        if(newBalance != balance) {
            updateBalance(owner, player, newBalance, newBalance - balance);
        }
    }

    /**
     * Converts any number of items into a dollar balance. This is done using the denominations defined in
     * BankOfNoir.
     * @param items An array of items to convert into a balance
     * @return The monetary worth of the items.
     */
    public double itemsToBalance(ItemStack[] items) {
        double balance = 0;
        Map<MoneyDenomination, Integer> amounts = new HashMap<MoneyDenomination, Integer>();

        for(ItemStack item : items) {
            if(item == null) continue;
            if(!eco.isDenomination(item.getType())) continue;
            MoneyDenomination denom = eco.getDenomination(item.getType());
            amounts.put(denom, (amounts.containsKey(denom) ? amounts.get(denom) : 0) + item.getAmount());
        }

        for(Map.Entry<MoneyDenomination, Integer> entry : amounts.entrySet()) {
            balance += entry.getKey().getValue() * entry.getValue();
        }
        return balance;
    }

    /**
     * Using denominations defined in BankOfNoir, convert a dollar balance into an array of items.
     * This means a visual representation of money can be created.
     * <i>NOTE: this ignores the remainder balance, use {@link #setBankContents(org.bukkit.inventory.Inventory, Double)}</i>
     * @param balance The balance to convert
     * @return An array of items (in proper stack amounts) representing the balance
     */
    public ItemStack[] balanceToItems(double balance) {
        ArrayList<ItemStack> items = new ArrayList<ItemStack>();

        SortedSet<MoneyDenomination> denoms = new TreeSet<MoneyDenomination>(Ordering.natural().reverse());
        denoms.addAll(eco.getDenominations());

        for(MoneyDenomination denom : denoms) {
            int amount = (int) (balance / denom.getValue());
            balance = balance % denom.getValue();
            for(int i = 0; i < (amount / 64); i++) {
                items.add(new ItemStack(denom.getMaterial(), 64));
            }
            if(amount > 0 && amount % 64 != 0) {
                items.add(new ItemStack(denom.getMaterial(), amount % 64));
            }
        }
        return items.toArray(new ItemStack[items.size()]);
    }

    /**
     * Converts a balance into items, and then adds those items to the inventory.
     * Any items that cannot be added to the inventory and any balance that cannot be
     * converted into items are returned as a remainder balance.
     *
     * @param bank Inventory to add items to
     * @param balance Balance to be converted to items
     * @return Any leftover balance that could not be added to the inventory
     */
    public Double setBankContents(Inventory bank, Double balance) {
        bank.clear();

        HashMap<Integer, ItemStack> leftover = bank.addItem(balanceToItems(balance));

        double remainder = 0.0;
        if(!leftover.isEmpty()) {
            for(ItemStack item : leftover.values()) {
                if(item == null) continue;
                if(!eco.isDenomination(item.getType())) continue;
                MoneyDenomination denom = eco.getDenomination(item.getType());
                remainder += item.getAmount() * denom.getValue();
            }
        }
        remainder += getRemainder(balance);
        return remainder;
    }

    /**
     * Get the remainder for a given balance. This is the money that can not be converted
     * into a denomination.
     * @param balance Balance to check
     * @return Remainder of balance
     */
    public Double getRemainder(Double balance) {
        double newBalance = itemsToBalance(balanceToItems(balance));
        return balance - newBalance;
    }

    /**
     * Abstract method used to get the owner's balance. This can (and should) vary depending on implementation.
     * @param owner The owner to check
     * @return Their balance as a dollar amount
     */
    protected abstract double getBalance(T owner);

    /**
     * Returns the name of the owner.
     * @param owner Owner of the bank
     * @return Their name
     */
    protected abstract String getName(T owner);

    /**
     * Used for updating the owner's balance.
     * @param owner The owner of the balance to update
     * @param updater The person who caused the update (eg by adding to a bank)
     * @param balance The new balance of the owner's account
     * @param diff The difference between the old balance and the new balance
     */
    public abstract void updateBalance(T owner, Player updater, double balance, double diff);

}
