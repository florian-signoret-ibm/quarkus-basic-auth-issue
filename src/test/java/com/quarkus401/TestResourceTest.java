package com.quarkus401;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestResourceTest {
    private static final int NUMBER_OF_THREADS = 150;
    private static final int NUMBER_OF_LOOPS = 15000;
    private final AtomicInteger responseCount = new AtomicInteger();
    private static Exception exception = null;

    private static final Logger logger = LoggerFactory.getLogger(TestResourceTest.class);

    @Test
    public void testBasicAuth() throws InterruptedException, IOException {
        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(150);

        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManagerShared(true)
                .setConnectionManager(cm)
                .build()) {
            for (int i = 0; i < NUMBER_OF_THREADS; i++) {
                executorService.submit(sendRequest(httpClient, i, NUMBER_OF_LOOPS));
            }

            int receivedCount;
            do {
                Thread.sleep(1000);
                receivedCount = responseCount.get();
                logger.info("Received " + receivedCount + " requests out of " + NUMBER_OF_LOOPS * NUMBER_OF_THREADS);

                if (exception != null) {
                    fail(exception);
                }
            } while (receivedCount < NUMBER_OF_LOOPS * NUMBER_OF_THREADS);
        }
    }

    private Runnable sendRequest(CloseableHttpClient client, int threadId, int count) {
        return () -> {
            try {
                for (int i = 0; i < count; ++i) {
                    final HttpGet request = new HttpGet("http://localhost:8080");
                    final String auth = "admin:password";
                    final byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
                    final String authHeader = "Basic " + new String(encodedAuth);
                    final String requestInternalId = threadId + "-" + i;
                    request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
                    request.setHeader("request_internal_id", requestInternalId);

                    try (CloseableHttpResponse response = client.execute(request)) {
                        if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
                            throw new IllegalStateException("Unexpected response " + response.getStatusLine().getStatusCode());
                        }
                        String msg = EntityUtils.toString(response.getEntity(), "UTF-8");
                        assertEquals("hello", msg);
                    } catch (Exception e) {
                        logger.error("Caught exception for request " + requestInternalId);
                        exception = e;
                    } finally {
                        responseCount.incrementAndGet();
                    }
                }
            } catch (Exception e) {
                exception = e;
            }
        };
    }

}
