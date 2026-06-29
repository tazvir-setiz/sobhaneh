package ir.sobhan.sobhaneh.server;
import java.io.*;
import java.net.*;
import java.util.*;

public class CentralServer {
    public static List<int[]> usedRanges = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(8000);
        while (true) {
            new HostHandler(server.accept()).start();
        }
    }
}

class HostHandler extends Thread {
    private Socket s;
    public HostHandler(Socket s) { this.s = s; }

    public void run() {
        try (Scanner in = new Scanner(s.getInputStream());
             Formatter out = new Formatter(s.getOutputStream())) {

            String[] req = in.nextLine().split(" ");
            int min = Integer.parseInt(req[2]);
            int max = Integer.parseInt(req[3]);

            if (min < 10000) { out.format("ERROR Port must be >= 10000\n").flush(); return; }
            if (max - min > 1000) { out.format("ERROR Max 1000 ports\n").flush(); return; }

            synchronized (CentralServer.usedRanges) {
                for (int[] r : CentralServer.usedRanges) {
                    if (min <= r[1] && max >= r[0]) { out.format("ERROR Port in use\n").flush(); return; }
                }
            }

            int testPort = min + new Random().nextInt(max - min + 1);
            out.format("OK %d\n", testPort).flush();

            if (in.nextLine().equals("check")) {
                String code = "1234567890";
                try (Socket side = new Socket(s.getInetAddress(), testPort)) {
                    new Formatter(side.getOutputStream()).format("OK %s\n", code).flush();
                }
                if (in.nextLine().equals(code)) {
                    synchronized (CentralServer.usedRanges) {
                        CentralServer.usedRanges.add(new int[]{min, max});
                    }
                    out.format("OK\n").flush();
                } else { out.format("ERROR Invalid code\n").flush(); }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}