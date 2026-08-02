//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.central;

import ir.sobhaneh.central.models.HostInfo;
import ir.sobhaneh.central.models.WorkspaceInfo;
import ir.sobhaneh.common.Connection;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WorkspaceManager {
    private static final String RESPONSE_OK = "OK";
    private static final String ERROR_ALREADY_EXISTS = "ERROR Workspace already exists";
    private static final String ERROR_NO_HOST = "ERROR No available host";
    private static final String ERROR_HOST_REJECTED = "ERROR Host failed to create workspace";

    private final Object createLock = new Object();
    private final ConcurrentHashMap<String, WorkspaceInfo> workspaces = new ConcurrentHashMap<>();

    private static final int MAX_WORKSPACE_NAME_LENGTH = 60;
    private static final Pattern WORKSPACE_NAME_PATTERN = Pattern.compile("[A-Za-z0-9_]+");

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
        HostPortReservation reservation;
        String validationResult = validateWorkspaceName(name);
        if (validationResult != null) {
            return validationResult;
        }
        synchronized (createLock) {
            if (workspaces.containsKey(name)) {
                return ERROR_ALREADY_EXISTS;
            }
            reservation = reserveHostAndPort(registeredHosts);
            if (reservation == null) {
                return ERROR_NO_HOST;
            }
        }

        boolean confirmed = notifyHost(reservation.host(), reservation.port(), creatorUserId);
        if (!confirmed) {
            reservation.host().releasePort(reservation.port());
            return ERROR_HOST_REJECTED;
        }

        WorkspaceInfo info = buildWorkspaceInfo(name, reservation, creatorUserId);
        String conflict = registerWorkspaceIfAbsent(name, info);
        if (conflict != null) {
            reservation.host().releasePort(reservation.port());
            return conflict;
        }

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
        Connection hostConnection = host.getConnection();
        hostConnection.sendLine("create-workspace " + port + " " + creatorUserId);
        String response = hostConnection.readLine();
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
}
