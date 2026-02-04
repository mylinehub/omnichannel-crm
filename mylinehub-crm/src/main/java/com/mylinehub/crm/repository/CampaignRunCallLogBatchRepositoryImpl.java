// ============================================================
// FILE: src/main/java/com/mylinehub/crm/repository/CampaignRunCallLogBatchRepositoryImpl.java
// ============================================================
package com.mylinehub.crm.repository;

import com.mylinehub.crm.data.StartedCampaignData;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Repository
public class CampaignRunCallLogBatchRepositoryImpl implements CampaignRunCallLogBatchRepository {

    private final JdbcTemplate jdbc;

    public CampaignRunCallLogBatchRepositoryImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public int batchUpsertCallLogs(List<StartedCampaignData.CampaignRunCallLogMem> rows,
                                  Long fallbackRunId,
                                  Long campaignId) {
        if (rows == null || rows.isEmpty()) return 0;

        // POSTGRES version (ON CONFLICT)
        // IMPORTANT: include call_cost in INSERT + VALUES + UPDATE
        final String sql =
                "INSERT INTO campaign_run_call_log " +
                "(run_id, campaign_id, organization, campaign_name, event_at, channel_id, " +
                " from_number, to_number, employee_extension, call_state, call_cost, duration_ms, extra_json) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?) " +
                "ON CONFLICT (run_id, channel_id) DO UPDATE SET " +
                " organization = EXCLUDED.organization, " +
                " campaign_name = EXCLUDED.campaign_name, " +
                " event_at = EXCLUDED.event_at, " +
                " from_number = EXCLUDED.from_number, " +
                " to_number = EXCLUDED.to_number, " +
                " employee_extension = EXCLUDED.employee_extension, " +
                " call_state = EXCLUDED.call_state, " +
                " call_cost = EXCLUDED.call_cost, " +
                " duration_ms = EXCLUDED.duration_ms, " +
                " extra_json = EXCLUDED.extra_json";

        int[] counts = jdbc.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
            @Override
            public void setValues(java.sql.PreparedStatement ps, int i) throws java.sql.SQLException {
                StartedCampaignData.CampaignRunCallLogMem r = rows.get(i);

                Long runId = (r.runId != null) ? r.runId : fallbackRunId;
                Instant eventAt = (r.eventAt != null) ? r.eventAt : Instant.now();

                ps.setLong(1, runId);
                ps.setLong(2, campaignId);
                ps.setString(3, r.organization);
                ps.setString(4, r.campaignName);
                ps.setTimestamp(5, Timestamp.from(eventAt));
                ps.setString(6, r.channelId);
                ps.setString(7, r.fromNumber);
                ps.setString(8, r.toNumber);
                ps.setString(9, r.employeeExtension);
                ps.setString(10, r.callState);

                // call_cost (mem is double)
                ps.setDouble(11, r.callCost);

                ps.setLong(12, (r.durationMs != null) ? r.durationMs : 0L);
                ps.setString(13, r.extraJson);
            }

            @Override
            public int getBatchSize() {
                return rows.size();
            }
        });

        int total = 0;
        for (int c : counts) total += c;
        return total;
    }
}
