package nz.co.noirland.bankofnoir;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import nz.co.noirland.bankofnoir.config.PluginConfig;
import nz.co.noirland.zephcore.Util;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;

import java.util.ArrayList;
import java.util.List;

public class VaultConnector implements Economy {

    private final String name = "BankOfNoir";
    private final EcoManager eco = BankOfNoir.getEco();
    private final PluginConfig config = PluginConfig.inst();

    public VaultConnector() {
        final BankOfNoir plugin = BankOfNoir.inst();
        final ServicesManager sm = plugin.getServer().getServicesManager();
        sm.register(Economy.class, this, plugin, ServicePriority.Highest);
    }

    @Override
    public boolean isEnabled() {
        return BankOfNoir.inst() != null && BankOfNoir.inst().isEnabled();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return config.getDecimals();
    }

    @Override
    public String format(double amount) {
        return eco.format(amount);
    }

    @Override
    public String currencyNamePlural() {
        return config.getPlural();
    }

    @Override
    public String currencyNameSingular() {
        return config.getSingular();
    }

    @Override
    public boolean hasAccount(String player) {
        return eco.hasBalance(Util.uuid(player));
    }

    @Override
    public double getBalance(String player) {
        return eco.getBalance(Util.uuid(player));
    }

    @Override
    public boolean has(String player, double amount) {
        return eco.getBalance(Util.uuid(player)) >= amount;
    }

    @Override
    public EconomyResponse withdrawPlayer(String player, double amount) {
        double balance = eco.getBalance(Util.uuid(player));
        boolean hasAmount = balance >= amount;
        EconomyResponse response;

        if(hasAmount) {
            eco.setBalance(Util.uuid(player), amount);
            balance = eco.getBalance(Util.uuid(player));
            response = new EconomyResponse(amount, balance, EconomyResponse.ResponseType.SUCCESS, null);
        }else{
            response = new EconomyResponse(0, balance, EconomyResponse.ResponseType.FAILURE, Strings.VAULT_INSUFFICIENT_FUNDS);
        }
        return response;
    }

    @Override
    public EconomyResponse depositPlayer(String player, double amount) {
        eco.setBalance(Util.uuid(player), eco.getBalance(Util.uuid(player)) + amount);
        return new EconomyResponse(amount, eco.getBalance(nz.co.noirland.zephcore.Util.uuid(player)), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public boolean createPlayerAccount(String player) {
        return hasAccount(player);
    }

    @Override
    public EconomyResponse createBank(String name, String player) {
        return getNotImplemented();
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return getNotImplemented();
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return getNotImplemented();
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return getNotImplemented();
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return getNotImplemented();
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return getNotImplemented();
    }

    @Override
    public EconomyResponse isBankOwner(String name, String player) {
        return getNotImplemented();
    }

    @Override
    public EconomyResponse isBankMember(String name, String player) {
        return getNotImplemented();
    }

    @Override
    public List<String> getBanks() {
        return new ArrayList<String>();
    }

    private EconomyResponse getNotImplemented() {
        return new EconomyResponse(0,0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, Strings.VAULT_NO_BANKS);
    }

    @Override
    public EconomyResponse withdrawPlayer(String player, String world, double amount) {
        return withdrawPlayer(player, amount);
    }

    @Override
    public boolean hasAccount(String player, String world) {
        return hasAccount(player);
    }

    @Override
    public double getBalance(String player, String world) {
        return getBalance(player);
    }

    @Override
    public boolean has(String player, String world, double amount) {
        return has(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String player, String world, double amount) {
        return depositPlayer(player, amount);
    }

    @Override
    public boolean createPlayerAccount(String player, String world) {
        return createPlayerAccount(player);
    }

}
