package com.neko233.toolchain.common.base;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Key-Value 模板. placeholder = ${key}
 *
 * @author SolarisNeko on 2021-07-01
 **/
public class KvTemplate {

    private static final Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");


    private final String kvTemplate;
    private final Map<String, Object> originalValueKv = new HashMap<>(2, 0.8f);

    public KvTemplate(String kvTemplate) {
        this.kvTemplate = kvTemplate;
    }

    public static KvTemplate builder(String kvTemplate) {
        if (StringUtils.isBlank(kvTemplate)) {
            throw new RuntimeException("your kv template is blank !");
        }
        return new KvTemplate(kvTemplate);
    }


    public KvTemplate mergeJoin(String key, Object value, String union) {
        originalValueKv.merge(key, value, (v1, v2) -> v1 + union + v2);
        return this;
    }

    public KvTemplate put(String key, Object value) {
        originalValueKv.put(key, value);
        return this;
    }

    public KvTemplate put(Map<String, Object> kv) {
        if (kv == null) {
            return this;
        }
        kv.forEach(this::put);
        return this;
    }

    public String build() {
        return this.toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(kvTemplate);

        // high performance
        for (Map.Entry<String, Object> originalKv : originalValueKv.entrySet()) {
            String value = String.valueOf(originalKv.getValue());
            int start = builder.indexOf("${" + originalKv.getKey() + "}");
            if (start != -1) {
                int end = start + originalKv.getKey().length() + 3;
                builder.replace(start, end, value);
            }
        }
        return builder.toString();
    }


    /**
     * }
     * 使用了正则表达式 \\$\\{([^}]+)\\} 来匹配 ${name} 样式的占位符
     * <p>
     * \\$ 表示匹配 $ 字符
     * \{ 和 \} 表示匹配 { 和 } 字符
     * [^}]+ 表示匹配任意不是 } 的字符，这里使用了非贪婪模式，即 + 后面加上 ?，表示匹配尽可能少的字符，避免出现匹配多个占位符的情况。
     * <br>
     * Hello, ${name}! Welcome to ${city}. -> name, city
     *
     * @param template 输入文本
     * @return 占位符
     */
    public static List<String> parsePlaceHolder(String template) {
        List<String> placeholders = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(template);

        while (matcher.find()) {
            String placeholder = matcher.group(1);
            placeholders.add(placeholder);
        }

        return placeholders;
    }

    public static List<String> parsePlaceHolderAndIndex(String template) {
        List<String> placeholders = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(template);

        while (matcher.find()) {
            String placeholder = matcher.group(1);
            placeholders.add(placeholder);
        }

        return placeholders;
    }

    public static List<Integer> getPlaceholderIndexes(String text, String placeholder) {
        List<Integer> indexes = new ArrayList<>();

        // 构建正则表达式
        String regex = "\\$\\{" + Pattern.quote(placeholder) + "\\}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        // 查找并记录所有占位符的位置
        while (matcher.find()) {
            indexes.add(matcher.start());
        }

        return indexes;
    }

    public static Map<String, List<Integer>> find(String template) {
        Map<String, List<Integer>> result = new HashMap<>();

        Matcher matcher = pattern.matcher(template);
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            int index = matcher.start(0);

            List<Integer> positions = result.getOrDefault(placeholder, new ArrayList<>());
            positions.add(index);
            result.put(placeholder, positions);
        }

        return result;
    }
}
