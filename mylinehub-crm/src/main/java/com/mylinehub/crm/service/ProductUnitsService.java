package com.mylinehub.crm.service;

import com.mylinehub.crm.entity.dto.ProductUnitsDTO;
import com.mylinehub.crm.mapper.ProductUnitsMapper;
import com.mylinehub.crm.repository.ProductUnitsRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class ProductUnitsService {

    /**
     * were injected by the constructor using the lombok @AllArgsContrustor annotation
     */
    private final ProductUnitsRepository productUnitsRepository;
    private final ProductUnitsMapper productUnitsMapper;

    /**
     * The method is to retrieve all ProductUnits from the database and display them.
     *
     * After downloading all the data about the ProductUnits,
     * the data is mapped to dto which will display only those needed
     * @return list of all ProductUnits with specification of data in ProductUnitsDTO
     */
    
    public List<ProductUnitsDTO> getAllProductUnits(Pageable pageable){
        return productUnitsRepository.findAll(pageable)
                .stream()
                .map(productUnitsMapper::mapProductUnitsToDTO)
                .collect(Collectors.toList());
    }
}
