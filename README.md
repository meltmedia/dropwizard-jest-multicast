Jest Multicast
==============
Jest is a client for the elasticsearch REST API, which because it is HTTP is not meant for sending data to multiple hosts.
This library is a result of the need to do exactly that. 

Setup
-------------
Basic Client: 
```Java
MulticastClient client = new MulticastClient("http://localhost:9200");
```
More highly configured client:
```Java
MulticastClient client = new MulticastClient()
    .addCriticalDatabase("http://localhost:9200", "http://localhost:9201")
    .addNonCriticalDatabase("http://localhost:9203");
```
*providing a list of urls to the constructor will add those as critical database*
   
Usage
--------------
There are two kinds of endpoints supported by `MulticastClient`.
Critical operations are guaranteed to succeed or an exception will be thrown and the whole operation will fail.
Non-Critical operations are not guaranteed to succeed and the exception will be printed by execution will not stop on the operation
All critical operations will execute before non-critical operations

This library supports the following operations: 
- `Get`
- `Search`
- `Index`
- `Delete`
 
To use any of these functions, simply build the request and pass it to the `execute` function like you would normally do
using Jest.   
**example using elasticsearch QueryBuilder:**
```java
SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
searchSourceBuilder.query(QueryBuilders.matchAllQuery());
Search search = new Search.Builder(searchSourceBuilder.toString())
    .addIndex("index")
    .addType("type");
    
MulticastClient client = new MulticastClient("http://localhost:9200");
SearchResult result = client.execute(search);
```

For `search` and `get` functions, the client will query against the first critical database url passed,
or if no critical databases are given, then the first non-critical database. 