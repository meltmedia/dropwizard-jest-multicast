package com.qthibeau.dropwizard.jest;

import java.util.Collection;

/**
 * Created by qthibeault on 8/25/16.
 */

public class MulticastManager {
    private MulticastClient client;

    public MulticastManager(Collection<MulticastConfiguration> configurations) {
        this.client = new MulticastClient.Builder()
                .withConfigurations(configurations)
                .build();
    }

}
