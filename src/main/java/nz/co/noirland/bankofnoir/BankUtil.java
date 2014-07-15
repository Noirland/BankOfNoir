package nz.co.noirland.bankofnoir;

import nz.co.noirland.zephcore.Util;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

/**
 * Static utilities class for BankOfNoir.
 */
public class BankUtil {

    /**
     * Checks if the specified block is a bank sign.<br />
     * This checks that the sign is both a sign, and has the correct tag on the first line.
     * @param block The block to check.
     * @return Whether or not the block is a bank sign
     */
    public static boolean isBankSign(Block block) {
        if(!Util.isSign(block)) return false;
        Sign sign = (Sign) block.getState();
        return isBankLine(sign.getLine(0));
    }

    /**
     * Checks whether a given string is the bank chest tag. This strips away all colour, and ignores case.
     * @param line The String to check
     * @return Whether or not it is a bank chest tag
     */
    public static boolean isBankLine(String line) {
        return ChatColor.stripColor(line).replace(" ", "").equalsIgnoreCase(BankOfNoir.SIGN_TITLE);
    }

    /**
     * Recursive function used to check for bank signs placed around a chest or double chest.
     * @param block The block to check for a bank sign
     * @param origin The block that initiated the search. This ensured this doesn't loop endlessly
     * @return The first bank sign found if this chest has one, or null if it doesn't
     */
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
