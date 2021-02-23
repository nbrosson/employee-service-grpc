package grpc.server;

import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.ServerCall;
import io.grpc.Metadata;

public class HeaderServerInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        if (call.getMethodDescriptor().getFullMethodName().equalsIgnoreCase("EmployeeService/GetByBadgeNumber")) {
            for (String key: headers.keys()) {
                // print key: value
                System.out.println(key + ": " + headers.get(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER)));
            }
        }
        return next.startCall(call, headers);
    }
}

