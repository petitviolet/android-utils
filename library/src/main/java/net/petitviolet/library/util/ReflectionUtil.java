package net.petitviolet.library.util;

import java.lang.reflect.Field;

public class ReflectionUtil {

    private static Field extractField(Object target, String name) {
        for (Field field : target.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (name.equals(field.getName())) {
                return field;
            }
        }
        return null;
    }

    public static Object extractMember(Object target, String name) throws IllegalAccessException {
        Field field = extractField(target, name);
        return field == null ? null : field.get(target);
    }

    public static void setPrivateMember(Object target, String name, Object newValue) throws IllegalAccessException {
        Field field = extractField(target, name);
        if (field == null) {
            return;
        }
        Object targetField = field.get(target);
        field.set(targetField, newValue);
    }
}