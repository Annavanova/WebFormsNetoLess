package netoTestWebForms;

import java.io.IOException;


public class Main {
    public static final String GET = "GET";
    public static final String POST = "POST";

    private static final int SERVER_SOCKET = 9999;
    //private static final int poolSizeThreads = 64;

    public static void main(String[] args) throws InterruptedException {
        FormsServer FormsServer = new FormsServer(SERVER_SOCKET);
        FormsServer.addHandler(GET, "/messages", ((request, responseStream) -> {
            try {
                FormsServer.responseWithoutContent(responseStream,"404", "Not found");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
        FormsServer.addHandler(GET, "/", ((request, responseStream) -> FormsServer.defaultHandler(responseStream, "spring.png")));

        FormsServer.addHandler(POST, "/messages", (request, responseStream) ->
                FormsServer.responseWithoutContent(responseStream, "404", "Not found"));

        FormsServer.start();

    }

}
