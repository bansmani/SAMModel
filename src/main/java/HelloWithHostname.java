import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

public class HelloWithHostname {

    public static void main(String[] args) {
        for (int i = 0; i < 5; i++) {
            extracted();
        }
    }

    private static void extracted() {
        Date start = new Date();
        try {
            String hostname = new BufferedReader(
                    new InputStreamReader(Runtime.getRuntime().exec("hostname").getInputStream()))
                    .readLine();
            System.out.println("hello from " + hostname);

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(new Date().getTime() - start.getTime());
    }
}
