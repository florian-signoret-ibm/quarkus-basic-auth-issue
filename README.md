# quarkus-basic-auth-issue

To reproduce the issue:

- Build the code:
```mvn clean package -DskipTests```

- Build the docker image:
```docker build -f src/main/docker/Dockerfile.jvm -t quarkus/quarkus-basic-auth-issue .```

- Start the docker container:
```docker run -i --rm -p 8080:8080 quarkus/quarkus-basic-auth-issue```

- Run the test
