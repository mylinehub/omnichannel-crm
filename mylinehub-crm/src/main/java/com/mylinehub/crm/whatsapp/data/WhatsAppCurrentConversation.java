package com.mylinehub.crm.whatsapp.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.mylinehub.crm.whatsapp.dto.WhatsAppUpdateChatHistoryDTO;
import com.mylinehub.crm.whatsapp.dto.chat.WhatsAppChatDataParameterDTO;
import com.mylinehub.crm.whatsapp.entity.WhatsAppChatHistory;
import com.mylinehub.crm.whatsapp.repository.WhatsAppChatHistoryRepository;

public class WhatsAppCurrentConversation {

    // Base timeout constant (2 seconds)
    private static final long BASE_TIMEOUT_SECONDS = 2;

    // Phone Main , Phone With : Message ID LIST
    private static Map<String, Map<String, List<String>>> currentMessageID = new ConcurrentHashMap<>();

    // Message ID and Message
    private static Map<String, WhatsAppChatHistory> currentConversations = new ConcurrentHashMap<>();

    // Message ID and Message
    private static Map<String, WhatsAppChatHistory> backupCurrentConversations = new ConcurrentHashMap<>();

    // Message ID and Message Update
    private static Map<String, WhatsAppUpdateChatHistoryDTO> updateChatHistoryObject = new ConcurrentHashMap<>();

    // Locks for different shared variables
    private static final ReentrantLock lockCurrentMessageID = new ReentrantLock(false);
    private static final ReentrantLock lockCurrentConversations = new ReentrantLock(false);
    private static final ReentrantLock lockBackupCurrentConversations = new ReentrantLock(false);
    private static final ReentrantLock lockUpdateChatHistoryObject = new ReentrantLock(false);

