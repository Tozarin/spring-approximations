package generated.org.springframework.boot;

import generated.org.springframework.boot.pinnedValues.PinnedValueSource;
import generated.org.springframework.boot.pinnedValues.PinnedValueStorage;
import org.usvm.api.Engine;
import org.usvm.spring.api.SpringEngine;

import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class SymbolicValueFactory {

    public static String createNonEmptySymbolicString(PinnedValueSource source, String name) {
        String string = PinnedValueStorage.getPinnedValue(source, name, String.class);
        Engine.assume(string != null);
        Engine.assume(!string.isEmpty());
        return string;
    }

    public static Object createValidSymbolic(Class<?> type, boolean nullable) {
        Object result = SymbolicValueFactory.createSymbolic(type, nullable, false);
        validateInputValue(result);
        return result;
    }

    public static Object createValidKotlinSymbolic(Class<?> type, boolean nullable) {
        Object result = SymbolicValueFactory.createSymbolic(type, nullable, true);
        validateInputValue(result);
        return result;
    }

    public static void validateInputValue(Object input) {
        if (input instanceof LocalDate) {
            LocalDate date = (LocalDate)input;
            int month = date.getMonthValue();
            Engine.assume(0 < month && month < 13);
            int day = date.getDayOfMonth();
            Engine.assume(0 < day && day < 32);
        }

        if (input instanceof LocalDateTime) {
            LocalDateTime date = (LocalDateTime)input;
            int month = date.getMonthValue();
            Engine.assume(0 < month && month < 13);
            int day = date.getDayOfMonth();
            Engine.assume(0 < day && day < 32);
            // TODO: Other time-related constraints #AA
        }
    }

    private static void prioritizeNull(Object value) {
        if (value == null) SpringEngine.markAsEdgeCasePath();
        else SpringEngine.markAsGoodPath();
    }

    private static void deprioritizeNull(Object value) {
        if (value == null) SpringEngine.markAsBadPath();
        else SpringEngine.markAsGoodPath();
    }

    private static Object createSymbolicWithSameType(
            Class<?> type,
            boolean makeNullable,
            boolean assumeKotlinNullables
    ) {
        if (makeNullable) {
            Object value =
                    assumeKotlinNullables
                            ? Engine.makeNullableKotlinSymbolic(type)
                            : Engine.makeNullableSymbolic(type);
            prioritizeNull(value);
            return value;
        }

        return assumeKotlinNullables ? Engine.makeKotlinSymbolic(type) : Engine.makeSymbolic(type);
    }

    private static Object createSymbolicSubtype(Class<?> type, boolean makeNullable, boolean assumeKotlinNullables) {
        if (makeNullable) {
            Object value =
                    assumeKotlinNullables
                            ? Engine.makeNullableKotlinSymbolicSubtype(type)
                            : Engine.makeNullableSymbolicSubtype(type);
            prioritizeNull(value);
            return value;
        }

        return assumeKotlinNullables ? Engine.makeKotlinSymbolicSubtype(type) : Engine.makeSymbolicSubtype(type);
    }

    public static Object createSymbolic(Class<?> type, boolean makeNullable, boolean assumeKotlinNullables) {
        if (type == boolean.class)
            return Engine.makeSymbolicBoolean();
        if (type == int.class)
            return Engine.makeSymbolicInt();
        if (type == long.class)
            return Engine.makeSymbolicLong();
        if (type == float.class)
            return Engine.makeSymbolicFloat();
        if (type == byte.class)
            return Engine.makeSymbolicByte();
        if (type == char.class)
            return Engine.makeSymbolicChar();
        if (type == short.class)
            return Engine.makeSymbolicShort();

        if (type.isArray())
            return Engine.makeSymbolicArray(type.componentType(), Engine.makeSymbolicInt());

        if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
            if (type.isAssignableFrom(ArrayList.class))
                return createSymbolicWithSameType(ArrayList.class, makeNullable, assumeKotlinNullables);
            if (type.isAssignableFrom(HashSet.class))
                return createSymbolicWithSameType(HashSet.class, makeNullable, assumeKotlinNullables);
            if (type.isAssignableFrom(HashMap.class))
                return createSymbolicWithSameType(HashMap.class, makeNullable, assumeKotlinNullables);

            return createSymbolicSubtype(type, makeNullable, assumeKotlinNullables);
        }

        return createSymbolicWithSameType(type, makeNullable, assumeKotlinNullables);
    }
}
