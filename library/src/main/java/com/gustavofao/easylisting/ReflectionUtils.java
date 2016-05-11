package com.gustavofao.easylisting;

import com.gustavofao.easylisting.Annotations.FieldValue;
import com.gustavofao.easylisting.Annotations.InnerFieldValue;
import com.gustavofao.easylisting.Annotations.InnerValues;
import com.gustavofao.easylisting.Annotations.RowIdentifier;
import com.gustavofao.easylisting.Annotations.RowView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectionUtils {

    public static Field getRowIdentifierField(Object obj) {
        return getRowIdentifierField(obj.getClass());
    }

    public static Field getRowIdentifierField(Class<?> cls) {
        List<Field> fieldList = Arrays.asList(cls.getDeclaredFields());

        for (Field f : fieldList) {
            if (f.isAnnotationPresent(RowIdentifier.class)) {
                return f;
            }
        }

        return null;
    }

    /**
     * Get the layout resource id for a object
     * @param obj the object you want to get the layout id
     * @return the id of the res. Return the value for android.R.layout.simple_list_item_1 if has no annotation
     */
    public static int getLayoutIdForObject(Object obj) {
        int res = android.R.layout.simple_list_item_1;

        try {
            if (obj.getClass().isAnnotationPresent(RowView.class)) {
                res = obj.getClass().getAnnotation(RowView.class).value();
            }
        } catch (Exception ex) {}
        return res;
    }

    public static List<Field> getFieldsForRow(Object obj) {
        List<Field> allFields = Arrays.asList(obj.getClass().getDeclaredFields());
        List<Field> valueField = new ArrayList<>();

        for (Field f : allFields) {
            if (f.isAnnotationPresent(FieldValue.class))
                valueField.add(f);
        }

        return valueField;
    }

    public static List<Field> getInnerFieldsForRow(Object obj) {
        List<Field> allFields = Arrays.asList(obj.getClass().getDeclaredFields());
        List<Field> valueField = new ArrayList<>();

        for (Field f : allFields) {
            if (f.isAnnotationPresent(InnerValues.class))
                valueField.add(f);
        }

        return valueField;
    }

    public static List<Field> getInnerFieldsForRow(Object obj, Class parent) {
        List<Field> allFields = Arrays.asList(obj.getClass().getDeclaredFields());
        List<Field> valueField = new ArrayList<>();

        for (Field f : allFields) {
            if (f.isAnnotationPresent(InnerFieldValue.class)) {
                if (f.getAnnotation(InnerFieldValue.class).parent() == parent)
                    valueField.add(f);
            }
        }

        return valueField;
    }


}
