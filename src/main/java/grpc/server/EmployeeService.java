package grpc.server;
import com.google.protobuf.ByteString;
import grpc.messages.EmployeeServiceGrpc;
import grpc.messages.Messages;
import io.grpc.stub.StreamObserver;

public class EmployeeService extends EmployeeServiceGrpc.EmployeeServiceImplBase {

    /**
     * Unary request
     * @param request
     * @param responseObserver
     */
    @Override
    public void getByBadgeNumber(Messages.GetByBadgeNumberRequest request,
                                 StreamObserver<Messages.EmployeeResponse> responseObserver) {
        for (Messages.Employee e : Employees.getInstance()) {
            if (e.getBadgeNumber() == request.getBadgeNumber()) {
                Messages.EmployeeResponse response = Messages.EmployeeResponse.newBuilder()
                        .setEmployee(e)
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();  // tells the client we are done sending messages
                return;
            }
        }
        responseObserver.onError(new Exception("Employee not found with badgeNumber: " +
                request.getBadgeNumber()));
    }

    /**
     * Server Streaming request. Similar implementation than below method, but in streaming mode.
     * @param request
     * @param responseObserver
     */
    @Override
    public void getAll(Messages.GetAllRequest request,
                       StreamObserver<Messages.EmployeeResponse> responseObserver) {
        System.out.println("GetAllRequest");
        for (Messages.Employee e : Employees.getInstance()) {
                Messages.EmployeeResponse response = Messages.EmployeeResponse.newBuilder()
                        .setEmployee(e)
                        .build();
                responseObserver.onNext(response);
        }

        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<Messages.AddPhotoRequest> addPhoto(
            StreamObserver<Messages.AddPhotoResponse> responseObserver) {

        System.out.println("AddPhotoRequest");

        return new StreamObserver<Messages.AddPhotoRequest>() {
            private ByteString result;

            @Override
            public void onNext(Messages.AddPhotoRequest value) {
                if (result == null) {
                    result = value.getData();
                } else {
                    result = result.concat(value.getData());
                }
                System.out.println("Received message with " + value.getData().size() + " bytes");
            }

            @Override
            public void onError(Throwable thrwbl) {
                System.out.println(thrwbl);
            }

            @Override
            public void onCompleted() {
                System.out.println("Total bytes received: " + result.size());
                responseObserver.onNext(Messages.AddPhotoResponse
                        .newBuilder()
                        .setIsOk(true)
                        .build());
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<Messages.EmployeeRequest> saveAll(
            StreamObserver<Messages.EmployeeResponse> responseObserver) {

        return new StreamObserver<Messages.EmployeeRequest>() {
            @Override
            public void onNext(Messages.EmployeeRequest value) {
                Employees.getInstance().add(value.getEmployee());
                responseObserver.onNext(
                        Messages.EmployeeResponse.newBuilder()
                        .setEmployee(value.getEmployee())
                        .build()
                );
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                for (Messages.Employee e : Employees.getInstance()) {
                    System.out.println(e);
                }
                responseObserver.onCompleted();
            }
        };

    }
}
