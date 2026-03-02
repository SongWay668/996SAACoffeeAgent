package com.example.service;

import com.example.entity.Product;
import com.example.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    public Product getById(Long id) {
        return productMapper.selectById(id);
    }

    public List<Product> getAll() {
        return productMapper.selectAll();
    }

    public List<Product> getAvailable() {
        return productMapper.selectByStatus(1);
    }

    public int create(Product product) {
        return productMapper.insert(product);
    }

    public int update(Product product) {
        return productMapper.update(product);
    }

    public int delete(Long id) {
        return productMapper.deleteById(id);
    }

    public Product getProductByName(String name) {
        return productMapper.selectByName(name);
    }

    public List<Product> searchProductsByName(String keyword) {
        return productMapper.searchByName(keyword);
    }
}
