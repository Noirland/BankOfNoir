package nz.co.noirland.bankofnoir.database.schema;

import nz.co.noirland.bankofnoir.BankOfNoir;
import nz.co.noirland.bankofnoir.database.queries.BankQuery;
import nz.co.noirland.zephcore.database.Schema;

import java.sql.SQLException;

public class Schema1 implements Schema {

    public void run() {
        try {
            createPlayersTable();
            createSchemaTable();
        } catch (SQLException e) {
            BankOfNoir.debug().disable("Could not update database to schema 1!", e);
        }
    }

    private void createSchemaTable() throws SQLException {
        new BankQuery("CREATE TABLE `{PREFIX}_schema` (`version` TINYINT UNSIGNED);").execute();
        new BankQuery("INSERT INTO `{PREFIX}_schema` VALUES(1);").execute();
    }

    private void createPlayersTable() throws SQLException {
        new BankQuery("CREATE TABLE `{PREFIX}_players` (`player` VARCHAR(16), `balance` INT UNSIGNED, PRIMARY KEY(`player`))").execute();
    }
}
