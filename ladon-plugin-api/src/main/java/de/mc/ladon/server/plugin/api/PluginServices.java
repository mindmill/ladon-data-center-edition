package de.mc.ladon.server.plugin.api;

import de.mc.ladon.server.core.api.persistence.Database;
import de.mc.ladon.server.core.api.persistence.dao.*;

public interface PluginServices {
    RepositoryDAO getRepositoryDao();

    MetadataDAO getMetadataDAO();

    BinaryDataDAO getBinaryDataDAO();

    ChangeTokenDAO getChangeTokenDAO();

    ChunkDAO getChunkDAO();

    UserRoleDAO getUserRoleDAO();

    Database getDatabase();
}
