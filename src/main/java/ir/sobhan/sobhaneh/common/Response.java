//in the name of ALLAH
//YA MAHDI

package ir.sobhan.sobhaneh.common;

import ir.sobhan.sobhaneh.common.response.ResponseStatus;

public class Response {

    private ResponseStatus status;
    private ErrorType error;
    private Object data;

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
}