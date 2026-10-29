import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

// ВАЖНО: здесь НЕ должно быть строки package ...
public class SutServer {
    public static void main(String[] args) throws IOException {
        int port = getPortFromArgs(args);

        System.out.println("Starting SUT on port: " + port);

        HttpServer server;
        try {
            // Создаём сервер. bind произойдёт при start()
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            System.err.println("Failed to bind port " + port + ": " + e.getMessage());
            e.printStackTrace();
            return; // Завершаем сразу, чтобы CI показал ошибку
        }

        server.createContext("/", new FormHandler());
        server.setExecutor(null);
        server.start();

        System.out.println("SUT Server started and listening on port: " + port);

        // Бесконечный цикл — сервер должен жить, пока его не убьёт CI
        while (true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                System.out.println("Shutdown signal received.");
                server.stop(0);
                break;
            }
        }
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
