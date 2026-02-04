package com.mylinehub.crm.repository;

import com.mylinehub.crm.entity.Absenteeism;
import com.mylinehub.crm.entity.Employee;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface AbsenteeismRepository extends JpaRepository<Absenteeism, Long> {

    @EntityGraph(attributePaths = {"employee", "reasonOfAbsenteeismCode"})
    List<Absenteeism> findAllBy(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"employee", "reasonOfAbsenteeismCode"})
    Optional<Absenteeism> findById(Long aLong);
    
    List<Absenteeism> findAllByOrganization(String organization);
    
    List<Absenteeism> findAllByEmployeeAndOrganization(Employee employee,String organization);
    
    List<Absenteeism> findAllByReasonForAbsenseAndOrganization(String reasonForAbsense,String organization);
    
    List<Absenteeism> findAllByDateFromGreaterThanEqualAndDateToLessThanEqualAndOrganization(Date dateFrom,Date dateTo,String organization);

    Absenteeism getAbsenteeismByIdAndOrganization(Long id,String organization);
    
}
