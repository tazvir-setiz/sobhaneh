//in the name of ALLAH
//YA MAHDI

package ir.sobhan.sobhaneh.centralserver.repository;

import ir.sobhan.sobhaneh.common.dto.HostDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class HostRepository {
    private static final HashMap<String, ArrayList<HostDTO>> hosts = new HashMap<>();

    private HostRepository() {
    }

    private static boolean portConflict(int oStart, int oEnd, int nStart, int nEnd) {
        return (nStart <= oEnd) && (nEnd >= oStart);
    }

    public static boolean addHost(HostDTO host) {
        if (host == null) {
            return false;
        }
        if (!hosts.containsKey(host.getIp())) {
            hosts.put(host.getIp(), new ArrayList<>());
        }
        ArrayList<HostDTO> list = hosts.get(host.getIp());
        for (HostDTO oldHost : list) {
            if (portConflict(oldHost.getStartPort(), oldHost.getEndPort(), host.getStartPort(), host.getEndPort())) {
                return false;
            }
        }
        list.add(host);
        return true;
    }

    public static boolean removeHost(String ip, int startPort, int endPort) {
        ArrayList<HostDTO> list = hosts.get(ip);
        if (list == null) {
            return false;
        }
        for (int i = 0; i < list.size(); i++) {
            HostDTO oldHost = list.get(i);
            if (oldHost.getStartPort() == startPort && oldHost.getEndPort() == endPort) {
                list.remove(oldHost);
                if (list.isEmpty()) {
                    hosts.remove(ip);
                }
                return true;
            }
        }
        return false;
    }

    public static HostDTO findHost(String ip, int startPort, int endPort) {
        if (hosts.get(ip) == null) {
            return null;
        }
        ArrayList<HostDTO> list = hosts.get(ip);
        for (HostDTO oldHost : list) {
            if (oldHost.getStartPort() == startPort && oldHost.getEndPort() == endPort) {
                return oldHost;
            }
        }
        return null;
    }

    public static HostDTO getFreeHost() {
        ArrayList<HostDTO> freeHosts = new ArrayList<>();
        for (ArrayList<HostDTO> list : hosts.values()) {
            for (HostDTO host : list) {
                if (host.getFreePort() != -1) {
                    freeHosts.add(host);
                }
            }
        }
        if (freeHosts.isEmpty()) {
            return null;
        }
        Random random = new Random();
        return freeHosts.get(random.nextInt(freeHosts.size()));
    }

    public static ArrayList<HostDTO> getHosts(String ip) {
        return hosts.get(ip);
    }

    public static HashMap<String, ArrayList<HostDTO>> getAllHosts() {
        return hosts;
    }
}

