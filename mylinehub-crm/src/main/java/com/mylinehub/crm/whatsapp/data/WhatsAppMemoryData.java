package com.mylinehub.crm.whatsapp.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.mylinehub.crm.whatsapp.data.dto.FileCategoryDateCheckDto;
import com.mylinehub.crm.whatsapp.data.dto.WhatsAppTemplateVariableListDto;
import com.mylinehub.crm.whatsapp.entity.WhatsAppOpenAiAccount;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumber;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumberTemplateVariable;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumberTemplates;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPrompt;

public class WhatsAppMemoryData {

    // Base timeout constant (2 seconds)
    private static final long BASE_TIMEOUT_SECONDS = 2;

    // PhoneNumber as initial key, date as second key.
    private static Map<String, Map<String, FileCategoryDateCheckDto>> verifyIfCategoryCreatedMap = new ConcurrentHashMap<>();

    // Key is Phone Number
    private static Map<String, WhatsAppPhoneNumber> whatsAppPhoneNumbers = new ConcurrentHashMap<>();

    // Key is Organization
    private static Map<String, List<WhatsAppOpenAiAccount>> whatsAppOpenAIAccount = new ConcurrentHashMap<>();

    // Key is Phone Number
    private static Map<String, List<WhatsAppPhoneNumberTemplates>> whatsAppPhoneNumbersTemplates = new ConcurrentHashMap<>();

    // Key is Phone Number
    private static Map<String, List<WhatsAppPhoneNumberTemplateVariable>> whatsAppPhoneNumbersTemplateVariales = new ConcurrentHashMap<>();

