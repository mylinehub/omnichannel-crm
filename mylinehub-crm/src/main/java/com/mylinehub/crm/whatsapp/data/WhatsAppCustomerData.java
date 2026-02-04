package com.mylinehub.crm.whatsapp.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import com.mylinehub.crm.config.SpringContext;
import com.mylinehub.crm.entity.CustomerPropertyInventory;
import com.mylinehub.crm.entity.Customers;
import com.mylinehub.crm.rag.dto.AiPropertyInventoryVerificationOutputDto;
import com.mylinehub.crm.service.CustomerPropertyInventoryService;
import com.mylinehub.crm.service.CustomerService;
import com.mylinehub.crm.whatsapp.dto.chat.WhatsAppCustomerDataDto;
import com.mylinehub.crm.whatsapp.dto.chat.WhatsAppCustomerParameterDataDto;
import com.mylinehub.crm.whatsapp.entity.WhatsAppChatHistory;

public class WhatsAppCustomerData {

    // Base timeout constant (2 seconds)
    private static final long BASE_TIMEOUT_SECONDS = 2;

    // Allowed values
    private final static int allowedContextChats = 25;
    public final static int aiBlockTimeHours = 2;
    public final static int maxBlockNumber = 5;

     // Cache: key = PhoneNumber + Organization
     private static final Map<String, WhatsAppCustomerDataDto> whatsAppCustomer = new ConcurrentHashMap<>();
	 // inventoryId -> cacheKey (phone + organization)
	 // If an inventoryId exists here => it is SAVED and safe to clean.
	 private static final Map<Long, String> savedInventoryIdToCacheKey = new ConcurrentHashMap<>();

 
    // Lock for whatsAppCustomer cache
    private static final ReentrantLock lockWhatsAppCustomer = new ReentrantLock(false);

    public static Map<Long, String> getSavedInventoryIdToCacheKey() {
    	return new HashMap<>(savedInventoryIdToCacheKey);
    }
    
