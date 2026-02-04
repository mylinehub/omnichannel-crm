package com.mylinehub.crm.whatsapp.taskscheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mylinehub.crm.entity.CustomerPropertyInventory;
import com.mylinehub.crm.entity.Customers;
import com.mylinehub.crm.service.CustomerPropertyInventoryService;
import com.mylinehub.crm.whatsapp.data.WhatsAppCustomerData;
import com.mylinehub.crm.whatsapp.dto.chat.WhatsAppCustomerDataDto;
import com.mylinehub.crm.whatsapp.dto.chat.WhatsAppCustomerParameterDataDto;

import lombok.Data;

@Data
public class WhatsAppSaveCustomerPropertyInventoryRunnable implements Runnable {

    private String jobId;

    // REQUIRED
    private CustomerPropertyInventoryService customerPropertyInventoryService;

    // batch size (default 100)
    private int batchSize = 100;

    // Save only if lastVerified is within last N hours
    private static final long MAX_LAST_VERIFIED_AGE_HOURS = 3;

    @Override
    public void run() {
        try {
            if (customerPropertyInventoryService == null) {
                System.out.println("[SAVE-INVENTORY] customerPropertyInventoryService is null. jobId=" + jobId);
                return;
            }

            // 1) Snapshot all WhatsAppCustomerData cache entries
            WhatsAppCustomerParameterDataDto p = new WhatsAppCustomerParameterDataDto();
            p.setAction("get");
            Map<String, WhatsAppCustomerDataDto> all = WhatsAppCustomerData.workWithWhatsAppCustomerData(p);

            if (all == null || all.isEmpty()) {
                System.out.println("[SAVE-INVENTORY] Cache empty. jobId=" + jobId);
                return;
            }

            // 2) Collect inventories that are "recent"
            Instant cutoff = Instant.now().minus(Duration.ofHours(MAX_LAST_VERIFIED_AGE_HOURS));

            List<CustomerPropertyInventory> toSave = new ArrayList<>();
            List<String> cacheKeysForToSave = new ArrayList<>();

            for (Map.Entry<String, WhatsAppCustomerDataDto> e : all.entrySet()) {
                String cacheKey = e.getKey(); // phone + org
                WhatsAppCustomerDataDto data = e.getValue();
                if (cacheKey == null || data == null) continue;

                if (data.getLastVerified() == null) continue;
                if (data.getLastVerified().toInstant().isBefore(cutoff)) continue;

                Customers c = data.getCustomer();
                if (c == null) continue;

                CustomerPropertyInventory inv = c.getPropertyInventory();
                if (inv == null) continue;

                // IMPORTANT:
                // - first-time inventory is now inserted immediately in WhatsAppCustomerData (AI block)
                // - runnable should only persist updates (so id should exist)
                if (inv.getId() == null) continue;

                // ensure relationship is set before save
                inv.setCustomer(c);

                toSave.add(inv);
                cacheKeysForToSave.add(cacheKey);
            }

            if (toSave.isEmpty()) {
                System.out.println("[SAVE-INVENTORY] Nothing to save. jobId=" + jobId + " cacheSize=" + all.size());
                return;
            }

            // 3) Save updates in batches
            int bSize = (batchSize <= 0 ? 100 : batchSize);
            int saved = customerPropertyInventoryService.saveCustomerPropertyInventoriesInBatches(toSave, bSize);

            // 4) Safe mark (helps overlayFromWhatsAppMemory + cleanup mapping)
            int marked = 0;
            for (int i = 0; i < toSave.size(); i++) {
                CustomerPropertyInventory inv = toSave.get(i);
                String cacheKey = cacheKeysForToSave.get(i);
                if (inv == null || inv.getId() == null || cacheKey == null) continue;

                WhatsAppCustomerData.markSavedInventoryId(inv.getId(), cacheKey);
                marked++;
            }

            System.out.println("[SAVE-INVENTORY] Done. jobId=" + jobId
                    + " cacheSize=" + all.size()
                    + " toSave=" + toSave.size()
                    + " saved=" + saved
                    + " marked=" + marked
                    + " batchSize=" + bSize);

        } catch (Exception e) {
            System.out.println("[WhatsAppSaveCustomerPropertyInventoryRunnable] Exception jobId=" + jobId);
            e.printStackTrace();
        }
    }
}
