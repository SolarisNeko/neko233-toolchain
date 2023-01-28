package com.neko233.toolchain.ripplex;

import com.neko233.toolchain.ripplex.config.MeasureConfig;
import com.neko233.toolchain.ripplex.constant.AggregateType;
import com.neko233.toolchain.ripplex.pojo.RippleUserTestDto;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author SolarisNeko
 * Date on 2022-05-01
 */
public class MaxTest {

    @Test
    public void test() {
        /**
         * Data
         */
        List<RippleUserTestDto> dataList = new ArrayList<RippleUserTestDto>() {{
            add(RippleUserTestDto.builder().id(1).name("neko").job("worker").age(10).salary(1000d).build());
            add(RippleUserTestDto.builder().id(2).name("doge").job("worker").age(20).salary(2000d).build());
            add(RippleUserTestDto.builder().id(3).name("doge").job("worker").age(30).salary(1000d).build());
            add(RippleUserTestDto.builder().id(4).name("boss").job("boss").age(40).salary(666666d).build());
        }};


        // 构建 Ripple
        List<RippleUserTestDto> build = RippleX.builder()
                .data(dataList)
                .dimensionColumnNames("job")
                .excludeColumnNames("id")
                .measureConfig(MeasureConfig.builder()
                        .set("salary", AggregateType.MAX)
                )
                .returnType(RippleUserTestDto.class)
                .build();
        List<RippleUserTestDto> ripple = build.stream()
                .sorted(RippleUserTestDto::compareTo)
                .collect(Collectors.toList());

        Assert.assertEquals(Double.valueOf(2000d), ripple.get(0).getSalary());
        Assert.assertEquals(Double.valueOf(666666d), ripple.get(1).getSalary());
    }

}
