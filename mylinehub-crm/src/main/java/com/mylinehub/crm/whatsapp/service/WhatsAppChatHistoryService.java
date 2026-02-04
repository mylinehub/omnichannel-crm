package com.mylinehub.crm.whatsapp.service;
import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylinehub.crm.service.CurrentTimeInterface;
import com.mylinehub.crm.utils.DateRangeHelper;
import com.mylinehub.crm.whatsapp.data.WhatsAppCurrentConversation;
import com.mylinehub.crm.whatsapp.data.WhatsAppMemoryData;
import com.mylinehub.crm.whatsapp.data.dto.UnreadCountDTO;
import com.mylinehub.crm.whatsapp.dto.WhatsAppUpdateChatHistoryDTO;
import com.mylinehub.crm.whatsapp.dto.chat.WhatsAppAllChatDTO;
import com.mylinehub.crm.whatsapp.dto.chat.WhatsAppChatDataParameterDTO;
import com.mylinehub.crm.whatsapp.entity.WhatsAppChatHistory;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumber;
import com.mylinehub.crm.whatsapp.mapper.WhatsAppAllChatMapper;
import com.mylinehub.crm.whatsapp.repository.WhatsAppChatHistoryRepository;
import lombok.AllArgsConstructor;
import com.mylinehub.crm.repository.CustomerRepository;
import com.mylinehub.crm.entity.Customers;

/**
 * @author Anand Goel
 * @version 1.0
 */

@Service
@AllArgsConstructor
public class WhatsAppChatHistoryService implements CurrentTimeInterface{
	
	  /**
     * were injected by the constructor using the lombok @AllArgsContrustor annotation
     */
    private final WhatsAppChatHistoryRepository whatsAppChatHistoryRepository;
    private final CustomerRepository customerRepository;
    private final WhatsAppAllChatMapper whatsAppAllChatMapper;
//    private final ErrorRepository errorRepository;
    private ApplicationContext applicationContext;
    
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter IST_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(IST);
    
