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

public abstract class AbstractBankManager<T> implements Listener {

    protected EcoManager eco = EcoManager.inst();

    protected final Map<T, BankInventory<T>> openBanks = new HashMap<T, BankInventory<T>>();

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

    public BankInventory<T> getOpenBank(Inventory inv) {
        for(BankInventory<T> entry : openBanks.values()) {
            if(entry.getBank().equals(inv)) {
                return entry;
            }
        }
        return null;
    }

    public void removeOpenBank(BankInventory<T> bank) {
        if(bank.getBank().getViewers().size() > 1) return;
        openBanks.remove(bank.getOwner());
    }

    public BankInventory<T> createBank(T owner) {
        Inventory bank = Bukkit.createInventory(null, EcoManager.BANK_SIZE, "Bank: " + ChatColor.GOLD + getName(owner));

        Double remainder = setBankContents(bank, getBalance(owner));

        return new BankInventory<T>(owner, bank, remainder);
    }

    public void updateBank(T owner, double diff) {
        if(!openBanks.containsKey(owner)) return;
        BankInventory<T> bank = openBanks.get(owner);
        Double bankBal = itemsToBalance(bank.getBank().getContents()) + bank.getRemainder();
        bank.setRemainder(setBankContents(bank.getBank(), bankBal + diff));
    }

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
     * @return Remainder + overflow after inventory is loaded
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

    public Double getRemainder(Double balance) {
        double newBalance = itemsToBalance(balanceToItems(balance));
        return balance - newBalance;
    }

    protected abstract double getBalance(T owner);

    protected abstract String getName(T owner);

    public abstract void updateBalance(T owner, Player updater, double balance, double diff);

}
