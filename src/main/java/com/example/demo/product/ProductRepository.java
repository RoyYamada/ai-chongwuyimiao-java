package com.example.demo.product;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepository {
    private final JdbcTemplate jdbcTemplate;

    public ProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Product> mapper = (rs, i) -> {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setSkuCode(rs.getString("sku_code"));
        p.setName(rs.getString("name"));
        return p;
    };

    public List<Product> search(String keyword, int limit) {
        String k = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        return jdbcTemplate.query(
                "select id, sku_code, name from product where name ilike ? or sku_code ilike ? order by id desc limit ?",
                mapper, k, k, limit);
    }

    public Optional<Product> findByBarcode(String barcode) {
        List<Product> list = jdbcTemplate.query(
                "select p.id, p.sku_code, p.name from product p join product_barcode b on p.id=b.product_id where b.barcode=?",
                mapper, barcode);
        return list.stream().findFirst();
    }
}
