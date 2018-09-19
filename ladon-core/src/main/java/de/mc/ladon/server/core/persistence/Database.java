/*
 * Copyright (c) 2015 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.core.persistence;

/**
 * Database for Ladon
 * Created by Ralf Ulrich on 17.12.14.
 */
public interface Database {

    void init();

    void initSchema();

    boolean isInitialized();

    void setDatabaseConfig(DatabaseConfig config);

    void runScript(String script);

    void close();

}
