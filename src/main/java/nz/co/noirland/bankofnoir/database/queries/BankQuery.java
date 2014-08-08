package nz.co.noirland.bankofnoir.database.queries;

import nz.co.noirland.bankofnoir.database.BankDatabase;
import nz.co.noirland.zephcore.database.MySQLDatabase;
import nz.co.noirland.zephcore.database.queries.MySQLQuery;

public class BankQuery extends MySQLQuery {

    protected MySQLDatabase getDB() {
        return BankDatabase.inst();
    }

    public BankQuery(int nargs, String query) {
        super(nargs, query);
    }

    public BankQuery(String query) {
        super(query);
    }

    public BankQuery(Object[] values, String query) {
        super(values, query);
    }

}
