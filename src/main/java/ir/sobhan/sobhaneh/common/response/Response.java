//in the name of ALLAH
//YA MAHDI

package ir.sobhan.sobhaneh.common.response;

import ir.sobhan.sobhaneh.centralserver.service.LoginService;
import ir.sobhan.sobhaneh.centralserver.service.RegisterService;
import ir.sobhan.sobhaneh.common.dto.HostDTO;

public class Response {

    private ResponseStatus status;
    private ErrorType error = null;
    private Object data = null;

    public Response(ResponseStatus status) {
        this.status = status;
    }

    public Response(ErrorType error) {
        this.status = ResponseStatus.ERROR;
        this.error = error;
    }

    public Response(Object data) {
        this.status = ResponseStatus.OK;
        this.data = data;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public ErrorType getError() {
        return error;
    }

    public Object getData() {
        return data;
    }

    public String toString() {
        if(status == ResponseStatus.ERROR) return ""+status + error;
        if(data instanceof RegisterService) return "OK";
        if(data instanceof LoginService) return "OK";
        if(data instanceof HostDTO) {
            HostDTO host = (HostDTO)data;
            return "" + status + host.getFreePort();
        }
        return ""+status;
    }
}