package test;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class TestFunction implements Function<String, Map<String, String>> {
    @Override
    public Map<String, String> apply(String s)
    {
        Map<String, String> map = new HashMap<>();
        map.put("name", s);
        map.put("date", new Date().toString());
        return map;
    }
}
