package de.mc.ladon.server.persistence.cassandra.entities.impl;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import de.mc.ladon.server.core.persistence.entities.api.Content;

/**
 * Database content with cassandra annotations
 * Created by Ralf Ulrich on 05.05.16.
 */
@Table(keyspace = "LADON", name = "CONTENT", readConsistency = "ONE", writeConsistency = "LOCAL_QUORUM")
public class DbContent implements Content {
    @PartitionKey
    private String repoId;
    @PartitionKey(1)
    private String streamId;
    @ClusteringColumn
    private Long count;
    private String chunkId;


    public DbContent() {
    }

    public DbContent(String repoId, String streamId, Long count, String chunkId) {
        this.repoId = repoId;
        this.streamId = streamId;
        this.count = count;
        this.chunkId = chunkId;
    }

    @Override
    public String getRepoId() {
        return repoId;
    }

    @Override
    public void setRepoId(String repoId) {
        this.repoId = repoId;
    }

    @Override
    public String getStreamId() {
        return streamId;
    }

    @Override
    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    @Override
    public Long getCount() {
        return count;
    }

    @Override
    public void setCount(Long count) {
        this.count = count;
    }

    @Override
    public String getChunkId() {
        return chunkId;
    }

    @Override
    public void setChunkId(String chunkId) {
        this.chunkId = chunkId;
    }
}
