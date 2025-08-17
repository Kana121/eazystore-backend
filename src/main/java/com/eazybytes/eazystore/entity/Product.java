package com.eazybytes.eazystore.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Cascade;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "popularity", nullable = false)
    private Integer popularity;

    @Column
    private Integer quantity;
    private String category;
    private String subcategory;

    @ElementCollection
    @CollectionTable(name = "product_colors",
            joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "color", length = 30)
    private List<String> colors = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "product_sizes",
            joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "size", length = 10)
    private List<String> sizes = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "file_name")
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    private List<String> imageFileNames = new ArrayList<>();
}
