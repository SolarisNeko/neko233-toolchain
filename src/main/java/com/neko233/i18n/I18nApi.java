package com.neko233.i18n;

import com.neko233.common.base.MapUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * i18n is a full text kv struct
 */
public interface I18nApi {


    /**
     * 加载 i18n 数据
     *
     * @param i18nName example: en, cn
     * @return Properties KV
     */
    default Properties load(String i18nName) {
        return load(i18nName, null);
    }

    Properties load(String i18nName, String fullPathName);


    Properties getAll();


    /**
     * get value from i18n.key
     */
    default String get(String key) {
        Map<String, String> map = get(Collections.singleton(key));
        if (MapUtils.isEmpty(map)) {
            return null;
        }
        return map.get(key);
    }


    Map<String, String> get(Collection<String> keys);


    default boolean set(String key, String value) {
        return set(Collections.singletonMap(key, value));
    }


    boolean set(Map<String, String> kvMap);


}
