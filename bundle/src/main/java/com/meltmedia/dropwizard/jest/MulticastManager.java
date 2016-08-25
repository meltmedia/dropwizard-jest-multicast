package com.meltmedia.dropwizard.jest;

import io.dropwizard.lifecycle.Managed;

import java.util.Collection;

/**
 * Created by qthibeault on 8/25/16.
 */

public class MulticastManager implements Managed {
    private Collection<MulticastConfiguration> configurations;
    private MulticastClient client;

    public MulticastManager(Collection<MulticastConfiguration> configurations) {
        this.configurations = configurations;
    }

    public MulticastClient getClient() {
        return this.client;
    }

    @Override
    public void start() throws Exception {
        this.client = new MulticastClient.Builder()
                .withConfigurations(configurations)
                .build();
    }

    @Override
    public void stop() throws Exception {
        this.client.shutdownClient();
    }
}
