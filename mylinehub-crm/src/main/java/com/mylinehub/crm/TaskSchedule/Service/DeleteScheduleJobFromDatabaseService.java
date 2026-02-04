package com.mylinehub.crm.TaskSchedule.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mylinehub.crm.data.TrackedSchduledJobs;
import com.mylinehub.crm.entity.RunningSchedule;
import com.mylinehub.crm.service.CurrentTimeInterface;
import com.mylinehub.crm.service.RunningScheduleService;
import com.mylinehub.crm.service.SchedulerService;

import lombok.AllArgsConstructor;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class DeleteScheduleJobFromDatabaseService implements CurrentTimeInterface {

    private final RunningScheduleService runningScheduleService;
    private final SchedulerService schedulerService;

    public boolean deleteScheduleJobFromDatabaseIfExecuted(String jobId) {
        boolean toReturn = false;

        System.out.println("Attempting to delete scheduled job from DB if executed for jobId: " + jobId);
        try {
            List<RunningSchedule> runningSchedules = runningScheduleService.getAllRunningSchedulesByJobId(jobId);
            if (runningSchedules != null && !runningSchedules.isEmpty()) {
                RunningSchedule runningSchedule = runningSchedules.get(0);

                System.out.println("Found RunningSchedule with scheduleType: " + runningSchedule.getScheduleType());

                if (TrackedSchduledJobs.cron.equals(runningSchedule.getScheduleType())) {
                    System.out.println("Schedule type is 'cron'. Skipping deletion.");
                    // Do nothing for cron jobs
                } else {
                    System.out.println("Deleting RunningSchedule and removing scheduled task for jobId: " + jobId);
                    runningScheduleService.deleteAllRunningSchedulesByJobId(jobId);
                    schedulerService.removeScheduledTask(jobId);
                    toReturn = true;
                }
            } else {
                System.out.println("No RunningSchedule found for jobId: " + jobId);
            }
        } catch (Exception e) {
            System.out.println("Exception occurred while deleting scheduled job for jobId: " + jobId);
            e.printStackTrace();
        }

        System.out.println("Completed deleteScheduleJobFromDatabaseIfExecuted for jobId: " + jobId + " with result: " + toReturn);
        return toReturn;
    }
}
