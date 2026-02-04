package com.mylinehub.voicebridge.service;

import com.mylinehub.voicebridge.billing.CallBillingInfo;
import com.mylinehub.voicebridge.session.CallSession;

public interface CallReportingService {

    /** Old behavior — still needed for backward compatibility */
    void reportCall(CallBillingInfo info);

    /** New behavior — used for CRM + CDR posting */
    void reportCall(CallSession session, CallBillingInfo info);
}
