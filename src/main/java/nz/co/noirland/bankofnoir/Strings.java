package nz.co.noirland.bankofnoir;

import org.bukkit.ChatColor;

public class Strings {

    public static final String AMOUNT_NAN = "Amount is not a number!";
    public static final String NO_PERMISSION = ChatColor.DARK_RED + "You do not have permission to use that command.";
    public static final String PLAYER_NOT_EXISTS = "%s has never played on this server.";

    public static final String VAULT_NO_BANKS = "Banks not supported in BankOfNoir.";
    public static final String VAULT_INSUFFICIENT_FUNDS = "You do not have enough money to do that.";

    public static final String PAY_NO_CONSOLE = "Consoles cannot pay a player. Please use /bankadmin adjust [player] [amount]";
    public static final String PAY_WRONG_ARGS = "You must specify a player and an amount to pay.";
    public static final String PAY_SELF = "You can't pay yourself!";
    public static final String PAY_INSUFFICIENT_BALANCE = "You don't have enough money, you need %s";
    public static final String PAY_SUCCESSFUL = "Sent %s to %s.";

    public static final String BANK_NO_CONSOLE = "Consoles cannot use /bank.";
    public static final String BANK_NO_ACCESS = "You do not own this Vault.";
    public static final String BANK_ALREADY_OWNED = "That chest is already a Bank.";

    public static final String BANKADMIN_BAL_UPDATED = "Updated balance of %s by %s";

    public static final String ECO_DEPOSITED = "Deposited %s";
    public static final String ECO_WITHDREW = "Withdrew %s";

}
