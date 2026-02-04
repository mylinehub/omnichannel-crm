package com.mylinehub.crm.whatsapp.mapper;

import java.util.List;
import java.util.Map;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import com.mylinehub.crm.whatsapp.data.WhatsAppCurrentConversation;
import com.mylinehub.crm.whatsapp.dto.chat.WhatsAppAllChatDTO;
import com.mylinehub.crm.whatsapp.dto.chat.WhatsAppChatDataParameterDTO;
import com.mylinehub.crm.whatsapp.entity.WhatsAppChatHistory;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumber;

@Mapper(componentModel = "spring")
public abstract class WhatsAppAllChatMapper {

    public WhatsAppAllChatDTO mapPhoneNumberWithToChatInfoDto(
            String phoneNumberWith,
            @Context WhatsAppPhoneNumber whatsAppPhoneNumber,
            @Context Map<String, Integer> unreadCountMap) {

        //System.out.println("Mapping phoneNumberWith: " + phoneNumberWith);
        //System.out.println("Context WhatsAppPhoneNumber: " + (whatsAppPhoneNumber != null ? whatsAppPhoneNumber.getPhoneNumber() : "null"));

        WhatsAppAllChatDTO dto = new WhatsAppAllChatDTO();

        try {
            dto.setPhoneNumber(phoneNumberWith);
            dto.setPhoneNumberId(whatsAppPhoneNumber.getId());
            dto.setProjectID(whatsAppPhoneNumber.getWhatsAppProject().getId());

            int unreadCount = computeUnreadCount(phoneNumberWith, whatsAppPhoneNumber,unreadCountMap);
            dto.setBadgeText(unreadCount); // assuming badgeText is a String

            //System.out.println("Mapped WhatsAppAllChatDTO: " + dto);
        } catch (Exception e) {
            //System.out.println("Error while mapping WhatsAppAllChatDTO: " + e.getMessage());
        }

        return dto;
    }

    private int computeUnreadCount(String phoneNumberWith, WhatsAppPhoneNumber whatsAppPhoneNumber,Map<String, Integer> unreadCountMap) {
        //System.out.println("Computing unread count for phoneNumberWith: " + phoneNumberWith);
        int toReturn = 0;

        try {

            toReturn = unreadCountMap.getOrDefault(phoneNumberWith, 0);

            //System.out.println("Interim Undread Count without memeory data: " + toReturn);
            
            WhatsAppChatDataParameterDTO dto = new WhatsAppChatDataParameterDTO();
            dto.setAction("get-all");

            Map<String, WhatsAppChatHistory> currentConversations = WhatsAppCurrentConversation.workOnCurrentMemeoryConversations(dto);
            Map<String, Map<String, List<String>>> currentMessageID = WhatsAppCurrentConversation.workOnCurrentMessageIdConversations(dto);

            Map<String, List<String>> phoneWithListMap = currentMessageID.get(phoneNumberWith);

            if (phoneWithListMap != null) {
                List<String> messageIdList = phoneWithListMap.get(whatsAppPhoneNumber.getPhoneNumber());

                if (messageIdList != null) {
                    //System.out.println("Found " + messageIdList.size() + " messages in memory for phoneNumberWith: " + phoneNumberWith);

                    for (String messageId : messageIdList) {
                        WhatsAppChatHistory current = currentConversations.get(messageId);

                        if (current != null && !current.isReadSelf()) {
                            toReturn++;
                            //System.out.println("Unread in-memory message ID: " + messageId);
                        }
                    }
                } else {
                    //System.out.println("No messageIdList for phoneNumber: " + whatsAppPhoneNumber.getPhoneNumber());
                }
            } else {
                //System.out.println("No phoneWithListMap for phoneNumberWith: " + phoneNumberWith);
            }

        } catch (Exception e) {
            //System.out.println("Exception in computeUnreadCount: " + e.getMessage());
        }

        //System.out.println("Final unread count: " + toReturn);
        return toReturn;
    }
}
