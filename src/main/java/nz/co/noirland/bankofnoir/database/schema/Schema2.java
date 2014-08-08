package nz.co.noirland.bankofnoir.database.schema;

import nz.co.noirland.bankofnoir.BankOfNoir;
import nz.co.noirland.bankofnoir.database.queries.BankQuery;
import nz.co.noirland.bankofnoir.database.queries.GetAllBalancesQuery;
import nz.co.noirland.zephcore.UUIDFetcher;
import nz.co.noirland.zephcore.database.Schema;
import nz.co.noirland.zephcore.database.queries.MySQLQuery;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Schema2 implements Schema {

    @Override
    public void run() {
        try {
            convertNamesToUUID();
            updateSchema();
        } catch (SQLException e) {
            BankOfNoir.debug().disable("Failed to update database to schema 2!", e);
        }
    }

    private void updateSchema() throws SQLException {
        new BankQuery("UPDATE `{PREFIX}_schema` SET `version` = 2").execute();
    }

    private void convertNamesToUUID() throws SQLException {
        BankOfNoir.debug().warning("Updating database players to UUIDs (this may take a while)");
        List<Map<String, Object>> res = new GetAllBalancesQuery().executeQuery();

        ArrayList<String> names = new ArrayList<String>();
        for(Map<String, Object> row : res) {
            names.add((String) row.get("player"));
        }

        Map<String, UUID> uuids = UUIDFetcher.getUUIDs(names);

        new BankQuery("ALTER TABLE `{PREFIX}_players` MODIFY COLUMN `player` VARCHAR(36)").execute();

        MySQLQuery updateQuery = new BankQuery(2, "UPDATE {PREFIX}_players SET player=? WHERE player=?");

        for(Map.Entry<String, UUID> entry : uuids.entrySet()) {
            updateQuery.setValue(1, entry.getValue().toString());
            updateQuery.setValue(2, entry.getKey());
            try {
                updateQuery.execute();
            } catch (SQLException e) {
                BankOfNoir.debug().warning("Could not update " + entry.getKey() + " to UUID!", e);
            }
        }

        BankOfNoir.debug().warning("Completed update to UUIDs.");
    }
}
