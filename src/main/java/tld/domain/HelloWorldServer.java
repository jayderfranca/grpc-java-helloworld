package tld.domain;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HelloWorldServer {

    int port;
    Server server;

    HelloWorldServer(int port) {
        this.port = port;
        this.server = ServerBuilder
                .forPort(port)
                .addService(new GreeterServiceImpl())
                .build();
        log.info("'" + GreeterServiceImpl.class.getCanonicalName() + "' service has been registered.");
    }

    public void start() throws IOException {
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            try {
                HelloWorldServer.this.stop();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
            System.err.println("*** server shut down");
        }));
    }

    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        log.info("Starting gRPC Server ...");
        HelloWorldServer server = new HelloWorldServer(10001);
        server.start();
        log.info("gRPC Server started, listening on port 10001.");
        server.blockUntilShutdown();
    }
}
