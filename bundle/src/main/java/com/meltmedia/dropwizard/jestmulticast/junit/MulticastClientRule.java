package com.meltmedia.dropwizard.jestmulticast.junit;

import com.meltmedia.dropwizard.jestmulticast.MulticastClient;
import com.meltmedia.dropwizard.jestmulticast.MulticastConfiguration;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.Collections;

/**
 * Created by qthibeault on 9/29/16.
 */

public class MulticastClientRule implements TestRule {

    private MulticastConfiguration configuration;
    private MulticastClient client;

    public MulticastClientRule( String uri ) {
        this.configuration = new MulticastConfiguration();
        this.configuration.setServers(Collections.singletonList(uri));
        this.configuration.setCritical(true);
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
                        .withConfigurations(Collections.singletonList(configuration))
                        .build();

                statement.evaluate();
            }

        };
    }
}

