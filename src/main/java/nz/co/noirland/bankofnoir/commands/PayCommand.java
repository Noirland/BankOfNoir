package nz.co.noirland.bankofnoir.commands;

import nz.co.noirland.bankofnoir.BankOfNoir;
import nz.co.noirland.bankofnoir.EcoManager;
import nz.co.noirland.bankofnoir.Strings;
import nz.co.noirland.zephcore.Util;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.UUID;

public class PayCommand implements CommandExecutor {

    private final EcoManager eco = EcoManager.inst();

    /*
      /pay [player] [amount] - Pays another player the given amount.
    */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(Strings.PAY_NO_CONSOLE);
            return true;
        }
        Player from = (Player) sender;

        if(args.length < 2) {
            BankOfNoir.sendMessage(from, Strings.PAY_WRONG_ARGS);
            return true;
        }

        String p = args[0];
        UUID toUuid = Util.uuid(p);
        if(toUuid == null) {
            BankOfNoir.sendMessage(sender, String.format(Strings.PLAYER_NOT_EXISTS, p));
            return true;
        }
        OfflinePlayer to = Util.player(toUuid);

        if(!to.hasPlayedBefore()) {
            BankOfNoir.sendMessage(from, String.format(Strings.PLAYER_NOT_EXISTS, to.getName()));
            return true;
        }

        if(to.equals(from)) {
            BankOfNoir.sendMessage(from, Strings.PAY_SELF);
            return true;
        }

        String a = args[1].replace("$", "");
        double amount;
        try {
            amount = Double.parseDouble(a);
        } catch (NumberFormatException e) {
            BankOfNoir.sendMessage(from, Strings.AMOUNT_NAN);
            return true;
        }
        if(amount < 0) {
            BankOfNoir.sendMessage(from, String.format(Strings.PAY_NEGATIVE_AMOUNT));
            return true;
        }
        if(amount == 0) {
            BankOfNoir.sendMessage(from, String.format(Strings.PAY_ZERO_AMOUNT));
            return true;
        }
        amount = Util.round(amount, new DecimalFormat("#.##"));

        Double fromBal = eco.getBalance(from.getUniqueId());
        Double toBal   = eco.getBalance(to.getUniqueId());

        if(fromBal < amount) {
            BankOfNoir.sendMessage(from, String.format(Strings.PAY_INSUFFICIENT_BALANCE, eco.format(amount - fromBal)));
            return true;
        }
        eco.setBalance(to.getUniqueId(), toBal + amount);
        eco.setBalance(from.getUniqueId(), fromBal - amount);

        if(to.getPlayer() != null) {
            BankOfNoir.sendMessage(to.getPlayer(), String.format(Strings.PAY_RECIEVED, eco.format(amount), from.getName()));
        }

        BankOfNoir.sendMessage(from, String.format(Strings.PAY_SUCCESSFUL, eco.format(amount), to.getName()));
        return true;
    }

}
