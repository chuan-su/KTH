# Guessing Game

### Run with jetty

```bash
mvn clean package
mvn jetty:run
```

go to [localhost:8080/guess](http://localhost:8080/guess)

### Run with tomcat docker image

```bash
docker build -t guessing-game:1.0 .
docker run --rm -p 8081:8080 guessing-game:1.0
```

go to [localhost:8081/guess](http://localhost:8081/guess)

