package com.mylinehub.crm.rag.service;

import org.springframework.stereotype.Service;

@Service
public class AuditService {
    public void audit(String msg) {
        System.out.println("[Audit] " + msg);
    }
}
