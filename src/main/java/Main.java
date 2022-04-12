import java.io.BufferedOutputStream;

public class Main {
    public static void main(String[] args) {
        var server = new Server();

        server.addHandler("GET", "/messages", new MyHandler() {
            @Override
            public void handle(Request request, BufferedOutputStream responseStream) {
                
            }
        });

        server.listen(9999);
    }
}
