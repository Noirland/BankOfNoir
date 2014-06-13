package nz.co.noirland.bankofnoir;

import nz.co.noirland.zephcore.Util;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BankManager extends AbstractBankManager<UUID> {

    public BankManager() {
        eco = BankOfNoir.getEco();
    }

    @Override
    protected double getBalance(UUID owner) {
        return eco.getBalance(owner);
    }

    @Override
    protected String getName(UUID owner) {
        return Util.player(owner).getName();
    }

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
