package nz.co.noirland.bankofnoir;

import nz.co.noirland.bankofnoir.commands.BankAdminCommand;
import nz.co.noirland.bankofnoir.commands.BankCommand;
import nz.co.noirland.bankofnoir.commands.PayCommand;
import nz.co.noirland.bankofnoir.database.SQLDatabase;
import nz.co.noirland.zephcore.Debug;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The main plugin class. {@link #onEnable()} is called once the server is ready to enable the plugin.
 */
public class BankOfNoir extends JavaPlugin {

    private static BankOfNoir inst;
    private static Debug debug;

    /**
     * String to check for on the first line of a bank sign.
     */
    public static final String SIGN_TITLE = "[bank]";

    /**
     * @return the singleton instance of this plugin.
     */
    public static BankOfNoir inst() {
        return inst;
    }

    /**
     * @return the debugger for this plugin.
     */
    public static Debug debug() {
        return debug;
    }

    /**
     * Called when this plugin is loaded. This starts up and checks the database, initiailised the economy
     * and the Vault implementation, and registers all events and commands.
     */
    public void onEnable() {
        inst = this;
        debug = new Debug(this);

        SQLDatabase.inst().checkSchema();

        EcoManager eco = EcoManager.inst();
        new VaultConnector();

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(eco.getBankManager(), this);
        getCommand("bank").setExecutor(new BankCommand());
        getCommand("bankadmin").setExecutor(new BankAdminCommand());
        getCommand("pay").setExecutor(new PayCommand());
    }

    /**
     * Simple wrapper around the bukkit sendMessage, to include the plugin prefix.
     * @param to person to send message to
     * @param msg message to send
     */
    public static void sendMessage(CommandSender to, String msg) {
        to.sendMessage(ChatColor.RED + "[BankOfNoir] " + ChatColor.RESET + msg);
    }

}
