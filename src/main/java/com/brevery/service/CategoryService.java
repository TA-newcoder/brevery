package com.brevery.service;

import com.brevery.dto.response.CategoryDTO;
import com.brevery.entity.Category;
import com.brevery.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .filter(c -> c.getIsActive() != null && c.getIsActive())
                .map(c -> CategoryDTO.builder()
                        .categoryId(c.getCategoryId())
                        .name(c.getName())
                        .description(c.getDescription())
                        .imageUrl(c.getImageUrl())
                        .build())
                .collect(Collectors.toList());
    }
}
