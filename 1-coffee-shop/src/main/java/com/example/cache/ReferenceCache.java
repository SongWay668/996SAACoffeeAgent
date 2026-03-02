package com.example.cache;

import com.example.entity.Reference;
import com.example.mapper.ReferenceMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reference 数据缓存
 * 用于缓存 reference 表数据，避免频繁查询数据库
 */
@Slf4j
@Component
public class ReferenceCache {

    @Autowired
    private ReferenceMapper referenceMapper;

    /**
     * 缓存结构：refType -> refCode -> refValue
     */
    private Map<String, Map<Integer, String>> cache = new ConcurrentHashMap<>();

    /**
     * 缓存所有 reference 数据
     * refType -> refCode -> Reference对象
     */
    private Map<String, Map<Integer, Reference>> fullCache = new ConcurrentHashMap<>();

    /**
     * 应用启动时加载缓存
     */
    @PostConstruct
    public void init() {
        loadCache();
        log.info("Reference 缓存初始化完成");
    }

    /**
     * 定时刷新缓存（每5分钟）
     */
    @Scheduled(fixedRate = 300000)
    public void refreshCache() {
        log.info("开始刷新 Reference 缓存");
        loadCache();
        log.info("Reference 缓存刷新完成");
    }

    /**
     * 手动刷新缓存
     */
    public void manualRefresh() {
        log.info("手动刷新 Reference 缓存");
        loadCache();
    }

    /**
     * 加载缓存数据
     */
    private void loadCache() {
        try {
            List<Reference> allReferences = referenceMapper.selectAll();

            Map<String, Map<Integer, String>> newCache = new ConcurrentHashMap<>();
            Map<String, Map<Integer, Reference>> newFullCache = new ConcurrentHashMap<>();

            for (Reference ref : allReferences) {
                // 构建值缓存
                newCache.computeIfAbsent(ref.getRefType(), k -> new ConcurrentHashMap<>())
                        .put(ref.getRefCode(), ref.getRefValue());

                // 构建完整对象缓存
                newFullCache.computeIfAbsent(ref.getRefType(), k -> new ConcurrentHashMap<>())
                        .put(ref.getRefCode(), ref);
            }

            this.cache = newCache;
            this.fullCache = newFullCache;

            log.debug("Reference 缓存加载完成，共加载 {} 条记录", allReferences.size());
        } catch (Exception e) {
            log.error("加载 Reference 缓存失败", e);
        }
    }

    /**
     * 获取参考值
     *
     * @param refType 参考类型（如 "feedback_type", "rating"）
     * @param refCode 参考代码（如 1, 2, 3, 4, 5）
     * @return 参考值，如果不存在返回 null
     */
    public String getRefValue(String refType, Integer refCode) {
        if (refType == null || refCode == null) {
            return null;
        }
        Map<Integer, String> typeMap = cache.get(refType);
        return typeMap != null ? typeMap.get(refCode) : null;
    }

    /**
     * 获取完整的 Reference 对象
     */
    public Reference getReference(String refType, Integer refCode) {
        if (refType == null || refCode == null) {
            return null;
        }
        Map<Integer, Reference> typeMap = fullCache.get(refType);
        return typeMap != null ? typeMap.get(refCode) : null;
    }

    /**
     * 获取指定类型的所有参考数据
     */
    public Map<Integer, String> getRefTypeData(String refType) {
        return cache.getOrDefault(refType, Collections.emptyMap());
    }

    /**
     * 获取指定类型的所有完整对象
     */
    public List<Reference> getRefTypeReferences(String refType) {
        Map<Integer, Reference> typeMap = fullCache.get(refType);
        if (typeMap == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(typeMap.values());
    }

    /**
     * 检查参考数据是否存在
     */
    public boolean exists(String refType, Integer refCode) {
        return getRefValue(refType, refCode) != null;
    }

    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("refTypeCount", cache.size());
        stats.put("totalRecords", cache.values().stream().mapToInt(Map::size).sum());
        stats.put("refTypes", new ArrayList<>(cache.keySet()));
        return stats;
    }
}
