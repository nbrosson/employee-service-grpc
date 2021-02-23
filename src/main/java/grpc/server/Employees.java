package grpc.server;

import grpc.messages.Messages;
import java.util.ArrayList;

public class Employees extends ArrayList<Messages.Employee> {

    private static Employees employees;

    public static Employees getInstance() {
        if (employees == null) {
            employees = new Employees();
        }
        return employees;
    }

    private Employees() {
        this.add(Messages.Employee.newBuilder()
                .setId(1)
                .setBadgeNumber(2080)
                .setFirstName("Grace")
                .setLastName("Decker")
                .setVacationAccrualRate(2)
                .setVacationAccrued(30)
                .build());
    }
}
