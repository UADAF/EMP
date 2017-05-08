package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.UUID;

public class TestClient {
    private static BufferedReader in;

    private static OutputStreamWriter out;
    static boolean isWorking = true;
    @SuppressWarnings("resource")
    public static void main(String[] args) throws Exception {
        BufferedReader c = new BufferedReader(new InputStreamReader(System.in));
        UUID id = UUID.fromString("3abd5430-065b-42c0-9020-31482ff66781");
        Socket bot = new Socket("52.48.142.75", 6666);
        in = new BufferedReader(new InputStreamReader(bot.getInputStream()));
        out = new OutputStreamWriter(bot.getOutputStream());
        System.out.println(in.readLine());
        out.write("CLBK#CONNECT:;:" + id);
        out.flush();
        new Thread(() -> {
            while (isWorking) {
                try {
                    String line = in.readLine();
                    if (line == null) {
                        isWorking = false;
                        System.out.print("Stop");
                    } else {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(0);
                }
            }
            System.exit(0);
        }).start();
        while (isWorking) {
            out.write(c.readLine());
            out.flush();
        }
    }

}