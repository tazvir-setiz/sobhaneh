package ir.sobhaneh.central;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class HostService {

    private static final int MIN_PORT = 10000;
    private static final int MAX_RANGE_SIZE = 1000;

    private final Map<String, List<HostInfo>> hosts = new ConcurrentHashMap<>();

    public CreateHostResult createHost(String ip, int startPort, int endPort) {
        if (startPort < MIN_PORT) {
            return CreateHostResult.PORT_NUMBER_MUST_BE_AT_LEAST_10000;
        }

        int rangeSize = endPort - startPort + 1;
        if (rangeSize > MAX_RANGE_SIZE) {
            return CreateHostResult.AT_MOST_1000_PORTS_IS_ALLOWED;
        }

        List<HostInfo> existingHostsOnIp = hosts.get(ip);
        if (existingHostsOnIp != null) {
            for (HostInfo existing : existingHostsOnIp) {
                boolean overlaps = startPort <= existing.getEndPort() && existing.getStartPort() <= endPort;
                if (overlaps) {
                    return CreateHostResult.PORT_IN_USE_BY_ANOTHER_HOST;
                }
            }
        }

        return CreateHostResult.OK;
    }

    public String generateCheckCode() {
        StringBuilder codeBuilder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            codeBuilder.append(random.nextInt(10));
        }
        String code = codeBuilder.toString();
        return code;
    }

    public boolean verifyCode(String code, String recivedCode) {
        if (recivedCode == null || !recivedCode.equals(code)) {
            return false;
        }
        return true;
    }
}
