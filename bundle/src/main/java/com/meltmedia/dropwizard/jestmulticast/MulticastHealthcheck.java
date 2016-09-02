package com.meltmedia.dropwizard.jestmulticast;

import com.codahale.metrics.health.HealthCheck;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.cluster.Health;

import java.util.List;
import java.util.function.Supplier;

/**
 * Created by qthibeault on 9/2/16.
 */
public class MulticastHealthcheck extends HealthCheck {
    protected Supplier<MulticastClient> clientSupplier;
    public MulticastHealthcheck( Supplier<MulticastClient> clientSupplier ) {
        this.clientSupplier = clientSupplier;
    }

    @Override
    protected Result check() throws Exception {
        MulticastClient client = this.clientSupplier.get();
        List<JestResult> results = client.checkHealth(new Health.Builder().build());
        for(JestResult result : results) {
            if(!result.isSucceeded()){
                return Result.unhealthy("Cannot get health for cluster");
            }
        }

        // If all results succeeded then assume everything is healthy
        return Result.healthy();
    }
}
