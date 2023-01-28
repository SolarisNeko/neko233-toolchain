package com.neko233.toolchain.common.parser.functionText;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FunctionText {

    private String functionName;
    private List<String> metadata;
    private Map<String, String> kv;

    public static void main(String[] args) {
        new FunctionText();
    }

}
