package com.meltmedia.dropwizard.jestmulticast;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import io.searchbox.action.Action;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.JestResultHandler;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.cluster.Health;
import io.searchbox.core.Delete;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by qthibeault on 8/25/16.
 */

public class MulticastClient implements JestClient {

    private List<JestClient> criticalClients;
    private List<JestClient> nonCriticalClients;

    /* Make constructor private because we are following the builder pattern */
    private MulticastClient() {
        this.criticalClients = new ArrayList<>();
        this.nonCriticalClients = new ArrayList<>();
    };

    private static List<Class<?>> multiCastTypes = Lists.newArrayList(Index.class);
    private static List<Class<?>> unicastTypes = Lists.newArrayList(Search.class);

    @Override
    public <T extends JestResult> T execute(Action<T> action) throws IOException, UnsupportedOperationException {

        Stream<JestClient> clients;

        if( action.getClass().isAssignableFrom(Search.class) ) {
            clients = criticalClients.subList(0,1).stream();
        }
        else if( action.getClass().isAssignableFrom(Index.class) || action.getClass().isAssignableFrom(Delete.class) ) {
            clients = Stream.concat(criticalClients.stream(), nonCriticalClients.stream());
        }
        else{
            throw new UnsupportedOperationException("This operation is not implemented at this time.");
        }

        return clients.map((JestClient client) -> {

            try {
                return client.execute(action);
            }
            catch(IOException e) {

                if( criticalClients.contains(client) ) {
                    throw new UncheckedIOException(e);
                }
                else{
                    e.printStackTrace();
                    return null;
                }
            }
        })
        .collect(Collectors.toList())
                .get(0);
    }

    @FunctionalInterface
    public interface multiExecuteConsumer<T, K> {
        public void accept(T t, K k) throws Exception;
    }

    public void multiExecute(multiExecuteConsumer<JestClient, Boolean> clientConsumer) throws Exception {
        List<JestClient> clients = Stream.concat(criticalClients.stream(), nonCriticalClients.stream()).collect(Collectors.toList());
        for(JestClient client : clients) {
            clientConsumer.accept(client, criticalClients.contains(client));
        }
    }


    @Override
    public <T extends JestResult> void executeAsync(Action<T> action, JestResultHandler<? super T> jestResultHandler) {
        Stream<JestClient> clients = Stream.concat(criticalClients.stream(), nonCriticalClients.stream());
        clients.forEach((JestClient client) -> {
            client.executeAsync(action, jestResultHandler);
        });
    }

    @Override
    public void shutdownClient() {
        Stream<JestClient> clients = Stream.concat(criticalClients.stream(), nonCriticalClients.stream());
        clients.forEach(JestClient::shutdownClient);
    }

    @Override
    public void setServers(Set<String> set) {

    }

    public List<JestResult> checkHealth(Health health) throws IOException {
        Stream<JestClient> clients = Stream.concat(criticalClients.stream(), nonCriticalClients.stream());
        return clients.map((JestClient client) -> {
            try {
                return client.execute(health);
            }
            catch(IOException e) {
                throw new UncheckedIOException(e);
            }
        })
        .collect(Collectors.toList());
    }

    public static class Builder {

        private MulticastClient client;

        public Builder() {
            this.client = new MulticastClient();
        }

        public MulticastClient build() {
            return this.client;
        }

        public Builder withConfigurations(Collection<MulticastConfiguration> configurations) {

            configurations.forEach((MulticastConfiguration configuration) -> {

                configuration.getServers().forEach((String url) -> {

                    JestClientFactory clientFactory = new JestClientFactory();
                    HttpClientConfig clientConfig = new HttpClientConfig.Builder(url)
                            .connTimeout(configuration.getConnectionTimeout())
                            .readTimeout(configuration.getReadTimeout())
                            .maxTotalConnection(configuration.getMaxTotalConnections())
                            .defaultMaxTotalConnectionPerRoute(configuration.getMaxTotalConnectionsPerRoute())
                            .multiThreaded(true)
                            .build();

                    clientFactory.setHttpClientConfig(clientConfig);

                    if (configuration.isCritical()) {
                        client.criticalClients.add(clientFactory.getObject());
                    }
                    else {
                        client.nonCriticalClients.add(clientFactory.getObject());
                    }

                });
            });

            return this;
        }
    }

    public List<JestClient> getCriticalClients() {
        return this.criticalClients;
    }

    public List<JestClient> getNonCriticalClients() {
        return this.nonCriticalClients;
    }

}
