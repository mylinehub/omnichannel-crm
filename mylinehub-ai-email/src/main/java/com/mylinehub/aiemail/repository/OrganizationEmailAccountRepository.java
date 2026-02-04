package com.mylinehub.aiemail.repository;

import com.mylinehub.aiemail.model.OrganizationEmailAccount;
import com.mylinehub.aiemail.model.EmailConnectionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationEmailAccountRepository extends JpaRepository<OrganizationEmailAccount, Long> {

    Optional<OrganizationEmailAccount> findByEmailAddressAndActive(String emailAddress,boolean active);

    List<OrganizationEmailAccount> findByActiveTrue();

    List<OrganizationEmailAccount> findByActiveTrueAndConnectionType(EmailConnectionType type);
}
