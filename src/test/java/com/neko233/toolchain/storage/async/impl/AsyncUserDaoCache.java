package com.neko233.toolchain.storage.async.impl;

import com.alibaba.fastjson2.JSON;
import com.neko233.mock.db.DataSourceMock;
import com.neko233.sql.lightrail.orm.OrmHandler;
import com.neko233.toolchain.storage.async.AbstractCacheDataSyncDao;
import com.neko233.toolchain.storage.async.DataSyncScheduleParamDto;
import com.neko233.toolchain.storage.async.DataUniqueId;
import com.neko233.toolchain.storage.async.DemoSyncDataUser;
import com.neko233.toolchain.common.base.CollectionUtils233;
import com.neko233.toolchain.common.base.KvTemplate233;
import com.neko233.toolchain.idGenerator.snowflake.IdGeneratorBySnowflake;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author SolarisNeko
 * Date on 2022-12-16
 */
@Slf4j
public class AsyncUserDaoCache extends AbstractCacheDataSyncDao<DemoSyncDataUser> {

    private static final DataSource dao = DataSourceMock.createDataSource();
    private static final IdGeneratorBySnowflake idGeneratorBySnowflake = new IdGeneratorBySnowflake("demo", 1, 1);


    public static final String selectSql = "Select * From test_sync_user Where id = ${id} ";
    public static final String selectAllSql = "Select * From test_sync_user  ";

    public static final String upsertSql = "insert into test_sync_user(id, name)\n" +
            "values (${id}, '${name}') " +
            "on duplicate key update name = '${name}'";


    public AsyncUserDaoCache(int mqMaxSize) {
        super(mqMaxSize, null);
    }

    public AsyncUserDaoCache(int mqMaxSize, DataSyncScheduleParamDto dataSyncScheduleParamDto) {
        super(mqMaxSize, dataSyncScheduleParamDto);
    }

    @Override
    public DemoSyncDataUser getByUniqueId(DataUniqueId<DemoSyncDataUser> uniqueId) {
        DemoSyncDataUser asyncWriteData = getAsyncWriteData(uniqueId);
        if (asyncWriteData != null) {
            return asyncWriteData;
        }

        Map<String, Object> params = uniqueId.daoOperateParams();
        String id = String.valueOf(params.get("id"));

        try (Connection c = dao.getConnection()) {
            String sql = KvTemplate233.builder(selectSql)
                    .put("id", id)
                    .build();
            PreparedStatement ps = c.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            List<DemoSyncDataUser> selectDataList = OrmHandler.ormBatch(rs, DemoSyncDataUser.class);
            if (CollectionUtils233.isEmpty(selectDataList)) {
                return null;
            }
            return selectDataList.get(0);
        } catch (Exception e) {
            log.error("dao query error", e);
        }

        return null;
    }

    @Override
    public List<DemoSyncDataUser> getAll() {
        try (Connection c = dao.getConnection()) {
            PreparedStatement ps = c.prepareStatement(selectAllSql);
            ResultSet rs = ps.executeQuery();
            List<DemoSyncDataUser> selectDataList = OrmHandler.ormBatch(rs, DemoSyncDataUser.class);

            // add async new
            List<DemoSyncDataUser> allAsyncWriteData = getAllAsyncWriteData();
            selectDataList.addAll(allAsyncWriteData);

            Collection<DemoSyncDataUser> userList = selectDataList.stream()
                    .collect(Collectors.toMap(DemoSyncDataUser::uniqueId, v -> v, (v1, v2) -> mergeNewData().apply(v1, v2)))
                    .values();
            return new ArrayList<>(userList);
        } catch (Exception e) {
            log.error("dao query error", e);
        }

        return new ArrayList<>();
    }

    @Override
    public boolean insertOrUpdate(List<DemoSyncDataUser> dataList) {
        // 可以引入一个 独立的 Merge 机制

        String sql;
        try (Connection c = dao.getConnection()) {
            for (DemoSyncDataUser data : dataList) {
                if (data == null) {
                    continue;
                }
                sql = KvTemplate233.builder(upsertSql)
                        .put("id", String.valueOf(Optional.ofNullable(data.getId()).orElse(idGeneratorBySnowflake.nextId().intValue())))
                        .put("name", data.getName())
                        .build();
                PreparedStatement ps = c.prepareStatement(sql);
                boolean isOk = ps.executeUpdate() > 0;
                if (!isOk) {
                    log.error("execute not ok. sql = {} | data = {}", sql, JSON.toJSONString(data));
                }
            }
            return true;
        } catch (Exception e) {
            log.error("dao happen error", e);
            return false;
        }
    }

    @Override
    public BiFunction<DemoSyncDataUser, DemoSyncDataUser, DemoSyncDataUser> mergeNewData() {
        return (v1, v2) -> v2;
    }
}
