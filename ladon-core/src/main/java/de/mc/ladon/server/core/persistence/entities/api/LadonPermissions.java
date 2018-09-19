/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.core.persistence.entities.api;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * LadonPermissions
 * Created by Ralf Ulrich on 10.01.16.
 */
public class LadonPermissions {

    /**
     * CMIS read permission {@code cmis:read}.
     */
    public static final String READ = "ladon:read";
    /**
     * CMIS write permission {@code cmis:write}.
     */
    public static final String WRITE = "ladon:write";
    /**
     * CMIS read permission for acl {@code cmis:read_acp}.
     */
    public static final String READ_ACP = "ladon:read_acp";
    /**
     * CMIS write permission for acl {@code cmis:write_acp}.
     */
    public static final String WRITE_ACP = "ladon:write_acp";
    /**
     * CMIS all permission {@code cmis:all}.
     */
    public static final String ALL = "ladon:all";


    public static final List<String> BASIC_PERMISSIONS = Lists.newArrayList( READ,WRITE,ALL);

    public static final List<String> PERMISSIONS_LIST = Lists.newArrayList(
            READ, WRITE, READ_ACP, WRITE_ACP, ALL
    );
}
