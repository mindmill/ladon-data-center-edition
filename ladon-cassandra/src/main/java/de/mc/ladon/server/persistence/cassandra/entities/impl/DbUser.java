package de.mc.ladon.server.persistence.cassandra.entities.impl;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import de.mc.ladon.server.core.api.persistence.entities.User;

import java.util.HashSet;
import java.util.Set;

/**
 * User object with cassandra annotations
 * Created by Ralf Ulrich on 05.05.16.
 */
@Table(keyspace = "LADON", name = "USERS", readConsistency = "ONE", writeConsistency = "LOCAL_QUORUM")
public class DbUser implements User {

    @PartitionKey
    private String name;
    private String password;
    private Boolean enabled = false;
    private Set<String> roles = new HashSet<>(2);

    public DbUser() {
    }

    public DbUser(String username) {
        this.name = username;
    }

    public DbUser(String username, String password, Boolean enabled, Set<String> roles) {
        this.name = username;
        this.password = password;
        this.enabled = enabled;
        this.roles = roles;
    }

//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return roles.stream().map(r -> (GrantedAuthority) () -> "ROLE_" + r).collect(Collectors.toList());
//    }

    @Override
    public String toString() {
        return "DbUser{" +
                "username='" + name + '\'' +
                ", password='" + password + '\'' +
                ", enabled=" + enabled +
                ", roles=" + roles +
                '}';
    }


    @Override
    public String getName() {
        return name;
    }

    public void setName(String username) {
        this.name = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
