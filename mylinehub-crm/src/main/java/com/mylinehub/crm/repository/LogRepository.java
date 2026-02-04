package com.mylinehub.crm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mylinehub.crm.entity.Logs;

@Repository
public interface LogRepository extends JpaRepository<Logs, Long> {
	
	List<Logs> findAllByOrganization(String organization);

}