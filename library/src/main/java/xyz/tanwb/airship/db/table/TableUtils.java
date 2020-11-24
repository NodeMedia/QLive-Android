package xyz.tanwb.airship.db.table;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedHashMap;

import xyz.tanwb.airship.db.annotation.Column;
import xyz.tanwb.airship.db.converter.ColumnConverterFactory;

public final class TableUtils {

    private TableUtils() {
    }

    public static synchronized LinkedHashMap<String, ColumnEntity> findColumnMap(Class<?> entityType) throws Exception {
        LinkedHashMap<String, ColumnEntity> columnMap = new LinkedHashMap<String, ColumnEntity>();
        addColumns2Map(entityType, columnMap);
        return columnMap;
    }

    private static void addColumns2Map(Class<?> entityType, HashMap<String, ColumnEntity> columnMap) throws Exception {
        if (Object.class.equals(entityType)) return;

        Field[] fields = entityType.getDeclaredFields();
        for (Field field : fields) {
            int modify = field.getModifiers();
            if (Modifier.isStatic(modify) || Modifier.isTransient(modify)) {
                continue;
            }

            Column columnAnn = field.getAnnotation(Column.class);
            if (columnAnn != null) {
                if (ColumnConverterFactory.isSupportColumnConverter(field.getType())) {
                    ColumnEntity column = new ColumnEntity(entityType, field, columnAnn);
                    if (!columnMap.containsKey(column.getName())) {
                        columnMap.put(column.getName(), column);
                    }
                }
            }
        }

        addColumns2Map(entityType.getSuperclass(), columnMap);
    }
}
