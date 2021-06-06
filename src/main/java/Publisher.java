import redis.clients.jedis.Jedis;

import java.util.Date;
import java.util.UUID;

public class Publisher {
    private static Jedis jedis = new Jedis("localhost", 6379);

    public static void main(String[] args) {

        String requestId = UUID.randomUUID().toString();
        String executor = "function";
        String user = "java";
        String appName = "main";
        String dArgs = "test.TestFunction";
        String argument = "Manish";

//        String response = executeInContainer(requestId, executor, user, appName, dArgs, argument);
//        System.out.println(response);

        for (int i = 0; i < 24; i++) {
            Date start = new Date();
            String response = executeInContainer(requestId, executor, user, appName, dArgs, argument);
            System.out.println(response);
            System.out.println(new Date().getTime() - start.getTime());
        }
    }

    private static String executeInContainer(String requestId, String executor, String user, String function, String dArgs, String argument) {
        // "userid~|~cmd~|~arguments"
        String commandString = requestId + "~|~" + executor + "~|~" + user + "~|~"
                + function + "~|~" + dArgs + "~|~" + argument;

        jedis.rpush("reqqueue", commandString);
        //TODO: queuename should be user-function-respqueue
        return jedis.blpop(0, "output-respqueue").get(1);
    }
}
