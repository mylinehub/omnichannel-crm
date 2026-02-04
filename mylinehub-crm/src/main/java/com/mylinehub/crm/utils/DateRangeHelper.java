package com.mylinehub.crm.utils;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;

public class DateRangeHelper {

    public static Timestamp[] getRangeFromOffsets(int startOffset, int endOffset) {
        if (endOffset < startOffset) {
            int tmp = startOffset;
            startOffset = endOffset;
            endOffset = tmp;
        }

        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);

        // Start = beginning of day (startOffset)
        LocalDate startDate = today.minusDays(startOffset);
        Timestamp startTime = Timestamp.from(startDate.atStartOfDay(zone).toInstant());

        // End = end of day (endOffset)
        LocalDate endDate = today.minusDays(endOffset);
        Timestamp endTime = Timestamp.from(endDate.plusDays(1).atStartOfDay(zone).toInstant().minusMillis(1));

        return new Timestamp[]{endTime, startTime};
    }
}
