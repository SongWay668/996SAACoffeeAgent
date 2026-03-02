package com.example.tool;


import com.example.entity.Product;
import com.example.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CoffeeConsultMcpTools {

    private final ProductService productService;

    public CoffeeConsultMcpTools(ProductService productService){
        this.productService = productService;

    }

    @McpTool(name="consult_get_products", description = "获取海风咖啡店所有可用产品的完整列表，包括产品名称、详细描述、当前价格和库存数量。帮助用户了解可选择的咖啡产品。")
    public String consultGetProducts(){
        try{
            List<Product> productList = productService.getAll();
            if(productList.isEmpty()){
                return "当前没有任何可用产品。";
            }
            StringBuilder result = new StringBuilder(
                    "海风咖啡店可用商品列表:\n"
            );
            for(Product product:productList){
                result.append(String.format("- %s: %s, 价格: %.2f元, 库存: %d件\n",
                        product.getName(),product.getDescription(),
                        product.getPrice(),product.getStock()));
            }
            return result.toString();
        }catch (Exception e) {
            return "获取产品列表失败: " + e.getMessage();
        }
    }

    @McpTool(name="consult_get_product_info", description = "获取指定产品的详细信息，包括产品描述、价格和当前库存状态。帮助用户了解产品的具体信息。")
    public String consultGetProductInfo(@McpToolParam(description = "产品名称,必须是海风咖啡店的现有产品,如：美式咖啡，冰镇西瓜咖啡，冷萃咖啡，肉桂拿铁") String productName){
        try{
            Product product = productService.getProductByName(productName);
            if (product == null) {
                return "产品不存在或已下架: " + productName;
            }
            return String.format("产品信息:\n名称: %s\n描述: %s\n价格: %.2f元\n库存: %d件\n保质期: %d分钟\n制作时间: %d分钟",
                    product.getName(), product.getDescription(), product.getPrice(),
                    product.getStock(), product.getShelfTime(), product.getPreparationTime());
        }catch (Exception e) {
            return "获取产品信息失败: " + e.getMessage();
        }
    }

    @McpTool(name="consult_search_products", description = "根据产品名称进行模糊搜索，返回匹配的产品列表。支持部分名称搜索，例如搜索'拿铁'可以找到所有包含'拿铁'字的产品。")
    public String consultSearchProducts(@McpToolParam(description = "产品名称关键词，支持模糊匹配，例如：拿、拿铁、咖啡等") String productName) {
        try {
            List<Product> products = productService.searchProductsByName(productName);
            if (products.isEmpty()) {
                return "找到匹配的产品:"+productName;
            }

            StringBuilder result = new StringBuilder("搜索结果 (\" + products.size() + \" 个产品):\\n");
            for (Product product : products) {
                result.append(String.format("- %s: %s, 价格: %.2f元, 库存: %d件\n",
                        product.getName(), product.getDescription(), product.getPrice(), product.getStock()));
            }

            return result.toString();
        } catch (Exception e) {
            return "搜索产品失败: " + e.getMessage();
        }
    }
    //非consult开头，不应该被consult client加载
    @McpTool(name="test", description = "测试是否被过滤")
    public String test(){
        return null;
    }
}
