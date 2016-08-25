package com.meltmedia.dropwizard.jestmulticast;

import io.searchbox.action.Action;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.JestResultHandler;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Delete;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
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

    @Override
    public <T extends JestResult> T execute(Action<T> action) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("This operation is not implemented at this time.");
    }

    private SearchResult execute(Search search) throws IOException {
        JestClient client;

        if( !this.criticalClients.isEmpty() ) {
            client = criticalClients.get(0);
        }
        else {
            if( !this.nonCriticalClients.isEmpty() ) {
                client = nonCriticalClients.get(0);
            }
            else {
                throw new IllegalStateException("Cannot perform search without being provided at least one client");
            }
        }

        return client.execute(search);
    }

    private JestResult execute(Index index) throws IOException {

        Stream<JestResult> criticalResults = this.criticalClients.stream().map((JestClient client) -> {
            try {
                return client.execute(index);
            }
            catch(IOException e) {
                throw new UncheckedIOException(e);
            }
        });

        Stream<JestResult> nonCriticalResults = this.nonCriticalClients.stream().map((JestClient client) -> {
            try {
                return client.execute(index);
            }
            catch(IOException e) {
                e.printStackTrace();
                return null;
            }
        });


        return Stream.concat(criticalResults, nonCriticalResults).collect(Collectors.toList()).get(0);

    }

    private JestResult execute(Delete delete) throws IOException {

        Stream<JestResult> criticalResults = this.criticalClients.stream().map((JestClient client) -> {
            try {
                return client.execute(delete);
            }
            catch(IOException e) {
                throw new UncheckedIOException(e);
            }
        });

        Stream<JestResult> nonCriticalResults = this.nonCriticalClients.stream().map((JestClient client) -> {
            try {
                return client.execute(delete);
            }
            catch(IOException e) {
                e.printStackTrace();
                return null;
            }
        });


        return Stream.concat(criticalResults, nonCriticalResults).collect(Collectors.toList()).get(0);

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

                configuration.getDatabaseUrls().forEach((String url) -> {

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
