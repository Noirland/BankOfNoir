package nz.co.noirland.bankofnoir.database;

import nz.co.noirland.bankofnoir.BankOfNoir;
import nz.co.noirland.bankofnoir.BankConfig;
import nz.co.noirland.bankofnoir.database.queries.GetAllBalancesQuery;
import nz.co.noirland.bankofnoir.database.queries.UpdateBalanceQuery;
import nz.co.noirland.bankofnoir.database.schema.Schema1;
import nz.co.noirland.bankofnoir.database.schema.Schema2;
import nz.co.noirland.bankofnoir.database.schema.Schema3;
import nz.co.noirland.zephcore.Debug;
import nz.co.noirland.zephcore.database.mysql.MySQLDatabase;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BankDatabase extends MySQLDatabase {

    private static BankDatabase inst;

    private static BankConfig config = BankConfig.inst();

    public static BankDatabase inst() {
        if(inst == null) {
            inst = new BankDatabase();
        }
        return inst;
    }

    private BankDatabase() {
        inst = this;
        schemas.put(1, new Schema1());
        schemas.put(2, new Schema2());
        schemas.put(3, new Schema3());
    }

    @Override
    public Debug debug() {
        return BankOfNoir.debug();
    }

    @Override
    protected String getHost() {
        return config.getHost();
    }

    @Override
    protected int getPort() {
        return config.getPort();
    }

    @Override
    protected String getDatabase() {
        return config.getDatabase();
    }

    @Override
    protected String getUsername() {
        return config.getUsername();
    }

    @Override
    protected String getPassword() {
        return config.getPassword();
    }

    @Override
    public String getPrefix() {
        return config.getPrefix();
    }

    public void setBalance(UUID player, double balance) {
        new UpdateBalanceQuery(player, balance).executeAsync();
    }

    public Map<UUID, Double> getAllBalances() {
        HashMap<UUID, Double> ret = new HashMap<UUID, Double>();
        List<Map<String, Object>> res;
        try {
            res = new GetAllBalancesQuery().executeQuery();
        } catch (SQLException e) {
            BankOfNoir.debug().warning("Could not get balances from database!", e);
            return ret;
        }

        for(Map<String, Object> row : res) {
            UUID uuid = UUID.fromString((String) row.get("player"));
            double balance = (Double) row.get("balance");
            ret.put(uuid, balance);
        }
        return ret;
    }
}
