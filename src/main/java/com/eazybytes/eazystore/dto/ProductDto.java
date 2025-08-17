package com.eazybytes.eazystore.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
public class ProductDto {

    private Long productId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer quantity;
    private Integer popularity;
    private String category;
    private String subcategory;
    private List<String> colors;
    private List<String> sizes;
    private List<byte[]> images;  // Changed from List<String> to List<byte[]>
    private List<String> imageNames;  // Keep track of original file names
    private String imageContentType;  // Common content type for all images (assuming same type)
    private Instant createdAt;
}