    public static Map<String, WhatsAppCustomerDataDto> workWithWhatsAppCustomerData(WhatsAppCustomerParameterDataDto dto) {
        Map<String, WhatsAppCustomerDataDto> toReturn = null;

        while (true) {
            int queueLength = lockWhatsAppCustomer.getQueueLength();
            long timeout = queueLength + BASE_TIMEOUT_SECONDS;

            try {
                if (lockWhatsAppCustomer.tryLock(timeout, TimeUnit.SECONDS)) {
                    try {
                        toReturn = executeCustomerData(dto);
                    } finally {
                        lockWhatsAppCustomer.unlock();
                    }
                    break;

                } else {
                    System.out.println("[INFO] Could not acquire lock for workWithWhatsAppCustomerData, retrying...");
                    Thread.sleep(500);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("[ERROR] Interrupted while waiting for lock in workWithWhatsAppCustomerData");
                e.printStackTrace();
                break;

            } catch (Exception e) {
                System.err.println("[ERROR] Exception in workWithWhatsAppCustomerData:");
                e.printStackTrace();
                toReturn = null;
                break;
            }
        }

        return toReturn;
    }

    private static Map<String, WhatsAppCustomerDataDto> executeCustomerData(WhatsAppCustomerParameterDataDto dto) {
        Map<String, WhatsAppCustomerDataDto> toReturn = null;
        String key = resolveKey(dto);

        try {

            CustomerService customerService = SpringContext.getBean(CustomerService.class);
            WhatsAppCustomerDataDto current = null;

            switch (dto.getAction()) {

                case "get-one-and-chage-date-or-save-or-fetch-customer-if-applicable":
                	
                    if (key == null) {
                        System.err.println("[Information] workWithWhatsAppCustomerData: key is null, non key type operation : " + dto.getAction());
                    }
                    
                    current = whatsAppCustomer.get(key);

                    if (current == null) {
                        current = new WhatsAppCustomerDataDto();
                        current.setIsBlockedForAICount(0);
                        current.setSessionFirstMessage(true);

                        Customers customer = customerService.getCustomerByPhoneNumberAndOrganization(
                                dto.getPhoneNumber(),
                                dto.getOrganization().getOrganization()
                        );

                        if (customer == null) {
                            customer = new Customers();
                            customer.setFirstname(dto.getFirstName());
                            customer.setLastname(dto.getLastName());
                            customer.setEmail(dto.getEmail());
                            customer.setPhoneNumber(dto.getPhoneNumber());
                            customer.setWhatsAppRegisteredByPhoneNumberId(dto.getWhatsAppRegisteredByPhoneNumberId());
                            customer.setWhatsAppRegisteredByPhoneNumber(dto.getWhatsAppRegisteredByPhoneNumber());
                            customer.setWhatsApp_wa_id(dto.getWhatsApp_wa_id());
                            customer.setWhatsAppDisplayPhoneNumber(dto.getWhatsAppDisplayPhoneNumber());
                            customer.setWhatsAppPhoneNumberId(dto.getWhatsAppPhoneNumberId());
                            customer.setWhatsAppProjectId(dto.getWhatsAppProjectId());
                            customer.setBusiness(dto.getBusinessPortfolio());
                            customer.setOrganization(dto.getOrganization().getOrganization());
                            customer.setAutoWhatsAppAIReply(dto.isTurnOnAutoReply());
                            customer.setFirstWhatsAppMessageIsSend(false);
                            customer = customerService.applyDefaultValues(customer);
                            customer = customerService.createCustomerByOrganizationWithoutDTO(customer);
                        }

                        current.setCustomer(customer);
                        current.setLastVerified(new Date());
                        current.setLastUpdated(new Date());
                        current.setBlockedForAI(false);
                        whatsAppCustomer.put(key, current);

                    } else {
                        current.setSessionFirstMessage(false);
                        current.setLastVerified(new Date());
                        current.setLastUpdated(new Date());
                        whatsAppCustomer.put(key, current);
                    }

                    // IMPORTANT: return new map reference (no internal map ref out)
                    toReturn = new HashMap<>();
                    toReturn.put(key, current);
                    return toReturn;

                case "get-one":
                	
                    if (key == null) {
                        System.err.println("[Information] workWithWhatsAppCustomerData: key is null, non key type operation : " + dto.getAction());
                    }
                    
                    current = whatsAppCustomer.get(key);
                    if (current != null) {
                        // return new map reference
                        toReturn = new HashMap<>();
                        toReturn.put(key, current);
                    }
                    break;
                    
                case "get-many-by-cache-keys":
                    List<String> keys = dto.getCacheKeys();
                    
                    if (keys == null) {
                        System.err.println("[Information] workWithWhatsAppCustomerData: keys is null");
                    }

                    if (keys == null || keys.isEmpty()) return new HashMap<>();

                    toReturn = new HashMap<>();
                    for (String k : keys) {
                        if (k == null) continue;
                        WhatsAppCustomerDataDto d = whatsAppCustomer.get(k);
                        if (d != null) toReturn.put(k, d);
                    }
                    break;


                case "get":
                    // IMPORTANT: do NOT return internal ConcurrentHashMap reference
                    toReturn = new HashMap<>(whatsAppCustomer);
                    break;

                case "update":
                	
                	if (key == null) {
                        System.err.println("[Information] workWithWhatsAppCustomerData: key is null, non key type operation : " + dto.getAction());
                    }
                	
                    current = whatsAppCustomer.get(key);
                    if (current == null) {
                        current = new WhatsAppCustomerDataDto();
                        current.setIsBlockedForAICount(0);
                        current.setSessionFirstMessage(true);
                    }

                    if (current.getChatList() == null)
                        current.setChatList(new ArrayList<>());

                    List<WhatsAppChatHistory> chatList = current.getChatList();

                    if (dto.getWhatsAppChatHistory() != null) {
                        if (chatList.size() >= allowedContextChats)
                            chatList.remove(0);
                        chatList.add(dto.getWhatsAppChatHistory());
                        current.setChatList(chatList);
                    }

                    if (dto.getLanguage() != null)
                        current.setLanguage(dto.getLanguage());

                    if (dto.getWhatsAppBotThread() != null)
                        current.setWhatsAppBotThread(dto.getWhatsAppBotThread());

                    if (dto.getLanguageThread() != null)
                        current.setLanguageThread(dto.getLanguageThread());

                    if (dto.getSummarizeThread() != null)
                        current.setSummarizeThread(dto.getSummarizeThread());

                    if (dto.getScript() != null)
                        current.setScript(dto.getScript());

                    current.setLastUpdated(new Date());
                    whatsAppCustomer.put(key, current);
                    break;

                case "update-customer-email":
                	
                	if (key == null) {
                        System.err.println("[Information] workWithWhatsAppCustomerData: key is null, non key type operation : " + dto.getAction());
                    }
                	
                    current = whatsAppCustomer.get(key);
                    if (current == null) {
                        current = new WhatsAppCustomerDataDto();
                        current.setIsBlockedForAICount(0);
                        current.setSessionFirstMessage(true);
                    }

                    Customers customer = current.getCustomer();
                    if (customer != null) customer.setEmail(dto.getEmail());
                    current.setCustomer(customer);
                    current.setLastUpdated(new Date());
                    whatsAppCustomer.put(key, current);

                    System.out.println("[UPDATED] customer email update complete");
                    customerService.updateCustomerEmailById(customer.getId(), dto.getEmail());
                    break;

                case "delete":
                	
                	if (key == null) {
                        System.err.println("[Information] workWithWhatsAppCustomerData: key is null, non key type operation : " + dto.getAction());
                    }
                	
                    whatsAppCustomer.remove(key);
                    break;

                case "update-first-message-sent":
                	
                	if (key == null) {
                        System.err.println("[Information] workWithWhatsAppCustomerData: key is null, non key type operation : " + dto.getAction());
                    }
                	
                    current = whatsAppCustomer.get(key);
                    if (current == null) {
                        System.out.println("[WARN] No entry found for update-first-message-sent, key=" + key);
                        return null;
                    }
                    Customers c = current.getCustomer();
                    if (c != null) c.setFirstWhatsAppMessageIsSend(true);
                    current.setCustomer(c);
                    current.setLastUpdated(new Date());
                    whatsAppCustomer.put(key, current);
                    System.out.println("[UPDATE-FIRST] Flag updated for key=" + key);
                    break;

                case "update-language-and-customerHeuristicMessageCollation":
                	
                	if (key == null) {
                        System.err.println("[Information] workWithWhatsAppCustomerData: key is null, non key type operation : " + dto.getAction());
                    }
                	
                    current = whatsAppCustomer.get(key);
                    if (current == null) {
                        return null;
                    }

                    String messageOriginal = dto.getWhatsAppChatHistory().getMessageString();
                    String messageEnglish = (dto.getEnglishMessageToAdd() != null && !dto.getEnglishMessageToAdd().isEmpty())
                            ? dto.getEnglishMessageToAdd() : messageOriginal;

                    if (current.getCustomerHeuristicMessageCollationEnglish() == null)
                        current.setCustomerHeuristicMessageCollationEnglish(messageEnglish);
                    else
                        current.setCustomerHeuristicMessageCollationEnglish(
                                current.getCustomerHeuristicMessageCollationEnglish() + " " + messageEnglish);

                    if (current.getCustomerHeuristicMessageCollationOriginal() == null)
                        current.setCustomerHeuristicMessageCollationOriginal(messageOriginal);
                    else
                        current.setCustomerHeuristicMessageCollationOriginal(
                                current.getCustomerHeuristicMessageCollationOriginal() + " " + messageOriginal);

                    current.setLanguage(dto.getLanguage());
                    current.setLastUpdated(new Date());
                    whatsAppCustomer.put(key, current);
                    break;

                case "clear-customerHeuristicMessageCollation":
                	
                	if (key == null) {
                        System.err.println("[Information] workWithWhatsAppCustomerData: key is null, non key type operation : " + dto.getAction());
                    }
                	
                    current = whatsAppCustomer.get(key);
                    if (current == null) {
                        return null;
                    }
                    current.setCustomerHeuristicMessageCollationEnglish(null);
                    current.setCustomerHeuristicMessageCollationOriginal(null);
                    current.setLastUpdated(new Date());
                    whatsAppCustomer.put(key, current);
                    break;

                case "block-ai":
                	
                	if (key == null) {
                        System.err.println("[Information] workWithWhatsAppCustomerData: key is null, non key type operation : " + dto.getAction());
                    }
                	
                    current = whatsAppCustomer.get(key);
                    if (current == null) {
                        return null;
                    }

                    int count = current.getIsBlockedForAICount();
                    if (count > maxBlockNumber) {
                        current.setIsBlockedForAICount(count + 1);
                        current.setBlockedForAI(true);
                    } else {
                        current.setIsBlockedForAICount(count + 1);
                    }
                    current.setLastUpdated(new Date());
                    whatsAppCustomer.put(key, current);
                    break;

                case "clear-non-frequent-data":
                    System.out.println("[CLEANUP] Removing old entries...");
                    Date threshold = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(dto.getDeleteLastNDays()));
                    int removed = 0;
                    List<Customers> removedCustomers = new ArrayList<>();
                    for (String cacheKey : new ArrayList<>(whatsAppCustomer.keySet())) {
                        WhatsAppCustomerDataDto data = whatsAppCustomer.get(cacheKey);
                        if (data != null && data.getLastVerified().before(threshold)) {

                            if (data.getCustomer() != null) {
                            	removedCustomers.add(data.getCustomer());

                            	if(data.getCustomer().getPropertyInventory()!=null)
                                savedInventoryIdToCacheKey.remove(data.getCustomer().getPropertyInventory().getId());
                            }
                                
                            whatsAppCustomer.remove(cacheKey);
                            
                            removed++;
                        }
                    }
                    break;

                case "save-data-to-db":
                    saveCustomerFlagsForCleaning(customerService);
                    break;

                case "update-customer-property-inventory-from-ai":

                    if (key == null) {
                        System.err.println("[Information] workWithWhatsAppCustomerData: key is null, non key type operation : " + dto.getAction());
                    }

                    current = whatsAppCustomer.get(key);
                    if (current == null) {
                        System.out.println("[WARN] No entry found for update-customer-property-inventory-from-ai, key=" + key);
                        return null;
                    }

                    AiPropertyInventoryVerificationOutputDto out = dto.getAiPropertyInventoryVerificationOutputDto();
                    if (out == null) {
                        System.out.println("[WARN] AiPropertyInventoryVerificationOutputDto is null, key=" + key);
                        return null;
                    }

                    Customers cust2 = current.getCustomer();
                    if (cust2 == null) {
                        System.out.println("[WARN] Customer is null in memory, key=" + key);
                        return null;
                    }

                    // Optional: update customer name in memory
                    if (out.getCustomerFirstName() != null && !out.getCustomerFirstName().trim().isEmpty())
                        cust2.setFirstname(out.getCustomerFirstName().trim());

                    if (out.getCustomerLastName() != null && !out.getCustomerLastName().trim().isEmpty())
                        cust2.setLastname(out.getCustomerLastName().trim());

                    // ---- Ensure inventory object exists in memory ----
                    CustomerPropertyInventory inv = cust2.getPropertyInventory();
                    if (inv == null) {
                        inv = new CustomerPropertyInventory();
                        inv.setCustomer(cust2);
                        cust2.setPropertyInventory(inv);
                    } else {
                        // ensure relationship is correct
                        inv.setCustomer(cust2);
                    }

                    // --------- Copy/Transform fields (LLM output -> entity) ----------
                    java.util.function.Function<String, String> clean = (s) -> (s == null) ? null : s.trim();
                    java.util.function.Predicate<String> hasText = (s) -> (s != null && !s.trim().isEmpty());

                    if (hasText.test(out.getPurpose())) inv.setPurpose(clean.apply(out.getPurpose()));
                    if (hasText.test(out.getPremiseName())) inv.setPremiseName(clean.apply(out.getPremiseName()));
                    if (hasText.test(out.getPropertyType())) inv.setPropertyType(clean.apply(out.getPropertyType()));

                    inv.setRent(out.isRent());

                    if (out.getRentValue() != null) inv.setRentValue(out.getRentValue());
                    if (out.getBhk() != null) inv.setBhk(out.getBhk());
                    if (hasText.test(out.getFurnishedType())) inv.setFurnishedType(clean.apply(out.getFurnishedType()));
                    if (out.getSqFt() != null) inv.setSqFt(out.getSqFt());
                    if (hasText.test(out.getCity())) inv.setCity(clean.apply(out.getCity()));
                    if (hasText.test(out.getNearby())) inv.setNearby(clean.apply(out.getNearby()));
                    if (hasText.test(out.getArea())) inv.setArea(clean.apply(out.getArea()));
                    if (out.getPropertyAge() != null) inv.setPropertyAge(out.getPropertyAge());
                    if (hasText.test(out.getUnitType())) inv.setUnitType(clean.apply(out.getUnitType()));
                    if (hasText.test(out.getTenant())) inv.setTenant(clean.apply(out.getTenant()));
                    if (hasText.test(out.getFacing())) inv.setFacing(clean.apply(out.getFacing()));
                    if (out.getTotalFloors() != null) inv.setTotalFloors(out.getTotalFloors());
                    if (hasText.test(out.getBrokerage())) inv.setBrokerage(clean.apply(out.getBrokerage()));
                    if (out.getBalconies() != null) inv.setBalconies(out.getBalconies());
                    if (out.getWashroom() != null) inv.setWashroom(out.getWashroom());
                    if (hasText.test(out.getUnitNo())) inv.setUnitNo(clean.apply(out.getUnitNo()));
                    if (hasText.test(out.getFloorNo())) inv.setFloorNo(clean.apply(out.getFloorNo()));
                    if (hasText.test(out.getPropertyDescription())) inv.setPropertyDescription1(clean.apply(out.getPropertyDescription()));
                    if (out.getMoreThanOneProperty() != null) inv.setMoreThanOneProperty(out.getMoreThanOneProperty());
                    if (out.getAvailable() != null) inv.setAvailable(out.getAvailable());
                    inv.setUpdatedByAi(true);
                    //  Persist FIRST TIME inventory immediately (only if new)
                    if (inv.getId() == null) {

                        // IMPORTANT: customer must already have DB id for FK
                        if (cust2.getId() == null) {
                            System.out.println("[INV-CREATE-ERROR] Customer has no DB id yet, cannot insert inventory. key=" + key);
                        } else {
                            try {
                                CustomerPropertyInventoryService invService =
                                        SpringContext.getBean(CustomerPropertyInventoryService.class);

                                // requires you to add saveOne() to service OR use repository directly
                                CustomerPropertyInventory saved = invService.saveOne(inv);

                                // Put saved inv back into memory (now has ID)
                                cust2.setPropertyInventory(saved);
                                inv = saved;

                                if (inv.getId() != null) {
                                    savedInventoryIdToCacheKey.put(inv.getId(), key);
                                }

                                System.out.println("[INV-CREATED] invId=" + inv.getId() + " key=" + key);

                            } catch (Exception ex) {
                                System.out.println("[INV-CREATE-ERROR] Failed to insert new inventory. key=" + key);
                                ex.printStackTrace();
                            }
                        }
                    }

                    // Save back in memory object
                    current.setCustomer(cust2);
                    current.setLastUpdated(new Date());
                    current.setLastVerified(new Date());

                    // For already-existing inventory, keep mapping for overlay/cleanup too
                    if (inv != null && inv.getId() != null) {
                        savedInventoryIdToCacheKey.put(inv.getId(), key);
                    }

                    whatsAppCustomer.put(key, current);
                    break;

                default:
                    break;
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Exception in workWithWhatsAppCustomerData:");
            e.printStackTrace();
        }

        return toReturn;
    }

    public static void saveCustomerPropertyInventory(List<CustomerPropertyInventory> inventories,CustomerPropertyInventoryService customerPropertyInventoryService, int batchSize) {

        System.out.println("[saveCustomerPropertyInventoryForCleaning] Saving inventory from memory...");

        if (customerPropertyInventoryService == null) {
            System.out.println("[saveCustomerPropertyInventoryForCleaning] customerService is null. Skipping.");
            return;
        }

        if (inventories.isEmpty()) {
            System.out.println("[saveCustomerPropertyInventoryForCleaning] No inventories found.");
            return;
        }

        int bSize = (batchSize <= 0 ? 100 : batchSize);

        try {
            int saved = customerPropertyInventoryService.saveCustomerPropertyInventoriesInBatches(inventories, bSize);
            System.out.println("[saveCustomerPropertyInventoryForCleaning] Saved inventories count=" + saved);
        } catch (Exception e) {
            System.out.println("[saveCustomerPropertyInventoryForCleaning] ERROR while saving inventories");
            e.printStackTrace();
        }
    }

    
    public static void saveCustomerFlagsForCleaning(CustomerService customerService) {

        System.out.println("[save-data-to-db] save-data-to-db old entries...");
        List<Customers> customers = new ArrayList<>();
        for (String cacheKey : new ArrayList<>(whatsAppCustomer.keySet())) {
            WhatsAppCustomerDataDto data = whatsAppCustomer.get(cacheKey);
            if (data.getCustomer() != null)
                customers.add(data.getCustomer());
        }

        if (customers == null || customers.isEmpty()) {
            System.out.println("No customers to update.");
            return;
        }

        final int BATCH_SIZE = 150;

        for (int i = 0; i < customers.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, customers.size());
            List<Customers> batch = customers.subList(i, end);

            List<Long> ids = batch.stream().map(Customers::getId).collect(Collectors.toList());

            List<Boolean> firstWhatsAppFlags = batch.stream()
                    .map(Customers::isFirstWhatsAppMessageIsSend)
                    .collect(Collectors.toList());

            List<String> preferredLanguages = batch.stream()
                    .map(Customers::getPreferredLanguage)
                    .collect(Collectors.toList());

            List<String> secondPreferredLanguages = batch.stream()
                    .map(Customers::getSecondPreferredLanguage)
                    .collect(Collectors.toList());

            System.out.println("Processing batch: " + (i / BATCH_SIZE + 1) + " | Size: " + batch.size());
            System.out.println("Customer IDs: " + ids);

            boolean firstFlag = firstWhatsAppFlags.get(0);
            String preferredLang = preferredLanguages.get(0);
            String secondPreferredLang = secondPreferredLanguages.get(0);

            int updatedFirstFlag = customerService.updateCustomerFirstWhatsAppMessageFlagForBatch(firstFlag, ids);
            int updatedPreferred = customerService.updateCustomerPreferredLanguageForBatch(preferredLang, ids);
            int updatedSecondPreferred = customerService.updateCustomerSecondPreferredLanguageForBatch(secondPreferredLang, ids);

            System.out.println("Updated firstWhatsAppMessageFlag: " + updatedFirstFlag);
            System.out.println("Updated preferredLanguage: " + updatedPreferred);
            System.out.println("Updated secondPreferredLanguage: " + updatedSecondPreferred);
        }
    }

    private static String resolveKey(WhatsAppCustomerParameterDataDto dto) {
        String phone = (dto.getPhoneNumber() != null && !dto.getPhoneNumber().trim().isEmpty())
                ? dto.getPhoneNumber().trim()
                : null;

        if (phone == null || phone.isEmpty()) {
            System.err.println("[WARN] Unable to resolve phone number for cache key. Returning null.");
            return null;
        }

        String org = dto.getOrganization() != null && dto.getOrganization().getOrganization() != null
                ? dto.getOrganization().getOrganization().trim()
                : "UNKNOWN_ORG";

        String key = phone + org;
        return key;
    }

    public static void markSavedInventoryId(Long inventoryId, String cacheKey) {
        if (inventoryId == null) return;
        if (cacheKey == null || cacheKey.trim().isEmpty()) return;
        savedInventoryIdToCacheKey.put(inventoryId, cacheKey);
    }

    private static <T> List<List<T>> partitionList(List<T> list, int size) {
        if (list == null || list.isEmpty()) return Collections.emptyList();
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return partitions;
    }
}
