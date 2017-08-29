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
import vc.inreach.aws.request.*;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by qthibeault on 8/25/16.
 */

public class MulticastClient implements JestClient {
  private static final Logger log = LoggerFactory.getLogger(MulticastClient.class);
  
  public static class StaticCredentialsProvider implements AWSCredentialsProvider {
    public StaticCredentialsProvider( AWSCredentials credentials ) {
      this.credentials = credentials;
    }

    private AWSCredentials credentials;

    @Override
    public AWSCredentials getCredentials() {
      return credentials;
    }

    @Override
    public void refresh() {}
  }

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

        // determine if the method requires operations across all clusters
        if( action.getClass().isAssignableFrom(Index.class) || action.getClass().isAssignableFrom(Delete.class) ) {
            clients = Stream.concat(criticalClients.stream(), nonCriticalClients.stream());
        }
        // otherwise only use a single client. Prefer the critical clients first if one exists.
        else{
            clients = (criticalClients.size() > 0) ? criticalClients.subList(0,1).stream() : nonCriticalClients.subList(0,1).stream();
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
      throw new UnsupportedOperationException("Multicast clients cannot have their server list changed.");
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
                Optional<Credentials> credentials = Optional.ofNullable(configuration.getCredentials());
                Optional<AwsConfiguration> aws = Optional.ofNullable(configuration.getAws());
                
                if( credentials.isPresent() && aws.isPresent() ) {
                  throw new IllegalArgumentException(configuration.getClusterName()+" has both aws credentials and basic credentials defined.");
                }
                
                  log.info("creating client for {}", configuration.getClusterName());
                  JestClientFactory clientFactory = aws
                    .map(awsConfig->{
                      StaticCredentialsProvider provider = new StaticCredentialsProvider(new BasicAWSCredentials(awsConfig.getAccessKey(), awsConfig.getSecretKey()));
                      String region = regionFromConfiguration(configuration);
                      AWSSigner awsSigner = new AWSSigner(provider, region, "es", () -> LocalDateTime.now(ZoneOffset.UTC));
                      AWSSigningRequestInterceptor requestInterceptor = new AWSSigningRequestInterceptor(awsSigner);
                      return (JestClientFactory)new JestClientFactory() {
                        @Override
                        protected HttpClientBuilder configureHttpClient( HttpClientBuilder builder ) {
                          return super.configureHttpClient(builder)
                            .addInterceptorLast(requestInterceptor);
                        }

                        @Override
                        protected HttpAsyncClientBuilder configureHttpClient( HttpAsyncClientBuilder builder ) {
                          return super.configureHttpClient(builder)
                            .addInterceptorLast(requestInterceptor);
                        }
                      };
                      
                    }).orElseGet(JestClientFactory::new);

                    HttpClientConfig.Builder clientConfigBuilder = new HttpClientConfig.Builder(configuration.getServers())
                            .connTimeout(configuration.getConnectionTimeout())
                            .readTimeout(configuration.getReadTimeout())
                            .maxTotalConnection(configuration.getMaxTotalConnections())
                            .defaultMaxTotalConnectionPerRoute(configuration.getMaxTotalConnectionsPerRoute())
                            .multiThreaded(true);
                    
                      credentials.ifPresent(c->{
                        clientConfigBuilder.defaultCredentials(c.getUsername(), c.getPassword());
                      });

                      HttpClientConfig clientConfig = clientConfigBuilder.build();

                    clientFactory.setHttpClientConfig(clientConfig);

                    if (configuration.isCritical()) {
                        client.criticalClients.add(clientFactory.getObject());
                    }
                    else {
                        client.nonCriticalClients.add(clientFactory.getObject());
                    }

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
    
    static String regionFromUrl(String url) {
      String region = url.replaceFirst(".*\\.([^\\.]+)\\.es\\.amazonaws\\.com\\Z", "$1");
      if( url.equals(region) ) {
        throw new IllegalArgumentException("could not extract region from url "+url);
      }
      return region;
    }
    
    static String regionFromConfiguration( MulticastConfiguration configuration ) {
      return configuration.getServers()
      .stream()
      .map(MulticastClient::regionFromUrl)
      .distinct()
      .collect(
        collectingAndThen(
          toList(),
          regions->{
            if( regions.isEmpty() ) {
              throw new IllegalStateException(configuration.getClusterName()+" could not identify a region.");
            }
            else if( regions.size() > 1 ) {
              throw new IllegalStateException(configuration.getClusterName()+" services mapped to multiple regions");
            }
            else {
              return regions.get(1);
            }
          }));
    }
}
