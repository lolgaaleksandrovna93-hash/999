import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Простой HTTP-сервер для тестирования (System Under Test — SUT).
 */
public class SutServer {
    public static void main(String[] args) throws IOException { // Исправлена сигнатура метода: добавлены квадратные скобки []
        int port = getPortFromArgs(args); // Получаем порт из аргументов или используем дефолтный 9999

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        System.out.println("🛡️ Запуск сервера на порте " + port);

        // Регистрируем обработчик запросов
        server.createContext("/", new FormHandler());
        server.setExecutor(null); // Используем стандартный пул потоков JVM
        server.start();

        // Сервер работает бесконечно до остановки процесса (Ctrl+C)
    }

    /**
     * Возвращает номер порта из первого аргумента командной строки,
     * либо использует значение по умолчанию (9999).
     *
     * @param args массив аргументов запуска программы
     * @return номер порта
     */
    private static int getPortFromArgs(String[] args) {
        if (args.length > 0 && !args[0].isBlank()) {
            try {
                return Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("❗ Неверно указан порт: '" + args[0] + "', используется порт по умолчанию.");
            }
        }
        return 9999;
    }

    /**
     * Вложенный статический класс-обработчик запросов.
     * Отвечает HTML‑страницей с информацией о работе сервера.
     */
    static class FormHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String remoteAddr = t.getRemoteAddress().toString(); // IP клиента

            // Логируем входящий запрос
            System.out.printf(
                    "🔵 Получен запрос от %s | Метод: %s | Путь: %s%n",
                    remoteAddr, t.getRequestMethod(), t.getRequestURI()
            );

            // Формируем ответ
            String responseBody = "<h1>SUT is running</h1><p>Порт: " +
                    t.getLocalAddress().getPort() +
                    "</p>";

            // Отправляем заголовок ответа
            t.sendResponseHeaders(200, responseBody.length());

            // Отправляем тело ответа
            t.getResponseBody().write(responseBody.getBytes());
            t.getResponseBody().close();
        }
    }
}