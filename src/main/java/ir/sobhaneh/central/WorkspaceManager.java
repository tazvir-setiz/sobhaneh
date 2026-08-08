//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.central;

import ir.sobhaneh.central.models.HostInfo;
import ir.sobhaneh.central.models.WorkspaceInfo;
import ir.sobhaneh.host.ChatStore;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class WorkspaceManager {
    private static final String RESPONSE_OK = "OK";
    private static final String ERROR_ALREADY_EXISTS = "ERROR Workspace already exists";
    private static final String ERROR_NO_HOST = "ERROR No available host";
    private static final String ERROR_HOST_REJECTED = "ERROR Host failed to create workspace";
    private static final int MAX_WORKSPACE_NAME_LENGTH = 60;
    private static final Pattern WORKSPACE_NAME_PATTERN = Pattern.compile("[A-Za-z0-9_]+");
    private final Object createLock = new Object();
    private final ConcurrentHashMap<String, WorkspaceInfo> workspaces = new ConcurrentHashMap<>();

    private static String validateWorkspaceName(String name) {
        if (name.length() > MAX_WORKSPACE_NAME_LENGTH) {
            return "ERROR Workspace name too long (max " + MAX_WORKSPACE_NAME_LENGTH + ")";
        }
        if (!WORKSPACE_NAME_PATTERN.matcher(name).matches()) {
            return "ERROR Workspace name must contain only letters, digits, and underscore";
        }
        return null;
    }

    public WorkspaceInfo findByName(String name) {
        return workspaces.get(name);
    }

    public String createWorkspace(String name, long creatorUserId, List<HostInfo> registeredHosts) throws IOException {
        System.out.println("[WorkspaceManager] createWorkspace requested: name=" + name + " creatorUserId=" + creatorUserId);
        HostPortReservation reservation;
        String validationResult = validateWorkspaceName(name);
        if (validationResult != null) {
            System.out.println("[WorkspaceManager] Name validation failed: " + validationResult);
            return validationResult;
        }
        synchronized (createLock) {
            if (workspaces.containsKey(name)) {
                System.out.println("[WorkspaceManager] Workspace already exists: " + name);
                return ERROR_ALREADY_EXISTS;
            }
            reservation = reserveHostAndPort(registeredHosts);
            if (reservation == null) {
                System.out.println("[WorkspaceManager] No available host for workspace: " + name);
                return ERROR_NO_HOST;
            }
        }
        System.out.println("[WorkspaceManager] Reserved host=" + reservation.host().getIp() + " port=" + reservation.port());

        boolean confirmed = notifyHost(reservation.host(), reservation.port(), creatorUserId);
        if (!confirmed) {
            System.out.println("[WorkspaceManager] Host rejected create-workspace on port=" + reservation.port());
            reservation.host().releasePort(reservation.port());
            return ERROR_HOST_REJECTED;
        }

        WorkspaceInfo info = buildWorkspaceInfo(name, reservation, creatorUserId);
        String conflict = registerWorkspaceIfAbsent(name, info);
        if (conflict != null) {
            System.out.println("[WorkspaceManager] Conflict registering workspace: " + name);
            reservation.host().releasePort(reservation.port());
            return conflict;
        }

        System.out.println("[WorkspaceManager] Workspace created successfully: " + name);
        return buildSuccessResponse(reservation);
    }

    private HostPortReservation reserveHostAndPort(List<HostInfo> hosts) {
        for (HostInfo host : hosts) {
            int port = host.allocateRandomPort();
            if (port != -1) {
                return new HostPortReservation(host, port);
            }
        }
        return null;
    }

    private boolean notifyHost(HostInfo host, int port, long creatorUserId) throws IOException {
        String response = host.getConnectionListener()
                .sendAndWait("create-workspace " + port + " " + creatorUserId);
        return RESPONSE_OK.equals(response);
    }

    private WorkspaceInfo buildWorkspaceInfo(String name, HostPortReservation reservation, long creatorUserId) {
        return new WorkspaceInfo(name, reservation.host().getIp(), reservation.port(), creatorUserId);
    }

    private String registerWorkspaceIfAbsent(String name, WorkspaceInfo info) {
        WorkspaceInfo existing = workspaces.putIfAbsent(name, info);
        return existing == null ? null : ERROR_ALREADY_EXISTS;
    }

    private String buildSuccessResponse(HostPortReservation reservation) {
        return RESPONSE_OK + " " + reservation.host().getIp() + " " + reservation.port();
    }

    private record HostPortReservation(HostInfo host, int port) {
    }

    public String findNameByPort(int port) {
        for (WorkspaceInfo info : workspaces.values()) {
            if (info.port() == port) {
                return info.name();
            }
        }
        return null;
    }

    public Map<String, WorkspaceInfo> exportWorkspaces() {
        Map<String, WorkspaceInfo> result = new HashMap<>();
        for (Map.Entry<String, WorkspaceInfo> entry : workspaces.entrySet()) {
            WorkspaceInfo original = entry.getValue();
            WorkspaceInfo sanitized = new WorkspaceInfo(original.name(), null, 0, original.creatorUserId());
            result.put(entry.getKey(), sanitized);
        }
        return result;
    }

    public void importWorkspaces(Map<String, WorkspaceInfo> savedWorkspaces) {
        workspaces.clear();
        workspaces.putAll(savedWorkspaces);
    }

    public Collection<WorkspaceInfo> getAllWorkspacesRaw() {
        return workspaces.values();
    }

    public String restoreWorkspace(WorkspaceInfo oldInfo, List<HostInfo> registeredHosts,
                                   ChatArchiveManager chatArchiveManager) throws IOException {
        System.out.println("[WorkspaceManager] restoreWorkspace requested: name=" + oldInfo.name());
        HostPortReservation reservation;
        synchronized (createLock) {
            reservation = reserveHostAndPort(registeredHosts);
            if (reservation == null) {
                return ERROR_NO_HOST;
            }
        }

        boolean confirmed = notifyHost(reservation.host(), reservation.port(), oldInfo.creatorUserId());
        if (!confirmed) {
            reservation.host().releasePort(reservation.port());
            return ERROR_HOST_REJECTED;
        }

        ChatStore archivedChatStore = chatArchiveManager.getWorkspaceChatStore(oldInfo.name());
        if (archivedChatStore != null) {
            String json = new ir.sobhaneh.host.JsonMapper().chatStoreDataToJson(archivedChatStore.toChatStoreData());
            String restoreResponse = reservation.host().getConnectionListener()
                    .sendAndWait("restore-chats " + reservation.port() + " " + json);
            System.out.println("[WorkspaceManager] restore-chats response: " + restoreResponse);
        }

        WorkspaceInfo newInfo = buildWorkspaceInfo(oldInfo.name(), reservation, oldInfo.creatorUserId());
        workspaces.put(oldInfo.name(), newInfo);

        return buildSuccessResponse(reservation);
    }
}