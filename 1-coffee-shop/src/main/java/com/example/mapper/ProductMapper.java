package com.example.mapper;

import com.example.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ProductMapper {
    Product selectById(@Param("id") Long id);
    List<Product> selectAll();
    List<Product> selectByStatus(@Param("status") Integer status);
    Product selectByName(@Param("name") String name);
    Product selectByNameAndStatus(@Param("name") String name, @Param("status") Integer status);
    List<Product> searchByName(@Param("keyword") String keyword);
    java.math.BigDecimal getPriceByName(@Param("name") String name);
    boolean checkStock(@Param("name") String name, @Param("quantity") Integer quantity);
    int insert(Product product);
    int update(Product product);
    int deleteById(@Param("id") Long id);
}
