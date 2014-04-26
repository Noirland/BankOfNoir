package nz.co.noirland.bankofnoir.commands;

import nz.co.noirland.bankofnoir.BankOfNoir;
import nz.co.noirland.bankofnoir.EcoManager;
import nz.co.noirland.bankofnoir.Permissions;
import nz.co.noirland.bankofnoir.Strings;
import nz.co.noirland.bankofnoir.config.PluginConfig;
import nz.co.noirland.bankofnoir.database.SQLDatabase;
import nz.co.noirland.zephcore.Util;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BankAdminCommand implements CommandExecutor {

    /*
      /bankadmin reload # Reloads the plugin
      /bankadmin see [player] # Shows another player's Bank, and allows changes to be made.
      /bankadmin adjust [player] [amount] # Adjusts specified player's balance by a negative or positive amount
     */

    private final EcoManager eco = BankOfNoir.getEco();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if(args.length < 1) return false;

        if(args[0].equalsIgnoreCase("reload")) {
            if(!sender.hasPermission(Permissions.RELOAD)) {
                sender.sendMessage(Strings.NO_PERMISSION);
                return true;
            }
            reloadCommand(sender);
        } else if(args[0].equalsIgnoreCase("see")) {
            if(args.length < 2) {
                return false;
            }
            if(!sender.hasPermission(Permissions.SEE)) {
                sender.sendMessage(Strings.NO_PERMISSION);
                return true;
            }
            seeCommand(sender, args[1]);
            return true;
        } else if(args[0].equalsIgnoreCase("adjust")) {
            if(args.length < 3) {
                return false;
            }
            if(!sender.hasPermission(Permissions.ADJUST)) {
                sender.sendMessage(Strings.NO_PERMISSION);
                return true;
            }
            adjustCommand(sender, args[1], args[2]);
        }

        return true;
    }

    private void reloadCommand(CommandSender sender) {
        if(!sender.hasPermission(Permissions.RELOAD)) {
            sender.sendMessage(Strings.NO_PERMISSION);
            return;
        }

        PluginConfig.inst().reload();
        eco.setDenominations(PluginConfig.inst().getDenoms());
        SQLDatabase.inst().openConnection();
        eco.reloadBalances();
        BankOfNoir.sendMessage(sender, Strings.BANKADMIN_RELOADED);
    }

    private void seeCommand(CommandSender sender, String target) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(Strings.BANK_NO_CONSOLE);
            return;
        }
        UUID tarUuid = Util.uuid(target);
        if(tarUuid == null) {
            BankOfNoir.sendMessage(sender, String.format(Strings.PLAYER_NOT_EXISTS, target));
            return;
        }
        OfflinePlayer tar = Util.player(tarUuid);
        if(!tar.hasPlayedBefore() && !tar.isOnline()) {
            BankOfNoir.sendMessage(sender, String.format(Strings.PLAYER_NOT_EXISTS, tar.getName()));
            return;
        }

        Player player = (Player) sender;
        player.openInventory(BankOfNoir.getEco().getBank(tar.getUniqueId()).getBank());
    }

    private void adjustCommand(CommandSender sender, String toStr, String amountStr) {
        amountStr = amountStr.replace("$", "");
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            BankOfNoir.sendMessage(sender, Strings.AMOUNT_NAN);
            return;
        }

        UUID toUuid = nz.co.noirland.zephcore.Util.uuid(toStr);
        if(toUuid == null) {
            BankOfNoir.sendMessage(sender, String.format(Strings.PLAYER_NOT_EXISTS, toStr));
            return;
        }
        OfflinePlayer to = Util.player(toUuid);
        if(!to.hasPlayedBefore() && !to.isOnline()) {
            BankOfNoir.sendMessage(sender, String.format(Strings.PLAYER_NOT_EXISTS, toStr));
            return;
        }

        Double toBal = eco.getBalance(to.getUniqueId());

        if(amount == 0) return;

        if(toBal + amount < 0) {
            BankOfNoir.sendMessage(sender, Strings.BANKADMIN_NEGATIVE);
            return;
        }

        eco.setBalance(to.getUniqueId(), toBal + amount);
        String format;
        if(to.getPlayer() != null) {
            if(amount < 0) {
                format = Strings.ECO_WITHDREW;
            }else{
                format = Strings.ECO_DEPOSITED;
            }
            BankOfNoir.sendMessage(to.getPlayer(), String.format(format, eco.format(Math.abs(amount))));
        }
        BankOfNoir.sendMessage(sender, String.format(Strings.BANKADMIN_BAL_UPDATED, to.getName(), amount));
    }
}
