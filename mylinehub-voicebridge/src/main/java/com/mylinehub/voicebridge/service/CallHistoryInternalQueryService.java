package com.mylinehub.voicebridge.service;

import com.mylinehub.voicebridge.models.CallHistoryRecord;
import com.mylinehub.voicebridge.repository.CallHistoryRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CallHistoryInternalQueryService {

    private final CallHistoryRecordRepository repo;

    public List<CallHistoryRecord> searchByPhoneAndEntryType(String phone, String entryType) {
        String p = (phone == null) ? "" : phone.trim();
        String e = (entryType == null) ? "" : entryType.trim();

        if (p.isEmpty() || e.isEmpty()) {
            return Collections.emptyList();
        }
        return repo.findByPhoneEqualsOrContainsAndEntryType(p, e);
    }

    public CallHistoryRecord getByIdOrNull(Long id) {
        if (id == null) return null;
        return repo.findById(id).orElse(null);
    }

    public List<CallHistoryRecord> findAfterStartedAt(Instant from, String entryType) {
        if (from == null) return Collections.emptyList();
        String e = (entryType == null) ? "" : entryType.trim();
        if (e.isEmpty()) {
            return repo.findByStartedAtAfterOrderByStartedAtAsc(from);
        }
        return repo.findByStartedAtAfterAndEntryTypeOrderByStartedAtAsc(from, e);
    }
}
