package com.mylinehub.voicebridge.repository;

import com.mylinehub.voicebridge.models.CallHistoryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface CallHistoryRecordRepository extends JpaRepository<CallHistoryRecord, Long> {

    /**
     * Search by:
     * 1) Phone number (EQUAL OR CONTAINS)
     * 2) Entry type (mandatory)
     *
     * Behaviour:
     * - If phone matches exactly → included
     * - If phone is contained   → included
     *
     * Ordered by latest call first.
     */
    @Query("""
        select r
        from CallHistoryRecord r
        where
            r.entryType = :entryType
            and (
                r.callerNumber = :phone
                or lower(r.callerNumber) like concat('%', lower(:phone), '%')
            )
        order by r.startedAt desc
    """)
    List<CallHistoryRecord> findByPhoneEqualsOrContainsAndEntryType(
            @Param("phone") String phone,
            @Param("entryType") String entryType
    );
    
    // For ZIP export by time
    List<CallHistoryRecord> findByStartedAtAfterOrderByStartedAtAsc(Instant from);

    // Optional filter: time + entryType
    List<CallHistoryRecord> findByStartedAtAfterAndEntryTypeOrderByStartedAtAsc(Instant from, String entryType);
    
    @Query("""
            select r
            from CallHistoryRecord r
            where r.organization = :org
              and r.channelId in :channelIds
            order by r.startedAt asc
        """)
    List<CallHistoryRecord> findByOrganizationAndChannelIdInOrderByStartedAtAsc(
                @Param("org") String organization,
                @Param("channelIds") List<String> channelIds
    );
}
