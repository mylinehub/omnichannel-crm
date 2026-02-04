package com.mylinehub.crm.rag.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.mylinehub.crm.rag.data.dto.PromptAndAssistantDataDTO;
import com.mylinehub.crm.rag.model.SystemPrompts;
import com.mylinehub.crm.rag.model.AssistantEntity;

public class PromptAndAssistantData {

    // Base timeout in seconds for lock acquisition
    private static final int BASE_TIMEOUT_SECONDS = 2;

    // Locks for system prompts and assistants
    private static final ReentrantLock systemPromptsLock = new ReentrantLock(false); // non-fair
    private static final ReentrantLock assistantsLock = new ReentrantLock(false);    // non-fair

    // Organization is key, value is list of System Prompts belonging to this organization
    private static final Map<String, List<SystemPrompts>> allSystemPrompts = new ConcurrentHashMap<>();
    // Organization is key, value is list of AssistantEntity
    private static final Map<String, List<AssistantEntity>> allAssistants = new ConcurrentHashMap<>();

    /**
     * Manage system prompts per organization
     */
    public static Map<String, List<SystemPrompts>> workWithAllSystemPrompts(PromptAndAssistantDataDTO dto) {
        Map<String, List<SystemPrompts>> toReturn = null;
        boolean acquired = false;

        while (!acquired) {
            try {
                int timeoutSeconds = systemPromptsLock.getQueueLength() + BASE_TIMEOUT_SECONDS;
                acquired = systemPromptsLock.tryLock(timeoutSeconds, TimeUnit.SECONDS);
                if (!acquired) continue; // retry

                if (dto == null || dto.getOrganization() == null) {
                    System.out.println("[PromptData]  DTO or organization is null — skipping");
                    return null;
                }

                String org = dto.getOrganization();
                String action = dto.getAction();
                List<SystemPrompts> current = null;

                System.out.println("[PromptData] Action: " + action + " | Org: " + org);

                switch (action) {

                    case "get-one":
                        current = allSystemPrompts.get(org);
                        System.out.println("[PromptData] get-one => " + (current == null ? "No prompts found" : current.size() + " prompts found"));
                        if (current != null) {
                            toReturn = new HashMap<>();
                            toReturn.put(org, new ArrayList<>(current));
                        }
                        break;

                    case "get":
                        // Return shallow copy
                        toReturn = new HashMap<>();
                        System.out.println("[PromptData] get => Returning all prompts for all orgs: " + allSystemPrompts.keySet());
                        for (Map.Entry<String, List<SystemPrompts>> entry : allSystemPrompts.entrySet()) {
                            toReturn.put(entry.getKey(), new ArrayList<>(entry.getValue()));
                        }
                        break;

                    case "update":
                        current = allSystemPrompts.get(org);
                        if (current == null) current = new ArrayList<>();
                        else current.removeIf(p -> p.getId() != null && dto.getPrompt() != null &&
                                p.getId().equals(dto.getPrompt().getId()));

                        if (dto.getPrompt() != null) {
                            current.add(dto.getPrompt());
                            System.out.println("[PromptData] update => Added/Updated prompt: " + dto.getPrompt().getId());
                        }
                        allSystemPrompts.put(org, current);
                        System.out.println("[PromptData] update => New total: " + current.size());
                        break;

                    case "delete":
                        current = allSystemPrompts.get(org);
                        if (current != null && dto.getPrompt() != null) {
                            int before = current.size();
                            current.removeIf(p -> p.getId() != null && p.getId().equals(dto.getPrompt().getId()));
                            allSystemPrompts.put(org, current);
                            System.out.println("[PromptData] delete => Removed prompt ID: " + dto.getPrompt().getId()
                                    + " | Before: " + before + " | After: " + current.size());
                        }
                        break;

                    case "updateCompleteBatch":
                        allSystemPrompts.put(org, dto.getBatchPrompt());
                        System.out.println("[PromptData] updateCompleteBatch => Replaced batch for org: " + org
                                + " | Size: " + (dto.getBatchPrompt() != null ? dto.getBatchPrompt().size() : 0));
                        break;

                    default:
                        System.out.println("[PromptData] Unknown action: " + action);
                        break;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("[PromptData] Exception: " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (acquired) systemPromptsLock.unlock();
            }
        }

        return toReturn;
    }

    /**
     * Manage assistants per organization
     */
    public static Map<String, List<AssistantEntity>> workWithAllAssistants(PromptAndAssistantDataDTO dto) {
        Map<String, List<AssistantEntity>> toReturn = null;
        boolean acquired = false;

        while (!acquired) {
            try {
                int timeoutSeconds = assistantsLock.getQueueLength() + BASE_TIMEOUT_SECONDS;
                acquired = assistantsLock.tryLock(timeoutSeconds, TimeUnit.SECONDS);
                if (!acquired) continue; // retry

                if (dto == null || dto.getOrganization() == null) {
                    System.out.println("[AssistantData]  DTO or organization is null — skipping");
                    return null;
                }

                String org = dto.getOrganization();
                String action = dto.getAction();
                List<AssistantEntity> current = null;

                System.out.println("[AssistantData] Action: " + action + " | Org: " + org);

                switch (action) {

                    case "get-one":
                        current = allAssistants.get(org);
                        System.out.println("[AssistantData] get-one => " + (current == null ? "No assistants found" : current.size() + " assistants found"));
                        if (current != null) {
                            toReturn = new HashMap<>();
                            toReturn.put(org, new ArrayList<>(current));
                        }
                        break;

                    case "get":
                        toReturn = new HashMap<>();
                        System.out.println("[AssistantData] get => Returning all assistants for all orgs: " + allAssistants.keySet());
                        for (Map.Entry<String, List<AssistantEntity>> entry : allAssistants.entrySet()) {
                            toReturn.put(entry.getKey(), new ArrayList<>(entry.getValue()));
                        }
                        break;

                    case "update":
                        current = allAssistants.get(org);
                        if (current == null) current = new ArrayList<>();
                        else current.removeIf(a -> a.getId() != null && dto.getAssistantEntity() != null &&
                                a.getId().equals(dto.getAssistantEntity().getId()));

                        if (dto.getAssistantEntity() != null) {
                            current.add(dto.getAssistantEntity());
                            System.out.println("[AssistantData] update => Added/Updated assistant: " + dto.getAssistantEntity().getName());
                        }
                        allAssistants.put(org, current);
                        System.out.println("[AssistantData] update => New total: " + current.size());
                        break;

                    case "delete":
                        current = allAssistants.get(org);
                        if (current != null && dto.getAssistantEntity() != null) {
                            int before = current.size();
                            current.removeIf(a -> a.getId() != null && a.getId().equals(dto.getAssistantEntity().getId()));
                            allAssistants.put(org, current);
                            System.out.println("[AssistantData] delete => Removed assistant ID: " + dto.getAssistantEntity().getId()
                                    + " | Before: " + before + " | After: " + current.size());
                        }
                        break;

                    case "updateCompleteBatch":
                        allAssistants.put(org, dto.getBatchAssistantEntity());
                        System.out.println("[AssistantData] updateCompleteBatch => Replaced assistant batch for org: " + org
                                + " | Size: " + (dto.getBatchAssistantEntity() != null ? dto.getBatchAssistantEntity().size() : 0));
                        break;

                    default:
                        System.out.println("[AssistantData] Unknown action: " + action);
                        break;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("[AssistantData] Exception: " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (acquired) assistantsLock.unlock();
            }
        }

        return toReturn;
    }
}
