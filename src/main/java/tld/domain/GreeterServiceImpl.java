package tld.domain;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;
import tld.domain.proto.GreeterServiceGrpc.GreeterServiceImplBase;
import tld.domain.proto.HelloRequest;
import tld.domain.proto.HelloResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
@GRpcService
public class GreeterServiceImpl extends GreeterServiceImplBase {

    @Override
    public void sayHelloUnary(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        log.info("server received {}", request);
        String message = "Hello " + request.getFirstName() + " " + request.getLastName() + "!";
        HelloResponse response = HelloResponse.newBuilder().setMessage(message).build();
        log.info("server responded {}", response);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<HelloRequest> sayHelloBidi(StreamObserver<HelloResponse> responseObserver) {

        List<Timer> timers = new ArrayList<>();
        List<HelloResponse> responses = new ArrayList<>();

        class ResponseTask extends TimerTask {
            public void run() {
                responseObserver.onNext(HelloResponse.newBuilder().setMessage("Wake up!!").build());
                timers.get(0).cancel();
            }
        }

        return new StreamObserver<>() {

            @Override
            public void onNext(HelloRequest request) {
                log.info("server received {}", request);
                String message = "Hello " + request.getFirstName() + " " + request.getLastName() + "! Welcome!";
                responses.add(HelloResponse.newBuilder().setMessage(message).build());
                responseObserver.onNext(HelloResponse.newBuilder().setMessage("Received data from client, total: " + responses.size() ).build());

                if (timers.size() == 0) {
                    timers.add(new Timer());
                    timers.get(0).schedule(new ResponseTask(), 5000);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("***** ERROR *****", throwable);
            }

            @Override
            public void onCompleted() {
                for (HelloResponse response: responses)
                    responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        };
    }
}
