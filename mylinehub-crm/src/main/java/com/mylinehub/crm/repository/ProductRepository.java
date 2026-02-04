package com.mylinehub.crm.repository;

import com.mylinehub.crm.entity.Product;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Product getProductByNameAndOrganization(String name,String organization);
    Product getProductByIdAndOrganization(Long id,String organization);
    
    @Query("select  new com.mylinehub.crm.entity.Product(e.id,e.name, e.productType, e.productStringType, e.units , e.sellingPrice, e.purchasePrice, e.taxRate , e.unitsOfMeasure, e.organization,e.imageName,e.imageType) from Product e where e.organization = ?1")
    List<Product> findAllByOrganization(String organization);
    
    @Query("select  new com.mylinehub.crm.entity.Product(e.id,e.name, e.productType, e.productStringType, e.units , e.sellingPrice, e.purchasePrice, e.taxRate , e.unitsOfMeasure, e.organization,e.imageName,e.imageType) from Product e where e.productStringType = ?1 and e.organization = ?2")
    List<Product> findProductsByProductStringTypeFullNameContaining(String productStringType, Pageable pageable);
    
    @Query("select  new com.mylinehub.crm.entity.Product(e.id,e.name, e.productType, e.productStringType, e.units , e.sellingPrice, e.purchasePrice, e.taxRate , e.unitsOfMeasure, e.organization,e.imageName,e.imageType) from Product e where e.productStringType = ?1 and e.organization = ?2")
    List<Product> findAllByProductStringTypeAndOrganization(String productStringType,String organization);
    
    List<Product> findProductsByNameContaining(String name, Pageable pageable);
    
    @Query(value = "select e from Product e where e.id in (?1) and e.organization = ?2")
    List<Product> findAllProductsByIdIn(List<Long> ids, String organization);

    @Query("SELECT COALESCE(SUM(e.imageSize), 0) FROM Product e WHERE e.organization = ?1 AND e.imageData IS NOT NULL")
    Long getTotalImageSizeForOrganization(String organization);
    
}
