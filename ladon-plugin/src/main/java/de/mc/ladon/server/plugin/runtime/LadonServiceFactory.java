package de.mc.ladon.server.plugin.runtime;

import de.mc.ladon.server.core.api.persistence.Database;
import de.mc.ladon.server.core.api.persistence.dao.*;
import de.mc.ladon.server.plugin.api.PluginContext;
import de.mc.ladon.server.plugin.api.PluginServices;
import org.osgi.service.component.annotations.Component;

import javax.inject.Inject;
import javax.inject.Named;


@Named
@Component
public class LadonServiceFactory implements PluginServices {


    private static PluginContext context;

    @Inject
    public LadonServiceFactory(PluginContext ctx) {
        context = ctx;
    }


    @Override
    public RepositoryDAO getRepositoryDao() {
        return context.getRepositoryDAO();
    }

    @Override
    public MetadataDAO getMetadataDAO() {
        return context.getMetadataDAO();
    }

    @Override
    public BinaryDataDAO getBinaryDataDAO() {
        return context.getBinaryDataDAO();
    }

    @Override
    public ChangeTokenDAO getChangeTokenDAO() {
        return context.getChangeTokenDAO();
    }

    @Override
    public ChunkDAO getChunkDAO() {
        return context.getChunkDAO();
    }

    @Override
    public UserRoleDAO getUserRoleDAO() {
        return context.getUserRoleDAO();
    }

    @Override
    public Database getDatabase() {
        return context.getDatabase();
    }

}
