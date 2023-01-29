package com.neko233.toolchain.parser.functionText;

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
    /**
     * also is a param value...
     * demo: ifNull(D1, 1) == 1
     */
    private List<String> metadata;
    private Map<String, String> kv;

}
