package de.mc.ladon.server.persistence.cassandra.entities.impl;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import de.mc.ladon.server.core.api.persistence.entities.Role;

/**
 * Role object with cassandra annotations
 * Created by Ralf Ulrich on 05.05.16.
 */
@Table(keyspace = "LADON", name = "ROLES", readConsistency = "ONE", writeConsistency = "LOCAL_QUORUM")
public class DbRole implements Role {
    @PartitionKey
    private String roleid;
    private String member;

    public DbRole() {
    }

    public DbRole(String roleid, String member) {
        this.roleid = roleid;
        this.member = member;
    }

    @Override
    public String getRoleid() {
        return roleid;
    }

    @Override
    public void setRoleid(String roleid) {
        this.roleid = roleid;
    }

    @Override
    public String getMember() {
        return member;
    }

    @Override
    public void setMember(String member) {
        this.member = member;
    }
}
