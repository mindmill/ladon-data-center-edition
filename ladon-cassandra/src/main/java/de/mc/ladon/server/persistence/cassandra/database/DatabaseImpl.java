/*
 * Copyright (c) 2015 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.persistence.cassandra.database;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.DowngradingConsistencyRetryPolicy;
import com.google.common.io.Files;
import de.mc.ladon.server.core.api.persistence.Database;
import de.mc.ladon.server.core.api.persistence.DatabaseConfig;
import org.apache.cassandra.exceptions.CassandraException;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ralf on 17.12.14.
 */
@Named
public class DatabaseImpl implements Database {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseImpl.class);
    protected Cluster cluster;
    protected Session session;


    private DatabaseConfig config;

    @Inject
    @Override
    public void setDatabaseConfig(DatabaseConfig config) {
        this.config = config;

    }

    @Override
    public boolean isInitialized() {
        return session != null;
    }

    private boolean ladonKeyspaceExists() {
        return cluster.getMetadata().getKeyspace("ladon") != null;
    }

    private void initSession() {
        if(session != null ){
            LOGGER.info("Session is already initialized, skipping");
            return;
        }
        Cluster.Builder cb = Cluster.builder().addContactPoints(config.getNodes().toArray(new String[0]))
                .withRetryPolicy(DowngradingConsistencyRetryPolicy.INSTANCE)
                .withCompression(ProtocolOptions.Compression.SNAPPY)
                //.withReconnectionPolicy(new ConstantReconnectionPolicy(5000))
                .withLoadBalancingPolicy(DCAwareRoundRobinPolicy.builder().withLocalDc(config.getDatacenter()).build());
        Integer port = config.getPort();
        if (port != null) {
            cb = cb.withPort(port);
        }
        String username = config.getUser();
        cb = cb.withCredentials(username, "cassandra");
        cluster = cb.build();
        Metadata metadata = cluster.getMetadata();
        LOGGER.info("Connected to cluster:  " + metadata.getClusterName());

        for (Host host : metadata.getAllHosts()) {
            LOGGER.info("Datacenter: " + host.getDatacenter() + " Host: " + host.getAddress() + " Rack: " + host.getRack());
        }

        session = cluster.connect();
    }


//    public void resetPW() {
//        try {
//            Thread.sleep(11000);
//            initSession("cassandra");
//            session.execute("ALTER USER cassandra WITH PASSWORD '" + config.getPassword() + "';");
//            LOGGER.info("Reset to default password done");
//            Thread.sleep(10000);
//            initSession(config.getPassword());
//            LOGGER.info("Successfully set new password");
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }

    public void updateReplication(int newValue) {
        session.execute("ALTER KEYSPACE ladon WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : " + newValue + " };");
        LOGGER.info("Update replication factor to " + newValue + " done");
    }


    public void init() {
        try {
            initSession();
        } catch (CassandraException e) {
            LOGGER.error("init failed", e);
        }
    }


    public Metadata getClusterMetadata() {
        return cluster.getMetadata();
    }


    private List<String> readScript(String name) {
        URL url = getClass().getResource(name);
        File script = new File(url.getFile());
        try {
            return Files.readLines(script, Charset.defaultCharset());
        } catch (IOException e) {
            LOGGER.error("the script " + name + " could not be found");
            return Collections.emptyList();
        }

    }

    private void executeScript(List<String> lines) {
        Arrays.stream(lines.stream()
                .filter((s) -> !s.contains("--"))
                .collect(Collectors.joining())
                .split(";")).forEach((s) -> {
            LOGGER.info(s);
            session.execute(s + ";");
        });
    }


    @Override
    public void runScript(String name) {
        executeScript(readScript(name));

    }


    public void initSchema() {
        boolean initialized = isInitialized() && ladonKeyspaceExists();
        if (!initialized) {
            LOGGER.warn("Schema ist nicht vorhanden, Wird nun erzeugt, hierzu sollten alle Ladon Nodes im Cluster verbunden sein.");
        } else {
            LOGGER.info("Schema is already initialized, skipping");
            return;
        }

        VelocityContext context = new VelocityContext();
        context.put("datacenterString", config.getReplicationfactor());


        Template template = null;

        try {
            java.util.Properties p = new java.util.Properties();
            p.setProperty("resource.loader", "class");
            p.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
            Velocity.init(p);
            template = Velocity.getTemplate("scripts/createSchema.cql");
            StringWriter sw = new StringWriter();
            template.merge(context, sw);
            List<String> lines = Arrays.asList(sw.toString().split("\\r?\\n"));
            executeScript(lines);
        } catch (Exception e) {
            LOGGER.error("error while initializing database", e);
        }

    }


    @PreDestroy
    public void close() {
        cluster.close();
    }

}
