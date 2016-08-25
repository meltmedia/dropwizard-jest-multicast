# dropwizard-jest-multicast
There is not a good way to send data using jest to multiple elasticsearch 
databases. This project aims to fix that by overriding the `JestClient`
class to manage multiple connections. 

## Usage 
This is a dropwizard bundle to putting it into your project is as easy as:
```java
private MulticastBundle bundle;

@Override
public void initialize(Bootstrap<ExampleConfiguration> bootstrap) {
    bootstrap.addBundle(
            bundle = new MulticastBundle.Builder<ExampleConfiguration>()
                .withConfiguration(ExampleConfiguration::getElasticsearch)
                .build()
    );
}
```

