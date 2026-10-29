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

        System.out.println("SUT Server starting on port: " + port);

        server.createContext("/", new FormHandler());
        server.setExecutor(null);
        server.start();

        System.out.println("SUT Server is running. Press Enter to stop...");

        // Ожидание нажатия Enter для корректного завершения (удобно для локальной разработки)
        // В CI-окружении (GitHub Actions) консоли нет, процесс будет убит командой taskkill.
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            reader.readLine();
        } catch (IOException ignored) {
            // Игнорируем ошибку, если поток ввода недоступен (например, в некоторых IDE или CI)
        }

        System.out.println("Shutdown signal received.");
        server.stop(0);
    }

    private static int getPortFromArgs(String[] args) {
        // Сначала проверяем, что массив не пустой
        if (args != null && args.length > 0) {
            String portArg = args[0]; // Берем первый аргумент
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

