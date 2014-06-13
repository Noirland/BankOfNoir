package nz.co.noirland.bankofnoir;

import nz.co.noirland.bankofnoir.config.PluginConfig;
import nz.co.noirland.bankofnoir.database.SQLDatabase;
import org.bukkit.Material;

import java.util.*;

public class EcoManager {

    public static final int BANK_SIZE = 6 * 9;

    private final HashMap<UUID, Double> balances = new HashMap<UUID, Double>();
    private final ArrayList<MoneyDenomination> denominations = new ArrayList<MoneyDenomination>();
    private final PluginConfig config = PluginConfig.inst();
    private final String format = "%." + config.getDecimals() + "f %s";
    private final SQLDatabase db = SQLDatabase.inst();

    private BankManager bankManager;

    EcoManager(Collection<MoneyDenomination> denoms) {
        bankManager = new BankManager();
        denominations.addAll(denoms);
        reloadBalances();
    }

    public BankManager getBankManager() {
        return bankManager;
    }

    public void setDenominations(Collection<MoneyDenomination> denoms) {
        denominations.clear();
        denominations.addAll(denoms);
    }

    public void reloadBalances() {
        Map<UUID, Double> db_balances = db.getAllBalances();
        for(UUID player : db_balances.keySet()) {
            Double diff = db_balances.get(player) - getBalance(player);
            bankManager.updateBank(player, diff);
        }
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
        bankManager.updateBank(player, diff);
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
}
