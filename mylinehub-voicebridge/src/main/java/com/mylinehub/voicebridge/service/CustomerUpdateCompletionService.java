package com.mylinehub.voicebridge.service;

import reactor.core.publisher.Mono;

public interface CustomerUpdateCompletionService {
  Mono<Boolean> completeAndUpdateCrm(String stasisAppName,
                                           String organization,
                                           String channelId,
                                           String callerNumber,
                                           String original,
                                           String transcriptEn,
                                           String summary,
                                           boolean isIvrCall,
                                           boolean savePropertyInventory,
                                           boolean saveFranchiseInventory,
                                           String ivrDtmf);
}
