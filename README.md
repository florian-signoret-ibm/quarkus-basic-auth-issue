# quarkus-basic-auth-issue

To reproduce the issue:

- Build the code:
```mvn clean package -DskipTests```

- You can try to reproduce using docker, or not
  - With docker:
    - Build the docker image:
    ```docker build -f src/main/docker/Dockerfile.jvm -t quarkus/quarkus-basic-auth-issue .```
    - Start the docker container:
    ```docker run -i --rm -p 8080:8080 quarkus/quarkus-basic-auth-issue```
  - Without docker:
    - Start the quarkus app using your favorite IDE

- Run the test: depending on your machine, the failure (unexpected 401) can take time to occur.
