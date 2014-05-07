package nz.co.noirland.bankofnoir.database;

public class DatabaseQueries {

    public static final String GET_SCHEMA         = "SELECT `version` FROM `" + DatabaseTables.SCHEMA.toString() + "`";
    public static final String SET_BALANCE        = "INSERT INTO `" + DatabaseTables.PLAYERS.toString() + "`(`player`, `balance`) VALUES (?,?) ON DUPLICATE KEY UPDATE `balance`=VALUES(`balance`)";
    public static final String GET_BALANCE        = "SELECT * FROM `" + DatabaseTables.PLAYERS.toString() + "` WHERE `player` = ?";
    public static final String GET_ALL_BALANCES   = "SELECT * FROM `" + DatabaseTables.PLAYERS.toString() + "`";


}
