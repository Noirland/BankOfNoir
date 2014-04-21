package nz.co.noirland.bankofnoir;

import com.google.common.collect.Ordering;
import nz.co.noirland.bankofnoir.config.PluginConfig;
import nz.co.noirland.bankofnoir.database.SQLDatabase;
import nz.co.noirland.zephcore.Util;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class EcoManager {

    public static final int BANK_SIZE = 6 * 9;

    private final HashMap<UUID, Double> balances = new HashMap<UUID, Double>();
    private final ArrayList<MoneyDenomination> denominations = new ArrayList<MoneyDenomination>();
    private final PluginConfig config = PluginConfig.inst();
    private final String format = "%." + config.getDecimals() + "f %s";
    private final SQLDatabase db = SQLDatabase.inst();
    private final Map<UUID, BankInventory> openBanks = new HashMap<UUID, BankInventory>();

    EcoManager(Collection<MoneyDenomination> denoms) {
        denominations.addAll(denoms);
        reloadBalances();
    }

    public void setDenominations(Collection<MoneyDenomination> denoms) {
        denominations.clear();
        denominations.addAll(denoms);
    }

    public void reloadBalances() {
        Map<UUID, Double> db_balances = db.getAllBalances();
        balances.clear();
        balances.putAll(db_balances);
    }

    public double getBalance(UUID player) {
        if(!(balances.containsKey(player))) {
            return 0;
        }
        return balances.get(player);
    }

    public void setBalance(UUID player, double balance) {
        /*
        - Get balance of currently open inventory + remainder
        - Add/subtract adjust
        - Recreate items and remainder
        - Change open inventory and remainder
         */
        Double diff = balance - getBalance(player);
        balances.put(player, balance);
        db.setBalance(player, balance);
        if(!openBanks.containsKey(player)) return;
        BankInventory bank = openBanks.get(player);
        Double bankBalance = itemsToBalance(bank.getBank().getContents()) + bank.getRemainder();
        bank.setRemainder(setBankContents(bank.getBank(), bankBalance + diff));

    }

    public boolean hasBalance(UUID player) {
        return balances.containsKey(player);
    }

    public String format(double amount) {
        return String.format(format, amount, amount==1.0 ? config.getSingular() : config.getPlural());
    }

    public ArrayList<MoneyDenomination> getDenominations() {
        return denominations;
    }

    public boolean isDenomination(Material material) {
        return !(getDenomination(material) == null);
    }

    public MoneyDenomination getDenomination(Material material) {
        for(MoneyDenomination denom : getDenominations()) {
            if(denom.getMaterial() == material) return denom;
        }
        return null;
    }

    public ItemStack[] balanceToItems(double balance) {
        ArrayList<ItemStack> items = new ArrayList<ItemStack>();

        SortedSet<MoneyDenomination> denoms = new TreeSet<MoneyDenomination>(Ordering.natural().reverse());
        denoms.addAll(getDenominations());

        for(MoneyDenomination denom : denoms) {
            int amount = (int) (balance / denom.getValue());
            balance = balance % denom.getValue();
            while(amount > 0) {
                int num = 64;
                if(amount < 64) {
                    num = amount;
                }
                items.add(new ItemStack(denom.getMaterial(), num));
                amount -= num;
            }
        }
        return items.toArray(new ItemStack[items.size()]);
    }

    public double itemsToBalance(ItemStack[] items) {
        double balance = 0;
        Map<MoneyDenomination, Integer> amounts = new HashMap<MoneyDenomination, Integer>();

        for(ItemStack item : items) {
            if(item == null) continue;
            if(!isDenomination(item.getType())) continue;
            MoneyDenomination denom = getDenomination(item.getType());
            amounts.put(denom, (amounts.containsKey(denom) ? amounts.get(denom) : 0) + item.getAmount());
        }

        for(Map.Entry<MoneyDenomination, Integer> entry : amounts.entrySet()) {
            balance += entry.getKey().getValue() * entry.getValue();
        }
        return balance;
    }

    public BankInventory getBank(UUID player) {
        BankInventory bank;
        if(openBanks.containsKey(player)) {
            bank = openBanks.get(player);
        } else {
            bank = createBank(player);
            openBanks.put(player, bank);
        }
        return bank;
    }

    public BankInventory getOpenBank(Inventory inv) {
        BankInventory bank = null;
        for(Map.Entry<UUID, BankInventory> entry : openBanks.entrySet()) {
            if(entry.getValue().getBank().equals(inv)) {
                bank = entry.getValue();
                break;
            }
        }
        return bank;
    }

    public void removeOpenBank(BankInventory bank) {
        if(bank.getBank().getViewers().size() > 1) return;
        openBanks.remove(bank.getOwner());
    }

    public BankInventory createBank(UUID player) {
        Inventory bank = BankOfNoir.inst().getServer().createInventory(null, BANK_SIZE, "Bank: " + ChatColor.GOLD + Util.player(player).getName());

        Double remainder = setBankContents(bank, getBalance(player));

        return new BankInventory(player, bank, remainder);
    }

    public Double getRemainder(Double balance) {
        double newBalance = itemsToBalance(balanceToItems(balance));
        return balance - newBalance;
    }

    /**
     * @return Remainder + overflow after inventory is loaded
     */
    private Double setBankContents(Inventory bank, Double balance) {
        bank.clear();

        HashMap<Integer, ItemStack> leftover = bank.addItem(balanceToItems(balance));

        double remainder = 0.0;
        if(!leftover.isEmpty()) {
            for(ItemStack item : leftover.values()) {
                if(item == null) continue;
                if(!isDenomination(item.getType())) continue;
                MoneyDenomination denom = getDenomination(item.getType());
                remainder += item.getAmount() * denom.getValue();
            }
        }
        remainder += getRemainder(balance);
        return remainder;
    }
}
