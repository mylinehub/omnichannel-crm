/*
 * Auto-formatted: src/main/java/com/mylinehub/voicebridge/service/DeductAiAmountService.java
 */
package com.mylinehub.voicebridge.service;

import reactor.core.publisher.Mono;

/**
 * Deduct AI billing amount in CRM (in-memory OrganizationData) for a call.
 *
 * Multi-tenant note:
 *  - We MUST know which stasis_app_name (tenant) the request belongs to,
 *    because each tenant may have its own:
 *      * base URL
 *      * endpoint path
 *      * system login credentials (token)
 */
public interface DeductAiAmountService {

    /**
     * Deduct AI amount for a call based on duration.
     *
     * CRM API does:
     *   - calculates deduction based on org settings (minute/call, charge amount)
     *   - updates in-memory OrganizationData
     *
     * @param stasisAppName tenant selector
     * @param organization organization name
     * @param callDurationSeconds call duration in seconds
     * @return Mono true if deducted successfully, else false
     */
    Mono<Boolean> deductAiAmount(String stasisAppName, String organization, long callDurationSeconds,Boolean dynamicCost,Integer callCost,String callCostMode, String linkId,String customerPhone,boolean redirectChannel,boolean ivrCall);
}