    public List<WhatsAppAllChatDTO> getAllChatHistoryForPhoneNumberMain(
            String phoneNumberMain,
            String organization,
            int startOffset,
            int endOffset) {

        System.out.println("----- getAllChatHistoryForPhoneNumberMain() START -----");
        System.out.println("phoneNumberMain = " + phoneNumberMain);
        System.out.println("organization = " + organization);
        System.out.println("startOffset = " + startOffset + ", endOffset = " + endOffset);

        List<WhatsAppAllChatDTO> whatsAppAllChatDTOToReturn = new ArrayList<>();

        // **** 0. BUILD DATE RANGE FROM OFFSETS ****
        ZoneId zoneId = ZoneId.of("Asia/Kolkata");
        ZonedDateTime now = ZonedDateTime.now(zoneId);

        int normalizedStart = Math.min(startOffset, endOffset);
        int normalizedEnd = Math.max(startOffset, endOffset);

        LocalDate endDate = now.minusDays(normalizedStart).toLocalDate();   // newest
        LocalDate startDate = now.minusDays(normalizedEnd).toLocalDate();   // oldest

        Timestamp startTime = Timestamp.valueOf(startDate.atStartOfDay());
        Timestamp endTime = Timestamp.valueOf(endDate.atTime(LocalTime.MAX));

        System.out.println("Computed date range:");
        System.out.println("  Start Date = " + startDate + " | Timestamp = " + startTime);
        System.out.println("  End Date   = " + endDate   + " | Timestamp = " + endTime);

        // **** 1. FETCH LATEST CHAT PER CONTACT FROM DATABASE ****
        System.out.println("Fetching DB chat rows...");
        List<Object[]> dbResults = whatsAppChatHistoryRepository
                .findDistinctPhoneNumberWithByPhoneNumberMainAndOrganizationAndDeleteSelfAndDateRange(
                        phoneNumberMain, organization, false, startTime, endTime);

        System.out.println("DB results count = " + dbResults.size());

        Map<String, Instant> latestTimeMap = new HashMap<>();
        for (Object[] row : dbResults) {
            String phoneWith = (String) row[0];
            Timestamp timestamp = (Timestamp) row[1];
            if (timestamp != null) {
                latestTimeMap.put(phoneWith, timestamp.toInstant());
                System.out.println("DB -> phoneWith: " + phoneWith + " | lastUpdate: " + timestamp.toInstant());
            }
        }

        // Build sorted DB candidate list
        List<String> allChatHistoryCandidates = latestTimeMap.entrySet().stream()
                .sorted(Map.Entry.<String, Instant>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        System.out.println("Initial DB candidates = " + allChatHistoryCandidates);

        // **** 2. FETCH MEMORY DATA AND MERGE ****
        WhatsAppChatDataParameterDTO parameterDTO = new WhatsAppChatDataParameterDTO();
        parameterDTO.setAction("get");
        parameterDTO.setPhoneNumberMain(phoneNumberMain);

        Map<String, Map<String, List<String>>> memoryData =
                WhatsAppCurrentConversation.workOnCurrentMessageIdConversations(parameterDTO);

        System.out.println("Memory data found = " + (memoryData != null));

        if (memoryData != null && !memoryData.isEmpty()) {
            Map<String, List<String>> memoryConversations = memoryData.get(phoneNumberMain);

            if (memoryConversations != null && !memoryConversations.isEmpty()) {
                System.out.println("Memory conversations count = " + memoryConversations.size());

                Set<String> seen = new HashSet<>(allChatHistoryCandidates);

                List<String> memoryPhones = new ArrayList<>(memoryConversations.keySet());
                List<String> newOnes = new ArrayList<>();

                for (String phoneWith : memoryPhones) {
                    List<String> msgIds = memoryConversations.get(phoneWith);

                    System.out.println("Memory -> phoneWith " + phoneWith + " msgCount = " +
                            (msgIds != null ? msgIds.size() : 0));

                    if (msgIds == null || msgIds.isEmpty()) continue;

                    String lastMsgId = msgIds.get(msgIds.size() - 1);

                    WhatsAppChatDataParameterDTO msgParam = new WhatsAppChatDataParameterDTO();
                    msgParam.setAction("get");
                    msgParam.setWhatsAppMessageId(lastMsgId);

                    Map<String, WhatsAppChatHistory> msgMap =
                            WhatsAppCurrentConversation.workOnCurrentMemeoryConversations(msgParam);

                    if (msgMap != null && msgMap.containsKey(lastMsgId)) {
                        WhatsAppChatHistory lastMsg = msgMap.get(lastMsgId);
                        if (lastMsg != null && lastMsg.getLastUpdateTime() != null) {
                            Instant lastUpdate = lastMsg.getLastUpdateTime().toInstant();

                            System.out.println("Memory -> lastUpdate for " + phoneWith + " = " + lastUpdate);

                            if (!lastUpdate.isBefore(startTime.toInstant())
                                    && !lastUpdate.isAfter(endTime.toInstant())) {
                                latestTimeMap.put(phoneWith, lastUpdate);
                            }
                        }
                    }

                    if (!seen.contains(phoneWith)) {
                        newOnes.add(phoneWith);
                        System.out.println("Memory-only phone added: " + phoneWith);
                    }
                }

                allChatHistoryCandidates.addAll(0, newOnes);
                System.out.println("Candidates after memory merge (before resort): " + allChatHistoryCandidates);
            }
        }

        // **** 2b. GLOBAL RESORT AFTER MERGE ****
        allChatHistoryCandidates = allChatHistoryCandidates.stream()
                .distinct()
                .sorted((a, b) -> {
                    Instant ta = latestTimeMap.getOrDefault(a, Instant.EPOCH);
                    Instant tb = latestTimeMap.getOrDefault(b, Instant.EPOCH);
                    return tb.compareTo(ta);
                })
                .collect(Collectors.toList());

        System.out.println("Final sorted candidates (after DB + memory merge): " + allChatHistoryCandidates);

        // **** 3. FETCH UNREAD COUNT ****
        Map<String, WhatsAppPhoneNumber> whatsAppMap =
                WhatsAppMemoryData.workWithWhatsAppPhoneNumberData(phoneNumberMain, null, "get-one");

        if (whatsAppMap != null && whatsAppMap.containsKey(phoneNumberMain)) {

            WhatsAppPhoneNumber whatsAppPhoneNumber = whatsAppMap.get(phoneNumberMain);

            List<UnreadCountDTO> unreadCounts =
                    whatsAppChatHistoryRepository.getUnreadCountsByPhoneNumberWith(phoneNumberMain, organization);

            Map<String, Integer> unreadCountMap = unreadCounts.stream()
                    .collect(Collectors.toMap(UnreadCountDTO::getPhoneNumberWith, UnreadCountDTO::getUnreadCount));

            whatsAppAllChatDTOToReturn = allChatHistoryCandidates.stream()
                    .map(item -> {
                        System.out.println("Mapping DTO for phoneWith: " + item);
                        return whatsAppAllChatMapper.mapPhoneNumberWithToChatInfoDto(item, whatsAppPhoneNumber, unreadCountMap);
                    })
                    .collect(Collectors.toList());
        }

        // **** 4. MAP CUSTOMER DETAILS ****
        System.out.println("Fetching customers...");
        List<Customers> allCustomers = customerRepository.findAllCustomersByPhoneNumberInAndOrganization(allChatHistoryCandidates,organization);
        Map<String, Customers> customerMap = allCustomers.stream()
                .filter(c -> c.getPhoneNumber() != null)
                .collect(Collectors.toMap(Customers::getPhoneNumber, c -> c));

        System.out.println("Customers found = " + customerMap.size());

        Iterator<WhatsAppAllChatDTO> iterator = whatsAppAllChatDTOToReturn.iterator();
        while (iterator.hasNext()) {
            WhatsAppAllChatDTO dto = iterator.next();
            Customers customer = customerMap.get(dto.getPhoneNumber());

            if (customer != null) {
                dto.setFirstName(customer.getFirstname());
                dto.setLastName(customer.getLastname());
                dto.setEmail(customer.getEmail());
                System.out.println("DTO updated with customer details: " + dto.getPhoneNumber());
            } else {
                System.out.println("No customer found, removing from result: " + dto.getPhoneNumber());
                iterator.remove();
            }
        }

        System.out.println("FINAL RESULT COUNT = " + whatsAppAllChatDTOToReturn.size());
        System.out.println("----- getAllChatHistoryForPhoneNumberMain() END -----");

        return whatsAppAllChatDTOToReturn;
    }



    
    public Integer updateLastReadIndexByPhoneNumberMainAndphoneNumberWithAndOrganizationAndIsDeleted(
            String phoneNumberMain,
            String phoneNumberWith,
            String organization) {

        int current = 0;

        try {
           System.out.println("updateLastReadIndexByPhoneNumberMainAndphoneNumberWithAndOrganizationAndIsDeleted called");
           System.out.println("phoneNumberMain: " + phoneNumberMain);
           System.out.println("phoneNumberWith: " + phoneNumberWith);
           System.out.println("organization: " + organization);

            whatsAppChatHistoryRepository.updateLastReadSelfByPhoneNumberMainAndPhoneNumberWithAndOrganizationAndDeleteSelf(
                    phoneNumberMain, phoneNumberWith, organization, false);

           System.out.println("UPDATE MEMORY DATA");
            //**** 2- UPDATE MEMORY DATA *****/
            WhatsAppChatDataParameterDTO whatsAppChatDataParameterDTO = new WhatsAppChatDataParameterDTO();
            whatsAppChatDataParameterDTO.setAction("get");
            whatsAppChatDataParameterDTO.setPhoneNumberMain(phoneNumberMain);
            
            //Update last read in memory
            Map<String, Map<String, List<String>>> currentConversationsMessageMainMap = WhatsAppCurrentConversation.workOnCurrentMessageIdConversations(whatsAppChatDataParameterDTO);
            Map<String, List<String>> currentConversationsMessageWithMap = null;
            
            if(currentConversationsMessageMainMap!=null && currentConversationsMessageMainMap.size()>0)
            {
            	currentConversationsMessageWithMap = currentConversationsMessageMainMap.get(phoneNumberMain);
            }
            
            if (currentConversationsMessageWithMap != null) {
               System.out.println("DEBUG: currentConversationsMessageList is not null. Proceeding to process PhoneNumberMain entries.");
               
                List<String> listKeys = currentConversationsMessageWithMap.get(phoneNumberWith);
                
                if(listKeys != null) {
                	ListIterator<String> iterator = listKeys.listIterator(listKeys.size());
                   System.out.println("DEBUG: Created ListIterator starting from end of listKeys.");

                    while (iterator.hasPrevious()) {
                        String currentWhatsAppMessageId = iterator.previous();
                       System.out.println("DEBUG: Iterating currentWhatsAppMessageId: " + currentWhatsAppMessageId);
                        WhatsAppChatDataParameterDTO whatsAppChatDataParameterDTOInternal = new WhatsAppChatDataParameterDTO();
                        whatsAppChatDataParameterDTOInternal.setAction("update-self-read");
                        whatsAppChatDataParameterDTOInternal.setPhoneNumberMain(phoneNumberMain);
                        whatsAppChatDataParameterDTOInternal.setPhoneNumberWith(phoneNumberWith);
                        whatsAppChatDataParameterDTOInternal.setWhatsAppMessageId(currentWhatsAppMessageId);
                        whatsAppChatDataParameterDTOInternal.setApplicationContext(applicationContext);
                        whatsAppChatDataParameterDTOInternal.setOrganization(organization);

                        WhatsAppCurrentConversation.workOnCurrentMemeoryConversations(whatsAppChatDataParameterDTOInternal);

                    }
                }
            }
            
            current = 1;

           System.out.println("Update successful for phoneNumberMain: " + phoneNumberMain +", phoneNumberWith: " + phoneNumberWith + ", organization: " + organization);
        } catch (Exception e) {
            current = 0;
           System.out.println("Exception during update: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        return current;
    }

    
    public List<WhatsAppChatHistory> getAllChatHistoryByPhoneNumberMainAndphoneNumberWithAndOrganization(
            String phoneNumberMain, String phoneNumberWith, String organization)
            throws JsonMappingException, JsonProcessingException {

        List<WhatsAppChatHistory> databaseData = new ArrayList<>();

       System.out.println("Finding current chat history");
       System.out.println("Service PhoneNumberMain From: " + phoneNumberMain);
       System.out.println("Service phoneNumberWith To: " + phoneNumberWith);
       System.out.println("Service Organization: " + organization);

        try {
            // 1 - Fetch from DB
           System.out.println("// 1 - Fetch from DB");
           System.out.println("Fetching chat history from database...");

            List<WhatsAppChatHistory> dbResult =
                    whatsAppChatHistoryRepository.findByPhoneNumberMainAndPhoneNumberWithAndOrganizationAndDeleteSelfOrderByLastUpdateTimeAsc(
                            phoneNumberMain, phoneNumberWith, organization, false);

            if (dbResult != null) {
                databaseData.addAll(dbResult);
            }

           System.out.println("Database records found: " + databaseData.size());

            // 2 - Fetch from memory
           System.out.println("// 2 - Fetch from memory");
           System.out.println("Fetching chat history from memory...");

            WhatsAppChatDataParameterDTO chatParams = new WhatsAppChatDataParameterDTO();
            chatParams.setAction("get-all");

            Map<String, WhatsAppChatHistory> memoryChats =
                    WhatsAppCurrentConversation.workOnCurrentMemeoryConversations(chatParams);

            Map<String, WhatsAppUpdateChatHistoryDTO> updateObjects =
                    WhatsAppCurrentConversation.workOnUpdateChatHistoryObject(chatParams);

            Map<String, Map<String, List<String>>> messageIdMap =
                    WhatsAppCurrentConversation.workOnCurrentMessageIdConversations(chatParams);

            // 3 - Update existing database records using updateObjects
           System.out.println("// 3 - Update status for database data");

            for (WhatsAppChatHistory chatHistory : databaseData) {
                if (chatHistory == null || chatHistory.getWhatsAppMessageId() == null) {
                   System.out.println("Skipping null chatHistory or missing messageId");
                    continue;
                }

                applyUpdateDTO(chatHistory, updateObjects.get(chatHistory.getWhatsAppMessageId()));
            }

            // 4 - Merge memory messages
           System.out.println("// 4 - Add new memory messages to DB records");

            if (messageIdMap == null) {
               System.out.println("Memory messageIdMap is null.");
                return databaseData;
            }

            Map<String, List<String>> messagesForWith = messageIdMap.get(phoneNumberMain);

            if (messagesForWith == null) {
               System.out.println("No memory messages found for phoneNumberMain: " + phoneNumberMain);
                return databaseData;
            }

            List<String> messageIds = messagesForWith.get(phoneNumberWith);

            if (messageIds == null || messageIds.isEmpty()) {
               System.out.println("No memory messages found for phoneNumberWith: " + phoneNumberWith);
                return databaseData;
            }

           System.out.println("Merging memory messages...");
           System.out.println("Memory message count: " + messageIds.size());

            for (String messageId : messageIds) {
                WhatsAppChatHistory memoryChat = memoryChats.get(messageId);

                if (memoryChat == null) {
                   System.out.println("Message ID not found in memory chats: " + messageId);
                    continue;
                }

                applyUpdateDTO(memoryChat, updateObjects.get(messageId));
                databaseData.add(memoryChat);
            }

           System.out.println("Total records after merge (DB + Memory): " + databaseData.size());

        } catch (Exception e) {
           System.out.println("Error while getting chat data: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        return databaseData;
    }

    
    /*
    ===================== Java Parameter Passing Behavior =====================

    Type      : Primitive
    Examples  : int, long, float, boolean
    Passed as : Value
    Can method change original? : No (changes affect only the local copy)
    Memory    : Stack (primitive stored directly)

    Type      : Object
    Examples  : String, List, CustomClass, etc.
    Passed as : Reference (by value)
    Can method change original? : ✅ Yes (method can mutate object fields)
    Memory    : Heap (reference stored on stack, actual object on heap)

    ============================================================================
  */
    private void applyUpdateDTO(WhatsAppChatHistory chatHistory, WhatsAppUpdateChatHistoryDTO updateDTO) {
        if (updateDTO == null) {
           System.out.println("No update object found for messageId: " + chatHistory.getWhatsAppMessageId());
            return;
        }

        String messageId = chatHistory.getWhatsAppMessageId();
       System.out.println("Applying update for messageId: " + messageId);

        // Update flags
        if (updateDTO.isSent()) chatHistory.setSent(true);
        if (updateDTO.isDelivered()) chatHistory.setDelivered(true);
        if (updateDTO.isRead()) chatHistory.setRead(true);
        if (updateDTO.isReadSelf()) chatHistory.setReadSelf(true);
        if (updateDTO.isFailed()) chatHistory.setFailed(true);
        if (updateDTO.isDeleted()) chatHistory.setDeleted(true);
        if (updateDTO.isWhatsAppActualBillable()) chatHistory.setWhatsAppActualBillable(true);

        // Update error and conversationId
        if (updateDTO.getWhatsAppError() != null) {
            chatHistory.setWhatsAppError(updateDTO.getWhatsAppError());
        }

        if (updateDTO.getConversationId() != null) {
            chatHistory.setConversationId(updateDTO.getConversationId());
        }
    }


    //Update ChatHistory To Database as per message ID
    //Update message ID list for phone main + phone with
    public int softAppendChatHistoryByPhoneNumberMainAndphoneNumberWithAndOrganization(WhatsAppChatHistory details) {

       System.out.println("softAppendChatHistoryByPhoneNumberMainAndphoneNumberWithAndOrganization");
        int result = 0;

            try {
                WhatsAppChatDataParameterDTO whatsAppChatDataParameterDTO = new WhatsAppChatDataParameterDTO();
                whatsAppChatDataParameterDTO.setAction("update");
                whatsAppChatDataParameterDTO.setPhoneNumberMain(details.getPhoneNumberMain());
                whatsAppChatDataParameterDTO.setPhoneNumberWith(details.getPhoneNumberWith());
                whatsAppChatDataParameterDTO.setWhatsAppMessageId(details.getWhatsAppMessageId());
                whatsAppChatDataParameterDTO.setDetails(details);
                whatsAppChatDataParameterDTO.setApplicationContext(applicationContext);
                whatsAppChatDataParameterDTO.setOrganization(details.getOrganization());

                WhatsAppCurrentConversation.workOnCurrentMemeoryConversations(whatsAppChatDataParameterDTO);

               System.out.println("workOnCurrentMessageIdConversations");
                whatsAppChatDataParameterDTO.setAction("update-message-id");
                WhatsAppCurrentConversation.workOnCurrentMessageIdConversations(whatsAppChatDataParameterDTO);

                result = 1;
               System.out.println("Chat history soft append successful");
            } catch (Exception e) {
               System.out.println("Exception in softAppendChatHistory: " + e.getMessage());
                e.printStackTrace();
            }
        return result;
    }


    //1. Move all messages to backup (Current messageId List cleared, all messages to backup (deep link) ,all messages cleared)
    //2. Get all backup here in this function (backup is not cleared. It remains as it is), so as it can be reused later to resist db shoot
    //3. Al backup is here. Out of all we find what are already in memory
    //4. Then in already present list, we update values, ass new values. This way in list previous records already have context
    //5. So they will update.
    //6. new ones should get created
    public int hardAppendChatHistoryByPhoneNumberMainAndPhoneNumberWithAndOrganization(WhatsAppChatHistoryRepository whatsAppChatHistoryRepositoryForHardInsert) throws JsonProcessingException {
        int result = 0;

       System.out.println("hardAppendChatHistoryByPhoneNumberMainAndPhoneNumberWithAndOrganization");

            WhatsAppChatDataParameterDTO whatsAppChatDataParameterDTO = new WhatsAppChatDataParameterDTO();
            whatsAppChatDataParameterDTO.setAction("move-to-backup");
            whatsAppChatDataParameterDTO.setApplicationContext(applicationContext);

           System.out.println("Moving current conversations to backup");
            WhatsAppCurrentConversation.workOnCurrentMemeoryConversations(whatsAppChatDataParameterDTO);

            whatsAppChatDataParameterDTO.setAction("get-all-backup");
           System.out.println("Retrieving all backup conversations from memory");
            Map<String, WhatsAppChatHistory> allChatMap = WhatsAppCurrentConversation.workOnCurrentMemeoryConversations(whatsAppChatDataParameterDTO);

            List<WhatsAppChatHistory> chatList = new ArrayList<>();
            List<String> messageIds = new ArrayList<>();
            List<WhatsAppChatHistory> alreadyPresentChatList = null;

            if (allChatMap != null && allChatMap.size() > 0) {
               System.out.println("Populating chatList and messageIds from memory backup");
                for (Map.Entry<String, WhatsAppChatHistory> entry : allChatMap.entrySet()) {
                    chatList.add(entry.getValue());
                    messageIds.add(entry.getValue().getWhatsAppMessageId());
                }

               System.out.println("Finding already present chat entries from database by message IDs");
                alreadyPresentChatList = whatsAppChatHistoryRepositoryForHardInsert.findAllByWhatsAppMessageIdIn(messageIds);

                if (alreadyPresentChatList != null) {
                   System.out.println("Clearing chatList to update with latest data");
                    chatList = new ArrayList<>();

                   System.out.println("Updating existing chat entries with latest memory values");
                   System.out.println("Removing updated entries from memory map");

                    for (int i = 0; i < alreadyPresentChatList.size(); i++) {
                        WhatsAppChatHistory dbChat = alreadyPresentChatList.get(i);
                        WhatsAppChatHistory memChat = allChatMap.get(dbChat.getWhatsAppMessageId());
                        if (memChat != null) {
                            alreadyPresentChatList.set(i, updateChatHistoryWithNewValues(dbChat, memChat));
                            allChatMap.remove(dbChat.getWhatsAppMessageId());
                        }
                    }

                   System.out.println("Adding remaining memory chats to alreadyPresentChatList");
                    for (Map.Entry<String, WhatsAppChatHistory> entry : allChatMap.entrySet()) {
                        alreadyPresentChatList.add(entry.getValue());
                    }
                }
            }

            ObjectMapper mapper = new ObjectMapper();

            if (alreadyPresentChatList != null && !alreadyPresentChatList.isEmpty()) {
               System.out.println("Saving updated chat history list to database");
                whatsAppChatHistoryRepositoryForHardInsert.saveAll(alreadyPresentChatList);

               System.out.println("Total chat rows saved: " + alreadyPresentChatList.size());
               System.out.println("Saved chat records JSON: " + mapper.writeValueAsString(alreadyPresentChatList));

                result = alreadyPresentChatList.size();
            } else if (!chatList.isEmpty()) {
               System.out.println("Saving new chat history list to database");
                whatsAppChatHistoryRepositoryForHardInsert.saveAll(chatList);

               System.out.println("Total chat rows saved: " + chatList.size());
               System.out.println("Saved chat records JSON: " + mapper.writeValueAsString(chatList));

                result = chatList.size();
            } else {
               System.out.println("No records to hard append in chat history");
            }
        return result;
    }


    public WhatsAppChatHistory updateChatHistoryWithNewValues(WhatsAppChatHistory current, WhatsAppChatHistory currentToUpdateFrom) {
       System.out.println("updateChatHistoryWithNewValues called for messageId: " + current.getWhatsAppMessageId());

        if (currentToUpdateFrom.isDeleted()) {
            current.setDeleted(true);
        }
        if (currentToUpdateFrom.isDelivered()) {
            current.setDelivered(true);
        }
        if (currentToUpdateFrom.isFailed()) {
            current.setFailed(true);
        }
        if (currentToUpdateFrom.isInbound()) {
            current.setInbound(true);
        }
        if (currentToUpdateFrom.isOutbound()) {
            current.setOutbound(true);
        }
        if (currentToUpdateFrom.isRead()) {
            current.setRead(true);
        }
        if (currentToUpdateFrom.isReadSelf()) {
            current.setReadSelf(true);
        }
        if (currentToUpdateFrom.isSent()) {
            current.setSent(true);
        }
        if (currentToUpdateFrom.isWhatsAppActualBillable()) {
            current.setWhatsAppActualBillable(true);
        }
        if (currentToUpdateFrom.getWhatsAppMediaId() != null) {
            current.setWhatsAppMediaId(currentToUpdateFrom.getWhatsAppMediaId());
        }
        if (currentToUpdateFrom.getBlobType() != null) {
            current.setBlobType(currentToUpdateFrom.getBlobType());
        }
        if (currentToUpdateFrom.getConversationId() != null) {
            current.setConversationId(currentToUpdateFrom.getConversationId());
        }
        if (currentToUpdateFrom.getFileName() != null) {
            current.setFileName(currentToUpdateFrom.getFileName());
        }
        if (currentToUpdateFrom.getFileSizeInMB() != null) {
            current.setFileSizeInMB(currentToUpdateFrom.getFileSizeInMB());
        }
        if (currentToUpdateFrom.getFromExtension() != null) {
            current.setFromExtension(currentToUpdateFrom.getFromExtension());
        }
        if (currentToUpdateFrom.getFromName() != null) {
            current.setFromName(currentToUpdateFrom.getFromName());
        }
        if (currentToUpdateFrom.getFromTitle() != null) {
            current.setFromTitle(currentToUpdateFrom.getFromTitle());
        }
        if (currentToUpdateFrom.getMessageOrigin() != null) {
            current.setMessageOrigin(currentToUpdateFrom.getMessageOrigin());
        }
        if (currentToUpdateFrom.getMessageString() != null) {
            current.setMessageString(currentToUpdateFrom.getMessageString());
        }
        if (currentToUpdateFrom.getMessageType() != null) {
            current.setMessageType(currentToUpdateFrom.getMessageType());
        }
        if (currentToUpdateFrom.getOrganization() != null) {
            current.setOrganization(currentToUpdateFrom.getOrganization());
        }
        if (currentToUpdateFrom.getPhoneNumberMain() != null) {
            current.setPhoneNumberMain(currentToUpdateFrom.getPhoneNumberMain());
        }
        if (currentToUpdateFrom.getPhoneNumberWith() != null) {
            current.setPhoneNumberWith(currentToUpdateFrom.getPhoneNumberWith());
        }
        if (currentToUpdateFrom.getWhatsAppError() != null) {
            current.setWhatsAppError(currentToUpdateFrom.getWhatsAppError());
        }
        if (currentToUpdateFrom.getWhatsAppMessageId() != null) {
            current.setWhatsAppMessageId(currentToUpdateFrom.getWhatsAppMessageId());
        }

        current.setLastUpdateTime(new Date());

       System.out.println("Updated chat history last update time to: " + current.getLastUpdateTime());

        return current;
    }


    public int deleteChatHistoryByPhoneNumberMainAndphoneNumberWithAndOrganization(String phoneNumberMain, String phoneNumberWith, String organization) {
        int result;
        try {
           System.out.println("deleteChatHistoryByPhoneNumberMainAndphoneNumberWithAndOrganization called for phoneMain: " + phoneNumberMain + ", phoneWith: " + phoneNumberWith + ", organization: " + organization);

            WhatsAppChatDataParameterDTO whatsAppChatDataParameterDTO = new WhatsAppChatDataParameterDTO();
            whatsAppChatDataParameterDTO.setAction("delete-by-phone-with-and-messages");
            whatsAppChatDataParameterDTO.setPhoneNumberMain(phoneNumberMain);
            whatsAppChatDataParameterDTO.setPhoneNumberWith(phoneNumberWith);
            whatsAppChatDataParameterDTO.setApplicationContext(applicationContext);
            whatsAppChatDataParameterDTO.setOrganization(organization);

           System.out.println("Calling workOnCurrentMessageIdConversations for delete");
            WhatsAppCurrentConversation.workOnCurrentMessageIdConversations(whatsAppChatDataParameterDTO);

           System.out.println("Soft deleting chat history from database");
            whatsAppChatHistoryRepository.softDeleteByPhoneNumberMainAndPhoneNumberWithAndOrganizationAndDeleteSelf(phoneNumberMain, phoneNumberWith, organization, false);

            result = 1;
        } catch (Exception e) {
            result = 0;
           System.out.println("Exception in deleteChatHistoryByPhoneNumberMainAndphoneNumberWithAndOrganization: " + e.getMessage());
            throw e;
        }
        return result;
    }

    public int deleteAllChatHistoryByPhoneNumberMainAndOrganization(String phoneNumberMain, String organization) {
        int result;
        try {
           System.out.println("deleteAllChatHistoryByPhoneNumberMainAndOrganization called for phoneMain: " + phoneNumberMain + ", organization: " + organization);

            WhatsAppChatDataParameterDTO whatsAppChatDataParameterDTO = new WhatsAppChatDataParameterDTO();
            whatsAppChatDataParameterDTO.setAction("delete-by-phone-main-and-messages");
            whatsAppChatDataParameterDTO.setPhoneNumberMain(phoneNumberMain);
            whatsAppChatDataParameterDTO.setApplicationContext(applicationContext);
            whatsAppChatDataParameterDTO.setOrganization(organization);

           System.out.println("Calling workOnCurrentMessageIdConversations for delete all");
            WhatsAppCurrentConversation.workOnCurrentMessageIdConversations(whatsAppChatDataParameterDTO);

           System.out.println("Soft deleting all chat history from database");
            whatsAppChatHistoryRepository.softDeleteAllByPhoneNumberMainAndOrganizationAndDeleteSelf(phoneNumberMain, organization, false);

            result = 1;
        } catch (Exception e) {
           System.out.println("Exception in deleteAllChatHistoryByPhoneNumberMainAndOrganization: " + e.getMessage());
            result = 0;
            throw e;
        }
        return result;
    }
    
    
    public byte[] exportChatHistoryExcelDbOnly(String organization, String phoneMain, String startDate, String endDate) {
        if (organization == null || organization.trim().isEmpty()) {
            throw new IllegalArgumentException("organization required");
        }
        if (phoneMain == null || phoneMain.trim().isEmpty()) {
            throw new IllegalArgumentException("phoneMain required");
        }
        if (startDate == null || startDate.trim().isEmpty()) {
            throw new IllegalArgumentException("startDate required (yyyy-MM-dd)");
        }
        if (endDate == null || endDate.trim().isEmpty()) {
            throw new IllegalArgumentException("endDate required (yyyy-MM-dd)");
        }

        DateRange range = toIstDayRange(startDate.trim(), endDate.trim());

        List<WhatsAppChatHistory> rows;
        rows = whatsAppChatHistoryRepository.findAllForExportByPhoneMainOrgAndDateRange(
                    phoneMain.trim(), organization.trim(), range.startTs, range.endTs);

        return buildExcel(organization, phoneMain, range, rows);
    }

    // ------------------------------------------------------------
    // Excel Builder
    // ------------------------------------------------------------
    private byte[] buildExcel(String organization, String phoneMain, DateRange range, List<WhatsAppChatHistory> rows) {
        try (Workbook wb = new XSSFWorkbook()) {
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Sheet 1: Summary
            Sheet s1 = wb.createSheet("Summary");

            String[] c1 = new String[] {
                    "organization",
                    "phoneMain(filter)",
                    "start(IST)",
                    "end(IST)",
                    "totalRows"
            };

            Row h1 = s1.createRow(0);
            for (int i = 0; i < c1.length; i++) {
                Cell c = h1.createCell(i);
                c.setCellValue(c1[i]);
                c.setCellStyle(headerStyle);
            }

            Row r1 = s1.createRow(1);
            r1.createCell(0).setCellValue(nz(organization));
            r1.createCell(1).setCellValue(nz(phoneMain));
            r1.createCell(2).setCellValue(IST_FMT.format(range.startInstant));
            r1.createCell(3).setCellValue(IST_FMT.format(range.endInstant));
            r1.createCell(4).setCellValue(rows == null ? 0 : rows.size());

            for (int i = 0; i < c1.length; i++) s1.autoSizeColumn(i);

            // Sheet 2: ChatHistory
            Sheet s2 = wb.createSheet("ChatHistory");

            String[] c2 = new String[] {
                    "id",
                    "organization",
                    "phoneNumberMain",
                    "phoneNumberWith",
                    "lastUpdateTime(IST)",
                    "inbound",
                    "outbound",
                    "messageType",
                    "messageString",
                    "fromExtension",
                    "fromName",
                    "fromTitle",
                    "messageOrigin",
                    "whatsAppMessageId",
                    "conversationId",
                    "mediaId",
                    "fileName",
                    "blobType",
                    "fileSizeInMB",
                    "sent",
                    "delivered",
                    "read",
                    "readSelf",
                    "failed",
                    "deleted",
                    "deleteSelf",
                    "whatsAppError",
                    "whatsAppActualBillable"
            };

            Row h2 = s2.createRow(0);
            for (int i = 0; i < c2.length; i++) {
                Cell c = h2.createCell(i);
                c.setCellValue(c2[i]);
                c.setCellStyle(headerStyle);
            }

            int idx = 1;
            if (rows != null) {
                for (WhatsAppChatHistory ch : rows) {
                    if (ch == null) continue;
                    Row rr = s2.createRow(idx++);

                    int col = 0;
                    rr.createCell(col++).setCellValue(ch.getId() == null ? 0L : ch.getId());
                    rr.createCell(col++).setCellValue(nz(ch.getOrganization()));
                    rr.createCell(col++).setCellValue(nz(ch.getPhoneNumberMain()));
                    rr.createCell(col++).setCellValue(nz(ch.getPhoneNumberWith()));

                    rr.createCell(col++).setCellValue(
                            ch.getLastUpdateTime() == null ? "" : IST_FMT.format(ch.getLastUpdateTime().toInstant())
                    );

                    rr.createCell(col++).setCellValue(ch.isInbound());
                    rr.createCell(col++).setCellValue(ch.isOutbound());

                    rr.createCell(col++).setCellValue(nz(ch.getMessageType()));
                    rr.createCell(col++).setCellValue(nz(ch.getMessageString()));

                    rr.createCell(col++).setCellValue(nz(ch.getFromExtension()));
                    rr.createCell(col++).setCellValue(nz(ch.getFromName()));
                    rr.createCell(col++).setCellValue(nz(ch.getFromTitle()));
                    rr.createCell(col++).setCellValue(nz(ch.getMessageOrigin()));

                    rr.createCell(col++).setCellValue(nz(ch.getWhatsAppMessageId()));
                    rr.createCell(col++).setCellValue(nz(ch.getConversationId()));

                    rr.createCell(col++).setCellValue(nz(ch.getWhatsAppMediaId()));
                    rr.createCell(col++).setCellValue(nz(ch.getFileName()));
                    rr.createCell(col++).setCellValue(nz(ch.getBlobType()));
                    rr.createCell(col++).setCellValue(ch.getFileSizeInMB() == null ? "" : String.valueOf(ch.getFileSizeInMB()));

                    rr.createCell(col++).setCellValue(ch.isSent());
                    rr.createCell(col++).setCellValue(ch.isDelivered());
                    rr.createCell(col++).setCellValue(ch.isRead());
                    rr.createCell(col++).setCellValue(ch.isReadSelf());
                    rr.createCell(col++).setCellValue(ch.isFailed());
                    rr.createCell(col++).setCellValue(ch.isDeleted());
                    rr.createCell(col++).setCellValue(ch.isDeleteSelf());

                    rr.createCell(col++).setCellValue(nz(ch.getWhatsAppError()));
                    rr.createCell(col++).setCellValue(ch.isWhatsAppActualBillable());
                }
            }

            // autosize (cap if huge)
            int maxAuto = Math.min(c2.length, 30);
            for (int i = 0; i < maxAuto; i++) s2.autoSizeColumn(i);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            wb.write(bos);
            return bos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to create WhatsApp chat history excel", e);
        }
    }

    // ------------------------------------------------------------
    // Date helper: startDate/endDate in IST (yyyy-MM-dd)
    // ------------------------------------------------------------
    private DateRange toIstDayRange(String startDate, String endDate) {
        LocalDate s = LocalDate.parse(startDate);
        LocalDate e = LocalDate.parse(endDate);

        // normalize
        if (e.isBefore(s)) {
            LocalDate tmp = s;
            s = e;
            e = tmp;
        }

        ZonedDateTime zStart = s.atStartOfDay(IST);
        ZonedDateTime zEnd = e.atTime(LocalTime.MAX).atZone(IST);

        Instant start = zStart.toInstant();
        Instant end = zEnd.toInstant();

        return new DateRange(
                start,
                end,
                Timestamp.from(start),
                Timestamp.from(end)
        );
    }

    private static String nz(String s) {
        return (s == null) ? "" : s;
    }

    private static class DateRange {
        final Instant startInstant;
        final Instant endInstant;
        final Timestamp startTs;
        final Timestamp endTs;

        DateRange(Instant startInstant, Instant endInstant, Timestamp startTs, Timestamp endTs) {
            this.startInstant = startInstant;
            this.endInstant = endInstant;
            this.startTs = startTs;
            this.endTs = endTs;
        }
    }
    

}
