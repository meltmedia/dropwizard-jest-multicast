package com.meltmedia.dropwizard.jest;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Created by qthibeault on 8/25/16.
 */

@RunWith(Parameterized.class)
public class MulticastClientReplacementTest {

    @ClassRule
    public static WireMockClassRule wireMockRule = new WireMockClassRule(9201);

    @Rule
    public WireMockClassRule esMock = wireMockRule;

    private JestClient client;

    public MulticastClientReplacementTest(String name, JestClient client) {
        this.client = client;
    }

    @Test
    public void testSearch() throws IOException {

        esMock.stubFor(post(urlEqualTo("/testindex/testtype/_search"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{ }")));

        Search search = new Search.Builder("{ \"query\": { \"match_all\":{}  } }")
                .addIndex("testindex")
                .addType("testtype")
                .build();

        SearchResult result = this.client.execute(search);
        assertThat(result, is(notNullValue()));

        esMock.verify(postRequestedFor(urlEqualTo("/testindex/testtype/_search")));
    }

    @Test
    public void testIndex() throws IOException {

        esMock.stubFor(post(urlEqualTo("/testindex/testtype"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ }")));

        Index index = new Index.Builder(new Object())
                .index("testindex")
                .type("testtype")
                .build();

        JestResult result = this.client.execute(index);
        esMock.verify(postRequestedFor(urlMatching("/testindex/testtype/*")));
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> clientParameterProvider() {
        JestClientFactory clientFactory = new JestClientFactory();
        HttpClientConfig clientConfig = new HttpClientConfig.Builder("http://localhost:9201")
                .multiThreaded(true)
                .build();

        clientFactory.setHttpClientConfig(clientConfig);
        JestClient jestClient = clientFactory.getObject();

        List<MulticastConfiguration> clientConfigurations = new ArrayList<>();
        MulticastConfiguration multicastConfiguration = new MulticastConfiguration();
        multicastConfiguration.setCritical(true);
        multicastConfiguration.setDatabaseUrls(Collections.singletonList("http://localhost:9201"));
        clientConfigurations.add(multicastConfiguration);

        MulticastClient multicastClient = new MulticastClient.Builder()
                .withConfigurations(clientConfigurations)
                .build();


        return Arrays.asList(new Object[][]{
                { "jest client", jestClient },
                { "multicast client", multicastClient }
        });
    }

}
