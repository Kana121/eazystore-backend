package com.eazybytes.eazystore.service;

import com.eazybytes.eazystore.dto.AddProduct;
import com.eazybytes.eazystore.dto.ProductDto;
import com.eazybytes.eazystore.dto.ProductRequestDTO;
import com.eazybytes.eazystore.entity.Product;
import com.eazybytes.eazystore.exception.ResourceNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IProductService {

    List<ProductDto> getProducts();
    
    ProductDto getProductById(Long id) throws ResourceNotFoundException;
    
    ProductDto addProduct(AddProduct product);
    
    ProductDto updateProduct(Long id, AddProduct product) throws ResourceNotFoundException;
    
    void deleteProduct(Long id) throws ResourceNotFoundException;
}
