package ir.sobhan.sobhaneh.host;
import ir.sobhan.sobhaneh.host.workspace.Workspace;

import java.io.*;
import java.net.*;
import java.util.*;

public class Host {
	public static void main(String[] args) throws Exception {
		Socket s = new Socket("127.0.0.1", 8000);
		Scanner in = new Scanner(s.getInputStream());
		Formatter out = new Formatter(s.getOutputStream());

		out.format("create-host 127.0.0.1 10000 10999\n").flush();
		String[] res = in.nextLine().split(" ");

		if (res[0].equals("OK")) {
			int port = Integer.parseInt(res[1]);
			ServerSocket ts = new ServerSocket(port);
			out.format("check\n").flush();

			Socket side = ts.accept();
			String code = new Scanner(side.getInputStream()).nextLine().split(" ")[1];
			side.close(); ts.close();

			out.format("%s\n", code).flush();
			if (in.nextLine().equals("OK")) {
				new Workspace(10465).start();
			}
		}
	}
}
