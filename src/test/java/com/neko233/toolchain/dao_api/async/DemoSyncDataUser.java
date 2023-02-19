package com.neko233.toolchain.dao_api.async;

import com.neko233.toolchain.common.base.StringUtils233;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * @author SolarisNeko on 2023-02-05
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemoSyncDataUser implements DataUniqueId<DemoSyncDataUser> {

    private Integer id;
    private String name;

    @Override
    public Map<String, Object> daoOperateParams() {
        return new HashMap<String, Object>() {{
            put("id", id);
            put("name", name);
        }};
    }

    @Override
    public String uniqueId() {
        return StringUtils233.join(UNIQUE_ID_SPLIT, id);
    }

    @Override
    public BiFunction<DemoSyncDataUser, DemoSyncDataUser, DemoSyncDataUser> mergeDataFunction() {
        return merge();
    }

    @NotNull
    private static BiFunction<DemoSyncDataUser, DemoSyncDataUser, DemoSyncDataUser> merge() {
        return (v1, v2) -> v2;
    }
}
