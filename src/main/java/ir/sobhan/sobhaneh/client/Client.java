package ir.sobhan.sobhaneh.client;

import java.io.*;
import java.net.*;
import java.util.*;

public class Client {
    public static void main(String[] args) throws Exception {
        Socket s = new Socket("127.0.0.1", 10465);
        new Thread(() -> {
            try (Scanner in = new Scanner(s.getInputStream())) {
                while (in.hasNextLine()) System.out.println(in.nextLine());
            } catch (Exception e) {}
        }).start();

        PrintWriter out = new PrintWriter(s.getOutputStream(), true);
        Scanner sc = new Scanner(System.in);
        while (sc.hasNextLine()) out.println(sc.nextLine());
    }
}