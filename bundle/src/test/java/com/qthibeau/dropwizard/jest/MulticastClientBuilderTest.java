package com.qthibeau.dropwizard.jest;

import io.searchbox.client.JestClient;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * Created by qthibeault on 8/25/16.
 */

public class MulticastClientBuilderTest {

    private MulticastClient client;

    private MulticastConfiguration generateClientConfiguration(Boolean isCritical) {

        MulticastConfiguration clientConfiguration1 = new MulticastConfiguration();
        clientConfiguration1.setClusterName("elasticsearch");
        clientConfiguration1.setDatabaseUrls(Arrays.asList("http://localhost:9200", "http://localhost:9201"));
        clientConfiguration1.setCritical(true);

        return clientConfiguration1;
    }

    @Test
    public void emptyBuilderTest() {
        client = new MulticastClient.Builder()
                .build();

        assertThat(client.getCriticalClients(), is(empty()));
        assertThat(client.getNonCriticalClients(), is(empty()));
    }

    @Test
    public void criticalClientBuilderTest() {
        List<MulticastConfiguration> clientConfigurations = new LinkedList<>();

        MulticastConfiguration clientConfiguration = generateClientConfiguration(true);
        clientConfigurations.add(clientConfiguration);

        MulticastClient client = new MulticastClient.Builder()
                .withConfigurations(clientConfigurations)
                .build();

        assertThat(client.getCriticalClients(), hasSize(2));
        assertThat(client.getNonCriticalClients(), is(empty()));
    }

    @Test
    public void nonCriticalClientBuilderTest() {
        List<MulticastConfiguration> clientConfigurations = new LinkedList<>();

        MulticastConfiguration clientConfiguration = generateClientConfiguration(false);
        clientConfigurations.add(clientConfiguration);

        MulticastClient client = new MulticastClient.Builder()
                .withConfigurations(clientConfigurations)
                .build();

        assertThat(client.getNonCriticalClients(), hasSize(2));
        assertThat(client.getCriticalClients(), is(empty()));
    }

    @Test
    public void bothClientTypeBuilderTest() {
        List<MulticastConfiguration> clientConfigurations = new LinkedList<>();

        MulticastConfiguration nonCriticalClientConfiguration = generateClientConfiguration(false);
        MulticastConfiguration criticalClientConfiguration = generateClientConfiguration(true);

        clientConfigurations.add(nonCriticalClientConfiguration);
        clientConfigurations.add(criticalClientConfiguration);

        MulticastClient client = new MulticastClient.Builder()
                .withConfigurations(clientConfigurations)
                .build();

        assertThat(client.getNonCriticalClients(), hasSize(2));
        assertThat(client.getCriticalClients(), hasSize(2));
    }

}
