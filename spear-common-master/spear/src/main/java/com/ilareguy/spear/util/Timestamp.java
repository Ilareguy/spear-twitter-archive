package com.ilareguy.spear.util;

public abstract class Timestamp {

    /**
     * @return The amount of seconds elapsed since January 1, 1970, 00:00:00 GMT, formatted
     * as a string.
     */
    public static long now(){
        return (new java.sql.Timestamp(System.currentTimeMillis()).getTime() / 1000);
    }

}
