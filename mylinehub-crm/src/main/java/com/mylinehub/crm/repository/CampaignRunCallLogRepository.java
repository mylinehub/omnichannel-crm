// ============================================================
// FILE: src/main/java/com/mylinehub/crm/repository/CampaignRunCallLogRepository.java
// ============================================================
package com.mylinehub.crm.repository;

import com.mylinehub.crm.entity.CampaignRunCallLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

import javax.transaction.Transactional;

public interface CampaignRunCallLogRepository
        extends JpaRepository<CampaignRunCallLog, Long>, CampaignRunCallLogBatchRepository {

    @Query(
            "select distinct c.channelId " +
            "from CampaignRunCallLog c " +
            "where c.runId = :runId and c.campaignId = :campaignId"
    )
    List<String> findDistinctChannelIdsByRunIdAndCampaignId(
            @Param("runId") Long runId,
            @Param("campaignId") Long campaignId
    );

    @Query("select e.callState as state, count(e) as cnt " +
           "from CampaignRunCallLog e " +
           "where e.runId = :runId " +
           "group by e.callState")
    List<Object[]> countByStateForRun(@Param("runId") Long runId);

    @Query("select e.callState as state, count(e) as cnt " +
           "from CampaignRunCallLog e " +
           "where e.runId = :runId and (" +
           "   lower(coalesce(e.channelId,'')) like lower(concat('%',:q,'%')) or " +
           "   lower(coalesce(e.fromNumber,'')) like lower(concat('%',:q,'%')) or " +
           "   lower(coalesce(e.toNumber,'')) like lower(concat('%',:q,'%')) or " +
           "   lower(coalesce(e.employeeExtension,'')) like lower(concat('%',:q,'%')) or " +
           "   lower(coalesce(e.callState,'')) like lower(concat('%',:q,'%')) or " +
           "   lower(coalesce(e.extraJson,'')) like lower(concat('%',:q,'%')) " +
           ") group by e.callState")
    List<Object[]> countByStateForRunWithSearch(@Param("runId") Long runId, @Param("q") String q);

    // ------------------------------------------------------------
    // Existing (DB-only)
    // ------------------------------------------------------------

    Page<CampaignRunCallLog> findByCampaignIdOrderByEventAtDesc(Long campaignId, Pageable pageable);

    Page<CampaignRunCallLog> findByRunIdOrderByEventAtDesc(Long runId, Pageable pageable);

    List<CampaignRunCallLog> findByRunIdOrderByEventAtAsc(Long runId);

    // ------------------------------------------------------------
    // DB search (run-scoped)
    // ------------------------------------------------------------

    /**
     * DB-only paging with search (runId scope).
     * searchText matches: channelId/from/to/extension/callState/extraJson
     */
    @Query(
            "select c from CampaignRunCallLog c " +
            "where c.runId = :runId " +
            "and ( :q = '' or :q is null or " +
            "      lower(coalesce(c.channelId,'')) like lower(concat('%', :q, '%')) or " +
            "      lower(coalesce(c.fromNumber,'')) like lower(concat('%', :q, '%')) or " +
            "      lower(coalesce(c.toNumber,'')) like lower(concat('%', :q, '%')) or " +
            "      lower(coalesce(c.employeeExtension,'')) like lower(concat('%', :q, '%')) or " +
            "      lower(coalesce(c.callState,'')) like lower(concat('%', :q, '%')) or " +
            "      lower(coalesce(c.extraJson,'')) like lower(concat('%', :q, '%')) " +
            "    ) " +
            "order by c.eventAt desc"
    )
    Page<CampaignRunCallLog> findByRunIdWithSearchOrderByEventAtDesc(
            @Param("runId") Long runId,
            @Param("q") String searchText,
            Pageable pageable
    );

    // ------------------------------------------------------------
    // DB search (campaign-scoped) - optional utility
    // ------------------------------------------------------------

    @Query(
            "select c from CampaignRunCallLog c " +
            "where c.campaignId = :campaignId " +
            "and ( :q = '' or :q is null or " +
            "      lower(coalesce(c.channelId,'')) like lower(concat('%', :q, '%')) or " +
            "      lower(coalesce(c.fromNumber,'')) like lower(concat('%', :q, '%')) or " +
            "      lower(coalesce(c.toNumber,'')) like lower(concat('%', :q, '%')) or " +
            "      lower(coalesce(c.employeeExtension,'')) like lower(concat('%', :q, '%')) or " +
            "      lower(coalesce(c.callState,'')) like lower(concat('%', :q, '%')) or " +
            "      lower(coalesce(c.extraJson,'')) like lower(concat('%', :q, '%')) " +
            "    ) " +
            "order by c.eventAt desc"
    )
    Page<CampaignRunCallLog> findByCampaignIdWithSearchOrderByEventAtDesc(
            @Param("campaignId") Long campaignId,
            @Param("q") String searchText,
            Pageable pageable
    );

    // ------------------------------------------------------------
    // PostgreSQL UPSERT (optional utility; not used by batch flush)
    // UNIQUE constraint/index on (run_id, channel_id)
    // ------------------------------------------------------------

    @Modifying
    @Transactional
    @Query(
            value =
                    "INSERT INTO campaign_run_call_log " +
                    " (run_id, campaign_id, organization, campaign_name, event_at, channel_id, " +
                    "  from_number, to_number, employee_extension, call_state, call_cost, duration_ms, extra_json) " +
                    "VALUES " +
                    " (:runId, :campaignId, :organization, :campaignName, :eventAt, :channelId, " +
                    "  :fromNumber, :toNumber, :employeeExtension, :callState, :callCost, :durationMs, :extraJson) " +
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
                    " extra_json = EXCLUDED.extra_json ",
            nativeQuery = true
    )
    int upsertCallLog(
            @Param("runId") Long runId,
            @Param("campaignId") Long campaignId,
            @Param("organization") String organization,
            @Param("campaignName") String campaignName,
            @Param("eventAt") Instant eventAt,
            @Param("channelId") String channelId,
            @Param("fromNumber") String fromNumber,
            @Param("toNumber") String toNumber,
            @Param("employeeExtension") String employeeExtension,
            @Param("callState") String callState,
            @Param("callCost") Double callCost,
            @Param("durationMs") Long durationMs,
            @Param("extraJson") String extraJson
    );

    // ------------------------------------------------------------
    // Aggregations (optional)
    // ------------------------------------------------------------

    @Query(
            "select c.callState, count(c) " +
            "from CampaignRunCallLog c " +
            "where c.runId = :runId " +
            "group by c.callState"
    )
    List<Object[]> countByCallStateForRun(@Param("runId") Long runId);

    @Query(
            "select distinct c.channelId " +
            "from CampaignRunCallLog c " +
            "where c.runId = :runId"
    )
    List<String> findDistinctChannelIdsByRunId(@Param("runId") Long runId);

    @Query(
            "select distinct c.channelId " +
            "from CampaignRunCallLog c " +
            "where c.runId = :runId and c.channelId in :channelIds"
    )
    List<String> findExistingChannelIdsForRun(
            @Param("runId") Long runId,
            @Param("channelIds") List<String> channelIds
    );
}
