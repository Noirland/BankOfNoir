package nz.co.noirland.bankofnoir.database.queries;

import java.util.UUID;

public class UpdateBalanceQuery extends BankQuery {

    private static final String QUERY = "INSERT INTO `{PREFIX}_players`(`player`, `balance`) VALUES (?,?) ON DUPLICATE KEY UPDATE `balance`=VALUES(`balance`)";

    public UpdateBalanceQuery(UUID player, double balance) {
        super(2, QUERY);
        setValue(1, player.toString());
        setValue(2, balance);
    }

}
