package com.neko233.toolchain.wal;

import java.io.File;

/**
 * @author SolarisNeko
 * Date on 2023-01-29
 */
public abstract class DefaultWal<D> extends AbstractWal<D> {

    /**
     * @param templateFilePath 模板文件, 可不存在的
     * @param dataSchema   数据结构
     * @throws Exception 参数检查异常
     */
    public DefaultWal(File templateFilePath, Class<D> dataSchema) throws Exception {
        super(templateFilePath, dataSchema, null, null, null, null);
    }

}
