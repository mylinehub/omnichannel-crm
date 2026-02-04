package com.mylinehub.crm.data.current;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.mylinehub.crm.entity.dto.ChatKeyValueDTO;

public class CurrentConversations {

    // Base timeout in seconds for lock acquisition
    private static final int BASE_TIMEOUT_SECONDS = 2;

    // Non-fair lock for currentConversations
    private static final ReentrantLock currentConversationsLock = new ReentrantLock(false);

    // Shared map for current conversations
    // Keeping LinkedHashMap but all access is protected by lock
    private static LinkedHashMap<String, LinkedHashMap<String, List<ChatKeyValueDTO>>> currentConversations =
            new LinkedHashMap<>();

    /**
     * IMPORTANT CHANGE (as requested):
     * - Do NOT leak internal LinkedHashMap/List references outside this class.
     * - "get" now returns a DEEP COPY snapshot of the map for extensionFrom:
     *     - new LinkedHashMap<>(...)
     *     - and new ArrayList<>(list) for each (extensionWith -> list) value.
     *
     * No business logic change for update/remove/delete; only defensive-copy at boundaries.
     */
    public static LinkedHashMap<String, List<ChatKeyValueDTO>> workOnCurrentMemeoryConversation(
            String extensionFrom,
            String extensionWith,
            List<ChatKeyValueDTO> details,
            String action) {

        LinkedHashMap<String, List<ChatKeyValueDTO>> toReturn = null;
        boolean acquired = false;

        while (!acquired) {
            try {
                // Timeout = current queue length + base timeout
                int timeoutSeconds = currentConversationsLock.getQueueLength() + BASE_TIMEOUT_SECONDS;
                acquired = currentConversationsLock.tryLock(timeoutSeconds, TimeUnit.SECONDS);
                if (!acquired) continue; // retry if lock not acquired

                switch (action) {
                    case "get":
                        // DEFENSIVE COPY: return snapshot so caller cannot mutate internals
                        return deepCopyConversationMap(currentConversations.get(extensionFrom));

                    case "update":
                        LinkedHashMap<String, List<ChatKeyValueDTO>> values = currentConversations.get(extensionFrom);
                        if (values != null) {
                            currentConversations.get(extensionFrom).put(extensionWith, details);
                        } else {
                            values = new LinkedHashMap<>();
                            values.put(extensionWith, details);
                            currentConversations.put(extensionFrom, values);
                        }
                        break;

                    case "remove":
                        LinkedHashMap<String, List<ChatKeyValueDTO>> values1 = currentConversations.get(extensionFrom);
                        if (values1 != null) {
                            currentConversations.get(extensionFrom).remove(extensionWith);
                        }
                        break;

                    case "delete":
                        currentConversations.remove(extensionFrom);
                        break;

                    default:
                        break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                toReturn = null;
                e.printStackTrace();
            } finally {
                if (acquired) currentConversationsLock.unlock();
            }
        }

        return toReturn;
    }

    // ---------------------------
    // DEFENSIVE COPY HELPERS
    // ---------------------------

    // Deep copy: LinkedHashMap<String, List<ChatKeyValueDTO>> (new map + new ArrayList for each list)
    private static LinkedHashMap<String, List<ChatKeyValueDTO>> deepCopyConversationMap(
            LinkedHashMap<String, List<ChatKeyValueDTO>> src) {

        LinkedHashMap<String, List<ChatKeyValueDTO>> out = new LinkedHashMap<>();
        if (src == null || src.isEmpty()) return out;

        for (Map.Entry<String, List<ChatKeyValueDTO>> e : src.entrySet()) {
            String extensionWith = e.getKey();
            List<ChatKeyValueDTO> list = e.getValue();
            if (list == null) {
                out.put(extensionWith, null);
            } else {
                out.put(extensionWith, new ArrayList<>(list));
            }
        }
        return out;
    }
}
