package nz.co.noirland.bankofnoir.database.schema;

import nz.co.noirland.bankofnoir.BankOfNoir;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public abstract class Schema {

    private static final SortedMap<Integer, Schema> schemas = new TreeMap<Integer, Schema>();

    static {
        if(schemas.isEmpty()) {
            schemas.put(1, new Schema1());
            schemas.put(2, new Schema2());
        }
    }

    public static Map<Integer, Schema> getSchemas() {
        return schemas;
    }

    public static int getCurrentSchema() {
        return schemas.lastKey();
    }

    public static Schema getSchema(int version) {
        if(schemas.containsKey(version)) {
            return schemas.get(version);
        }else{
            BankOfNoir.debug().debug("Could not find schema version: " + version);
            return null;
        }
    }
    public abstract void updateDatabase();



}
