package com.eazybytes.eazystore.service.impl;

import com.eazybytes.eazystore.dto.AddProduct;
import com.eazybytes.eazystore.dto.ProductDto;
import com.eazybytes.eazystore.entity.Product;
import com.eazybytes.eazystore.repository.ProductRepository;
import com.eazybytes.eazystore.service.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import com.eazybytes.eazystore.exception.ResourceNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {

    private final ProductRepository productRepository;
    private final String uploadDir = "uploads/products-images/";
    private static final int MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB max image size

    @Override
    @Cacheable(value = "products")
    public List<ProductDto> getProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "products", allEntries = true)
    public ProductDto addProduct(AddProduct product) {
        List<MultipartFile> images = product.getImages();
        validateInput(product, images);

        Path uploadPath = createUploadDirectory();
        List<String> imageFileNames = storeImages(images, uploadPath);

        Product productEntity = createProductEntity(product, imageFileNames);
        Product savedProduct = productRepository.save(productEntity);

        return convertToDto(savedProduct);
    }
    
    @Override
    @Cacheable(value = "products", key = "#id")
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id.toString()));
        return convertToDto(product);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductDto updateProduct(Long id, AddProduct product) {
        // Find existing product
        Product existingProduct = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id.toString()));
            
        // Handle image updates if new images are provided
        List<String> imageFileNames = existingProduct.getImageFileNames();
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            // Delete old images
            deleteProductImages(existingProduct);
            
            // Store new images
            Path uploadPath = createUploadDirectory();
            imageFileNames = storeImages(product.getImages(), uploadPath);
        }
        
        // Update product fields
        BeanUtils.copyProperties(product, existingProduct, "id", "createdAt", "updatedAt", "imageFileNames");
        existingProduct.setImageFileNames(imageFileNames);
        existingProduct.setUpdatedAt(Instant.now());
        
        Product updatedProduct = productRepository.save(existingProduct);
        return convertToDto(updatedProduct);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id.toString()));

        // Delete associated images
        deleteProductImages(product);
        
        // Delete the product
        productRepository.delete(product);
    }
    
    private void deleteProductImages(Product product) {
        if (product.getImageFileNames() != null) {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            product.getImageFileNames().forEach(fileName -> {
                try {
                    Path filePath = uploadPath.resolve(fileName);
                    Files.deleteIfExists(filePath);

                } catch (IOException e) {
                    throw new RuntimeException("Failed to delete image file: " + fileName, e);
                }
            });
        }
    }

    private void validateInput(AddProduct product, List<MultipartFile> images) {
        if (product == null) {
            throw new IllegalArgumentException("Product data cannot be null");
        }
        if (images == null || images.isEmpty()) {
            throw new IllegalArgumentException("At least one image is required");
        }
    }

    private Path createUploadDirectory() {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            return uploadPath;
        } catch (IOException ex) {
            throw new RuntimeException("Could not create upload directory", ex);
        }
    }

    private List<String> storeImages(List<MultipartFile> images, Path uploadPath) {
        return images.stream()
                .filter(image -> !image.isEmpty())
                .map(image -> storeImage(image, uploadPath))
                .collect(Collectors.toList());
    }

    private String storeImage(MultipartFile image, Path uploadPath) {
        try {
            String originalFilename = StringUtils.cleanPath(image.getOriginalFilename());
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID() + fileExtension;

            Path targetLocation = uploadPath.resolve(fileName);
            Files.copy(image.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store file " + image.getOriginalFilename(), ex);
        }
    }

    private Product createProductEntity(AddProduct product, List<String> imageFileNames) {
        Product entity = new Product();
        BeanUtils.copyProperties(product, entity);
        entity.setImageFileNames(imageFileNames);

        // Set default values if not provided
        if (entity.getPopularity() == null) {
            entity.setPopularity(0);
        }
        if (entity.getQuantity() == null) {
            entity.setQuantity(0);
        }

        return entity;
    }

    private ProductDto convertToDto(Product product) {
        ProductDto dto = new ProductDto();
        BeanUtils.copyProperties(product, dto);
        dto.setProductId(product.getId());
        dto.setQuantity(product.getQuantity());
        dto.setPopularity(product.getPopularity());
        dto.setCategory(product.getCategory());
        dto.setSubcategory(product.getSubcategory());
        // Clean and set colors
        if (product.getColors() != null) {
            List<String> cleanedColors = product.getColors().stream()
                    .flatMap(color -> {
                        // Remove all non-alphanumeric characters except # and comma
                        String clean = color.replaceAll("[^a-zA-Z0-9,#]", "");
                        // Split by comma if multiple colors are in one string
                        return Arrays.stream(clean.split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty());
                    })
                    .distinct()
                    .collect(Collectors.toList());
            dto.setColors(cleanedColors);
        }
        
        // Clean and set sizes
        if (product.getSizes() != null) {
            List<String> cleanedSizes = product.getSizes().stream()
                    .flatMap(size -> {
                        // Remove all non-alphanumeric characters except comma
                        String clean = size.replaceAll("[^a-zA-Z0-9,]", "");
                        // Split by comma if multiple sizes are in one string
                        return Arrays.stream(clean.split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty());
                    })
                    .distinct()
                    .collect(Collectors.toList());
            dto.setSizes(cleanedSizes);
        }
        
        // Set image names
        dto.setImageNames(product.getImageFileNames());
        
        // Read and set image data
        if (product.getImageFileNames() != null && !product.getImageFileNames().isEmpty()) {
            // Filter out null values and collect valid image data
            List<byte[]> validImageData = new ArrayList<>();

            List<String> validImageNames = new ArrayList<>();
            
            for (String fileName : product.getImageFileNames()) {
                byte[] imageData = readImageFile(fileName);
                if (imageData != null) {
                    validImageData.add(imageData);
                    validImageNames.add(fileName);
                }
            }
            
            dto.setImages(validImageData);
            dto.setImageNames(validImageNames);
            
            // Set content type based on the first valid image file extension
            if (!validImageNames.isEmpty()) {
                String fileName = validImageNames.get(0);
                try {
                    String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
                    dto.setImageContentType("image/" + (fileExtension.equals("jpg") ? "jpeg" : fileExtension));
                } catch (Exception e) {
                    System.err.println("Error determining content type for file: " + fileName);
                }
            }
        }
        
        dto.setCreatedAt(product.getCreatedAt());
        return dto;
    }
    
    private byte[] readImageFile(String fileName) {
        try {
            if (fileName == null || fileName.isBlank()) {
                return null;
            }
            
            Path imagePath = Paths.get(uploadDir).resolve(fileName).normalize();
            if (!Files.exists(imagePath)) {
                // Instead of throwing an exception, log the missing file and return null
                System.err.println("Warning: Image file not found: " + fileName);
                return null;
            }
            
            // Check file size before reading
            long fileSize = Files.size(imagePath);
            if (fileSize > MAX_IMAGE_SIZE) {
                System.err.println("Warning: Image file too large: " + fileName + " (max " + (MAX_IMAGE_SIZE/1024/1024) + "MB)");
                return null;
            }
            
            return Files.readAllBytes(imagePath);
        } catch (IOException e) {
            // Log the error but don't fail the entire request
            System.err.println("Error reading image file " + fileName + ": " + e.getMessage());
            return null;
        }
    }
}