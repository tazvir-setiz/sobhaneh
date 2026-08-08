package ir.sobhaneh.central.persistence;

import ir.sobhaneh.central.models.User;
import ir.sobhaneh.central.models.WorkspaceInfo;
import ir.sobhaneh.host.ChatStore;

import java.io.Serializable;
import java.util.Map;


public record CentralPersistedState(Map<String, User> users,
                                    Map<String, WorkspaceInfo> workspaces,
                                    Map<String, ChatStore> workspacesChats,
                                    Map<String, Map<Long, String>> workspacesUsernames) implements Serializable {
}
