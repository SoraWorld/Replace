package replace;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    private static Map<String, String> fieldMap = new HashMap<>();
    private static Map<String, String> paramMap = new HashMap<>();
    private static Map<String, String> methodMap = new HashMap<>();
    private static List<String> paths = new ArrayList<>();

    public static void main(String args[]) {

        try {
            if (args.length == 1) replace(args[0]);
            else showUsage();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void replace(String suffix) throws Exception {
        Log("开始生成文件列表...");
        Runtime.getRuntime().exec("cmd /c dir /b/a-d/s > list");
        File list = new File("list");
        long last = System.currentTimeMillis();
        while (!list.exists() || System.currentTimeMillis() - last < 5000) {
            Thread.sleep(10);
            System.out.print(".");
        }
        System.out.print("\n\r");
        Log("开始读取映射表...");
        loadCsv();
        Log("开始读取文件...");
        loadSrc(suffix);
        Log("开始转换文件...");
        int i = 0;
        for (String path : paths) {
            new Thread(() -> {
                try {
                    _replace(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
            Log("已加载 [" + (++i) + "] 个文件...");
        }
        Log("文件已全部加载,等待处理结束...");
    }

    private static void _replace(String path) throws IOException {
        File file = new File(path);
        String content = FileUtils.readFileToString(file, "utf8");
        for (Map.Entry<String, String> entry : fieldMap.entrySet())
            content = content.replace(entry.getKey(), entry.getValue());
        for (Map.Entry<String, String> entry : paramMap.entrySet())
            content = content.replace(entry.getKey(), entry.getValue());
        for (Map.Entry<String, String> entry : methodMap.entrySet())
            content = content.replace(entry.getKey(), entry.getValue());
        if (file.delete()) FileUtils.writeStringToFile(file, content, "utf8");
        else Log("ERROR");
    }

    private static void loadCsv() throws Exception {
        File field = new File("fields.csv");
        File param = new File("params.csv");
        File method = new File("methods.csv");

        List<String> fields = new ArrayList<>();
        try {
            fields = FileUtils.readLines(field, "utf8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> params = FileUtils.readLines(param, "utf8");
        List<String> methods = FileUtils.readLines(method, "utf8");
        loadMap(fields, fieldMap);
        loadMap(params, paramMap);
        loadMap(methods, methodMap);
    }

    private static void loadMap(List<String> list, Map<String, String> map) throws Exception {
        int size = list.size();
        if (size > 0 && list.get(0).contains("name,side")) {
            for (int i = 1; i < size; i++) {
                String[] columns = list.get(i).split(",");
                if (columns.length >= 2) map.put(columns[0], columns[1]);
            }
        } else {
            throw new Exception();
        }
    }

    private static void loadSrc(String suffix) throws Exception {

        File list = new File("list");
        if (!list.exists()) throw new Exception();
        paths = FileUtils.readLines(list, "utf8");
        paths.removeIf(s -> !s.endsWith(suffix));
    }

    private static void showUsage() {
        Log("java -jar replace.jar <suffix>");
    }

    private static void Log(String message) {
        System.out.println("[Decompile] " + message);
    }

}