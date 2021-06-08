```
mvn clean package
mvn jetty:run
```

```
docker build -t guessing-game:1.0 .
docker run --rm -p 8081:8080 guessing-game:1.0
```
