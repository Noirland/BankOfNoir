package nz.co.noirland.bankofnoir.database.schema;

import nz.co.noirland.bankofnoir.BankOfNoir;
import nz.co.noirland.bankofnoir.database.DatabaseTables;
import nz.co.noirland.bankofnoir.database.SQLDatabase;

import java.sql.SQLException;

public class Schema1 extends Schema {

    private final SQLDatabase db = SQLDatabase.inst();

    public void updateDatabase() {
        createPlayersTable();
        createSchemaTable();
    }

    private void createSchemaTable() {
        String schemaTable = DatabaseTables.SCHEMA.toString();
        try{
            db.prepareStatement("CREATE TABLE `" + schemaTable + "` (`version` TINYINT UNSIGNED);").execute();
            db.prepareStatement("INSERT INTO `" + schemaTable + "` VALUES(1);").execute();
        }catch(SQLException e) {
            BankOfNoir.debug().disable("Could not create schema table!", e);
        }
    }

    private void createPlayersTable() {
        try {
            db.prepareStatement("CREATE TABLE `" + DatabaseTables.PLAYERS.toString() + "` (`player` VARCHAR(16), `balance` INT UNSIGNED, PRIMARY KEY(`player`))").execute();
        }catch(SQLException e) {
            BankOfNoir.debug().disable("Couldn't create players table!", e);
        }
    }
}
