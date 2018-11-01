import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Replace {

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
            new Thread(() -> _replace(path)).start();
            Log("已加载 [" + (++i) + "] 个文件...");
        }
        Log("文件已全部加载,等待处理结束...");
    }

    private static void _replace(String path) {
        File file = new File(path);
        List<String> lines = readLines(file);
        replaceLine(lines, fieldMap);
        replaceLine(lines, paramMap);
        replaceLine(lines, methodMap);
        if (file.delete()) writeLines(file, lines);
        else Log("ERROR");
    }

    private static void replaceLine(List<String> lines, Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            for (int i = 0; i < lines.size(); i++) {
                String text = lines.get(i);
                text = text.replace(entry.getKey(), entry.getValue());
                lines.set(i, text);
            }
        }
    }

    private static void loadCsv() throws Exception {
        File field = new File("fields.csv");
        File param = new File("params.csv");
        File method = new File("methods.csv");

        loadMap(readLines(field), fieldMap);
        loadMap(readLines(param), paramMap);
        loadMap(readLines(method), methodMap);
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
        paths = readLines(list);
        paths.removeIf(s -> !s.endsWith(suffix));
    }

    private static void showUsage() {
        Log("java -jar replace.jar <suffix>");
    }

    private static void Log(String message) {
        System.out.println("[Decompile] " + message);
    }

    private static List<String> readLines(File file) {
        List<String> list = new ArrayList<>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            reader.lines().forEach(list::add);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    private static void writeLines(File file, List<String> lines) {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
            lines.forEach(line -> {
                try {
                    writer.write(line);
                    writer.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
