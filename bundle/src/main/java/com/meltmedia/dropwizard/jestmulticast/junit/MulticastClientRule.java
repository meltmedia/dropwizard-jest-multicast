package com.meltmedia.dropwizard.jestmulticast.junit;

import com.google.common.collect.Lists;
import com.meltmedia.dropwizard.jestmulticast.MulticastClient;
import com.meltmedia.dropwizard.jestmulticast.MulticastConfiguration;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by qthibeault on 9/29/16.
 */

public class MulticastClientRule implements TestRule {
  
  public static class Builder {
    private List<MulticastConfiguration> configurations = Lists.newArrayList();
    public Builder addConfiguration( Consumer<MulticastConfiguration> config ) {
      MulticastConfiguration mc = new MulticastConfiguration();
      config.accept(mc);
      configurations.add(mc);
      return this;
    }
    
    public MulticastClientRule build() {
      return new MulticastClientRule(configurations);
    }
  }
  
  public static Builder builder() { return new Builder(); }

    private List<MulticastConfiguration> configurations;
    private MulticastClient client;

    public MulticastClientRule( String uri ) {
      this(Lists.newArrayList(
        new MulticastConfiguration()
          .withServers(Lists.newArrayList(uri))
          .withCritical(true)));
    }

    public MulticastClientRule( List<MulticastConfiguration> configurations ) {
      this.configurations = configurations;
    }

    public MulticastClient getClient() {
        return this.client;
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                client = new MulticastClient.Builder()
                        .withConfigurations(configurations)
                        .build();

                try {
                    statement.evaluate();
                }
                finally {
                    client.shutdownClient();
                }
            }

        };
    }
}

