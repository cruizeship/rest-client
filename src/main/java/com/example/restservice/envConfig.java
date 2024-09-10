package com.example.restservice;
public class envConfig {
    private static final String DB_NAME = System.getenv("DB_NAME");
    public static String getDB_NAME(){
        return DB_NAME;
    }
}
