//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.central.models;

import java.io.Serializable;

public record WorkspaceInfo(String name, String hostIp, int port, long creatorUserId) implements Serializable {
}
