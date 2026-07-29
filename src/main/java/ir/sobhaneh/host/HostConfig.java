//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.host;

public class HostConfig {
    private final String ip;
    private final int startPort;
    private final int endPort;

    public HostConfig(String ip, int startPort, int endPort) {
        this.ip = ip;
        this.startPort = startPort;
        this.endPort = endPort;
    }

    public String getIp() {
        return ip;
    }

    public int getStartPort() {
        return startPort;
    }

    public int getEndPort() {
        return endPort;
    }
}
