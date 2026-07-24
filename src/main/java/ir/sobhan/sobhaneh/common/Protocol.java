//in the name of ALLAH
//YA MAHDI

package ir.sobhan.sobhaneh.common;

public final class Protocol {

    private Protocol() {
    }

    public static final String REGISTER = "register";
    public static final String LOGIN = "login";
    public static final String CREATE_HOST = "create-host";
    public static final String CHECK = "check";
    public static final String CREATE_WORKSPACE = "create-workspace";
    public static final String CONNECT_WORKSPACE = "connect-workspace";
    public static final String CONNECT = "connect";
    public static final String DISCONNECT = "disconnect";
    public static final String SEND_MESSAGE = "send-message";
    public static final String GET_CHATS = "get-chats";
    public static final String GET_MESSAGES = "get-messages";
    public static final String WHOIS = "whois";
    public static final String SHUTDOWN = "shutdown";

    public static final String OK = "OK";
    public static final String ERROR = "ERROR";
    public static final String USERNAME = "username?";

    public static final String INVALID_CODE = "Invalid code";
    public static final String PORT_IN_USE = "Port in use by another host";
    public static final String PORT_NUMBER_TOO_SMALL = "Port number must be at least 10000";
    public static final String TOO_MANY_PORTS = "At most 1000 ports is allowed";
}