package nz.co.noirland.bankofnoir;

import nz.co.noirland.zephcore.Util;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * The default BankManager for the plugin, used for storing a player's bank.
 */
public class BankManager extends AbstractBankManager<UUID> {

    @Override
    protected double getBalance(UUID owner) {
        return eco.getBalance(owner);
    }

    @Override
    protected String getName(UUID owner) {
        return Util.player(owner).getName();
    }

    /**
     * Updates the player's balance. Also sends them (and the updater) a message telling them of the
     * update.
     * @param owner The owner of the balance to update
     * @param updater The person who caused the update (eg by adding to a bank)
     * @param balance The new balance of the owner's account
     * @param diff The difference between the old balance and the new balance
     */
    @Override
    public void updateBalance(UUID owner, Player updater, double balance, double diff) {
        eco.setBalance(owner, balance);
        String action;

        if(diff > 0) {
            action = Strings.ECO_DEPOSITED;
        }else {
            action = Strings.ECO_WITHDREW;
        }
        BankOfNoir.sendMessage(updater, String.format(action, eco.format(Math.abs(diff))));

        OfflinePlayer pOwner = Util.player(owner);
        if(!pOwner.equals(updater) && pOwner.hasPlayedBefore() && pOwner.isOnline()) {
            BankOfNoir.sendMessage(pOwner.getPlayer(), String.format(action, eco.format(Math.abs(diff))));
        }
    }
}
