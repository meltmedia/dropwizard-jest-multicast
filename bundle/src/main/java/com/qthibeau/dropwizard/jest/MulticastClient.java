package com.qthibeau.dropwizard.jest;

import io.searchbox.action.Action;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.JestResultHandler;
import io.searchbox.client.config.HttpClientConfig;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by qthibeault on 8/25/16.
 */
public class MulticastClient implements JestClient {

    private Set<JestClient> criticalClients;
    private Set<JestClient> nonCriticalClients;

    /* Make constructor private because we are following the builder pattern */
    private MulticastClient() {};

    @Override
    public <T extends JestResult> T execute(Action<T> action) throws IOException {
        Stream<JestClient> clients = Stream.concat(criticalClients.stream(), nonCriticalClients.stream());

        return clients.map((JestClient client) -> {

            try {

                return client.execute(action);
            }
            catch(IOException e) {

                if(criticalClients.contains(client)) {
                    throw new UncheckedIOException(e);
                }
                else {
                    e.printStackTrace();
                    return null;
                }
            }
        })
        .collect(Collectors.toList())
        .get(0);
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

}
