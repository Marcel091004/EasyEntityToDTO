package org.example;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class DTOMapper<T> {

    public T maptoDTO(T object) {
        T finalObject = null;
        Class<?> clazz = object.getClass();

        try {
            // Create a new instance of the target class (finalObject)
            finalObject = (T) clazz.getDeclaredConstructor().newInstance();

            // First, set all fields of finalObject to their default values (null for reference types, default values for primitives)
            for (Field field : finalObject.getClass().getDeclaredFields()) {
                field.setAccessible(true); // Make field accessible

                try {
                    if (field.getType().isPrimitive()) {
                        // For primitive types, set to their default values
                        if (field.getType() == int.class) {
                            field.setInt(finalObject, 0); // Default value for int
                        } else if (field.getType() == boolean.class) {
                            field.setBoolean(finalObject, false); // Default value for boolean
                        } else if (field.getType() == char.class) {
                            field.setChar(finalObject, '\u0000'); // Default value for char
                        } else if (field.getType() == byte.class) {
                            field.setByte(finalObject, (byte) 0); // Default value for byte
                        } else if (field.getType() == short.class) {
                            field.setShort(finalObject, (short) 0); // Default value for short
                        } else if (field.getType() == long.class) {
                            field.setLong(finalObject, 0L); // Default value for long
                        } else if (field.getType() == float.class) {
                            field.setFloat(finalObject, 0.0f); // Default value for float
                        } else if (field.getType() == double.class) {
                            field.setDouble(finalObject, 0.0d); // Default value for double
                        }
                    } else {
                        // For reference types, set to null
                        field.set(finalObject, null);
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Error accessing field: " + field.getName(), e);
                }
            }

            // Now, map fields from the original object to the finalObject
            for (Field variable : clazz.getDeclaredFields()) {
                boolean isExcludedFromDTO = variable.isAnnotationPresent(ExcludeFromDTOMapper.class);

                if (isExcludedFromDTO) {
                    continue; // Skip fields marked with @ExcludeFromDTOMapper
                }

                try {
                    // Get the value of the field from the original object
                    Object value = variable.get(object);

                    // Get the field in the new object (finalObject)
                    Field finalObjectField = clazz.getDeclaredField(variable.getName());
                    finalObjectField.setAccessible(true); // Make field accessible

                    // Set the field value in finalObject
                    finalObjectField.set(finalObject, value);

                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to access this field: " + variable.getName(), e);
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException("Field: " + variable.getName() + " doesn't seem to exist but is called", e);
                }
            }
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Error creating new Object", e);
        }

        return finalObject;
    }
}