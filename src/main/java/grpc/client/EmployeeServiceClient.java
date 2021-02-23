package grpc.client;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;
import com.google.protobuf.ByteString;
import grpc.messages.EmployeeServiceGrpc;
import grpc.messages.Messages;
import io.grpc.Channel;
import io.grpc.ClientInterceptors;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;

public class EmployeeServiceClient {

    public static void main(String[] args) throws Exception {
        ManagedChannel channel = NettyChannelBuilder
                .forAddress("localhost", 9000)
                .sslContext(GrpcSslContexts.forClient()
                        .trustManager(new File("./cert.pem")).build())
                .build();

        EmployeeServiceGrpc.EmployeeServiceBlockingStub blockingClient = EmployeeServiceGrpc.newBlockingStub(channel);

        // for client streaming and bi-directional streaming
        EmployeeServiceGrpc.EmployeeServiceStub nonBlockingClient = EmployeeServiceGrpc.newStub(channel);

        switch (Integer.parseInt(args[0])) {
            case 1:
                sendMetadata(blockingClient);
                break;
            case 2:
                getByBadgeNumber(blockingClient);
                break;
            case 3:
                getAll(blockingClient);
                break;
            case 4:
                addPhoto(nonBlockingClient);
                break;
            case 5:
                saveAll(nonBlockingClient);
                break;
        }

        // before shut down the main thread, wait for a while for async requests (backgrnd threads) to complete. NOT TO DO IN PROD.
        Thread.sleep(5000);
        channel.shutdown();
        channel.awaitTermination(1, TimeUnit.SECONDS);

    }

    private static void saveAll(EmployeeServiceGrpc.EmployeeServiceStub client) {
        List<Messages.Employee> employees = new ArrayList<Messages.Employee>();
        employees.add(Messages.Employee.newBuilder()
                .setBadgeNumber(123)
                .setFirstName("John")
                .setLastName("Smith")
                .setVacationAccrualRate(1.2f)
                .build());
        employees.add(Messages.Employee.newBuilder()
                .setBadgeNumber(234)
                .setFirstName("Lisa")
                .setLastName("Wu")
                .setVacationAccrualRate(1.7f)
                .setVacationAccrued(10)
                .build());

        StreamObserver<Messages.EmployeeRequest> stream =
                client.saveAll(new StreamObserver<Messages.EmployeeResponse>() {
                    @Override
                    public void onNext(Messages.EmployeeResponse v) {
                        System.out.println(v.getEmployee());
                    }

                    @Override
                    public void onError(Throwable thrwbl) {
                        System.out.println(thrwbl);
                    }

                    @Override
                    public void onCompleted() {
                        // no-op
                    }
                });

        for (Messages.Employee e : employees) {
            Messages.EmployeeRequest request = Messages.EmployeeRequest.newBuilder()
                    .setEmployee(e)
                    .build();
            stream.onNext(request);
        }
        stream.onCompleted();

    }

    private static void addPhoto(EmployeeServiceGrpc.EmployeeServiceStub client) {
        try {
            StreamObserver<Messages.AddPhotoRequest> stream =
                    client.addPhoto(
                            new StreamObserver<Messages.AddPhotoResponse>() {
                        @Override
                        public void onNext(Messages.AddPhotoResponse response) {
                            System.out.println(response.getIsOk());
                        }

                        @Override
                        public void onError(Throwable t) {
                            System.out.println(t);
                        }

                        @Override
                        public void onCompleted() {
                            // no-op
                        }
                    });
            FileInputStream fs = new FileInputStream("./test-image.jpg");
            while (true) {
                byte[] data = new byte[64 * 1024];

                int bytesRead = fs.read(data);
                if (bytesRead == -1) {
                    break;
                }

                if (bytesRead < data.length) {
                    byte[] newData = new byte[bytesRead];
                    System.arraycopy(data, 0, newData, 0, bytesRead);
                    data = newData;
                }

                Messages.AddPhotoRequest request = Messages.AddPhotoRequest.newBuilder()
                        .setData(ByteString.copyFrom(data)).build();
                stream.onNext(request);
            }

            stream.onCompleted();
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    private static void getAll(EmployeeServiceGrpc.EmployeeServiceBlockingStub client) {
        Iterator<Messages.EmployeeResponse> iterator = client.getAll(Messages.GetAllRequest.newBuilder().build());
        while (iterator.hasNext()) {
            System.out.println(iterator.next().getEmployee());
        }
    }

    private static void getByBadgeNumber(EmployeeServiceGrpc.EmployeeServiceBlockingStub blockingClient) {
        Messages.EmployeeResponse response = blockingClient.getByBadgeNumber(
                Messages.GetByBadgeNumberRequest.newBuilder().setBadgeNumber(2080).build());
        System.out.println(response.getEmployee());
    }

    private static void sendMetadata(
            EmployeeServiceGrpc.EmployeeServiceBlockingStub blockingClient) {
        Metadata md = new Metadata();
        md.put(Metadata.Key.of("username", Metadata.ASCII_STRING_MARSHALLER), "mvansickle");
        md.put(Metadata.Key.of("password", Metadata.ASCII_STRING_MARSHALLER), "password1");

        Channel ch = ClientInterceptors.intercept(blockingClient.getChannel(),
                MetadataUtils.newAttachHeadersInterceptor(md));

        blockingClient.withChannel(ch).getByBadgeNumber(
                Messages.GetByBadgeNumberRequest.newBuilder().build());
    }
}
