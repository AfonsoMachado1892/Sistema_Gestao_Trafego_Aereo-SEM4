package logging;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * HTTP server for the Remote Accesses Logging Server (US91).
 *
 * Exposes two human-readable status pages and the JSON endpoints that
 * feed them via AJAX (the pages poll periodically and refresh their
 * table without a full page reload):
 *
 *   GET /            - index, links to the two pages below
 *   GET /events      - last recorded remote-access events (US91)
 *   GET /active      - remote users currently active (US91)
 *   GET /api/events  - JSON array with the last recorded events
 *   GET /api/active  - JSON array with the currently active users
 *
 * Reads its data from {@link RemoteAccessesLoggingServer}, which is fed
 * by the UDP listener (US90).
 */
public class RemoteAccessesHttpServer implements Runnable {

    public static final int DEFAULT_PORT = 9091;

    /** Maximum number of events returned by /api/events (most recent first). */
    private static final int MAX_EVENTS = 100;

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final RemoteAccessesLoggingServer loggingServer;
    private final int port;
    private HttpServer httpServer;

    public RemoteAccessesHttpServer(RemoteAccessesLoggingServer loggingServer) {
        this(loggingServer, DEFAULT_PORT);
    }

    public RemoteAccessesHttpServer(RemoteAccessesLoggingServer loggingServer, int port) {
        this.loggingServer = loggingServer;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(port), 0);

            httpServer.createContext("/", this::handleIndex);
            httpServer.createContext("/events", this::handleEventsPage);
            httpServer.createContext("/active", this::handleActivePage);
            httpServer.createContext("/api/events", this::handleEventsApi);
            httpServer.createContext("/api/active", this::handleActiveApi);

            httpServer.setExecutor(null); // default executor
            httpServer.start();

