package com.example.service;

import com.example.cache.ReferenceCache;
import com.example.entity.Reference;
import com.example.mapper.ReferenceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class ReferenceService {

    @Autowired
    private ReferenceMapper referenceMapper;

    @Autowired
    private ReferenceCache referenceCache;

    /**
     * 根据类型获取参考数据（直接查询数据库）
     */
    public List<Reference> getByRefType(String refType) {
        return referenceMapper.selectByRefType(refType);
    }

    /**
     * 获取所有参考数据（直接查询数据库）
     */
    public List<Reference> getAll() {
        return referenceMapper.selectAll();
    }

    /**
     * 根据 refType 和 refCode 获取对应的 refValue（从缓存获取）
     * @param refType 参考类型（如 "feedback_type", "rating"）
     * @param refCode 参考代码（如 1, 2, 3, 4, 5）
     * @return 参考值（如 "产品反馈", "服务反馈", "非常满意"等）
     */
    public String getRefValue(String refType, Integer refCode) {
        return referenceCache.getRefValue(refType, refCode);
    }

    /**
     * 获取完整的 Reference 对象（从缓存获取）
     */
    public Reference getReference(String refType, Integer refCode) {
        return referenceCache.getReference(refType, refCode);
    }

    /**
     * 获取指定类型的所有参考数据（从缓存获取）
     */
    public Map<Integer, String> getRefTypeData(String refType) {
        return referenceCache.getRefTypeData(refType);
    }

    /**
     * 检查参考数据是否存在（从缓存获取）
     */
    public boolean exists(String refType, Integer refCode) {
        return referenceCache.exists(refType, refCode);
    }

    /**
     * 手动刷新缓存（用于修改 reference 表后立即生效）
     */
    public void refreshCache() {
        referenceCache.manualRefresh();
    }

    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStats() {
        return referenceCache.getCacheStats();
    }
}
