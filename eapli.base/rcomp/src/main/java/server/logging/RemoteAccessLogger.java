package server.logging;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility responsible for sending remote access events to the
 * Remote Accesses Logging Server (US90) via UDP datagrams.
 *
 * Message format (simple delimited string):
 *   timestamp|username|clientIp|clientPort|service|eventType
 *
 * - timestamp: ISO_LOCAL_DATE_TIME
 * - username:  authenticated user (or "UNKNOWN" if auth failed)
 * - clientIp:  IP address of the remote client
 * - clientPort: TCP port used by the remote client
 * - service:   US44, US78 or US86
 * - eventType: LOGIN_SUCCESS, LOGIN_FAILED, LOGOUT, DISCONNECT
 */
public class RemoteAccessLogger {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final String loggingServerHost;
    private final int loggingServerPort;

    public RemoteAccessLogger(String loggingServerHost, int loggingServerPort) {
        this.loggingServerHost = loggingServerHost;
        this.loggingServerPort = loggingServerPort;
    }

    /**
     * Sends a remote access event to the logging server.
     * Any failure to send is logged locally but never thrown,
     * so logging issues never disrupt the main TCP service.
     */
    public void log(String username, String clientIp, int clientPort, String service, String eventType) {
        String message = String.join("|",
                LocalDateTime.now().format(TIMESTAMP_FORMAT),
                username == null ? "UNKNOWN" : username,
                clientIp,
                String.valueOf(clientPort),
                service,
                eventType
        );

        try (DatagramSocket socket = new DatagramSocket()) {
            byte[] data = message.getBytes(StandardCharsets.UTF_8);
            InetAddress address = InetAddress.getByName(loggingServerHost);
            DatagramPacket packet = new DatagramPacket(data, data.length, address, loggingServerPort);
            socket.send(packet);
        } catch (IOException e) {
            System.out.println("[RemoteAccessLogger] Failed to send log event: " + e.getMessage());
        }
    }

    public void loginSuccess(String username, String clientIp, int clientPort, String service) {
        log(username, clientIp, clientPort, service, "LOGIN_SUCCESS");
    }

    public void loginFailed(String username, String clientIp, int clientPort, String service) {
        log(username, clientIp, clientPort, service, "LOGIN_FAILED");
    }

    public void logout(String username, String clientIp, int clientPort, String service) {
        log(username, clientIp, clientPort, service, "LOGOUT");
    }

    public void disconnect(String username, String clientIp, int clientPort, String service) {
        log(username, clientIp, clientPort, service, "DISCONNECT");
    }
}