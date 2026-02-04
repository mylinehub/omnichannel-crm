package com.mylinehub.crm.repository;

import com.mylinehub.crm.enums.MODE_OF_TRANSPORT_CODE;
import com.mylinehub.crm.entity.TypesOfTransport;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypesOfTransportRepository extends JpaRepository<TypesOfTransport, MODE_OF_TRANSPORT_CODE> {

	List<TypesOfTransport> findAllByOrganization(String organization);
}
