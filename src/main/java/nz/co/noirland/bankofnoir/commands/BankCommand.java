package nz.co.noirland.bankofnoir.commands;

import nz.co.noirland.bankofnoir.EcoManager;
import nz.co.noirland.bankofnoir.Permissions;
import nz.co.noirland.bankofnoir.Strings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BankCommand implements CommandExecutor {

    /*
      Opens the caller's Bank.
     */

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(Strings.BANK_NO_CONSOLE);
            return true;
        }
        if(!sender.hasPermission(Permissions.BANK)) {
            sender.sendMessage(Strings.NO_PERMISSION);
            return true;
        }

        Player player = (Player) sender;
        player.openInventory(EcoManager.inst().getBankManager().getBank(player.getUniqueId()).getBank());
        return true;
    }
}
