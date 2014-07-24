package nz.co.noirland.bankofnoir.database.queries;

public class GetAllBalancesQuery extends BankQuery {

    private static final String QUERY = "SELECT * FROM `{PREFIX}_players`";

    public GetAllBalancesQuery() {
        super(QUERY);
    }

}
