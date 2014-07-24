package nz.co.noirland.bankofnoir.database.queries;

import java.util.UUID;

public class GetBalanceQuery extends BankQuery {

    private static final String QUERY = "SELECT * FROM `{PREFIX}_players` WHERE `player` = ?";

    public GetBalanceQuery(UUID player) {
        super(1, QUERY);
        setValue(1, player.toString());
    }

}
