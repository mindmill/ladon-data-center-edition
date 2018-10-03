package de.mc.ladon.server.persistence.cassandra.entities.impl;


import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import de.mc.ladon.server.core.api.persistence.entities.DataChunk;

import java.nio.ByteBuffer;

/**
 * Database chunk with cassandra annotations
 * Created by Ralf Ulrich on 05.05.16.
 */
@Table(keyspace = "LADON", name = "CHUNKS", readConsistency = "ONE", writeConsistency = "LOCAL_QUORUM")
public class DbChunk implements DataChunk {
    @PartitionKey
    private String chunkId;
    @ClusteringColumn
    private String ref;
    private ByteBuffer content;

    public DbChunk() {
    }

    public DbChunk(String chunkId, String ref, ByteBuffer content) {
        this.chunkId = chunkId;
        this.ref = ref;
        this.content = content;
    }

    @Override
    public String getChunkId() {
        return chunkId;
    }

    @Override
    public void setChunkId(String chunkId) {
        this.chunkId = chunkId;
    }

    @Override
    public String getRef() {
        return ref;
    }

    @Override
    public void setRef(String ref) {
        this.ref = ref;
    }

    @Override
    public ByteBuffer getContent() {
        return content;
    }

    @Override
    public void setContent(ByteBuffer content) {
        this.content = content;
    }
}
