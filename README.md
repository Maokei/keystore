= Keyvalue-store

Java 11, vertx

Communication over the eventbus with json

== Building

To launch your tests:
```
./mvnw clean test
```

To package your application:
```
./mvnw clean package
```

To run app with default config application:
```
./mvnw clean compile exec:java
```

### Run with custom config:
```
./mvnw clean test
cd target
java -jar keyvalue-store-1.0.0-SNAPSHOT-fat.jar -conf ../config2.json
```

### performing put
perform put against http://localhost:port/kvstore/values/{key}
```
{
    "data": "hello"
}
```
Data will be the value accociated with the key
