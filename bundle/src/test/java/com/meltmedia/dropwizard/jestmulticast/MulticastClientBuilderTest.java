package com.meltmedia.dropwizard.jestmulticast;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

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
        clientConfiguration1.setServers(Arrays.asList("http://localhost:9200", "http://localhost:9201"));
        clientConfiguration1.setCritical(isCritical);

        return clientConfiguration1;
    }

    @After
    public void shutdownClient() {
        Optional.ofNullable(client)
          .ifPresent(MulticastClient::shutdownClient);
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

        client = new MulticastClient.Builder()
                .withConfigurations(clientConfigurations)
                .build();

        assertThat(client.getCriticalClients(), hasSize(1));
        assertThat(client.getNonCriticalClients(), is(empty()));
    }

    @Test
    public void nonCriticalClientBuilderTest() {
        List<MulticastConfiguration> clientConfigurations = new LinkedList<>();

        MulticastConfiguration clientConfiguration = generateClientConfiguration(false);
        clientConfigurations.add(clientConfiguration);

        client = new MulticastClient.Builder()
                .withConfigurations(clientConfigurations)
                .build();

        assertThat(client.getNonCriticalClients(), hasSize(1));
        assertThat(client.getCriticalClients(), is(empty()));
    }

    @Test
    public void bothClientTypeBuilderTest() {
        List<MulticastConfiguration> clientConfigurations = new LinkedList<>();

        MulticastConfiguration nonCriticalClientConfiguration = generateClientConfiguration(false);
        MulticastConfiguration criticalClientConfiguration = generateClientConfiguration(true);

        clientConfigurations.add(nonCriticalClientConfiguration);
        clientConfigurations.add(criticalClientConfiguration);

        client = new MulticastClient.Builder()
                .withConfigurations(clientConfigurations)
                .build();

        assertThat(client.getNonCriticalClients(), hasSize(1));
        assertThat(client.getCriticalClients(), hasSize(1));
    }
    
    @Test
    public void bothAwsAndCredentialsThrowsException() {
      List<MulticastConfiguration> configs = Lists.newArrayList(
        new MulticastConfiguration()
          .withClusterName("cluster1")
          .withServers("http://localhost:9200", "http://localhost:9201")
          .withAws(new AwsConfiguration())
          .withCredentials(new Credentials())
        );
      
      try {
        client = new MulticastClient.Builder()
          .withConfigurations(configs)
          .build();
        Assert.fail("client builder accepted.");
      } catch( IllegalArgumentException e) {
          assertThat(e.getMessage(), Matchers.containsString("cluster1"));
      } catch( Throwable t ) {
        Assert.fail("wrong exception thrown");
      }
        
    }

}
