package com.qthibeau.dropwizard.jest.resources;

import com.qthibeau.dropwizard.jest.MulticastClient;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by qthibeault on 8/25/16.
 */

@Path("/example")
public class ExampleResource {

    private Supplier<JestClient> clientSupplier;

    private class Thing {

        public final int id;
        public final String content;

        public Thing(int id, String content) {
            this.id = id;
            this.content = content;
        }
    }

    public ExampleResource(Supplier<JestClient> jestClientSupplier) {

        this.clientSupplier = jestClientSupplier;
    }

    @GET
    public List<Thing> getThings(@QueryParam("content") @DefaultValue("") String content) throws IOException {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        if(!content.isEmpty()){
            searchSourceBuilder.query(QueryBuilders.simpleQueryStringQuery(content));
        }
        else {
            searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        }

        Search search = new Search.Builder(searchSourceBuilder.toString())
                .addIndex("example")
                .addType("thing")
                .build();

        SearchResult result = this.clientSupplier.get().execute(search);

        return result.getHits(Thing.class).stream().map((SearchResult.Hit<Thing, Void> hit) -> {
            return hit.source;
        })
        .collect(Collectors.toList());
    }
}
