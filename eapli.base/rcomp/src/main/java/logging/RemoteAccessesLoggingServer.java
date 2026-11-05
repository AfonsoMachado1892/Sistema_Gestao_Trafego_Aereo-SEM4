package logging;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Remote Accesses Logging Server (US90).
 *
 * Receives UDP datagrams describing remote access events
 * (login success/failure, logout, disconnect) sent by the
 * remote-access services (US44, US78, US86).
 *
 * Stores:
 *  - a chronological list of all events (for US91 "last recorded events" page)
 *  - a map of currently active users, derived from LOGIN_SUCCESS / LOGOUT / DISCONNECT
 *    (for US91 "remote users currently active" page)
 *
 * The HTTP server (US91, see {@link RemoteAccessesHttpServer}) reads from
 * this class's in-memory state and is started alongside this UDP listener
 * in {@link #main(String[])}.
 */
public class
RemoteAccessesLoggingServer implements Runnable {

    public static final int DEFAULT_PORT = 9001;

    private final int port;
    private DatagramSocket socket;
    private volatile boolean running = false;

    // All received events, most recent last
    private final List<AccessEvent> events = new ArrayList<>();

    // Active sessions keyed by "username@clientIp:clientPort"
    private final Map<String, AccessEvent> activeUsers = new LinkedHashMap<>();

    public RemoteAccessesLoggingServer() {
        this(DEFAULT_PORT);
    }

    public RemoteAccessesLoggingServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            socket = new DatagramSocket(port);
            running = true;
            System.out.println("Remote Accesses Logging Server listening on UDP port " + port);

            byte[] buffer = new byte[1024];
            while (running) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                handleMessage(message);
            }
        } catch (IOException e) {
            if (running) {
                System.out.println("Remote Accesses Logging Server error: " + e.getMessage());
            }
        }
    }

    public void stop() {
        running = false;
        if (socket != null) socket.close();
    }

    private void handleMessage(String message) {
        try {
            AccessEvent event = AccessEvent.parse(message);

            synchronized (this) {
                events.add(event);

                String key = event.username + "@" + event.clientIp + ":" + event.clientPort;
                switch (event.eventType) {
                    case "LOGIN_SUCCESS":
                        activeUsers.put(key, event);
                        break;
                    case "LOGOUT":
                    case "DISCONNECT":
                        activeUsers.remove(key);
                        break;
                    default:
                        // LOGIN_FAILED and other events don't change active user list
                        break;
                }
            }

            System.out.println("[RemoteAccessesLoggingServer] " + event);

        } catch (IllegalArgumentException e) {
            System.out.println("[RemoteAccessesLoggingServer] Invalid message received: " + message);
        }
    }

    /** Returns a snapshot copy of all received events (most recent last). */
    public synchronized List<AccessEvent> getEvents() {
        return new ArrayList<>(events);
    }

    /** Returns a snapshot copy of the currently active users. */
    public synchronized List<AccessEvent> getActiveUsers() {
        return new ArrayList<>(activeUsers.values());
    }

    /**
     * Represents a single remote access event received from a client service.
     * Message format: timestamp|username|clientIp|clientPort|service|eventType
     */
    public static class AccessEvent {
        private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        public final LocalDateTime timestamp;
        public final String username;
        public final String clientIp;
        public final int clientPort;
        public final String service;
        public final String eventType;

        public AccessEvent(LocalDateTime timestamp, String username, String clientIp,
                           int clientPort, String service, String eventType) {
            this.timestamp = timestamp;
            this.username = username;
            this.clientIp = clientIp;
            this.clientPort = clientPort;
            this.service = service;
            this.eventType = eventType;
        }

        public static AccessEvent parse(String message) {
            String[] parts = message.split("\\|");
            if (parts.length != 6) {
                throw new IllegalArgumentException("Expected 6 fields, got " + parts.length);
            }

            LocalDateTime timestamp = LocalDateTime.parse(parts[0], TIMESTAMP_FORMAT);
            String username = parts[1];
            String clientIp = parts[2];
            int clientPort = Integer.parseInt(parts[3]);
            String service = parts[4];
            String eventType = parts[5];

            return new AccessEvent(timestamp, username, clientIp, clientPort, service, eventType);
        }

        @Override
        public String toString() {
            return timestamp.format(TIMESTAMP_FORMAT) + " | " + username + " | "
                    + clientIp + ":" + clientPort + " | " + service + " | " + eventType;
        }
    }

    /**
     * Starts both the UDP listener (US90) and the HTTP status server (US91).
     *
     * Usage: RemoteAccessesLoggingServer [udpPort] [httpPort]
     */
    public static void main(String[] args) {
        int udpPort = DEFAULT_PORT;
        int httpPort = RemoteAccessesHttpServer.DEFAULT_PORT;

        if (args.length > 0) {
            udpPort = Integer.parseInt(args[0]);
        }
        if (args.length > 1) {
            httpPort = Integer.parseInt(args[1]);
        }

        RemoteAccessesLoggingServer udpServer = new RemoteAccessesLoggingServer(udpPort);
        RemoteAccessesHttpServer httpServer = new RemoteAccessesHttpServer(udpServer, httpPort);

        Thread httpThread = new Thread(httpServer, "RemoteAccessesHttpServer");
        httpThread.setDaemon(true);
        httpThread.start();

        // Blocks the main thread receiving UDP datagrams (US90).
        udpServer.run();
    }
}