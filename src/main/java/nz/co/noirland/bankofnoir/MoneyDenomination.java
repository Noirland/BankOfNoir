package nz.co.noirland.bankofnoir;

import org.bukkit.Material;

public class MoneyDenomination implements Comparable<MoneyDenomination> {

    private final Material material;
    private final double value;
    private Material roundMat;
    private int roundNum;

    public MoneyDenomination(Material material, double value) {
        this.material = material;
        this.value = value;
    }

    public MoneyDenomination(Material material, double value, Material roundMat, int roundNum) {
        this.material = material;
        this.value = value;
        this.roundMat = roundMat;
        this.roundNum = roundNum;
    }

    public Material getMaterial() {
        return material;
    }

    public double getValue() {
        return value;
    }

    public Material getRoundMat() {
        return roundMat;
    }

    public int getRoundNum() {
        return roundNum;
    }

    @Override
    public int compareTo(MoneyDenomination o) {
        return Double.compare(getValue(), o.getValue());
    }
}
