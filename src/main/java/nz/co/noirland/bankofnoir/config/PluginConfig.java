package nz.co.noirland.bankofnoir.config;

import nz.co.noirland.bankofnoir.MoneyDenomination;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class PluginConfig extends Config {

    private static PluginConfig inst;

    private PluginConfig() {
        super("config.yml");
    }

    public static PluginConfig inst() {
        if(inst == null) {
            inst = new PluginConfig();
        }

        return inst;
    }

    // MySQL
    public String getPrefix()    { return config.getString("mysql.prefix", "bank_"); }
    public String getDatabase()  { return config.getString("mysql.database", "bankofnoir"); }
    public String getUsername()  { return config.getString("mysql.username"); }
    public String getPassword()  { return config.getString("mysql.password"); }
    public int    getPort()      { return config.getInt   ("mysql.port", 3306); }
    public String getHost()      { return config.getString("mysql.host", "localhost"); }

    public boolean getDebug()    { return config.getBoolean("debug", false);}

    public String  getSingular() { return config.getString("currency.singular", "Gold"); }
    public String  getPlural()   { return config.getString("currency.plural", "Gold"); }
    public int     getDecimals() { return config.getInt("currency.decimals", 2); }

    public List<MoneyDenomination> getDenoms() {
        ConfigurationSection section = config.getConfigurationSection("currency.denominations");
        ArrayList<MoneyDenomination> denoms = new ArrayList<MoneyDenomination>();
        for(String key : section.getKeys(false)) {
            if(section.get(key) instanceof ConfigurationSection) {
                ConfigurationSection denomSect = section.getConfigurationSection(key);
                denoms.add(new MoneyDenomination(Material.getMaterial(key), denomSect.getDouble("value"), Material.getMaterial(denomSect.getString("round.to")), denomSect.getInt("round.num")));
                continue;
            }
            denoms.add(new MoneyDenomination(Material.getMaterial(key), section.getDouble(key)));
        }
        return denoms;
    }
}
