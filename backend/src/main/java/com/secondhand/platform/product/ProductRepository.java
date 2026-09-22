package com.secondhand.platform.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByTitleContainingOrDescriptionContaining(String key, String word,Pageable pageable);
}
