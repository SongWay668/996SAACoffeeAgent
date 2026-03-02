package com.example.tool;


import com.example.service.DocumentRetrieverService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConsultTools {

    @Autowired
    private DocumentRetrieverService documentRetrieverService;

    /**
     * 知识库检索
     */
    @Tool(name="consult-search-knowledge", description = "根据用户查询内容检索海风咖啡店知识库，包括产品信息、店铺介绍、有什么、是什么等。支持模糊匹配，可以查询产品名称、描述、分类、茶底等信息。")
    public String searchKnowledge(
            @ToolParam(description = "查询内容，可以是产品名称、产品描述关键词、店铺信息关键词、有什么、是什么等，例如：美式咖啡、燕麦拿铁、品牌介绍等") String query) {
        log.info("=== ConsultTools.searchKnowledge 开始 ===");
        log.info("查询参数 - query: {}", query);
        try {
            String result = documentRetrieverService.searchKnowledge(query);
            log.info("ConsultTools.searchKnowledge 返回结果 - length: {}", result != null ? result.length() : 0);
            log.info("=== ConsultTools.searchKnowledge 结束 ===");
            return result;
        } catch (Exception e) {
            log.error("ConsultTools.searchKnowledge 异常", e);
            return "知识库检索失败: " + e.getMessage();
        }
    }
//
//    /**
//     * 用户记忆搜索 - 仅在user_id不为空时启用个性化推荐
//     */
//    @Tool(name="memory-search", description = "根据用户ID查询用户历史偏好和咨询习惯，仅在user_id不为空时启用个性化推荐。如果user_id为空，则返回提示信息说明无法进行个性化推荐。")
//    public String searchMemory(
//            @ToolParam(description = "用户ID，用于标识用户身份") String userId,
//            @ToolParam(description = "查询内容，用于搜索相关的用户记忆") String query) {
//        log.info("=== ConsultTools.searchMemory 开始 ===");
//        log.info("查询参数 - userId: {}, query: {}", userId, query);
//
//        // 判断user_id是否为空
//        if (userId == null || userId.trim().isEmpty()) {
//            log.info("user_id为空，不启用个性化推荐");
//            log.info("=== ConsultTools.searchMemory 结束 ===");
//            return "当前未提供用户ID，无法查询个性化偏好，将提供通用推荐。";
//        }
//
//        try {
//            // TODO: 需要注入MemoryService来调用实际的memory搜索
//            // 暂时返回提示信息，待后续集成MemoryService
//            log.info("user_id不为空，启用个性化推荐 - userId: {}", userId);
//            String result = "用户ID: " + userId + " 的个性化记忆（待集成MemoryService）";
//            log.info("ConsultTools.searchMemory 返回结果: {}", result);
//            log.info("=== ConsultTools.searchMemory 结束 ===");
//            return result;
//        } catch (Exception e) {
//            log.error("ConsultTools.searchMemory 异常", e);
//            return "用户记忆查询失败: " + e.getMessage();
//        }
//    }
//
//    /**
//     * 用户记忆存储 - 仅在user_id不为空时记录用户偏好
//     */
//    @Tool(name="memory-store", description = "记录用户偏好信息到记忆库，仅在user_id不为空时记录。用于保存用户在对话中表达的喜好、兴趣点、口味偏好等信息。")
//    public String storeMemory(
//            @ToolParam(description = "用户ID，用于标识用户身份") String userId,
//            @ToolParam(description = "需要记录的用户偏好内容，例如：用户喜欢甜的咖啡、不喜欢西瓜、喜欢冰咖啡等") String content) {
//        log.info("=== ConsultTools.storeMemory 开始 ===");
//        log.info("存储参数 - userId: {}, content: {}", userId, content);
//
//        // 判断user_id是否为空
//        if (userId == null || userId.trim().isEmpty()) {
//            log.info("user_id为空，不记录用户偏好");
//            log.info("=== ConsultTools.storeMemory 结束 ===");
//            return "当前未提供用户ID，跳过偏好记录。";
//        }
//
//        try {
//            // TODO: 需要注入MemoryService来调用实际的memory存储
//            // 暂时返回成功信息，待后续集成MemoryService
//            log.info("user_id不为空，记录用户偏好 - userId: {}, content: {}", userId, content);
//            String result = "成功记录用户偏好（待集成MemoryService）: " + content;
//            log.info("ConsultTools.storeMemory 返回结果: {}", result);
//            log.info("=== ConsultTools.storeMemory 结束 ===");
//            return result;
//        } catch (Exception e) {
//            log.error("ConsultTools.storeMemory 异常", e);
//            return "用户偏好记录失败: " + e.getMessage();
//        }
//    }
}
