import com.google.gson.Gson;
import org.apache.commons.lang3.SystemUtils;
import redis.clients.jedis.Jedis;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

public class Subscriber {

    static Map<String, String> instanceStore = new WeakHashMap<>();
    static Gson gson = new Gson();
    //    static String appRoot = "/apps/";
    static String appRoot = "C:/coding/java/redis-client/build/classes/";


    public static void main(String[] args) {
        // "userid~|~cmd~|~arguments"
        String jedisHost = args.length > 0 ? args[0] : "localhost";

        senitizeContainer();
        Jedis jedis = new Jedis(jedisHost, 6379);

        while (true) {
            try {
                functionRunner(jedis);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                senitizeContainer();
            }
        }
    }

    private static void functionRunner(Jedis jedis) {
        final String msg = jedis.blpop(0, "reqqueue").get(1);
        final String[] split = msg.split("~\\|~");

        String requestId = split[0];
        String executor = split[1];
        String userid = split[2];
        String appName = split[3];
        //for function dArgs is fully qualified class name
        String dArgs = split.length > 4 ? split[4] : "";
        String arguments = split.length > 5 ? split[5] : "";

        String result = null;
        try {
            if (executor.equals("function")) {
                result = executeFunction(userid, appName, dArgs, arguments);
            } else {
                result = executApp(userid, appName, dArgs, arguments);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.rpush("output-respqueue", requestId + "~|~" + result);
        }
    }

    public static String executeFunction(String userid, String appName, String function, String args) {
        final String url = appRoot + userid + "/" + appName + "/";
        try {


            URLClassLoader loader = new URLClassLoader(new URL[]{new URL("file:" + url)});

            final Class<?> aClass = loader.loadClass(function);

            final Class<?> parameterType = aClass.getDeclaredMethods()[0].getParameterTypes()[0];
            final Object o1 = gson.fromJson(args, parameterType);

            final Function func = (Function) aClass.getDeclaredConstructors()[0].newInstance();
            final Object output = func.apply(o1);
            return gson.toJson(output);

        } catch (MalformedURLException | ClassNotFoundException |
                IllegalAccessException | InstantiationException |
                InvocationTargetException e) {
            e.printStackTrace();
        }
        return "";
    }

    //must have main method
    public static String executApp(String userid, String appName, String dArgs, String args) {
        try {

            // running application on separate vm
            String classPath = " -cp " + appRoot + userid;
            final String cmd = "java " + dArgs + classPath + " " + appName + " " + args;
            System.out.println(cmd);
            Process process = Runtime.getRuntime().exec(cmd);

            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitVal = process.waitFor();
            if (exitVal == 0) {
                return output.toString();
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return "error";
    }

    private static void senitizeContainer() {
        //delete tmp var/logs
        if (SystemUtils.IS_OS_WINDOWS) return;

        String cmd = "rm -rf /tmp/*";
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

