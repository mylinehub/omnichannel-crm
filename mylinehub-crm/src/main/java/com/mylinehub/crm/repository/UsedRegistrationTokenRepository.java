package com.mylinehub.crm.repository;

import com.mylinehub.crm.entity.UsedRegistrationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UsedRegistrationTokenRepository extends JpaRepository<UsedRegistrationToken, Long> {
	UsedRegistrationToken getUsedRegistrationTokenByUsedToken(String usedToken);
}
