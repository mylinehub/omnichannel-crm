package com.mylinehub.crm.repository;

import com.mylinehub.crm.data.StartedCampaignData;
import java.util.List;

public interface CampaignRunCallLogBatchRepository {
    int batchUpsertCallLogs(List<StartedCampaignData.CampaignRunCallLogMem> rows, Long fallbackRunId, Long campaignId);
}
