package com.udea.incomeservice.infrastructure.driven.persistence.adapter;

import com.udea.incomeservice.domain.gateway.CategoryGateway;
import com.udea.incomeservice.domain.model.Category;
import com.udea.incomeservice.infrastructure.driven.persistence.mapper.CategoryEntityMapper;
import com.udea.incomeservice.infrastructure.driven.persistence.repository.CategoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CategoryAdapter implements CategoryGateway {

    private final CategoryJpaRepository repository;
    private final CategoryEntityMapper mapper;

    @Override
    public Category save(Category category) {
        return mapper.toDomain(repository.save(mapper.toEntity(category)));
    }

    @Override
    public boolean existsByUserIdAndNameAndType(Long userId, String name, String type) {
        return repository.existsByUserIdAndNameIgnoreCaseAndType(userId, name, type);
    }

    @Override
    public List<Category> findByUserIdAndType(Long userId, String type) {
        return repository.findByUserIdAndType(userId, type)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Category> findByUserId(Long userId) {
        return repository.findByUserId(userId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }
}
