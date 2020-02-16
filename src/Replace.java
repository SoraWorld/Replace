import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Himmelt
 */
public class Replace {

    private static List<String> paths = new ArrayList<>();

    private static String[] srgNames;
    private static String[] mcpNames;

    public static void main(String args[]) {
        try {
            if (args.length == 1) {
                replace(args[0]);
            } else {
                showUsage();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void replace(String suffix) throws Exception {
        log("开始生成文件列表...");
        Runtime.getRuntime().exec("cmd /c dir /b/a-d/s > list");
        File list = new File("list");
        long last = System.currentTimeMillis();
        while (!list.exists() || System.currentTimeMillis() - last < 5000) {
            Thread.sleep(10);
            System.out.print(".");
        }
        System.out.print("\n\r");
        log("开始读取映射表...");
        loadCsv();
        log("开始读取文件...");
        loadSrc(suffix);
        log("开始转换文件...");
        int i = 0;
        for (String path : paths) {
            log("正在处理 " + path);
            replaceFile(path);
        }
        log("文件已全部处理结束...");
    }

    private static void replaceFile(String path) throws IOException {
        File file = new File(path);
        String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        String result = StringUtils.replaceEach(content, srgNames, mcpNames);
        new Thread(() -> {
            try {
                FileUtils.writeStringToFile(file, result, StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void loadCsv() throws Exception {
        File field = new File("fields.csv");
        File method = new File("methods.csv");
        FileUtils.readLines(field, StandardCharsets.UTF_8);
        List<String> srgList = new ArrayList<>();
        List<String> mcpList = new ArrayList<>();
        loadMap(FileUtils.readLines(field, StandardCharsets.UTF_8), srgList, mcpList);
        loadMap(FileUtils.readLines(method, StandardCharsets.UTF_8), srgList, mcpList);
        if (srgList.size() != mcpList.size()) {
            throw new RuntimeException("srg size not match mcp size !!!");
        }
        srgNames = srgList.toArray(new String[0]);
        mcpNames = mcpList.toArray(new String[0]);
    }

    private static void loadMap(List<String> list, List<String> srgList, List<String> mcpList) throws Exception {
        if (srgList.size() != mcpList.size()) {
            throw new RuntimeException("srg size not match mcp size !!!");
        }
        int size = list.size();
        if (size > 0 && list.get(0).contains("name,side")) {
            for (int i = 1; i < size; i++) {
                String[] columns = list.get(i).split(",");
                if (columns.length >= 2) {
                    srgList.add(columns[0]);
                    mcpList.add(columns[1]);
                }
            }
        } else {
            throw new Exception();
        }
    }

    private static void loadSrc(String suffix) throws Exception {
        File list = new File("list");
        if (!list.exists()) {
            throw new Exception();
        }
        paths = FileUtils.readLines(list, StandardCharsets.UTF_8);
        paths.removeIf(s -> !s.endsWith(suffix));
    }

    private static void showUsage() {
        log("java -jar replace.jar <suffix>");
    }

    private static void log(String message) {
        System.out.println("[Decompile] " + message);
    }
}
