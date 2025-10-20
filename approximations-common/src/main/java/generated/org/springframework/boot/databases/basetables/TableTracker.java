package generated.org.springframework.boot.databases.basetables;

import org.usvm.spring.api.SpringEngine;

import java.util.ArrayList;
import java.util.List;

public class TableTracker {

    private final static List<String> tablesNames = new ArrayList<>();
    private final static List<Integer> lastIxs = new ArrayList<>();

    private static int indexOf(String tableName) {
        for (int i = 0; i < tablesNames.size(); i++) {
            String name = tablesNames.get(i);
            if (tableName.equals(name)) return i;
        }

        return -1;
    }

    public static <T> void tryTrack(String tableName, T value, int entityIx, Class<T> type) {
        int ix = indexOf(tableName);

        if (ix == -1) {
            tablesNames.add(tableName);
            lastIxs.add(entityIx);
            track(tableName, value, type, entityIx);
            return;
        }

        int oldEntityIx = lastIxs.get(ix);
        if (oldEntityIx >= entityIx) return;

        if (entityIx != oldEntityIx + 1) SpringEngine.println("[DB Warning] Unexpected entity index in tryTrack");

        lastIxs.remove(ix);
        lastIxs.add(ix, entityIx);

        track(tableName, value, type, entityIx);
    }

    public static <T> void track(String tableName, T value, Class<T> type, int ix) {
    }
}
