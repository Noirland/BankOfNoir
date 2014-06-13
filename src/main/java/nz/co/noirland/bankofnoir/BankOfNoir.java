package nz.co.noirland.bankofnoir;

import nz.co.noirland.bankofnoir.commands.BankAdminCommand;
import nz.co.noirland.bankofnoir.commands.BankCommand;
import nz.co.noirland.bankofnoir.commands.PayCommand;
import nz.co.noirland.bankofnoir.database.SQLDatabase;
import nz.co.noirland.zephcore.Debug;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class BankOfNoir extends JavaPlugin {

    private static BankOfNoir inst;
    private static Debug debug;

    public static final String SIGN_TITLE = "[bank]";

    public static BankOfNoir inst() {
        return inst;
    }

    public static Debug debug() {
        return debug;
    }

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

    public static void sendMessage(CommandSender to, String msg) {
        to.sendMessage(ChatColor.RED + "[BankOfNoir] " + ChatColor.RESET + msg);
    }

}
