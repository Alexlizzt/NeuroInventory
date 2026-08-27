package com.alexlizzt.inventory_service.infraestructure.persistence.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.alexlizzt.inventory_service.application.query.PageQuery;
import com.alexlizzt.inventory_service.application.query.PageResult;
import com.alexlizzt.inventory_service.domain.model.InventoryMovement;
import com.alexlizzt.inventory_service.domain.repository.InventoryMovementRepository;
import com.alexlizzt.inventory_service.infraestructure.persistence.entity.InventoryMovementEntity;
import com.alexlizzt.inventory_service.infraestructure.persistence.mapper.InventoryMovementEntityMapper;
import com.alexlizzt.inventory_service.infraestructure.persistence.springdata.InventoryMovementJpaRepository;

public class InventoryMovementRepositoryAdapter implements InventoryMovementRepository {

    private final InventoryMovementJpaRepository movementJpaRepository;
    private final InventoryMovementEntityMapper mapper;

    public InventoryMovementRepositoryAdapter(InventoryMovementJpaRepository movementJpaRepository, InventoryMovementEntityMapper mapper) {
        this.movementJpaRepository = movementJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public InventoryMovement save(InventoryMovement movement) {
        var entity = mapper.toEntity(movement);
        var savedEntity = movementJpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<InventoryMovement> findById(String id) {
            return movementJpaRepository.findById(id).stream()
                .map(mapper::toDomain)
                .findFirst();
    }

    @Override
    public List<InventoryMovement> findByProductId(String productId) {
        return movementJpaRepository.findByProductId(productId).stream()
                .map(mapper::toDomain)
                .toList();
    }
    // Implement the methods defined in the InventoryMovementRepository interface

    @Override
    public PageResult<InventoryMovement> findAllPaged(PageQuery pageQuery) {
        Sort sort = pageQuery.direction().equalsIgnoreCase("DESC") 
            ? Sort.by(pageQuery.sortBy()).descending() 
            : Sort.by(pageQuery.sortBy()).ascending();

        PageRequest pageable = PageRequest.of(pageQuery.page(), pageQuery.size(), sort);

        Page<InventoryMovementEntity> entityPage = movementJpaRepository.findAll(pageable);

        List<InventoryMovement> domainList = entityPage.getContent().stream()
                .map(mapper::toDomain)
                .toList();

        return new PageResult<>(
            domainList,
            entityPage.getNumber(),
            entityPage.getSize(),
            entityPage.getTotalElements(),
            entityPage.getTotalPages()
        );
    }

}
