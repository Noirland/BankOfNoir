package nz.co.noirland.bankofnoir.database.schema;

import nz.co.noirland.bankofnoir.BankOfNoir;
import nz.co.noirland.bankofnoir.database.queries.BankQuery;
import nz.co.noirland.zephcore.database.Schema;

import java.sql.SQLException;

public class Schema3 implements Schema {

    @Override
    public void run() {
        try {
            updateIntsToDoubles();
            updateSchema();
        } catch (SQLException e) {
            BankOfNoir.debug().disable("Failed to update database to schema 3!", e);
        }
    }

    private void updateSchema() throws SQLException {
        new BankQuery("UPDATE `{PREFIX}_schema` SET `version` = 3").execute();
    }

    private void updateIntsToDoubles() throws SQLException {
        new BankQuery("ALTER TABLE `{PREFIX}_players` MODIFY COLUMN `balance` DOUBLE").execute();
    }
}
