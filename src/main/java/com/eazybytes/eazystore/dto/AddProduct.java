package com.eazybytes.eazystore.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AddProduct {
    private Long productId;
    private String name;
    private String description;
    private Integer quantity;
    private String category;
    private String subcategory;
    private List<String> colors;
    private List<String> sizes;
    private BigDecimal price;
    private Integer popularity;
    private List<MultipartFile> images;

}
