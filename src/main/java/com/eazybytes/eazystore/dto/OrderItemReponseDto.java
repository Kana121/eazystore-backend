package com.eazybytes.eazystore.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderItemReponseDto(String productName, Integer quantity,
                                  BigDecimal price, List<byte[]> images ) {
}
