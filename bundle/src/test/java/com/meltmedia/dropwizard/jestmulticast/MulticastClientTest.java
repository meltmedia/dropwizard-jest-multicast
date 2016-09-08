package com.meltmedia.dropwizard.jestmulticast;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Created by qthibeault on 8/25/16.
 */

public class MulticastClientTest {

    private MulticastClient client;

    @Rule
    public WireMockRule esHost1 = new WireMockRule(9201);

    @Rule
    public WireMockRule esHost2 = new WireMockRule(9202);

    private MulticastConfiguration generateClientConfiguration(String url, Boolean isCritical) {

        MulticastConfiguration clientConfiguration1 = new MulticastConfiguration();
        clientConfiguration1.setClusterName("elasticsearch");
        clientConfiguration1.setServers(Collections.singletonList(url));
        clientConfiguration1.setCritical(isCritical);

        return clientConfiguration1;
    }

    public MulticastClientTest() {

        List<MulticastConfiguration> configurations = new ArrayList<>();
        configurations.add(generateClientConfiguration("http://localhost:9201", true));
        configurations.add(generateClientConfiguration("http://localhost:9202", false));

        this.client = new MulticastClient.Builder()
                .withConfigurations(configurations)
                .build();
    }

    @Test
    public void searchTest() throws IOException {
        /* This should only query a a single database */

        esHost1.stubFor(post(urlEqualTo("/testindex/testtype/_search"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{}")));

        Search search = new Search.Builder("{ \"query\": { \"match_all\": {} } }")
                .addIndex("testindex")
                .addType("testtype")
                .build();

        this.client.execute(search);

        esHost1.verify(postRequestedFor(urlEqualTo("/testindex/testtype/_search")));

    }

    @Test
    public void indexTest() throws IOException {
        /* this should query every database */

        esHost1.stubFor(post(urlEqualTo("/testindex/testtype"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{}")));

        esHost2.stubFor(post(urlEqualTo("/testindex/testtype"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{}")));

        Index index = new Index.Builder(new Object())
                .index("testindex")
                .type("testtype")
                .build();

        this.client.execute(index);

        esHost1.verify(postRequestedFor(urlEqualTo("/testindex/testtype")));
        esHost1.verify(postRequestedFor(urlEqualTo("/testindex/testtype")));
    }

    @Test
    public void multiExecuteTest() throws IOException {

        AtomicInteger operationCounter = new AtomicInteger();
        AtomicInteger failureCounter = new AtomicInteger();

        esHost1.stubFor(post(urlEqualTo("/testindex/testtype/_search"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{}")));

        this.client.multiExecute((JestClient client, Boolean isCritical) -> {
            operationCounter.incrementAndGet();

            Search search = new Search.Builder("{ \"query\": { \"match_all\": {} } }")
                    .addIndex("testindex")
                    .addType("testtype")
                    .build();

            try {
                client.execute(search);
            }
            catch(IOException e) {
                failureCounter.incrementAndGet();
            }
        });

        esHost1.verify(postRequestedFor(urlEqualTo("/testindex/testtype/_search")));
        assertThat(operationCounter.get(), is(equalTo(2)));
        assertThat(failureCounter.get(), is(equalTo(1)));
    }

    @Test(expected = UncheckedIOException.class)
    public void testCriticalFlag() throws IOException {
        this.client.execute(new Index.Builder(new Object()).build());
    }

}
