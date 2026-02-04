package com.mylinehub.crm.whatsapp.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mylinehub.crm.whatsapp.entity.WhatsAppErrorMessages;

@Repository
public interface WhatsAppErrorMessageRepository  extends JpaRepository<WhatsAppErrorMessages, Long> {

}
