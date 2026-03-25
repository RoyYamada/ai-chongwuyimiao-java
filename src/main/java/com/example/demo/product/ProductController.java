package com.example.demo.product;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductRepository repo;

    public ProductController(ProductRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Product> search(@RequestParam(required = false) String keyword,
                                @RequestParam(defaultValue = "50") int limit) {
        return repo.search(keyword, limit);
    }

    @GetMapping("/by-barcode/{code}")
    public Optional<Product> byBarcode(@PathVariable String code) {
        return repo.findByBarcode(code);
    }
}