    // phoneNumber -> category -> WhatsAppPrompt
    private static Map<String, Map<String, WhatsAppPrompt>> whatsAppPhoneCategoryPrompts = new ConcurrentHashMap<>();

    
    // Locks for WhatsApp data maps
    private static final ReentrantLock lockVerifyCategoryMap = new ReentrantLock(false);
    private static final ReentrantLock lockWhatsAppPhoneNumbers = new ReentrantLock(false);
    private static final ReentrantLock lockWhatsAppOpenAIAccount = new ReentrantLock(false);
    private static final ReentrantLock lockWhatsAppPhoneNumbersTemplates = new ReentrantLock(false);
    private static final ReentrantLock lockWhatsAppPhoneNumbersTemplateVariables = new ReentrantLock(false);
    private static final ReentrantLock lockWhatsAppPhoneCategoryPrompts = new ReentrantLock(false);

    
    // ============================================================
    // 1. workWithVerifyIfCategoryCreatedMapData
    // ============================================================
    public static Map<String, Map<String, FileCategoryDateCheckDto>> workWithVerifyIfCategoryCreatedMapData(
            String phoneNumber, Map<String, FileCategoryDateCheckDto> details, String action) {

        Map<String, Map<String, FileCategoryDateCheckDto>> toReturn = null;

        while (true) { // retry loop
            int queueLength = lockVerifyCategoryMap.getQueueLength();
            long timeout = BASE_TIMEOUT_SECONDS + queueLength;

            try {
                if (lockVerifyCategoryMap.tryLock(timeout, TimeUnit.SECONDS)) {
                    try {
                        System.out.println("[VERIFY-CATEGORY] Action: " + action + ", Phone: " + phoneNumber);
                        Map<String, FileCategoryDateCheckDto> current;

                        switch (action) {
                            case "get-one":
                                current = verifyIfCategoryCreatedMap.get(phoneNumber);
                                if (current != null) {
                                    // return new map ref (inner map copy)
                                    toReturn = new HashMap<>();
                                    toReturn.put(phoneNumber, copyInnerCategoryMap(current));
                                    System.out.println("[VERIFY-CATEGORY] Found entry for phone: " + phoneNumber);
                                }
                                break;

                            case "get":
                                // return new map ref (outer+inner map copies)
                                toReturn = copyVerifyCategoryCreatedMap(verifyIfCategoryCreatedMap);
                                System.out.println("[VERIFY-CATEGORY] Returned full map. Size: " + verifyIfCategoryCreatedMap.size());
                                break;

                            case "update":
                                // store copy (do not store caller's map reference)
                                verifyIfCategoryCreatedMap.put(phoneNumber, copyInnerCategoryMap(details));
                                System.out.println("[VERIFY-CATEGORY] Updated entry for phone: " + phoneNumber);
                                break;

                            case "reset":
                                verifyIfCategoryCreatedMap = new ConcurrentHashMap<>();
                                System.out.println("[VERIFY-CATEGORY] Map reset.");
                                break;

                            default:
                                System.out.println("[VERIFY-CATEGORY] Unknown action.");
                                break;
                        }

                    } finally {
                        lockVerifyCategoryMap.unlock();
                    }
                    break;

                } else {
                    System.out.println("[INFO] Could not acquire lockVerifyCategoryMap, retrying...");
                    Thread.sleep(500);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("[ERROR] Interrupted while waiting for lock in workWithVerifyIfCategoryCreatedMapData");
                e.printStackTrace();
                break;

            } catch (Exception e) {
                System.out.println("[ERROR] Exception in workWithVerifyIfCategoryCreatedMapData:");
                e.printStackTrace();
                break;
            }
        }

        return toReturn;
    }

    // ============================================================
    // 2. workWithWhatsAppPhoneNumberData
    // ============================================================
    public static Map<String, WhatsAppPhoneNumber> workWithWhatsAppPhoneNumberData(
            String phoneNumber, WhatsAppPhoneNumber details, String action) {

        Map<String, WhatsAppPhoneNumber> toReturn = null;

        while (true) {
            int queueLength = lockWhatsAppPhoneNumbers.getQueueLength();
            long timeout = BASE_TIMEOUT_SECONDS + queueLength;

            try {
                if (lockWhatsAppPhoneNumbers.tryLock(timeout, TimeUnit.SECONDS)) {
                    try {
                        System.out.println("[PHONE-NUMBER] Action: " + action + ", Phone: " + phoneNumber);
                        WhatsAppPhoneNumber current;

                        switch (action) {
                            case "set-new":
                                whatsAppPhoneNumbers = new ConcurrentHashMap<>();
                                System.out.println("[PHONE-NUMBER] Set new ConcurrentHashMap.");
                                break;

                            case "get-one":
                                current = whatsAppPhoneNumbers.get(phoneNumber);
                                if (current != null) {
                                    toReturn = new HashMap<>();
                                    toReturn.put(phoneNumber, current); // object ref unchanged; map ref is new
                                    System.out.println("[PHONE-NUMBER] Found entry for phone: " + phoneNumber);
                                }
                                break;

                            case "get":
                                // return new map ref (snapshot)
                                toReturn = new HashMap<>(whatsAppPhoneNumbers);
                                System.out.println("[PHONE-NUMBER] Returning full map. Size: " + whatsAppPhoneNumbers.size());
                                break;

                            case "update":
                                whatsAppPhoneNumbers.put(phoneNumber, details);
                                System.out.println("[PHONE-NUMBER] Updated phone: " + phoneNumber);
                                break;

                            case "delete-all":
                                current = whatsAppPhoneNumbers.get(phoneNumber);
                                if (current == null) {
                                    System.out.println("[PHONE-NUMBER] No entry found to delete.");
                                    return null;
                                }
                                whatsAppPhoneNumbers.remove(phoneNumber);
                                System.out.println("[PHONE-NUMBER] Deleted phone: " + phoneNumber);
                                break;

                            case "reset":
                                whatsAppPhoneNumbers = new ConcurrentHashMap<>();
                                System.out.println("[PHONE-NUMBER] Reset map.");
                                break;

                            default:
                                System.out.println("[PHONE-NUMBER] Unknown action.");
                                break;
                        }

                    } finally {
                        lockWhatsAppPhoneNumbers.unlock();
                    }
                    break;

                } else {
                    System.out.println("[INFO] Could not acquire lockWhatsAppPhoneNumbers, retrying...");
                    Thread.sleep(500);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("[ERROR] Interrupted while waiting for lock in workWithWhatsAppPhoneNumberData");
                e.printStackTrace();
                break;

            } catch (Exception e) {
                System.out.println("[ERROR] Exception in workWithWhatsAppPhoneNumberData:");
                e.printStackTrace();
                break;
            }
        }

        return toReturn;
    }

    // ============================================================
    // 3. workWithwhatsAppOpenAIAccount
    // ============================================================
    public static Map<String, List<WhatsAppOpenAiAccount>> workWithwhatsAppOpenAIAccount(
            String organization, WhatsAppOpenAiAccount details, String action) {

        Map<String, List<WhatsAppOpenAiAccount>> toReturn = null;

        while (true) {
            int queueLength = lockWhatsAppOpenAIAccount.getQueueLength();
            long timeout = BASE_TIMEOUT_SECONDS + queueLength;

            try {
                if (lockWhatsAppOpenAIAccount.tryLock(timeout, TimeUnit.SECONDS)) {
                    try {
                        System.out.println("[OPENAI-ACCOUNT] Action: " + action + ", Org: " + organization);
                        List<WhatsAppOpenAiAccount> current;

                        switch (action) {
                            case "set-new":
                                whatsAppOpenAIAccount = new ConcurrentHashMap<>();
                                System.out.println("[OPENAI-ACCOUNT] Initialized new ConcurrentHashMap.");
                                break;

                            case "get-one":
                                current = whatsAppOpenAIAccount.get(organization);
                                if (current != null) {
                                    toReturn = new HashMap<>();
                                    toReturn.put(organization, new ArrayList<>(current)); // list ref copy
                                    System.out.println("[OPENAI-ACCOUNT] Found accounts for org: " + organization);
                                }
                                break;

                            case "get":
                                // map+list snapshot
                                toReturn = copyMapOfLists(whatsAppOpenAIAccount);
                                System.out.println("[OPENAI-ACCOUNT] Returning full account map. Size: " + whatsAppOpenAIAccount.size());
                                break;

                            case "update":
                                current = whatsAppOpenAIAccount.getOrDefault(organization, new ArrayList<>());
                                // store list copy so external ref not kept
                                current = new ArrayList<>(current);
                                current.add(details);
                                whatsAppOpenAIAccount.put(organization, current);
                                System.out.println("[OPENAI-ACCOUNT] Added account for org: " + organization);
                                break;

                            case "update-existing":
                                current = whatsAppOpenAIAccount.get(organization);
                                if (current == null) {
                                    System.out.println("[OPENAI-ACCOUNT] No existing accounts found for update.");
                                    return null;
                                }
                                current = new ArrayList<>(current); // detach from external refs
                                int position = -1;
                                for (int i = 0; i < current.size(); i++) {
                                    if (current.get(i).getId() == details.getId()) {
                                        position = i;
                                        break;
                                    }
                                }
                                if (position != -1) {
                                    current.set(position, details);
                                    whatsAppOpenAIAccount.put(organization, current);
                                    System.out.println("[OPENAI-ACCOUNT] Updated existing account at position: " + position);
                                } else {
                                    System.out.println("[OPENAI-ACCOUNT] No matching account found to update.");
                                }
                                break;

                            case "delete":
                                current = whatsAppOpenAIAccount.get(organization);
                                if (current == null) {
                                    System.out.println("[OPENAI-ACCOUNT] No accounts found to delete.");
                                    return null;
                                }
                                current = new ArrayList<>(current); // detach
                                int deletePos = -1;
                                for (int i = 0; i < current.size(); i++) {
                                    if (current.get(i).getId() == details.getId()) {
                                        deletePos = i;
                                        break;
                                    }
                                }
                                if (deletePos != -1) {
                                    current.remove(deletePos);
                                    whatsAppOpenAIAccount.put(organization, current);
                                    System.out.println("[OPENAI-ACCOUNT] Deleted account at position: " + deletePos);
                                } else {
                                    System.out.println("[OPENAI-ACCOUNT] No matching account found to delete.");
                                }
                                break;

                            case "delete-all":
                                whatsAppOpenAIAccount.remove(organization);
                                System.out.println("[OPENAI-ACCOUNT] Deleted all accounts for org: " + organization);
                                break;

                            case "reset":
                                whatsAppOpenAIAccount = new ConcurrentHashMap<>();
                                System.out.println("[OPENAI-ACCOUNT] Reset entire account map.");
                                break;

                            default:
                                System.out.println("[OPENAI-ACCOUNT] Unknown action.");
                                break;
                        }

                    } finally {
                        lockWhatsAppOpenAIAccount.unlock();
                    }
                    break;

                } else {
                    System.out.println("[INFO] Could not acquire lockWhatsAppOpenAIAccount, retrying...");
                    Thread.sleep(500);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("[ERROR] Interrupted while waiting for lock in workWithwhatsAppOpenAIAccount");
                e.printStackTrace();
                break;

            } catch (Exception e) {
                System.out.println("[ERROR] Exception in workWithwhatsAppOpenAIAccount:");
                e.printStackTrace();
                break;
            }
        }

        return toReturn;
    }

    // ============================================================
    // 4. workWithWhatsAppPhoneNumbersTemplates
    // ============================================================
    public static Map<String, List<WhatsAppPhoneNumberTemplates>> workWithWhatsAppPhoneNumbersTemplates(
            String phoneNumber, WhatsAppPhoneNumberTemplates details, String action) {

        Map<String, List<WhatsAppPhoneNumberTemplates>> toReturn = null;

        while (true) {
            int queueLength = lockWhatsAppPhoneNumbersTemplates.getQueueLength();
            long timeout = BASE_TIMEOUT_SECONDS + queueLength;

            try {
                if (lockWhatsAppPhoneNumbersTemplates.tryLock(timeout, TimeUnit.SECONDS)) {
                    try {
                        System.out.println("[TEMPLATES] Action: " + action + ", Phone: " + phoneNumber);
                        List<WhatsAppPhoneNumberTemplates> current;

                        switch (action) {
                            case "set-new":
                                whatsAppPhoneNumbersTemplates = new ConcurrentHashMap<>();
                                System.out.println("[TEMPLATES] Initialized new ConcurrentHashMap.");
                                break;

                            case "get-one":
                                current = whatsAppPhoneNumbersTemplates.get(phoneNumber);
                                if (current != null) {
                                    toReturn = new HashMap<>();
                                    toReturn.put(phoneNumber, new ArrayList<>(current)); // list ref copy
                                    System.out.println("[TEMPLATES] Retrieved template list for phone: " + phoneNumber);
                                }
                                break;

                            case "get":
                                toReturn = copyMapOfListsTemplates(whatsAppPhoneNumbersTemplates);
                                System.out.println("[TEMPLATES] Returned full map. Size: " + whatsAppPhoneNumbersTemplates.size());
                                break;

                            case "update":
                                current = whatsAppPhoneNumbersTemplates.getOrDefault(phoneNumber, new ArrayList<>());
                                current = new ArrayList<>(current);
                                current.add(details);
                                whatsAppPhoneNumbersTemplates.put(phoneNumber, current);
                                System.out.println("[TEMPLATES] Added new template for phone: " + phoneNumber);
                                break;

                            case "update-existing":
                                current = whatsAppPhoneNumbersTemplates.get(phoneNumber);
                                if (current == null) return null;

                                current = new ArrayList<>(current);
                                int updatePos = -1;
                                for (int i = 0; i < current.size(); i++) {
                                    if (current.get(i).getId() == details.getId()) {
                                        updatePos = i;
                                        break;
                                    }
                                }

                                if (updatePos != -1) {
                                    current.set(updatePos, details);
                                    whatsAppPhoneNumbersTemplates.put(phoneNumber, current);
                                    System.out.println("[TEMPLATES] Updated existing template at position: " + updatePos);
                                } else {
                                    System.out.println("[TEMPLATES] No template found to update.");
                                }
                                break;

                            case "delete":
                                current = whatsAppPhoneNumbersTemplates.get(phoneNumber);
                                if (current == null) return null;

                                current = new ArrayList<>(current);
                                int deletePos = -1;
                                for (int i = 0; i < current.size(); i++) {
                                    if (current.get(i).getId() == details.getId()) {
                                        deletePos = i;
                                        break;
                                    }
                                }

                                if (deletePos != -1) {
                                    current.remove(deletePos);
                                    whatsAppPhoneNumbersTemplates.put(phoneNumber, current);
                                    System.out.println("[TEMPLATES] Deleted template at position: " + deletePos);
                                } else {
                                    System.out.println("[TEMPLATES] No template found to delete.");
                                }
                                break;

                            case "delete-all":
                                whatsAppPhoneNumbersTemplates.remove(phoneNumber);
                                System.out.println("[TEMPLATES] Removed all templates for phone: " + phoneNumber);
                                break;

                            case "reset":
                                whatsAppPhoneNumbersTemplates = new ConcurrentHashMap<>();
                                System.out.println("[TEMPLATES] Reset all templates.");
                                break;

                            default:
                                System.out.println("[TEMPLATES] Unknown action: " + action);
                                break;
                        }

                    } finally {
                        lockWhatsAppPhoneNumbersTemplates.unlock();
                    }
                    break;

                } else {
                    System.out.println("[INFO] Could not acquire lockWhatsAppPhoneNumbersTemplates, retrying...");
                    Thread.sleep(500);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("[ERROR] Interrupted while waiting for lock in workWithWhatsAppPhoneNumbersTemplates");
                e.printStackTrace();
                break;

            } catch (Exception e) {
                System.out.println("[ERROR] Exception in workWithWhatsAppPhoneNumbersTemplates:");
                e.printStackTrace();
                break;
            }
        }

        return toReturn;
    }

    // ============================================================
    // 5. workWithWhatsAppPhoneNumberTemplateVariable
    // ============================================================
    public static Map<String, List<WhatsAppPhoneNumberTemplateVariable>> workWithWhatsAppPhoneNumberTemplateVariable(
            String phoneNumber,
            WhatsAppPhoneNumberTemplateVariable details,
            WhatsAppTemplateVariableListDto whatsAppTemplateVariableListDto,
            String action
    ) {
        Map<String, List<WhatsAppPhoneNumberTemplateVariable>> toReturn = null;

        while (true) {
            int queueLength = lockWhatsAppPhoneNumbersTemplateVariables.getQueueLength();
            long timeout = BASE_TIMEOUT_SECONDS + queueLength;

            try {
                if (lockWhatsAppPhoneNumbersTemplateVariables.tryLock(timeout, TimeUnit.SECONDS)) {
                    try {
                        System.out.println("[TEMPLATE-VARIABLE] Action: " + action + ", Phone: " + phoneNumber);
                        List<WhatsAppPhoneNumberTemplateVariable> current;

                        switch (action) {
                            case "set-new":
                                whatsAppPhoneNumbersTemplateVariales = new ConcurrentHashMap<>();
                                System.out.println("[TEMPLATE-VARIABLE] Initialized new ConcurrentHashMap.");
                                break;

                            case "get-one":
                                current = whatsAppPhoneNumbersTemplateVariales.get(phoneNumber);
                                if (current != null) {
                                    toReturn = new HashMap<>();
                                    toReturn.put(phoneNumber, new ArrayList<>(current)); // list ref copy
                                    System.out.println("[TEMPLATE-VARIABLE] Retrieved variable list for phone: " + phoneNumber);
                                }
                                break;

                            case "get":
                                toReturn = copyMapOfListsTemplateVars(whatsAppPhoneNumbersTemplateVariales);
                                System.out.println("[TEMPLATE-VARIABLE] Returned full map. Size: " + whatsAppPhoneNumbersTemplateVariales.size());
                                break;

                            case "update":
                                current = whatsAppPhoneNumbersTemplateVariales.getOrDefault(phoneNumber, new ArrayList<>());
                                current = new ArrayList<>(current);
                                current.add(details);
                                whatsAppPhoneNumbersTemplateVariales.put(phoneNumber, current);
                                System.out.println("[TEMPLATE-VARIABLE] Added variable for phone: " + phoneNumber);
                                break;

                            case "update-all":
                                // store copy (do not keep caller list ref)
                                List<WhatsAppPhoneNumberTemplateVariable> incoming = whatsAppTemplateVariableListDto != null
                                        ? whatsAppTemplateVariableListDto.getToUpdate() : null;
                                whatsAppPhoneNumbersTemplateVariales.put(phoneNumber, (incoming == null) ? null : new ArrayList<>(incoming));
                                System.out.println("[TEMPLATE-VARIABLE] Bulk updated variables for phone: " + phoneNumber);
                                break;

                            case "update-existing":
                                current = whatsAppPhoneNumbersTemplateVariales.get(phoneNumber);
                                if (current == null) return null;

                                current = new ArrayList<>(current);
                                int updatePos = -1;
                                for (int i = 0; i < current.size(); i++) {
                                    if (current.get(i).getId() == details.getId()) {
                                        updatePos = i;
                                        break;
                                    }
                                }

                                if (updatePos != -1) {
                                    current.set(updatePos, details);
                                    whatsAppPhoneNumbersTemplateVariales.put(phoneNumber, current);
                                    System.out.println("[TEMPLATE-VARIABLE] Updated variable at position: " + updatePos);
                                } else {
                                    System.out.println("[TEMPLATE-VARIABLE] No variable found to update.");
                                }
                                break;

                            case "delete":
                                current = whatsAppPhoneNumbersTemplateVariales.get(phoneNumber);
                                if (current == null) return null;

                                current = new ArrayList<>(current);
                                int deletePos = -1;
                                for (int i = 0; i < current.size(); i++) {
                                    if (current.get(i).getId() == details.getId()) {
                                        deletePos = i;
                                        break;
                                    }
                                }

                                if (deletePos != -1) {
                                    current.remove(deletePos);
                                    whatsAppPhoneNumbersTemplateVariales.put(phoneNumber, current);
                                    System.out.println("[TEMPLATE-VARIABLE] Deleted variable at position: " + deletePos);
                                } else {
                                    System.out.println("[TEMPLATE-VARIABLE] No variable found to delete.");
                                }
                                break;

                            case "delete-all":
                                whatsAppPhoneNumbersTemplateVariales.remove(phoneNumber);
                                System.out.println("[TEMPLATE-VARIABLE] Removed all variables for phone: " + phoneNumber);
                                break;

                            case "reset":
                                whatsAppPhoneNumbersTemplateVariales = new ConcurrentHashMap<>();
                                System.out.println("[TEMPLATE-VARIABLE] Reset all template variables.");
                                break;

                            default:
                                System.out.println("[TEMPLATE-VARIABLE] Unknown action: " + action);
                                break;
                        }

                    } finally {
                        lockWhatsAppPhoneNumbersTemplateVariables.unlock();
                    }
                    break;

                } else {
                    System.out.println("[INFO] Could not acquire lockWhatsAppPhoneNumbersTemplateVariables, retrying...");
                    Thread.sleep(500);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("[ERROR] Interrupted while waiting for lock in workWithWhatsAppPhoneNumberTemplateVariable");
                e.printStackTrace();
                break;

            } catch (Exception e) {
                System.out.println("[ERROR] Exception in workWithWhatsAppPhoneNumberTemplateVariable:");
                e.printStackTrace();
                break;
            }
        }

        return toReturn;
    }

 // ============================================================
 // X. workWithWhatsAppPhoneCategoryPrompts
 // Key1 = phoneNumber
 // Key2 = category
 // Value = WhatsAppPrompt
 // ============================================================
 public static Map<String, Map<String, WhatsAppPrompt>> workWithWhatsAppPhoneCategoryPrompts(
         String phoneNumber,
         String category,
         WhatsAppPrompt details,
         Map<String, WhatsAppPrompt> categoryPromptMap,
         String action
 ) {
     Map<String, Map<String, WhatsAppPrompt>> toReturn = null;

     while (true) {
         int queueLength = lockWhatsAppPhoneCategoryPrompts.getQueueLength();
         long timeout = BASE_TIMEOUT_SECONDS + queueLength;

         try {
             if (lockWhatsAppPhoneCategoryPrompts.tryLock(timeout, TimeUnit.SECONDS)) {
                 try {
                     System.out.println("[PHONE-CATEGORY-PROMPTS] Action: " + action
                             + ", Phone: " + phoneNumber + ", Category: " + category);

                     switch (action) {

                         case "set-new":
                             whatsAppPhoneCategoryPrompts = new ConcurrentHashMap<>();
                             System.out.println("[PHONE-CATEGORY-PROMPTS] Initialized new ConcurrentHashMap.");
                             break;

                         case "get-one": {
                             Map<String, WhatsAppPrompt> inner = whatsAppPhoneCategoryPrompts.get(phoneNumber);
                             if (inner != null) {
                                 toReturn = new HashMap<>();
                                 toReturn.put(phoneNumber, copyInnerPromptMap(inner));
                                 System.out.println("[PHONE-CATEGORY-PROMPTS] Retrieved prompt map for phone: " + phoneNumber);
                             }
                             break;
                         }

                         case "get": {
                             toReturn = copyOuterPhoneCategoryPromptMap(whatsAppPhoneCategoryPrompts);
                             System.out.println("[PHONE-CATEGORY-PROMPTS] Returned full map. Size: " + whatsAppPhoneCategoryPrompts.size());
                             break;
                         }

                         case "get-one-category": {
                             // returns outer map with one phone, and inside map contains only that category if exists
                             Map<String, WhatsAppPrompt> inner = whatsAppPhoneCategoryPrompts.get(phoneNumber);
                             if (inner != null && category != null) {
                                 WhatsAppPrompt p = inner.get(category);
                                 if (p != null) {
                                     Map<String, WhatsAppPrompt> only = new HashMap<>();
                                     only.put(category, p);
                                     toReturn = new HashMap<>();
                                     toReturn.put(phoneNumber, only);
                                     System.out.println("[PHONE-CATEGORY-PROMPTS] Retrieved single category prompt for phone.");
                                 }
                             }
                             break;
                         }

                         case "update": {
                             // add/replace prompt for phone+category
                             if (phoneNumber == null || category == null || details == null) {
                                 System.out.println("[PHONE-CATEGORY-PROMPTS] update skipped due to null inputs.");
                                 break;
                             }

                             Map<String, WhatsAppPrompt> inner = whatsAppPhoneCategoryPrompts.getOrDefault(phoneNumber, new ConcurrentHashMap<>());
                             // detach to avoid external refs
                             inner = new HashMap<>(inner);

                             inner.put(category, details);
                             whatsAppPhoneCategoryPrompts.put(phoneNumber, new ConcurrentHashMap<>(inner));

                             System.out.println("[PHONE-CATEGORY-PROMPTS] Upserted prompt for phone=" + phoneNumber + " category=" + category);
                             break;
                         }

                         case "update-all": {
                             // replace full category->prompt map for a phone
                             if (phoneNumber == null) {
                                 System.out.println("[PHONE-CATEGORY-PROMPTS] update-all skipped: phoneNumber null");
                                 break;
                             }
                             Map<String, WhatsAppPrompt> safe = (categoryPromptMap == null) ? null : copyInnerPromptMap(categoryPromptMap);
                             if (safe == null) {
                                 whatsAppPhoneCategoryPrompts.remove(phoneNumber);
                             } else {
                                 whatsAppPhoneCategoryPrompts.put(phoneNumber, new ConcurrentHashMap<>(safe));
                             }
                             System.out.println("[PHONE-CATEGORY-PROMPTS] Replaced full prompt map for phone: " + phoneNumber);
                             break;
                         }

                         case "update-existing": {
                             // update prompt by id (find and replace within that phone map)
                             if (phoneNumber == null || details == null) {
                                 System.out.println("[PHONE-CATEGORY-PROMPTS] update-existing skipped due to null inputs.");
                                 break;
                             }

                             Map<String, WhatsAppPrompt> inner = whatsAppPhoneCategoryPrompts.get(phoneNumber);
                             if (inner == null || inner.isEmpty()) {
                                 System.out.println("[PHONE-CATEGORY-PROMPTS] No existing map for phone to update-existing.");
                                 return null;
                             }

                             Map<String, WhatsAppPrompt> detached = new HashMap<>(inner);

                             String foundCategory = null;
                             for (Map.Entry<String, WhatsAppPrompt> e : detached.entrySet()) {
                                 WhatsAppPrompt p = e.getValue();
                                 if (p != null && p.getId() != null && p.getId().equals(details.getId())) {
                                     foundCategory = e.getKey();
                                     break;
                                 }
                             }

                             if (foundCategory != null) {
                                 detached.put(foundCategory, details);
                                 whatsAppPhoneCategoryPrompts.put(phoneNumber, new ConcurrentHashMap<>(detached));
                                 System.out.println("[PHONE-CATEGORY-PROMPTS] Updated existing prompt id=" + details.getId()
                                         + " at category=" + foundCategory);
                             } else {
                                 System.out.println("[PHONE-CATEGORY-PROMPTS] No matching prompt id found to update-existing.");
                             }
                             break;
                         }

                         case "delete": {
                             // delete a category prompt for a phone OR delete by id if category is null
                             if (phoneNumber == null) {
                                 System.out.println("[PHONE-CATEGORY-PROMPTS] delete skipped: phoneNumber null");
                                 break;
                             }

                             Map<String, WhatsAppPrompt> inner = whatsAppPhoneCategoryPrompts.get(phoneNumber);
                             if (inner == null || inner.isEmpty()) {
                                 System.out.println("[PHONE-CATEGORY-PROMPTS] Nothing to delete.");
                                 return null;
                             }

                             Map<String, WhatsAppPrompt> detached = new HashMap<>(inner);

                             if (category != null) {
                                 detached.remove(category);
                                 System.out.println("[PHONE-CATEGORY-PROMPTS] Deleted category=" + category + " for phone=" + phoneNumber);
                             } else if (details != null && details.getId() != null) {
                                 String foundCategory = null;
                                 for (Map.Entry<String, WhatsAppPrompt> e : detached.entrySet()) {
                                     WhatsAppPrompt p = e.getValue();
                                     if (p != null && p.getId() != null && p.getId().equals(details.getId())) {
                                         foundCategory = e.getKey();
                                         break;
                                     }
                                 }
                                 if (foundCategory != null) {
                                     detached.remove(foundCategory);
                                     System.out.println("[PHONE-CATEGORY-PROMPTS] Deleted prompt id=" + details.getId()
                                             + " from category=" + foundCategory);
                                 } else {
                                     System.out.println("[PHONE-CATEGORY-PROMPTS] No matching prompt id found to delete.");
                                 }
                             }

                             if (detached.isEmpty()) {
                                 whatsAppPhoneCategoryPrompts.remove(phoneNumber);
                             } else {
                                 whatsAppPhoneCategoryPrompts.put(phoneNumber, new ConcurrentHashMap<>(detached));
                             }
                             break;
                         }

                         case "delete-all": {
                             if (phoneNumber == null) {
                                 System.out.println("[PHONE-CATEGORY-PROMPTS] delete-all skipped: phoneNumber null");
                                 break;
                             }
                             whatsAppPhoneCategoryPrompts.remove(phoneNumber);
                             System.out.println("[PHONE-CATEGORY-PROMPTS] Removed all prompts for phone: " + phoneNumber);
                             break;
                         }

                         case "reset": {
                             whatsAppPhoneCategoryPrompts = new ConcurrentHashMap<>();
                             System.out.println("[PHONE-CATEGORY-PROMPTS] Reset all phone-category prompts.");
                             break;
                         }

                         default:
                             System.out.println("[PHONE-CATEGORY-PROMPTS] Unknown action: " + action);
                             break;
                     }

                 } finally {
                     lockWhatsAppPhoneCategoryPrompts.unlock();
                 }
                 break;

             } else {
                 System.out.println("[INFO] Could not acquire lockWhatsAppPhoneCategoryPrompts, retrying...");
                 Thread.sleep(500);
             }

         } catch (InterruptedException e) {
             Thread.currentThread().interrupt();
             System.err.println("[ERROR] Interrupted while waiting for lock in workWithWhatsAppPhoneCategoryPrompts");
             e.printStackTrace();
             break;

         } catch (Exception e) {
             System.out.println("[ERROR] Exception in workWithWhatsAppPhoneCategoryPrompts:");
             e.printStackTrace();
             break;
         }
     }

     return toReturn;
 }

 
    // ---------------------------
    // DEFENSIVE COPY HELPERS
    // ---------------------------

 
    private static Map<String, FileCategoryDateCheckDto> copyInnerCategoryMap(Map<String, FileCategoryDateCheckDto> src) {
        if (src == null) return null;
        return new HashMap<>(src);
    }

    private static Map<String, Map<String, FileCategoryDateCheckDto>> copyVerifyCategoryCreatedMap(
            Map<String, Map<String, FileCategoryDateCheckDto>> src) {

        Map<String, Map<String, FileCategoryDateCheckDto>> out = new HashMap<>();
        if (src == null || src.isEmpty()) return out;

        for (Map.Entry<String, Map<String, FileCategoryDateCheckDto>> e : src.entrySet()) {
            out.put(e.getKey(), copyInnerCategoryMap(e.getValue()));
        }
        return out;
    }

    private static Map<String, List<WhatsAppOpenAiAccount>> copyMapOfLists(Map<String, List<WhatsAppOpenAiAccount>> src) {
        Map<String, List<WhatsAppOpenAiAccount>> out = new HashMap<>();
        if (src == null || src.isEmpty()) return out;

        for (Map.Entry<String, List<WhatsAppOpenAiAccount>> e : src.entrySet()) {
            List<WhatsAppOpenAiAccount> v = e.getValue();
            out.put(e.getKey(), (v == null) ? null : new ArrayList<>(v));
        }
        return out;
    }

    private static Map<String, List<WhatsAppPhoneNumberTemplates>> copyMapOfListsTemplates(
            Map<String, List<WhatsAppPhoneNumberTemplates>> src) {

        Map<String, List<WhatsAppPhoneNumberTemplates>> out = new HashMap<>();
        if (src == null || src.isEmpty()) return out;

        for (Map.Entry<String, List<WhatsAppPhoneNumberTemplates>> e : src.entrySet()) {
            List<WhatsAppPhoneNumberTemplates> v = e.getValue();
            out.put(e.getKey(), (v == null) ? null : new ArrayList<>(v));
        }
        return out;
    }

    private static Map<String, List<WhatsAppPhoneNumberTemplateVariable>> copyMapOfListsTemplateVars(
            Map<String, List<WhatsAppPhoneNumberTemplateVariable>> src) {

        Map<String, List<WhatsAppPhoneNumberTemplateVariable>> out = new HashMap<>();
        if (src == null || src.isEmpty()) return out;

        for (Map.Entry<String, List<WhatsAppPhoneNumberTemplateVariable>> e : src.entrySet()) {
            List<WhatsAppPhoneNumberTemplateVariable> v = e.getValue();
            out.put(e.getKey(), (v == null) ? null : new ArrayList<>(v));
        }
        return out;
    }
    
    private static Map<String, WhatsAppPrompt> copyInnerPromptMap(Map<String, WhatsAppPrompt> src) {
        if (src == null) return null;
        return new HashMap<>(src);
    }

    private static Map<String, Map<String, WhatsAppPrompt>> copyOuterPhoneCategoryPromptMap(
            Map<String, Map<String, WhatsAppPrompt>> src) {

        Map<String, Map<String, WhatsAppPrompt>> out = new HashMap<>();
        if (src == null || src.isEmpty()) return out;

        for (Map.Entry<String, Map<String, WhatsAppPrompt>> e : src.entrySet()) {
            out.put(e.getKey(), copyInnerPromptMap(e.getValue()));
        }
        return out;
    }

}
