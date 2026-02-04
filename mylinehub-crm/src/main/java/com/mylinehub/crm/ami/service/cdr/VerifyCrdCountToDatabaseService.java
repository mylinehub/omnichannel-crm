package com.mylinehub.crm.ami.service.cdr;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.mylinehub.crm.ami.TaskScheduler.HardInsertCallDetailAndCostRunnable;
import com.mylinehub.crm.data.CDRMemoryCollection;
import com.mylinehub.crm.data.TrackedSchduledJobs;
import com.mylinehub.crm.data.dto.CdrDTO;
import com.mylinehub.crm.repository.CallDetailRepository;
import com.mylinehub.crm.service.SchedulerService;
import com.mylinehub.crm.utils.LoggerUtils;

import lombok.AllArgsConstructor;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class VerifyCrdCountToDatabaseService {

    private final ApplicationContext applicationContext;

    // Single background worker to ensure hard-insert jobs never run in parallel
    private static final ExecutorService HARD_INSERT_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "cdr-hard-insert-worker");
        t.setDaemon(true);
        return t;
    });

    // Prevent duplicate triggers while one hard insert is already running
    private static final AtomicBoolean HARD_INSERT_RUNNING = new AtomicBoolean(false);

    public boolean checkTotalOfCallDetailsAndTrigger() throws Exception {

        System.out.println("Inside checkTotalOfCallDetailsAndTrigger");
        boolean toReturn = true;

        try {

            Map<String, CdrDTO> interimRecords = CDRMemoryCollection.workWithCDRInterimData(null, null, "get");

            int size = (interimRecords == null) ? 0 : interimRecords.size();
            System.out.println("Verify CDR : interimRecords size : " + size);

            if (size > 2000) {

                System.out.println("Total CDR is greator than 20");

                final String jobId = TrackedSchduledJobs.hardInsertCallDetailAndCostRunnableCron;
                final SchedulerService schedulerService = applicationContext.getBean(SchedulerService.class);

                // If already running, do not start another one (prevents burst duplicate runs)
                if (!HARD_INSERT_RUNNING.compareAndSet(false, true)) {
                    System.out.println("Hard insert already running, skipping new trigger");
                    return true;
                }

                // Remove existing cron first (same as your current logic)
                schedulerService.removeScheduledTask(jobId);

                // Prepare runnable (same as your current logic)
                final HardInsertCallDetailAndCostRunnable hardInsertCallDetailAndCostRunnable =
                        new HardInsertCallDetailAndCostRunnable();

                hardInsertCallDetailAndCostRunnable.setJobId(jobId);
                hardInsertCallDetailAndCostRunnable.setCallDetailRepository(applicationContext.getBean(CallDetailRepository.class));
                System.out.println("Total CDR size of is > 2000, hence saving in database first (ASYNC)");

                // Run heavy work asynchronously so AMI/event thread does not block
                HARD_INSERT_EXECUTOR.execute(() -> {
                    try {
                        hardInsertCallDetailAndCostRunnable.run();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        LoggerUtils.log.debug("Hard insert async exception: " + ex.getMessage());
                    } finally {
                        try {
                            // Reschedule after completion (same order as your original code)
                            schedulerService.removeIfExistsAndScheduleACronTask(
                                    jobId,
                                    hardInsertCallDetailAndCostRunnable,
                                    "0 */25 * * * ?"
                            );
                        } catch (Exception ex2) {
                            ex2.printStackTrace();
                            LoggerUtils.log.debug("Reschedule async exception: " + ex2.getMessage());
                        } finally {
                            HARD_INSERT_RUNNING.set(false);
                        }
                    }
                });

            } else {
                System.out.println("Total CDR is less than 2000, do nothing");
            }

        } catch (Exception e) {
            toReturn = false;
            e.printStackTrace();
        }

        return toReturn;
    }
}
