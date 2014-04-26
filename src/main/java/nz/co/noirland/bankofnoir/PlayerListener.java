package nz.co.noirland.bankofnoir;

import nz.co.noirland.zephcore.Util;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class PlayerListener implements Listener {

    private final EcoManager eco = BankOfNoir.getEco();


    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onOpenChest(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK || (event.getAction() == Action.RIGHT_CLICK_BLOCK && player.isSneaking())) return;
        if(block.getType() != Material.CHEST) return;

        Sign sign = BankUtil.checkForBankSign(block, null);
        if(sign == null) return;


        event.setCancelled(true);

        String owner = sign.getLine(2) + sign.getLine(3);
        String p = player.getName();
        if(!p.equals(owner) && !player.hasPermission(Permissions.SEE)) { // Name may have been concatenated
            BankOfNoir.sendMessage(player, Strings.BANK_NO_ACCESS);
            return;
        }

        player.openInventory(eco.getBank(nz.co.noirland.zephcore.Util.uuid(owner)).getBank());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCloseChest(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        Player player = (Player) event.getPlayer();

        BankInventory bank = eco.getOpenBank(inv);
        if(bank == null) {
            return;
        }
        UUID owner = bank.getOwner();
        eco.removeOpenBank(bank);

        double balance = eco.getBalance(owner);
        double newBalance = eco.itemsToBalance(inv.getContents()) + bank.getRemainder();

        for(ItemStack item : inv.getContents()) {
            if(item == null) continue;
            if(eco.isDenomination(item.getType())) continue;

            PlayerInventory pInv = player.getInventory();
            if(pInv.firstEmpty() != -1) {
                pInv.addItem(item);
                new UpdateInventoryTask(player);
            }else{
                player.getWorld().dropItem(player.getLocation(), item);
            }
        }

        if(newBalance != balance) {
            eco.setBalance(owner, newBalance);
            String action;

            if(newBalance > balance) {
                action = Strings.ECO_DEPOSITED;
            }else {
                action = Strings.ECO_WITHDREW;
            }
            BankOfNoir.sendMessage(player, String.format(action, eco.format(Math.abs(newBalance - balance))));

            OfflinePlayer pOwner = Util.player(owner);
            if(!pOwner.equals(player) && pOwner.hasPlayedBefore() && pOwner.isOnline()) {
                BankOfNoir.sendMessage(pOwner.getPlayer(), String.format(action, eco.format(Math.abs(newBalance - balance))));
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSignPlace(SignChangeEvent event) {
        Block bSign = event.getBlock();
        Player player = event.getPlayer();

        if(!BankUtil.isBankLine(event.getLine(0))) return;

        org.bukkit.material.Sign s = (org.bukkit.material.Sign) bSign.getState().getData();
        Block bAttached = bSign.getRelative(s.getAttachedFace());
        if(bAttached.getType() != Material.CHEST || s.getAttachedFace() == BlockFace.DOWN) {
            event.setCancelled(true);
            bSign.breakNaturally();
            return;
        }
        if(BankUtil.checkForBankSign(bAttached, bSign) != null) {
            BankOfNoir.sendMessage(player, Strings.BANK_ALREADY_OWNED);
            event.setCancelled(true);
            bSign.breakNaturally();
            return;
        }

        Inventory inv = ((InventoryHolder) bAttached.getState()).getInventory();
        ItemStack[] contents = inv.getContents();
        Double val = 0D;
        for(int i = 0; i<inv.getSize(); i++) {
            ItemStack item = contents[i];
            if(item == null) continue;
            inv.setItem(i, null);
            if(eco.isDenomination(item.getType())) {
                val += eco.getDenomination(item.getType()).getValue() * item.getAmount();
                continue;
            }

            HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
            for(ItemStack left : leftovers.values()) {
                player.getWorld().dropItem(player.getLocation(), left);
            }
            new UpdateInventoryTask(player);
        }
        if(val > 0) {
            eco.setBalance(player.getUniqueId(), eco.getBalance(player.getUniqueId()) + val);
            BankOfNoir.sendMessage(player, String.format(Strings.ECO_DEPOSITED, eco.format(val)));
        }

        String pName = player.getName();

         String[] lines = event.getLines();
        lines[0] = ChatColor.BOLD + lines[0];
        lines[1] = "";
        lines[2] = pName.substring(0, Math.min(pName.length(), 15));
        lines[3] = "";
        if(pName.length() > 15) {
            lines[3] = pName.substring(15);
        }

    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockExplode(EntityExplodeEvent event) {
        List<Block> exploded = event.blockList();
        Iterator<Block> it = exploded.iterator();
        while(it.hasNext()) {
            Block block = it.next();
            if(BankUtil.isBankSign(block) || BankUtil.checkForBankSign(block, null) != null) {
                it.remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        Block block = event.getBlock();
        if(BankUtil.isBankSign(block) || BankUtil.checkForBankSign(block, null) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Sign sign;
        Player player = event.getPlayer();
        if (BankUtil.isBankSign(block)) {
            sign = (org.bukkit.block.Sign) block.getState();
        }else{
            sign = BankUtil.checkForBankSign(block, null);
            if(sign == null) return;
        }

        String owner = sign.getLine(2) + sign.getLine(3);
        if(!player.getName().equals(owner) && !player.hasPermission(Permissions.SEE)) {
            BankOfNoir.sendMessage(player, Strings.BANK_NO_ACCESS);
            event.setCancelled(true);
        }
    }

    private class UpdateInventoryTask extends BukkitRunnable {
        private Player player;
        UpdateInventoryTask(Player player) {
            this.player = player;
            runTaskLater(BankOfNoir.inst(), 0);
        }

        @Override
        public void run() {
            player.updateInventory();
        }
    }
}
