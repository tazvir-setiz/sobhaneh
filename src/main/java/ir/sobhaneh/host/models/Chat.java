package ir.sobhaneh.host.models;

public class Chat {
    String usernameA;
    String usernameB;
    int lastSeq;

    public static String buildKey(String usernameA, String usernameB) {
        if(usernameA.compareTo(usernameB) >  0) return usernameA + "-"  + usernameB;
        else return usernameA + "-" + usernameB;
    }
}
