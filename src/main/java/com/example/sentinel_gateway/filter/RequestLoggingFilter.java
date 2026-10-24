package com.example.sentinel_gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    private static final String REQUEST_START_TIME_ATTRIBUTE = "requestStartTime";
    private static final String REQUEST_END_TIME_ATTRIBUTE = "requestEndTime";
    private static final String REQUEST_EXECUTION_TIME_ATTRIBUTE = "requestExecutionTime";
    private static final String CORRELATION_ID_ATTRIBUTE = "correlationId";
    private static final String REQUEST_TIMESTAMP_ATTRIBUTE = "requestTimestamp";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        
        logger.info("RequestLoggingFilter STARTED");
        Instant requestTimestamp = Instant.now();

        exchange.getAttributes().put(
                REQUEST_TIMESTAMP_ATTRIBUTE,
                requestTimestamp
        );

        long startTimeNanos = System.nanoTime();

        exchange.getAttributes().put(
                REQUEST_START_TIME_ATTRIBUTE,
                startTimeNanos
        );


        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {

                    String correlationId =
                            (String) exchange.getAttributes()
                                    .get(CORRELATION_ID_ATTRIBUTE);


                    String method =
                            exchange.getRequest()
                                    .getMethod()
                                    .name();


                    String path =
                            exchange.getRequest()
                                    .getPath()
                                    .toString();


                    HttpStatusCode status =
                            exchange.getResponse()
                                    .getStatusCode();

                    int statusCode =
                            status != null ? status.value() : 0;


                    String ip =
                            exchange.getRequest()
                                    .getHeaders()
                                    .getFirst("X-Forwarded-For");


                    if (ip == null && exchange.getRequest().getRemoteAddress() != null) {

                        ip = exchange.getRequest()
                                .getRemoteAddress()
                                .getAddress()
                                .getHostAddress();
                    }


                    long endTimeNanos = System.nanoTime();

                    exchange.getAttributes().put(
                            REQUEST_END_TIME_ATTRIBUTE,
                            endTimeNanos
                    );


                    long executionTimeNanos =
                            endTimeNanos - startTimeNanos;


                    exchange.getAttributes().put(
                            REQUEST_EXECUTION_TIME_ATTRIBUTE,
                            executionTimeNanos
                    );


                    logger.info(
                            "Request Completed | Timestamp={} | CorrelationId={} | Method={} | Path={} | Status={} | IP={} | ExecutionTime={} ns",
                            requestTimestamp,
                            correlationId,
                            method,
                            path,
                            statusCode,
                            ip,
                            executionTimeNanos
                    );

                }));
    }


    @Override
    public int getOrder() {
        return -2;
    }
}