package com.example.config;

import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.McpConnectionInfo;
import org.springframework.ai.mcp.McpToolFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConsultMcpToolFilter implements McpToolFilter {
    @Override
    public boolean test(McpConnectionInfo mcpConnectionInfo, McpSchema.Tool tool) {
        String serverName = mcpConnectionInfo.initializeResult().serverInfo().name();
        String serverVersion = mcpConnectionInfo.initializeResult().serverInfo().version();
        String toolName = tool.name();
        log.info("MCP server name ::{}",serverName);
        log.info("MCP serverVersion ::{}",serverVersion);
        log.info("MCP toolName ::{}",toolName);

        //过滤掉consult-get-products工具，强制使用本地工具consult-search-knowledge
//        if (toolName.startsWith("consult-get-products")) {
//            log.warn("过滤掉MCP工具: {} (请使用本地工具 consult-search-knowledge)", toolName);
//            return false;
//        }
//        return true;
        //只调用consult开头的工具（排除get-products）和memory开头的工具
         return toolName.startsWith("consult") || toolName.startsWith("memory");

//        //生产环境 - 只允许稳定版本的工具，排除实验性工具
//        if (isProductionEnvironment(serverName, serverVersion)) {
//            // 排除所有包含 "experimental" 的工具
//            if (tool.description() != null && tool.description().toLowerCase().contains("experimental")) {
//                return false;
//            }
//
//            // 生产环境只允许时间相关工具
//            return toolName.startsWith("get");
//        }
//
//        //开发环境 - 允许所有工具
//        if (isDevelopmentEnvironment(serverName)) {
//            return true;
//        }
//
//        //测试环境 - 只允许特定测试工具
//        if (isTestEnvironment(serverName)) {
//            return toolName.startsWith("consult");
//        }
//
//        // 场景4: 根据服务器名称过滤 - 只接受特定服务器的工具
//        if (serverName.equals("streamable-mcp-server")) {
//            return true;
//        }
//
//        // 默认情况：拒绝其他工具
//        return false;
    }

    /**
     * 判断是否为生产环境
     */
    private boolean isProductionEnvironment(String serverName, String serverVersion) {
        // 生产环境：服务器名称包含 "prod" 或版本号以 "1." 开头（稳定版本）
        return serverName.toLowerCase().contains("prod") ||
               (serverVersion.startsWith("1.") && !serverVersion.contains("beta"));
    }

    /**
     * 判断是否为开发环境
     */
    private boolean isDevelopmentEnvironment(String serverName) {
        // 开发环境：服务器名称包含 "dev" 或 "develop"
        return serverName.toLowerCase().contains("dev");
    }

    /**
     * 判断是否为测试环境
     */
    private boolean isTestEnvironment(String serverName) {
        // 测试环境：服务器名称包含 "test"
        return serverName.toLowerCase().contains("test");
    }
}
