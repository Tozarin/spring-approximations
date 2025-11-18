package generated.org.springframework.boot.databases.basetables;

import generated.org.springframework.boot.databases.ITable;
import generated.org.springframework.boot.databases.SequenceTable;
import generated.org.springframework.boot.databases.iterators.basetables.BaseTableIterator;
import generated.org.springframework.boot.databases.utils.ConcreteTableManager;
import org.jetbrains.annotations.NotNull;
import org.usvm.api.Engine;
import org.usvm.spring.api.SpringEngine;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static generated.org.springframework.boot.databases.utils.DatabaseValidators.deprioritizeValue;

public class BaseTable<T> extends ABaseTable<T> {

    private final T[] data;
    private final int size;
    private final Class<T> entityType;

    private final ConcreteTableManager<T> concreteManager;
    private final int userEntitiesSize;
    private final List<T> userData;
    private final Set<Integer> userEntitiesInitialized;

    private final boolean generatedId;

    private Supplier<T> blankInit;
    // build relation between T object and related classes
    private Consumer<T> relationsInit;
    private Consumer<T> relationsInitForConcrete;

    // used only iff idIndexes.size == 1 for id generation
    private Function<T, Object> getIdFunction;
    private BiConsumer<T, Object> setIdFunction;
    // builds array with all id columns
    private Function<T, Object[]> buildIdFunction;

    private Function<T, Object>[] gettersForSoft;
    private Class<?>[] typesForSoft;

    public BaseTable(
            Class<T> type,
            boolean generatedId,
            String[] idFieldsNames,
            String[] relatedFieldsNames
    ) {
        this.generatedId = generatedId;
        this.size = Engine.makeSymbolicInt();
        this.entityType = type;

        // TODO: think about tables with 0 size (execution prioritize 0 size, that leads to nulls by first())
        Engine.assume(size > 0);
        Engine.assume(size <= 10);

        this.concreteManager = new ConcreteTableManager<>(entityType, idFieldsNames, relatedFieldsNames);
        this.userEntitiesSize = concreteManager.getUserEntitiesSize();
        this.userData = concreteManager.getUserData();

        this.userEntitiesInitialized = new HashSet<>();
        this.data = Engine.makeConcreteArray(type, size + userEntitiesSize);
        Engine.assume(this.data != null);
        for (int i = 0; i < userEntitiesSize; i++)
            data[i] = userData.get(i);

        this.blankInit = null;
        this.relationsInit = null;
        this.relationsInitForConcrete = null;
        this.getIdFunction = null;
        this.setIdFunction = null;
        this.buildIdFunction = null;

        this.gettersForSoft = null;
        this.typesForSoft = null;
    }

    public int getUserEntitiesSize() { return userEntitiesSize; }

    @Override
    public int size() {
        return size + userEntitiesSize;
    }

    public T getRowEnsure(int ix) {
        Engine.assume(ix < size());
        T dataT = data[ix];
        if (dataT != null) {
            if (ix < userEntitiesSize && userEntitiesInitialized.add(ix))
                relationsInitForConcrete.accept(dataT);

            return dataT;
        }

        T t = Engine.makeKotlinSymbolic(entityType);
        Engine.assume(t != null);

        if (generatedId) setNewGeneratedId(t);

        relationsInit.accept(t);
        applyCommonSoftValidation(t);

        data[ix] = t;

        return data[ix];
    }

    public ITable<T> getConcreteEntities() {

        class Context {
            public int ix = 0;
            public final int maxSize = userEntitiesSize;
            public final List<T> entities = userData;
        }

        return new SequenceTable<>(
                new Context(),
                (Context ctx) -> ctx.ix < ctx.maxSize,
                (Context ctx) -> ctx.entities.get(ctx.ix++)
        );
    }

    private void applyCommonSoftValidation(T t) {
        for (int i = 0; i < gettersForSoft.length; i++) {
            Class<?> softType = typesForSoft[i];
            Function<T, Object> getter = gettersForSoft[i];
            Object value = getter.apply(t);
            deprioritizeValue(value, softType);
        }
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return new BaseTableIterator<>(this);
    }

    public void setDTOFunctions(
            Supplier<T> blankInit,
            Consumer<T> relationsInit,
            Consumer<T> relationsInitForConcrete,
            Function<T, Object[]> buildIdFunction,
            Function<T, Object> getIdFunction,
            BiConsumer<T, Object> setIdFunction
    ) {
        this.blankInit = blankInit;
        this.relationsInit = relationsInit;
        this.relationsInitForConcrete = relationsInitForConcrete;
        this.buildIdFunction = buildIdFunction;
        this.getIdFunction = getIdFunction;
        this.setIdFunction = setIdFunction;
    }

    public void setForSoft(Function<T, Object>[] getters, Class<?>[] types) {
        this.gettersForSoft = getters;
        this.typesForSoft = types;
    }

    public void setNewGeneratedId(T t) {
        if (!generatedId)
            SpringEngine.println("[DB Warning] Id generation for non-generatedId class");

        Object newId = concreteManager.generateNewId();
        setIdFunction.accept(t, newId);
    }

    @Override
    public Object[] buildId(T t) {
        return buildIdFunction.apply(t);
    }
}
