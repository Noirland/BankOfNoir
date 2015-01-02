package nz.co.noirland.bankofnoir;

import nz.co.noirland.zephcore.Config;
import nz.co.noirland.zephcore.Debug;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class BankConfig extends Config {

    private static BankConfig inst;

    @Override
    protected Plugin getPlugin() {
        return BankOfNoir.inst();
    }

    @Override
    protected Debug getDebug() {
        return BankOfNoir.debug();
    }

    private BankConfig() {
        super("config.yml");
    }

    public static BankConfig inst() {
        if(inst == null) {
            inst = new BankConfig();
        }

        return inst;
    }

    public void reload() {
        load();
    }

    // MySQL
    public String getPrefix()    { return config.getString("mysql.prefix", "bank"); }
    public String getDatabase()  { return config.getString("mysql.database", "bankofnoir"); }
    public String getUsername()  { return config.getString("mysql.username"); }
    public String getPassword()  { return config.getString("mysql.password"); }
    public int    getPort()      { return config.getInt   ("mysql.port", 3306); }
    public String getHost()      { return config.getString("mysql.host", "localhost"); }

    public String  getSingular() { return config.getString("currency.singular", "Gold"); }
    public String  getPlural()   { return config.getString("currency.plural", "Gold"); }
    public int     getDecimals() { return config.getInt("currency.decimals", 2); }

    public List<MoneyDenomination> getDenoms() {
        ConfigurationSection section = config.getConfigurationSection("currency.denominations");
        ArrayList<MoneyDenomination> denoms = new ArrayList<MoneyDenomination>();
        for(String key : section.getKeys(false)) {
            denoms.add(new MoneyDenomination(Material.getMaterial(key), section.getDouble(key)));
        }
        return denoms;
    }
}
