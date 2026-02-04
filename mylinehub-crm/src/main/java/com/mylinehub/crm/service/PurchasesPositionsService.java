package com.mylinehub.crm.service;

import com.mylinehub.crm.entity.PurchasesPositions;
import com.mylinehub.crm.entity.dto.PurchasesPositionsDTO;
import com.mylinehub.crm.mapper.PurchasesPositionsMapper;
import com.mylinehub.crm.repository.PurchasesPositionsRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author MAnand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class PurchasesPositionsService {

    /**
     * were injected by the constructor using the lombok @AllArgsContrustor annotation
     */
    private final PurchasesPositionsRepository purchasesPositionsRepository;
    private final PurchasesPositionsMapper purchasesPositionsMapper;

    /**
     * The method is to retrieve all purchases positions from the database and display them.
     *
     * After downloading all the data about the purchases positions,
     * the data is mapped to dto which will display only those needed
     * @return list of all purchases positions with specification of data in PurchasesPositionsToDTO
     */
    
    public List<PurchasesPositionsDTO> getAllPurchasesPositions(Pageable pageable){
        return purchasesPositionsRepository.findAll(pageable)
                .stream()
                .map(purchasesPositionsMapper::mapPurchasesPositionsToDTO)
                .collect(Collectors.toList());
    }

    /**
     * The method is to download a specific purchases position from the database and display it.
     * After downloading all the data about the purchases position,
     * the data is mapped to dto which will display only those needed
     *
     * @param id id of the purchases position to be searched for
     * @throws ResponseStatusException if the id of the purchases position you are looking for does not exist throws 404 status
     * @return detailed data about a specific purchases position
     */
    
    public PurchasesPositionsDTO getpurchasePositiontById(Long id) {
        PurchasesPositions purchasesPositions = purchasesPositionsRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase position cannot be found, the specified id does not exist"));
        return purchasesPositionsMapper.mapPurchasesPositionsToDTO(purchasesPositions);
    }
}
