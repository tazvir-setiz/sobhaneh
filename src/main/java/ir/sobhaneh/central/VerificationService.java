//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.central;

import ir.sobhaneh.common.Connection;

import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class VerificationService {

    public String generateCode() {
        StringBuilder sb = new StringBuilder();
        String availableDigits = "0123456789";
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            sb.append(availableDigits.charAt(random.nextInt(availableDigits.length())));
        }
        return sb.toString();
    }

    public boolean sendCode(String hostIp, int hostPort, String code) {
        try (Socket socket = new Socket(hostIp, hostPort);
             Connection connection = new Connection(socket)) {
            connection.sendLine(code);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
