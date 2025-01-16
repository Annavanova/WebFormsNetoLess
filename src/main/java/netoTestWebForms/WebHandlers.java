package netoTestWebForms;

import org.example.Request;

import java.io.BufferedOutputStream;
import java.io.IOException;

@FunctionalInterface
public interface WebHandlers {
    void handle(Request request, BufferedOutputStream responseStream) throws IOException;
}
