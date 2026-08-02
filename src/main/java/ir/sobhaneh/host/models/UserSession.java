//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.host.models;

import ir.sobhaneh.common.Connection;

public record UserSession(
    Connection connection,
    long userId,
    String username
    ){}
