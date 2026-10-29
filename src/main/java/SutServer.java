import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class SutServer {
    public static void main(String args) throws IOException {
        int port = getPortFromArgs(args);

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        System.out.println("SUT Server starting on port: " + port);

        server.createContext("/", new FormHandler());
        server.setExecutor(null);
        server.start();

        System.out.println("SUT Server is running. Waiting for shutdown signal...");

        // Бесконечный цикл, чтобы процесс не завершился сразу после server.start()
        while (true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                System.out.println("Shutdown signal received.");
                break;
            }
        }

        server.stop(0);
    }

    private static int getPortFromArgs(String args) {
        // Сначала проверяем, что массив не пустой
        if (args != null && args.length > 0) {
            String portArg = args;
            // Проверяем, что строка не пустая и не состоит только из пробелов
            if (!portArg.isBlank()) {
                try {
                    return Integer.parseInt(portArg);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid port specified: '" + portArg + "', using default port.");
                }
            }
        }
        return 9999;
    }

    static class FormHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String remoteAddr = t.getRemoteAddress().toString();

            System.out.println("Request received from: " + remoteAddr +
                    " | Method: " + t.getRequestMethod() +
                    " | Path: " + t.getRequestURI());

            String responseBody = "<h1>SUT is running</h1><p>Port: " +
                    t.getLocalAddress().getPort() +
                    "</p>";

            t.sendResponseHeaders(200, responseBody.length());
            t.getResponseBody().write(responseBody.getBytes());
            t.getResponseBody().close();
        }
    }
}

