import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

public class SutServer {
    public static void main(String[] args) throws IOException {
        int port = getPortFromArgs(args);

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new FormHandler());
        server.setExecutor(null);

        // Запускаем сервер (неблокирующий вызов)
        server.start();
        System.out.println("SUT Server starting on port: " + port);

        // Ждем, пока порт физически не откроется
        waitForPortToBeReady(port);

        System.out.println("SUT Server is running (CI mode).");

        // Бесконечный цикл, чтобы процесс не завершился
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

    private static void waitForPortToBeReady(int port) {
        int maxTries = 30;
        int tryCount = 0;
        while (tryCount < maxTries) {
            try {
                // Пытаемся создать сокет, подключенный к самому себе
                java.net.Socket socket = new java.net.Socket("localhost", port);
                socket.close();
                // Если подключение удалось — выходим из цикла
                return;
            } catch (IOException e) {
                // Порт еще закрыт, ждем
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
                tryCount++;
            }
        }
        throw new RuntimeException("Port " + port + " did not open in time.");
    }

    private static int getPortFromArgs(String[] args) {
        if (args != null && args.length > 0) {
            String portArg = args[0];
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
