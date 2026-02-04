package com.mylinehub.crm.rag.unsed;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiInterfaceInputDtoCopy {

	//This is original input from customer. In whatever laguage is types in from input box
    //The output ptoperties - languageScript, actualLanguage has to be filled as per this text
    //Language fill instructions should be in system prompt
    //If this is not English, ragResponseRequireFixAsPerLanguage is an output property in LLM response, make it true only if this input message is not in English
    private String customerOriginalMessageInput;
    
    //This is input from customer in English language , if it was not english originally
    //This was used to fetch vector result. 
    //As RAG is in english , hence it was important to convert language to english
    //If this is not English, ragResponseRequireFixAsPerLanguage is an output property in LLM response, make it true only if this input message is not in English
    private String customerConvertedMessageInput;
    
    
    // Input Handling, This includes all previous messages returned as response from User to LLM.
    // Last N Messages, as per Array Size
    private List<String> messageResponseHistoryFromUser;
    
    // Input Handling, This includes all previous messages returned as response from LLM to user.
    // Last N Messages, as per Array Size
    private List<String> messageResponseHistoryFromLLM;

    // RAG (Retrieved Augmented Generation)
    // Top N Messages, as per Array Size, all of them have a RANK Greator than 6 only.
    // If this is empty , null - Try to fetch answer of customer question from general internal as per Product type / Intent / Previous Message Discussion
    private List<String> ragResponse;
    
    
    //This tells if its all time response to customer.
    private boolean isAllTimeFirstMessage;
    
    //This tells if its current customer this session / Thread firstMessage.
    private boolean isSessionFirstMessage;
    
    //Check customer name , if it does not start with special character or number & is valid name, then only use this in 'llmResponse' output or else put generic such as 'Dear User,'
    //Use this only when either iStringAllTimeFirstMessage is true , greet as per all time first message or 'isSessionFirstMessage' is true , greet something like welcome back. User knows we know he is back
    private String customerName;
    
    //Tells how calculation is to be done is required or else this is null
    private String calculationLogic;
    
    //Video link data for all demo videos, this is filled when customer is asking video / demo.
    private String videoLinkData;
}