            System.out.println("Remote Accesses Logging HTTP Server listening on port " + port);
        } catch (IOException e) {
            System.out.println("Remote Accesses Logging HTTP Server error: " + e.getMessage());
        }
    }

    public void stop() {
        if (httpServer != null) {
            httpServer.stop(0);
        }
    }

    // ------------------------------------------------------------
    // Page handlers
    // ------------------------------------------------------------

    private void handleIndex(HttpExchange exchange) throws IOException {
        if (!"/".equals(exchange.getRequestURI().getPath())) {
            sendNotFound(exchange);
            return;
        }

        String html = page("Remote Accesses Logging Server",
                "<h1>Remote Accesses Logging Server</h1>"
                        + "<p>US90 (UDP logging) / US91 (visualization)</p>"
                        + "<ul>"
                        + "<li><a href='/events'>Last recorded events</a></li>"
                        + "<li><a href='/active'>Remote users currently active</a></li>"
                        + "</ul>",
                null);

        sendHtml(exchange, html);
    }

    private void handleEventsPage(HttpExchange exchange) throws IOException {
        String body = "<h1>Last Recorded Events</h1>"
                + nav("/events")
                + table("No events recorded yet.");

        sendHtml(exchange, page("Last Recorded Events", body, "/api/events"));
    }

    private void handleActivePage(HttpExchange exchange) throws IOException {
        String body = "<h1>Remote Users Currently Active</h1>"
                + nav("/active")
                + table("No active remote users.");

        sendHtml(exchange, page("Active Remote Users", body, "/api/active"));
    }

    private String nav(String current) {
        StringBuilder sb = new StringBuilder("<p>");
        sb.append("<a href='/'>Home</a> | ");
        sb.append("/events".equals(current)
                ? "Last recorded events"
                : "<a href='/events'>Last recorded events</a>");
        sb.append(" | ");
        sb.append("/active".equals(current)
                ? "Active users"
                : "<a href='/active'>Active users</a>");
        sb.append("</p>");
        return sb.toString();
    }

    private String table(String emptyMessage) {
        return "<table id='data-table'>"
                + "<thead><tr>"
                + "<th>Timestamp</th><th>Username</th><th>Client IP</th>"
                + "<th>Client Port</th><th>Service</th><th>Event</th>"
                + "</tr></thead>"
                + "<tbody><tr><td colspan='6' class='empty'>" + emptyMessage + "</td></tr></tbody>"
                + "</table>"
                + "<p class='updated'>Last update: <span id='last-update'>-</span></p>";
    }

    // ------------------------------------------------------------
    // JSON API handlers
    // ------------------------------------------------------------

    private void handleEventsApi(HttpExchange exchange) throws IOException {
        List<RemoteAccessesLoggingServer.AccessEvent> events = loggingServer.getEvents();

        // Most recent first, capped at MAX_EVENTS.
        List<RemoteAccessesLoggingServer.AccessEvent> ordered = new ArrayList<>(events);
        Collections.reverse(ordered);
        if (ordered.size() > MAX_EVENTS) {
            ordered = ordered.subList(0, MAX_EVENTS);
        }

        sendJson(exchange, toJsonArray(ordered));
    }

    private void handleActiveApi(HttpExchange exchange) throws IOException {
        List<RemoteAccessesLoggingServer.AccessEvent> active = loggingServer.getActiveUsers();
        sendJson(exchange, toJsonArray(active));
    }

    private String toJsonArray(List<RemoteAccessesLoggingServer.AccessEvent> events) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < events.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(toJson(events.get(i)));
        }
        sb.append(']');
        return sb.toString();
    }

    private String toJson(RemoteAccessesLoggingServer.AccessEvent e) {
        return "{"
                + "\"timestamp\":\"" + e.timestamp.format(TIMESTAMP_FORMAT) + "\","
                + "\"username\":\"" + escapeJson(e.username) + "\","
                + "\"clientIp\":\"" + escapeJson(e.clientIp) + "\","
                + "\"clientPort\":" + e.clientPort + ","
                + "\"service\":\"" + escapeJson(e.service) + "\","
                + "\"eventType\":\"" + escapeJson(e.eventType) + "\""
                + "}";
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // ------------------------------------------------------------
    // HTML page template + shared AJAX script
    // ------------------------------------------------------------

    private String page(String title, String bodyHtml, String apiUrl) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang='en'><head>");
        html.append("<meta charset='utf-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1'>");
        html.append("<title>").append(title).append("</title>");
        html.append(style());
        html.append("</head><body>");
        html.append("<div class='container'>");
        html.append(bodyHtml);
        html.append("</div>");
        if (apiUrl != null) {
            html.append(ajaxScript(apiUrl));
        }
        html.append("</body></html>");
        return html.toString();
    }

    private String style() {
        return "<style>"
                + "body{font-family:Arial,Helvetica,sans-serif;background:#f4f6f8;color:#222;margin:0;padding:20px;}"
                + ".container{max-width:900px;margin:0 auto;}"
                + "h1{color:#1f3a5f;}"
                + "table{width:100%;border-collapse:collapse;background:#fff;box-shadow:0 1px 3px rgba(0,0,0,.1);}"
                + "th,td{padding:8px 10px;border-bottom:1px solid #e0e0e0;text-align:left;font-size:14px;}"
                + "th{background:#1f3a5f;color:#fff;}"
                + "tr:nth-child(even) td{background:#fafafa;}"
                + ".empty{text-align:center;color:#888;}"
                + ".updated{color:#888;font-size:12px;}"
                + "a{color:#1f3a5f;}"
                + "</style>";
    }

    /**
     * Generic AJAX polling script: fetches {@code apiUrl} every 2 seconds
     * (without reloading the page) and rebuilds the table body with the
     * returned JSON array of events.
     */
    private String ajaxScript(String apiUrl) {
        return "<script>"
                + "function refresh(){"
                + "fetch('" + apiUrl + "').then(function(r){return r.json();}).then(function(data){"
                + "var tbody=document.querySelector('#data-table tbody');"
                + "if(!data || data.length===0){"
                + "tbody.innerHTML='<tr><td colspan=\"6\" class=\"empty\">No data</td></tr>';"
                + "}else{"
                + "var html='';"
                + "data.forEach(function(e){"
                + "html+='<tr>'"
                + "+'<td>'+e.timestamp+'</td>'"
                + "+'<td>'+e.username+'</td>'"
                + "+'<td>'+e.clientIp+'</td>'"
                + "+'<td>'+e.clientPort+'</td>'"
                + "+'<td>'+e.service+'</td>'"
                + "+'<td>'+e.eventType+'</td>'"
                + "+'</tr>';"
                + "});"
                + "tbody.innerHTML=html;"
                + "}"
                + "document.getElementById('last-update').textContent=new Date().toLocaleTimeString();"
                + "}).catch(function(err){console.error('refresh failed',err);});"
                + "}"
                + "refresh();"
                + "setInterval(refresh,2000);"
                + "</script>";
    }

    // ------------------------------------------------------------
    // Low-level response helpers
    // ------------------------------------------------------------

    private void sendHtml(HttpExchange exchange, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendJson(HttpExchange exchange, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendNotFound(HttpExchange exchange) throws IOException {
        byte[] bytes = "Not found".getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(404, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
