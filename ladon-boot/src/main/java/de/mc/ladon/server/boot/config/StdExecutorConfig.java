package de.mc.ladon.server.boot.config;

import de.mc.ladon.server.core.api.executor.LadonExecutorConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * LadonExecutorConfig
 * Created by Ralf Ulrich on 12.02.16.
 */
@Configuration
@ConfigurationProperties(prefix = "ladon.executors")
public class StdExecutorConfig implements LadonExecutorConfig{


    private int webthreads = 4;

    @Override
    public int getWebthreads() {
        return webthreads;
    }

    public void setWebthreads(int webthreads) {
        this.webthreads = webthreads;
    }
}

