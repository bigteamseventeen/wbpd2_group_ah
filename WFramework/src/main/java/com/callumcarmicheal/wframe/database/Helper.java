package com.callumcarmicheal.wframe.database;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class Helper {
    public static void BindArrayToPreparedStatement(PreparedStatement pstmt, Object[] values) throws SQLException {
        for (int x = 1; x <= values.length; x++) {
            Object o = values[x - 1];

            if (o instanceof String) {
                pstmt.setString(x, (String) o);
            } else if (o instanceof Integer) {
                pstmt.setInt(x, (Integer) o);
            } else if (o instanceof Boolean) {
                pstmt.setBoolean(x, (Boolean) o);
            } else if (o instanceof Float) {
                pstmt.setFloat(x, (Float) o);
            }

            // Todo: Add more value types to BindArrayToPreparedStatement
            else {
                // Just set it as a object
                pstmt.setObject(x, o);
            }
        }
    }

    public static <T> T[] ListToGenericArray(List<T> list) {
        Class clazz = list.get(0).getClass(); // check for size and null before
        T[] array = (T[]) java.lang.reflect.Array.newInstance(clazz, list.size());
        return list.toArray(array);
    }

    public enum SQLOrderType {
        ASC,
        DESC
    }

    // Constructor arguemnts
    private static Class[] instance_cArg = null;

    /**
     * Create a new model instance assuming the class has the constructor that passes in a connection
     * 
     * We are using reflection because we cannot create a instance of a generic class
     */
    public static <T> DatabaseModel<T> NewModelInstance(DatabaseModel<T> modelClass) {
        // If we have already not created the class constructor definition 
        if (instance_cArg == null) {
            instance_cArg = new Class[1];
            instance_cArg[0] = Connection.class;  
        }
        
        try {
            // Attempt to create a new instance of the modelClass 
            return modelClass.getClass().getDeclaredConstructor(instance_cArg).newInstance(modelClass.getConnection());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        
        return null;
    }
}