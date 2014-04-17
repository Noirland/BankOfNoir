package nz.co.noirland.bankofnoir.database.schema;

import nz.co.noirland.bankofnoir.BankOfNoir;
import nz.co.noirland.bankofnoir.config.PluginConfig;
import nz.co.noirland.bankofnoir.database.DatabaseQueries;
import nz.co.noirland.bankofnoir.database.DatabaseTables;
import nz.co.noirland.bankofnoir.database.SQLDatabase;
import nz.co.noirland.zephcore.UUIDFetcher;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class Schema2 extends Schema {

    private final SQLDatabase db = SQLDatabase.inst();

    @Override
    public void updateDatabase() {
        convertNamesToUUID();
        try {
            db.prepareStatement("UPDATE `" + PluginConfig.inst().getPrefix() + "schema` SET `version` = 2").execute();
        } catch (SQLException e) {
            BankOfNoir.debug().disable("Failed to update schema!");
        }
    }

    private void convertNamesToUUID() {
        PreparedStatement statement = db.prepareStatement(DatabaseQueries.GET_ALL_BALANCES);
        BankOfNoir.debug().warning("Updating database players to UUIDs.");
        try {
            ResultSet res = statement.executeQuery();
            ArrayList<String> names = new ArrayList<String>();
            while(res.next()) {
                names.add(res.getString("player"));
            }
            statement.close();

            db.prepareStatement("ALTER TABLE `" + DatabaseTables.PLAYERS.toString() + "` MODIFY COLUMN `player` VARCHAR(36)").execute();

            Map<String, UUID> uuids = UUIDFetcher.getUUIDs(names);
            for(Map.Entry<String, UUID> entry : uuids.entrySet()) {
                PreparedStatement s = db.prepareStatement("UPDATE " + DatabaseTables.PLAYERS.toString() + " SET player=? WHERE player=?");
                s.setString(1, entry.getValue().toString());
                s.setString(2, entry.getKey());
                s.execute();
            }
            BankOfNoir.debug().warning("Completed update to UUIDs.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
