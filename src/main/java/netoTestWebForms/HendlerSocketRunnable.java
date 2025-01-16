package netoTestWebForms;

import org.apache.http.NameValuePair;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


public class HendlerSocketRunnable implements Runnable {

    public static final String GET = "GET";
    public static final String POST = "POST";

    //enctype - кодировка данных формы
    private static final String FORM_URLENCODED = "application/x-www-form-urlencoded";

    private final Socket socket;
    private final List<String> validPaths;
    private final FormsServer formsServer;
    public HendlerSocketRunnable(Socket socket, List<String> validPaths, FormsServer formsServer) {
        this.socket = socket;
        this.validPaths = validPaths;
        this.formsServer = formsServer;
    }

    @Override
    public void run() {
        final var allowedMethods = List.of(GET, POST);

        try ( final BufferedInputStream in = new BufferedInputStream((socket.getInputStream()));
              final BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            // лимит на request line + заголовки
            final var limit = 4096;

            in.mark(limit);
            final var buffer = new byte[limit];
            final var read = in.read(buffer);

            // ищем request line
            final var requestLineDelimiter = new byte[]{'\r', '\n'};
            final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
            if (requestLineEnd == -1) {
                badRequest(out);
                socket.close();
                return;
            }

            // читаем request line
            final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
            if (requestLine.length != 3) {
                badRequest(out);
                socket.close();
                return;
            }

            final var method = requestLine[0];
            if (!allowedMethods.contains(method)) {
                badRequest(out);
                socket.close();
                return;
            }
            System.out.println(method);

            final var path = requestLine[1];
            if (!path.startsWith("/")) {
                badRequest(out);
                socket.close();
                return;
            }

            System.out.println(path);

            // ищем заголовки
            final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
            final var headersStart = requestLineEnd + requestLineDelimiter.length;
            final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
            if (headersEnd == -1) {
                badRequest(out);
                socket.close();
                return;
            }

            Request request = new Request();
            request.setMethod(method);
            request.setPath(path);
            request.setQueryParams();

            // отматываем на начало буфера
            in.reset();
            // пропускаем requestLine
            in.skip(headersStart);

            final var headersBytes = in.readNBytes(headersEnd - headersStart);
            final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));
            for (String s : headers) {
                request.addHeader(s);
            }
            System.out.println(headers);

            // для GET тела нет
            if (!method.equals(GET)) { //если метод не GET то будем парсить тело заголовка
                in.skip(headersDelimiter.length);
                // вычитываем Content-Length, чтобы прочитать body
                final Optional<String> contentLength = request.extractHeader("Content-Length");
                if (contentLength.isPresent()) {
                    final int length = Integer.parseInt(contentLength.get());//получаем значение,парсим значение в int
                    final byte[] bodyBytes = in.readNBytes(length); //читаем нужное количество байт
                    final var body = new String(bodyBytes); //из этого массива байт мы создаем строку
                    request.setBody(body);
                    //Считаем параметры для Content-Type = application/x-www-form-urlencoded
                    Optional<String> contentType = request.getHeaderValue("Content-Type");
                    if (contentType.isPresent() && contentType.get().equals(FORM_URLENCODED)) {
                        System.out.println("Параметры из тела: ");
                        List<NameValuePair> bodyParams = (List<NameValuePair>) request.getBodyParams();
                        for (NameValuePair pair:
                                bodyParams) {
                            System.out.println(pair.getName() + ": " + pair.getValue());
                        }
                    }
                }
            }
            /*if (!method.equals(GET)) {
                in.skip(headersDelimiter.length);
                // вычитываем Content-Length, чтобы прочитать body
                final var contentLength = extractHeader(headers, "Content-Length");
                if (contentLength.isPresent()) {
                    final var length = Integer.parseInt(contentLength.get());
                    final var bodyBytes = in.readNBytes(length);

                    final var body = new String(bodyBytes);
                    System.out.println(body);
                }
            }
*/
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();
        }
     catch (
IOException e){
        e.printStackTrace();
        }
    }

    private static void badRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }
    // from google guava with modifications
    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}
