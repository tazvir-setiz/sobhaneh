package ir.sobhaneh.central.persistence;

import ir.sobhaneh.central.models.User;
import ir.sobhaneh.central.models.WorkspaceInfo;

import java.io.Serializable;
import java.util.Map;


public record CentralPersistedState(Map<String, User> users,
                                    Map<String, WorkspaceInfo> workspaces) implements Serializable {
}
