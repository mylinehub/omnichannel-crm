package com.mylinehub.crm.gst.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mylinehub.crm.gst.entity.RegisteredErroredGST;

@Repository
public interface RegisterGstErrorsRepository  extends JpaRepository<RegisteredErroredGST, Long> {

	RegisteredErroredGST getByBusinessId(String businessId);
}
