package com.eazybytes.eazystore.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {


    // GET all categories
    @GetMapping
    public ResponseEntity<List<String>> getAllCategories() {
        List<String> categories = List.of("Electronics", "Clothing", "Books", "Sports", "Home");
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }
}