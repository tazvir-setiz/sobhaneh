package ir.sobhan.sobhaneh.host;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Host {
	public static void register(String hostAddress, int startPort, int endPort) throws IOException {
		try (Socket centralSocket = new Socket("127.0.0.1", 8000);
			 BufferedReader centralReader = new BufferedReader(new InputStreamReader(centralSocket.getInputStream()));
			 PrintWriter centralWriter = new PrintWriter(centralSocket.getOutputStream(), true)) {

			centralWriter.println("create-host " + hostAddress + " " + startPort + " " + endPort);
			String response = centralReader.readLine();
			if (response == null || !response.startsWith("OK ")) {
				throw new IOException(response == null ? "ERROR No response from central server" : response);
			}

			int listenPort = Integer.parseInt(response.substring(3).trim());
			try (ServerSocket hostSocket = new ServerSocket(listenPort, 1, InetAddress.getByName(hostAddress))) {
				centralWriter.println("check");
				try (Socket centralCheckSocket = hostSocket.accept();
					 BufferedReader hostReader = new BufferedReader(new InputStreamReader(centralCheckSocket.getInputStream()));
					 PrintWriter hostWriter = new PrintWriter(centralCheckSocket.getOutputStream(), true)) {

					String codeMessage = hostReader.readLine();
					if (codeMessage == null || !codeMessage.startsWith("OK ")) {
						throw new IOException("ERROR Invalid code message");
					}

					hostWriter.println(codeMessage.substring(3).trim());
				}
			}

			String finalResponse = centralReader.readLine();
			if (!"OK".equals(finalResponse)) {
				throw new IOException(finalResponse == null ? "ERROR No final response" : finalResponse);
			}
		}
	}

	public static void main(String[] args) throws IOException {
		if (args.length != 3) {
			throw new IllegalArgumentException("Usage: Host <hostAddress> <startPort> <endPort>");
		}

		String hostAddress = args[0];
		int startPort = Integer.parseInt(args[1]);
		int endPort = Integer.parseInt(args[2]);
		register(hostAddress, startPort, endPort);
	}
}
