package com.udea.incomeservice.domain.usecase;

import com.udea.incomeservice.domain.exception.DomainConstants;
import com.udea.incomeservice.domain.exception.DuplicateCategoryException;
import com.udea.incomeservice.domain.exception.InvalidIncomeException;
import com.udea.incomeservice.domain.gateway.CategoryGateway;
import com.udea.incomeservice.domain.model.Category;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class CategoryUseCase {

    private static final Set<String> VALID_TYPES = Set.of("INCOME", "EXPENSE");

    private final CategoryGateway categoryGateway;

    public Category createCategory(Category category) {
        category.setType(category.getType().toUpperCase());
        category.setName(category.getName().trim().toUpperCase());

        if (!VALID_TYPES.contains(category.getType())) {
            throw new InvalidIncomeException(DomainConstants.CATEGORY_TYPE_INVALID);
        }
        if (categoryGateway.existsByUserIdAndNameAndType(category.getUserId(), category.getName(), category.getType())) {
            throw new DuplicateCategoryException(DomainConstants.DUPLICATE_CATEGORY);
        }
        return categoryGateway.save(category);
    }

    public List<Category> getCategoriesByType(Long userId, String type) {
        return categoryGateway.findByUserIdAndType(userId, type.toUpperCase());
    }

    public List<Category> getAllCategories(Long userId) {
        return categoryGateway.findByUserId(userId);
    }
}