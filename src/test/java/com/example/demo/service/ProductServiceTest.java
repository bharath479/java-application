package com.example.demo.service;

import com.example.demo.dto.ProductRequest;
import com.example.demo.dto.ProductResponse;
import com.example.demo.entity.Product;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Spy
    private ProductMapper productMapper = new ProductMapper();

    @InjectMocks
    private ProductService productService;

    @Test
    void create_savesAndReturnsMappedResponse() {
        ProductRequest request = new ProductRequest("Widget", "A useful widget", BigDecimal.valueOf(9.99), 100);
        Product saved = Product.builder()
                .id(1L)
                .name("Widget")
                .description("A useful widget")
                .price(BigDecimal.valueOf(9.99))
                .quantity(100)
                .build();
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductResponse response = productService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Widget");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void getById_whenNotFound_throwsResourceNotFoundException() {
        when(productRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    void delete_whenNotFound_throwsResourceNotFoundException() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> productService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository, never()).deleteById(any());
    }

    @Test
    void update_whenFound_updatesAndSaves() {
        Product existing = Product.builder().id(5L).name("Old").price(BigDecimal.ONE).quantity(1).build();
        when(productRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductRequest request = new ProductRequest("New", "Updated desc", BigDecimal.TEN, 5);
        ProductResponse response = productService.update(5L, request);

        assertThat(response.name()).isEqualTo("New");
        assertThat(response.quantity()).isEqualTo(5);
    }
}
