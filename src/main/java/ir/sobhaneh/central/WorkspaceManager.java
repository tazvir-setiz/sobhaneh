//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.central;

import ir.sobhaneh.central.models.HostInfo;
import ir.sobhaneh.central.models.User;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class WorkspaceManager {
    public final Object createLock = new Object();
    private final ConcurrentHashMap<String, User> workspaces = new ConcurrentHashMap<>();

    public HostInfo allocateHost(List<HostInfo> hosts, AtomicInteger port){
        for(HostInfo host : hosts){
            port.set(host.allocateRandomPort());
            if(port.intValue()!= -1){
                return host;
            }
        }
        return null;
    }


}
