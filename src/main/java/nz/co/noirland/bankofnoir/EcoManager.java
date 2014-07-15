package nz.co.noirland.bankofnoir;

import nz.co.noirland.bankofnoir.config.PluginConfig;
import nz.co.noirland.bankofnoir.database.SQLDatabase;
import org.bukkit.Material;

import java.util.*;

/**
 * Class that manages the balances of players for BankOfNoir.
 * This also stores item denominations used to represent currency
 */
public class EcoManager {

    /**
     * The size that bank chests should be.
     */
    public static final int BANK_SIZE = 6 * 9;

    /**
     * A map of all player balances.
     */
    private final HashMap<UUID, Double> balances = new HashMap<UUID, Double>();

    /**
     * All denominations recognised by the plugin.
     */
    private final ArrayList<MoneyDenomination> denominations = new ArrayList<MoneyDenomination>();

    /**
     * the plugin's config instance.
     */
    private final PluginConfig config = PluginConfig.inst();

    /**
     * decimal format used to display balances.
     */
    private final String format = "%." + config.getDecimals() + "f %s";

    /**
     * Instance of database handler.
     */
    private final SQLDatabase db = SQLDatabase.inst();

    /**
     * Plugin instance.
     */
    private static EcoManager inst;

    /**
     * The player bank manager for the plugin.
     */
    private BankManager bankManager;

    /**
     * Get the manager's singleton instance. If one does not exist, create one and configure it.
     * @return The EcoManager instance
     */
    public static EcoManager inst() {
        if(inst == null) {
            inst = new EcoManager();
            inst.bankManager = new BankManager();
            inst.denominations.addAll(PluginConfig.inst().getDenoms());
            inst.reloadBalances();
        }
        return inst;
    }

    private EcoManager() {}

    /**
     * Get the BankManager associated with this economy.
     * @return The BankManager
     */
    public BankManager getBankManager() {
        return bankManager;
    }

    /**
     * Sets the recognised denominations, throwing out all the others.
     * @param denoms denominations to replace current ones
     */
    public void setDenominations(Collection<MoneyDenomination> denoms) {
        denominations.clear();
        denominations.addAll(denoms);
    }

    /**
     * Gets all the balances from the database to ensure they are up to date.
     * Also ensures that all open banks are up to date as well.
     */
    public void reloadBalances() {
        Map<UUID, Double> db_balances = db.getAllBalances();
        for(UUID player : db_balances.keySet()) {
            Double diff = db_balances.get(player) - getBalance(player);
            bankManager.updateBank(player, diff);
        }
        balances.clear();
        balances.putAll(db_balances);
    }

    /**
     * Gets the balance for the specified player
     * @param player UUID of player
     * @return Their balance, or default of 0 if they don't have one.
     */
    public double getBalance(UUID player) {
        if(!(balances.containsKey(player))) {
            return 0;
        }
        return balances.get(player);
    }

    /**
     * Set the balance of a player both in memory and in the database.
     * Also updates an open bank inventory if one exists.
     * @param player UUID of player
     * @param balance Their new balance
     */
    public void setBalance(UUID player, double balance) {
        Double diff = balance - getBalance(player);
        balances.put(player, balance);
        db.setBalance(player, balance);
        bankManager.updateBank(player, diff);
    }

    /**
     * Checks if a player has a balance in memory
     * @param player UUID of player
     * @return Whether or not they have a balance
     */
    public boolean hasBalance(UUID player) {
        return balances.containsKey(player);
    }

    /**
     * Convert a given balance to the plugin's balance format.
     * @param amount balance to convert
     * @return balance string
     */
    public String format(double amount) {
        return String.format(format, amount, amount==1.0 ? config.getSingular() : config.getPlural());
    }

    /**
     * Get all denominations recognised
     * @return List of all denominations
     */
    public ArrayList<MoneyDenomination> getDenominations() {
        return denominations;
    }

    /**
     * Checks whether a material is recognised as a denomination
     * @param material material to check
     * @return Whether the material is a denomination
     */
    public boolean isDenomination(Material material) {
        return !(getDenomination(material) == null);
    }

    /**
     * Get the MoneyDenomination object for a given material.
     * @param material material to get denomination for
     * @return MoneyDenomination, or null if none exists
     */
    public MoneyDenomination getDenomination(Material material) {
        for(MoneyDenomination denom : getDenominations()) {
            if(denom.getMaterial() == material) return denom;
        }
        return null;
    }
}
