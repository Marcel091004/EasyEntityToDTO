package org.example;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class DTOMapper<T> {

    public T maptoDTO(T object) {
        T finalObject;
        Class<?> clazz = object.getClass();

        try {
            finalObject = (T) clazz.getDeclaredConstructor().newInstance();

            finalObject = setDefaultValues(finalObject);

            for (Field variable : clazz.getDeclaredFields()) {
                boolean isExcludedFromDTO = variable.isAnnotationPresent(ExcludeFromDTOMapper.class);

                if (isExcludedFromDTO) {
                    continue;
                }

                try {
                    Object value = variable.get(object);

                    Field finalObjectField = clazz.getDeclaredField(variable.getName());
                    finalObjectField.setAccessible(true);

                    finalObjectField.set(finalObject, value);

                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to access this field: " + variable.getName(), e);
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException("Field: " + variable.getName() + " doesn't seem to exist but is called", e);
                }
            }
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException("Error creating new Object", e);
        }

        return finalObject;
    }


    private T setDefaultValues(T finalObject) {
        for (Field variable : finalObject.getClass().getDeclaredFields()) {
            boolean isExcludedFromDTO = variable.isAnnotationPresent(ExcludeFromDTOMapper.class);

            if (!isExcludedFromDTO) {
                continue;
            }

            variable.setAccessible(true);
            try {
                if (variable.getType().isPrimitive()) {
                    if (variable.getType() == int.class) {
                        variable.setInt(finalObject, 0);
                    } else if (variable.getType() == boolean.class) {
                        variable.setBoolean(finalObject, false);
                    } else if (variable.getType() == char.class) {
                        variable.setChar(finalObject, '\u0000');
                    } else if (variable.getType() == byte.class) {
                        variable.setByte(finalObject, (byte) 0);
                    } else if (variable.getType() == short.class) {
                        variable.setShort(finalObject, (short) 0);
                    } else if (variable.getType() == long.class) {
                        variable.setLong(finalObject, 0L);
                    } else if (variable.getType() == float.class) {
                        variable.setFloat(finalObject, 0.0f);
                    } else if (variable.getType() == double.class) {
                        variable.setDouble(finalObject, 0.0d);
                    }
                } else {
                    // For reference types, set to null
                    variable.set(finalObject, null);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Error accessing field: " + variable.getName(), e);
            }
        }
        return finalObject;
    }


}