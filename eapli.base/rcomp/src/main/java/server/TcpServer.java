package server;

import server.atcc.AirTransportCompanyCollaboratorClientHandler;
import server.pilot.PilotClientHandler;
import server.ClientHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Single TCP entry point for all remote-access services (US44, US78, US86).
 *
 * Protocol:
 *  1. Client connects.
 *  2. Client sends a service tag as the first UTF string: "US44", "US78" or "US86".
 *  3. Server replies "OK: <service>" if the tag is recognized, "ERROR: Unknown service" otherwise.
 *  4. The connection is then handed off to the corresponding ClientHandler,
 *     which proceeds with authentication and the service-specific command loop.
 */
public class TcpServer implements Runnable {
    private static final int PORT = 9999;
    private ServerSocket serverSocket;

    public void run() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("TCP Server listening on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection from: " + clientSocket.getInetAddress().getHostAddress());
                Thread t = new Thread(() -> dispatch(clientSocket));
                t.start();
            }

        } catch (IOException e) {
            System.out.println("TCP Server error: " + e.getMessage());
        }
    }

    /**
     * Reads the service tag sent by the client and hands off the socket
     * to the appropriate Runnable handler. Connections with an unknown
     * tag are rejected and closed.
     */
    private void dispatch(Socket clientSocket) {
        try {
            DataInputStream sIn = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream sOut = new DataOutputStream(clientSocket.getOutputStream());

            String serviceTag = sIn.readUTF();

            Runnable handler;
            switch (serviceTag) {
                case "US44":
                    sOut.writeUTF("OK: US44");
                    handler = new ClientHandler(clientSocket);
                    break;
                case "US86":
                    sOut.writeUTF("OK: US86");
                    handler = new PilotClientHandler(clientSocket);
                    break;
                case "US78":
                    sOut.writeUTF("OK: US78");
                    handler = new AirTransportCompanyCollaboratorClientHandler(clientSocket);
                    break;
                default:
                    sOut.writeUTF("ERROR: Unknown service");
                    clientSocket.close();
                    return;
            }

            handler.run();

        } catch (IOException e) {
            System.out.println("Dispatch error: " + e.getMessage());
            try {
                clientSocket.close();
            } catch (IOException ignored) {}
        }
    }

    public void stop() {
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            System.out.println("Error stopping server: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new TcpServer().run();
    }
}