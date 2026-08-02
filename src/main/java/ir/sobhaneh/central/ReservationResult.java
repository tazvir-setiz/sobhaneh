//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.central;

import ir.sobhaneh.central.models.HostInfo;
import lombok.Getter;

@Getter
public class ReservationResult {
    private final boolean success;
    private final String errorMessage;
    private final HostInfo hostInfo;
    private final int port;

    private ReservationResult(boolean success, String errorMessage, HostInfo hostInfo, int port) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.hostInfo = hostInfo;
        this.port = port;
    }

    public static ReservationResult success(HostInfo hostInfo, int port) {
        return new ReservationResult(true, null, hostInfo, port);
    }

    public static ReservationResult failure(String errorMessage) {
        return new ReservationResult(false, errorMessage, null, -1);
    }

}