    /**
     * IMPORTANT CHANGE (as requested):
     * - No internal ConcurrentHashMap / List references are returned to callers.
     * - "get-all" / "get-all-backup" return SNAPSHOT COPIES (new HashMap) so outside code cannot mutate internals.
     * - For currentMessageID returns, we return deep copies (outer map + inner map + list copies).
     *
     * Business logic remains the same; only defensive-copy at boundaries.
     */
    public static Map<String, WhatsAppChatHistory> workOnCurrentMemeoryConversations(WhatsAppChatDataParameterDTO parameter) {
        Map<String, WhatsAppChatHistory> toReturn = null;

        boolean acquired = false;
        long timeoutSeconds;

        while (!acquired) {
            try {
                int queueLength = lockCurrentConversations.getQueueLength();
                timeoutSeconds = BASE_TIMEOUT_SECONDS + queueLength;

                acquired = lockCurrentConversations.tryLock(timeoutSeconds, TimeUnit.SECONDS);

                if (!acquired) {
                    System.out.println("[workOnCurrentMemeoryConversations] Lock acquisition timed out ("
                            + timeoutSeconds + "s). Retrying...");
                }
            } catch (InterruptedException e) {
                System.out.println("[workOnCurrentMemeoryConversations] Interrupted while waiting for lock.");
                Thread.currentThread().interrupt();
                return null;
            }
        }

        try {
            String action = parameter.getAction();

            switch (action) {

                case "get":
                    WhatsAppChatHistory current = currentConversations.get(parameter.getWhatsAppMessageId());
                    if (current != null) {
                        toReturn = new HashMap<>();
                        toReturn.put(parameter.getWhatsAppMessageId(), current);
                    }
                    return toReturn;

                case "get-one-backup":
                    boolean backupAcquired = false;
                    while (!backupAcquired) {
                        try {
                            int backupQueueLen = lockBackupCurrentConversations.getQueueLength();
                            long backupTimeout = BASE_TIMEOUT_SECONDS + backupQueueLen;
                            backupAcquired = lockBackupCurrentConversations.tryLock(backupTimeout, TimeUnit.SECONDS);

                            if (!backupAcquired) {
                                System.out.println("[workOnCurrentMemeoryConversations] Waiting for backup lock ("
                                        + backupTimeout + "s). Retrying...");
                            }
                        } catch (InterruptedException e) {
                            System.out.println("[workOnCurrentMemeoryConversations] Interrupted while waiting for backup lock.");
                            Thread.currentThread().interrupt();
                            return null;
                        }
                    }

                    try {
                        current = backupCurrentConversations.get(parameter.getWhatsAppMessageId());
                        if (current != null) {
                            toReturn = new HashMap<>();
                            toReturn.put(parameter.getWhatsAppMessageId(), current);
                        }
                        return toReturn;
                    } finally {
                        lockBackupCurrentConversations.unlock();
                    }

                case "get-all":
                    return snapshotChatHistoryMap(currentConversations);

                case "get-all-backup":
                    return snapshotChatHistoryMap(backupCurrentConversations);

                case "update":
                    currentConversations.put(parameter.getWhatsAppMessageId(), parameter.getDetails());
                    break;

                case "update-sent":
                    toReturn = handleBooleanUpdate(parameter, "sent");
                    break;

                case "update-whatsAppMessageError":
                    toReturn = handleErrorUpdate(parameter);
                    break;

                case "update-conversationId":
                    toReturn = handleConversationIdUpdate(parameter);
                    break;

                case "update-delivered":
                    toReturn = handleBooleanUpdate(parameter, "delivered");
                    break;

                case "update-read":
                    toReturn = handleBooleanUpdate(parameter, "read");
                    break;

                case "update-self-read":
                    toReturn = handleBooleanUpdate(parameter, "self-read");
                    break;

                case "update-failed":
                    toReturn = handleBooleanUpdate(parameter, "failed");
                    break;

                case "update-deleted":
                    toReturn = handleBooleanUpdate(parameter, "deleted");
                    break;

                case "update-whatsAppActualBillable":
                    toReturn = handleBooleanUpdate(parameter, "whatsAppActualBillable");
                    break;

                case "delete":
                    currentConversations.remove(parameter.getWhatsAppMessageId());
                    break;

                case "move-to-backup":
                    boolean gotBackupLock = false;
                    while (!gotBackupLock) {
                        try {
                            int q1 = lockBackupCurrentConversations.getQueueLength();
                            long t1 = BASE_TIMEOUT_SECONDS + q1;

                            gotBackupLock = lockBackupCurrentConversations.tryLock(t1, TimeUnit.SECONDS);
                            if (!gotBackupLock) {
                                System.out.println("[workOnCurrentMemeoryConversations] Retrying to acquire backup lock...");
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            System.out.println("[workOnCurrentMemeoryConversations] Interrupted while acquiring backup lock.");
                            return null;
                        }
                    }

                    try {
                        currentMessageID = new ConcurrentHashMap<>();
                        // NOTE: shallow copy (same WhatsAppChatHistory object references), original behavior
                        backupCurrentConversations = new ConcurrentHashMap<>(currentConversations);
                        currentConversations = new ConcurrentHashMap<>();
                    } finally {
                        lockBackupCurrentConversations.unlock();
                    }
                    break;

                default:
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
            toReturn = null;
        } finally {
            lockCurrentConversations.unlock();
        }

        return toReturn;
    }

    // If found in current it is updated, or else put in update variable
    private static Map<String, WhatsAppChatHistory> handleBooleanUpdate(WhatsAppChatDataParameterDTO parameter, String field) {
        Map<String, WhatsAppChatHistory> toReturn = null;
        try {
            WhatsAppChatHistory current = currentConversations.get(parameter.getWhatsAppMessageId());

            if (current != null) {
                switch (field) {
                    case "sent":
                        current.setSent(true);
                        break;
                    case "delivered":
                        current.setDelivered(true);
                        break;
                    case "read":
                        current.setRead(true);
                        break;
                    case "self-read":
                        current.setReadSelf(true);
                        break;
                    case "failed":
                        current.setFailed(true);
                        break;
                    case "deleted":
                        current.setDeleted(true);
                        break;
                    case "whatsAppActualBillable":
                        current.setWhatsAppActualBillable(true);
                        break;
                    default:
                        break;
                }

                currentConversations.put(parameter.getWhatsAppMessageId(), current);
                toReturn = new HashMap<>();
                toReturn.put(parameter.getWhatsAppMessageId(), current);

            } else {
                WhatsAppUpdateChatHistoryDTO updateObj = updateChatHistoryObject.computeIfAbsent(
                        parameter.getWhatsAppMessageId(), k -> new WhatsAppUpdateChatHistoryDTO());

                switch (field) {
                    case "sent":
                        updateObj.setSent(true);
                        break;
                    case "delivered":
                        updateObj.setDelivered(true);
                        break;
                    case "read":
                        updateObj.setRead(true);
                        break;
                    case "self-read":
                        updateObj.setReadSelf(true);
                        break;
                    case "failed":
                        updateObj.setFailed(true);
                        break;
                    case "deleted":
                        updateObj.setDeleted(true);
                        break;
                    case "whatsAppActualBillable":
                        updateObj.setWhatsAppActualBillable(true);
                        break;
                    default:
                        break;
                }

                updateChatHistoryObject.put(parameter.getWhatsAppMessageId(), updateObj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return toReturn;
    }

    // If found in current it is updated, or else put in update variable
    private static Map<String, WhatsAppChatHistory> handleErrorUpdate(WhatsAppChatDataParameterDTO parameter) {
        Map<String, WhatsAppChatHistory> toReturn = null;
        try {
            WhatsAppChatHistory current = currentConversations.get(parameter.getWhatsAppMessageId());

            if (current != null) {
                current.setWhatsAppError(parameter.getError());
                currentConversations.put(parameter.getWhatsAppMessageId(), current);
                toReturn = new HashMap<>();
                toReturn.put(parameter.getWhatsAppMessageId(), current);

            } else {
                WhatsAppUpdateChatHistoryDTO updateObj = updateChatHistoryObject.computeIfAbsent(
                        parameter.getWhatsAppMessageId(), k -> new WhatsAppUpdateChatHistoryDTO());

                updateObj.setWhatsAppError(parameter.getError());
                updateChatHistoryObject.put(parameter.getWhatsAppMessageId(), updateObj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return toReturn;
    }

    // If found in current it is updated, or else put in update variable
    private static Map<String, WhatsAppChatHistory> handleConversationIdUpdate(WhatsAppChatDataParameterDTO parameter) {
        Map<String, WhatsAppChatHistory> toReturn = null;
        try {
            WhatsAppChatHistory current = currentConversations.get(parameter.getWhatsAppMessageId());

            if (current != null) {
                current.setConversationId(parameter.getConversationId());
                currentConversations.put(parameter.getWhatsAppMessageId(), current);
                toReturn = new HashMap<>();
                toReturn.put(parameter.getWhatsAppMessageId(), current);

            } else {
                WhatsAppUpdateChatHistoryDTO updateObj = updateChatHistoryObject.computeIfAbsent(
                        parameter.getWhatsAppMessageId(), k -> new WhatsAppUpdateChatHistoryDTO());

                updateObj.setConversationId(parameter.getConversationId());
                updateChatHistoryObject.put(parameter.getWhatsAppMessageId(), updateObj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return toReturn;
    }

    public static Map<String, Map<String, List<String>>> workOnCurrentMessageIdConversations(WhatsAppChatDataParameterDTO parameter) {
        Map<String, Map<String, List<String>>> toReturn = null;

        boolean acquired = false;
        long timeoutSeconds;

        while (!acquired) {
            try {
                int queueLength = lockCurrentMessageID.getQueueLength();
                timeoutSeconds = BASE_TIMEOUT_SECONDS + queueLength;

                acquired = lockCurrentMessageID.tryLock(timeoutSeconds, TimeUnit.SECONDS);

                if (!acquired) {
                    System.out.println("[workOnCurrentMessageIdConversations] Could not acquire lock (timeout " + timeoutSeconds + "s). Retrying...");
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                System.out.println("[workOnCurrentMessageIdConversations] Interrupted while waiting for lock.");
                Thread.currentThread().interrupt();
                return null;
            }
        }

        try {
            try {
                String action = parameter.getAction();

                switch (action) {

                    case "get":
                        Map<String, List<String>> current = currentMessageID.get(parameter.getPhoneNumberMain());
                        if (current != null) {
                            toReturn = new HashMap<>();
                            toReturn.put(parameter.getPhoneNumberMain(), copyInnerMapOfLists(current));
                        }
                        return toReturn;

                    case "get-all":
                        return deepCopyMessageIdMap(currentMessageID);

                    case "update-message-id":
                        current = currentMessageID.get(parameter.getPhoneNumberMain());

                        if (current != null) {
                            List<String> messageIds = current.get(parameter.getPhoneNumberWith());
                            if (messageIds != null) {
                                messageIds.add(parameter.getWhatsAppMessageId());
                            } else {
                                messageIds = new ArrayList<>();
                                messageIds.add(parameter.getWhatsAppMessageId());
                                current.put(parameter.getPhoneNumberWith(), messageIds);
                            }

                            current.put(parameter.getPhoneNumberWith(), messageIds);
                            currentMessageID.put(parameter.getPhoneNumberMain(), current);

                        } else {
                            current = new HashMap<>();
                            List<String> messageIds = new ArrayList<>();
                            messageIds.add(parameter.getWhatsAppMessageId());
                            current.put(parameter.getPhoneNumberWith(), messageIds);
                            currentMessageID.put(parameter.getPhoneNumberMain(), current);
                        }
                        return toReturn;

                    case "delete-by-phone-main-and-messages":
                        current = currentMessageID.remove(parameter.getPhoneNumberMain());
                        if (current != null) {
                            for (Map.Entry<String, List<String>> entry : current.entrySet()) {
                                List<String> messageIds = entry.getValue();
                                if (messageIds != null) {
                                    for (String messageId : messageIds) {
                                        WhatsAppChatHistory currentChat = currentConversations.get(messageId);
                                        if (currentChat != null) {
                                            currentChat.setDeleteSelf(true);
                                            currentConversations.put(messageId, currentChat);
                                        }
                                    }
                                }
                            }
                        }
                        break;

                    case "delete-by-phone-with-and-messages":
                        current = currentMessageID.get(parameter.getPhoneNumberMain());
                        if (current != null) {
                            List<String> messageIds = current.remove(parameter.getPhoneNumberWith());
                            currentMessageID.put(parameter.getPhoneNumberMain(), current);

                            if (messageIds != null) {
                                for (String messageId : messageIds) {
                                    WhatsAppChatHistory currentChat = currentConversations.get(messageId);
                                    if (currentChat != null) {
                                        currentChat.setDeleteSelf(true);
                                        currentConversations.put(messageId, currentChat);
                                    }
                                }
                            }
                        }
                        break;

                    default:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                toReturn = null;
            }
        } finally {
            lockCurrentMessageID.unlock();
        }

        return toReturn;
    }

    public static Map<String, WhatsAppUpdateChatHistoryDTO> workOnUpdateChatHistoryObject(WhatsAppChatDataParameterDTO parameter) {
        Map<String, WhatsAppUpdateChatHistoryDTO> toReturn = null;

        while (true) {
            int queueLength = lockUpdateChatHistoryObject.getQueueLength();
            long timeout = queueLength + BASE_TIMEOUT_SECONDS;

            try {
                if (lockUpdateChatHistoryObject.tryLock(timeout, TimeUnit.SECONDS)) {
                    try {
                        String action = parameter.getAction();

                        switch (action) {

                            case "get-all":
                                return snapshotUpdateMap(updateChatHistoryObject);

                            case "put-updates-to-database":
                                // FIX #1: remove nested re-lock attempt (we already hold the lock here)
                                Map<String, WhatsAppUpdateChatHistoryDTO> updateChatHistoryObjectCopy = updateChatHistoryObject;
                                updateChatHistoryObject = new ConcurrentHashMap<>();

                                List<String> messageIds = getKeysAsList(updateChatHistoryObjectCopy);

                                if (messageIds != null && !messageIds.isEmpty()) {
                                    WhatsAppChatHistoryRepository whatsAppChatHistoryRepository = parameter.getWhatsAppChatHistoryRepository();
                                    List<WhatsAppChatHistory> listOfChatHistory =
                                            whatsAppChatHistoryRepository.findAllByWhatsAppMessageIdIn(messageIds);

                                    List<WhatsAppChatHistory> updatedList = new ArrayList<>();

                                    for (WhatsAppChatHistory chatHistory : listOfChatHistory) {
                                        String messageId = chatHistory.getWhatsAppMessageId();
                                        if (messageId == null) {
                                            continue;
                                        }

                                        WhatsAppUpdateChatHistoryDTO updateDTO = updateChatHistoryObjectCopy.get(messageId);

                                        if (updateDTO != null) {
                                            if (updateDTO.isSent()) chatHistory.setSent(true);
                                            if (updateDTO.isDelivered()) chatHistory.setDelivered(true);
                                            if (updateDTO.isRead()) chatHistory.setRead(true);
                                            if (updateDTO.isFailed()) chatHistory.setFailed(true);
                                            if (updateDTO.isDeleted()) chatHistory.setDeleted(true);
                                            if (updateDTO.isWhatsAppActualBillable()) chatHistory.setWhatsAppActualBillable(true);

                                            if (updateDTO.getWhatsAppError() != null)
                                                chatHistory.setWhatsAppError(updateDTO.getWhatsAppError());

                                            if (updateDTO.getConversationId() != null)
                                                chatHistory.setConversationId(updateDTO.getConversationId());

                                            updatedList.add(chatHistory);
                                        }
                                    }

                                    if (!updatedList.isEmpty()) {
                                        whatsAppChatHistoryRepository.saveAll(updatedList);
                                    }
                                }
                                break;

                            default:
                                break;
                        }

                        break;

                    } finally {
                        lockUpdateChatHistoryObject.unlock();
                    }

                } else {
                    System.out.println("Could not acquire lock for workOnUpdateChatHistoryObject, retrying...");
                    Thread.sleep(500);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                toReturn = null;
                e.printStackTrace();
                break;
            } catch (Exception e) {
                toReturn = null;
                e.printStackTrace();
                break;
            }
        }

        return toReturn;
    }

    public static List<String> getKeysAsList(Map<String, ?> map) {
        return new ArrayList<>(map.keySet());
    }

    // ---------------------------
    // DEFENSIVE COPY HELPERS
    // ---------------------------

    private static Map<String, WhatsAppChatHistory> snapshotChatHistoryMap(Map<String, WhatsAppChatHistory> src) {
        if (src == null || src.isEmpty()) return new HashMap<>();
        return new HashMap<>(src);
    }

    private static Map<String, WhatsAppUpdateChatHistoryDTO> snapshotUpdateMap(Map<String, WhatsAppUpdateChatHistoryDTO> src) {
        if (src == null || src.isEmpty()) return new HashMap<>();
        return new HashMap<>(src);
    }

    private static Map<String, Map<String, List<String>>> deepCopyMessageIdMap(Map<String, Map<String, List<String>>> src) {
        Map<String, Map<String, List<String>>> out = new HashMap<>();
        if (src == null || src.isEmpty()) return out;

        for (Map.Entry<String, Map<String, List<String>>> e : src.entrySet()) {
            String phoneMain = e.getKey();
            Map<String, List<String>> inner = e.getValue();
            out.put(phoneMain, copyInnerMapOfLists(inner));
        }
        return out;
    }

    private static Map<String, List<String>> copyInnerMapOfLists(Map<String, List<String>> inner) {
        Map<String, List<String>> outInner = new HashMap<>();
        if (inner == null || inner.isEmpty()) return outInner;

        for (Map.Entry<String, List<String>> ie : inner.entrySet()) {
            String phoneWith = ie.getKey();
            List<String> ids = ie.getValue();
            if (ids == null) {
                outInner.put(phoneWith, null);
            } else {
                outInner.put(phoneWith, new ArrayList<>(ids));
            }
        }
        return outInner;
    }
}
