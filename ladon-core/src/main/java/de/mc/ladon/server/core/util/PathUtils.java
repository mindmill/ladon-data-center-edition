package de.mc.ladon.server.core.util;

import java.io.File;

public class PathUtils {
    private PathUtils() {
    }

    public static String getLadonHome() {
        String home = System.getProperty("ladon.home");
        String userDir = System.getProperty("user.home");
        return home == null ? userDir + File.separator + "ladon_data" : home;
    }

    public static String getSystemDir() {
        return getLadonHome() + File.separator + "system";
    }

}
