package nz.co.noirland.bankofnoir.database;

import nz.co.noirland.bankofnoir.BankOfNoir;
import nz.co.noirland.bankofnoir.config.PluginConfig;
import nz.co.noirland.bankofnoir.database.schema.Schema;
import nz.co.noirland.zephcore.database.AsyncDatabaseUpdateTask;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SQLDatabase {

    private static SQLDatabase inst;
    private Connection con;

    public static SQLDatabase inst() {
        if(inst == null) {
            inst = new SQLDatabase();
        }
        return inst;
    }

    private SQLDatabase() {
        openConnection();
    }

    // -- QUERY FUNCTIONS -- //

    public boolean isTable(String table) {
        try {
            prepareStatement("SELECT * FROM " + table).execute();
            return true; // Result can never be null, bad logic from earlier versions.
        } catch (SQLException e) {
            return false; // Query failed, table does not exist.
        }
    }

    public void setBalance(UUID player, double balance) {
        PreparedStatement statement = prepareStatement(DatabaseQueries.SET_BALANCE);
        try {
            statement.setString(1, player.toString());
            statement.setDouble(2, balance);
        } catch (SQLException e) {
            BankOfNoir.debug().debug("Could not set balance of " + player + " to " + balance + "!", e);
        }
        runStatementAsync(statement);
    }

    public Map<UUID, Double> getAllBalances() {
        PreparedStatement query = prepareStatement(DatabaseQueries.GET_ALL_BALANCES);
        HashMap<UUID, Double> ret = new HashMap<UUID, Double>();
        try {
            ResultSet res = query.executeQuery();

            while(res.next()) {
                ret.put(UUID.fromString(res.getString("player")), res.getDouble("balance"));
            }
        } catch(SQLException e) {
            BankOfNoir.debug().warning("Could not get balances from database!", e);
        }
        return ret;
    }

    // -- DATABASE FUNCTIONS -- //

    public PreparedStatement prepareStatement(String query) {
        try {
            if(query.startsWith("INSERT")) {
                return con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            }
            return con.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        } catch (SQLException e) {
            BankOfNoir.debug().disable("Could not create statement for database!", e);
            return null;
        }
    }

    public void disconnect() {
        try {
            con.close();
        } catch (SQLException e) {
            BankOfNoir.debug().debug("Couldn't close connection to database.", e);
        }
    }

    public void checkSchema() {
        int version = getSchema();
        int latest = Schema.getCurrentSchema();
        if(version == latest) {
            return;
        }
        if(version > latest) {
            BankOfNoir.debug().disable("Database schema is newer than this plugin version!");
        }

        for(int i = version + 1; i <= latest; i++) {
            Schema.getSchema(i).updateDatabase();
        }

    }

    private int getSchema() {
        try {
            if(isTable(DatabaseTables.SCHEMA.toString())) {
                ResultSet res = con.prepareStatement(DatabaseQueries.GET_SCHEMA).executeQuery();
                res.first();
                return res.getInt("version");
            }else{
                // SCHEMA table does not exist, tables not set up
                return 0;
            }
        } catch (SQLException e) {
            BankOfNoir.debug().disable("Could not get database schema!", e);
            return 0;
        }
    }

    public void runStatementAsync(PreparedStatement statement) {
        AsyncDatabaseUpdateTask.updates.add(statement);
    }

    public void openConnection() {
        try {
            if(con != null && !con.isClosed()) {
                con.close();
            }
        } catch (SQLException ignored) {}
        PluginConfig config = PluginConfig.inst();
        String url = "jdbc:mysql://" + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase();
        try {
            con = DriverManager.getConnection(url, config.getUsername(), config.getPassword());
        } catch (SQLException e) {
            BankOfNoir.debug().disable("Couldn't connect to database!", e);
        }
    }
    // -- UTILITY FUNCTIONS -- //

    public static int getNumRows(ResultSet rs) {
        try {
            rs.last();
            return rs.getRow();
        } catch (SQLException e) {
            BankOfNoir.debug().debug("Could not get number of rows of result set!", e);
            return -1;
        }
    }

}
