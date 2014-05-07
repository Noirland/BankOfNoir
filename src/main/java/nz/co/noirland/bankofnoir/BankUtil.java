package nz.co.noirland.bankofnoir;

import nz.co.noirland.zephcore.Util;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

public class BankUtil {
    public static boolean isBankSign(Block block) {
        if(!Util.isSign(block)) return false;
        Sign sign = (Sign) block.getState();
        return isBankLine(sign.getLine(0));
    }

    public static boolean isBankLine(String line) {
        return ChatColor.stripColor(line).replace(" ", "").equalsIgnoreCase(BankOfNoir.SIGN_TITLE);
    }

    public static Sign checkForBankSign(Block block, Block origin) {
        final BlockFace[] checkFaces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
        Sign ret = null;
        for(BlockFace face : checkFaces) {
            Block side = block.getRelative(face);
            if(side.equals(origin)) continue;
            if(block.getType() != Material.CHEST) continue;
            if(side.getType() == Material.CHEST) {
                ret = checkForBankSign(side, block);
            }
            if(side.getType() == Material.WALL_SIGN) {
                if(!Util.isSignAttachedToBlock(side, block)) continue;
                if(!isBankSign(side)) continue;
                ret = (Sign) side.getState();
            }

            if(ret != null) return ret;
        }
        return ret;
    }

}
