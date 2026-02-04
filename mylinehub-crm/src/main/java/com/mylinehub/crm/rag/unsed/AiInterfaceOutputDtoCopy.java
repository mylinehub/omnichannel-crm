package com.mylinehub.crm.rag.unsed;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;
import com.mylinehub.crm.rag.util.FlexibleMapDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiInterfaceOutputDtoCopy {
	
    // Message Intent, Values , Responses and Flags (Detected by LLM)
	
	//FROM RAG DATA ONLY
	//If rag has fileNames fetch them in below resonse from RAG. Do not use general internet to fill any data here
	//Here data comes strictly from RAG (which is input from user to LLM), no internet data is used.
	//key is filename.type such as photo.png , juice.jpeg, audio.wav etc complete in name which is key of map
	//Value is type of file , one value from enum / set,  which is selected one strictly out from audio,document,image,sticker,video
	@JsonDeserialize(using = FlexibleMapDeserializer.class)
	private Map<String, String> files; 

	
	//Requirment-Case 1: Fill "Requirment-Set 1" properties as isCustomerAskingCreateCustomerSupportTicket,isCustomerAskingAboutPreviousCustomerSupportTicket,scheduleCall, isCustomerAskingAboutCurrentScheduleCall
	
	//BOOLEAN PROPERTY 1 and its data
	//Support ticket can be created for feedback , complain , for human support / connect ,  user wants to interact with organization via some legal number or anything else
	//For such cases this bot will ask user question if he wants to raise question and he needs to enter as input.
	//If isCustomerAskingCreateCustomerSupportTicket is true , It is mandatory to have value for  productOrServiceName and customerEmailId. If we donot have it ask it in llmResponse
	//Use history data in input , and current input data from customer to realize them
	//When we have complete info then only mark it true otherwise in llm response tell user what bot have and what else bot needs
	private boolean isCustomerAskingCreateCustomerSupportTicket; //Requirment-Set 1 property
    //Fill product / service name from user input or rag response only for which customer want to raise feedback / complaint. This is product name customer is talking about
    private String productOrServiceName;
    //For both cases below customerEmailId is mandatory to be filled so LLM should have this info or else isCustomerAskingCreateCustomerSupportTicket / isCustomerAskingAboutPreviousCustomerSupportTicket cannot be made true
    private String customerEmailId;
	// As customer , if he needs to raise complain for Refund or replacement etc. Ask details about his complaint
    //If isCustomerAskingCreateCustomerSupportTicket is true (complaint is mandatory to be filled so LLM should have this info or else isCustomerAskingCreateCustomerSupportTicket cannot be true)
    //This is new complain context customer wants to raise
    private String complaint;
    
    //BOOLEAN PROPERTY 2
    //Decide this on current user input only , not on chat history.
    //This is true when user wants to know status of current open tickets, or else if they exist
    private boolean isCustomerAskingAboutPreviousCustomerSupportTicket; //Requirment-Set 1 property
    

    
    //BOOLEAN PROPERTY 3 and its data
    //This is made true when user wants to connect to user / human agent via phone.
    //But first suggestion should be , If user just want to connect to human agent he can raise a support ticket, thats better option, as human interaction via whatsapp is easy than audio initially. 
    //Still he wants to schedule call , inform in LLM response it will go to queue / IVR and organization executive will respond accordingly.
    //Use chat history input and user input for identifying these values
    //If scheduleCall is true ,  It is mandatory to have value for  scheduleDateTime. If we do not have it ask it in llmResponse
    //If user asks to schedule now, then put this data time after 5 minutes of current Date Time
    //This call will be schedule at given date time, do not ask complete time , take hour and make minutes , second zero to fit in said below format.
	private boolean scheduleCall; //Requirment-Set 1 property
    //If scheduleCall is true (scheduleDateTime is mandatory to be filled or else scheduleCall cannot be true)
    //Date Time on which call schedule is required should be in format such as "2025-10-15T14:30:00+05:30"
    private String scheduleDateTime;

    
    //BOOLEAN PROPERTY 2
    //Decide this on current user input only , not on chat history.
    //This is true when user wants to know status of current scheduled calls, or else if they exist
    private boolean isCustomerAskingAboutCurrentScheduleCall; //Requirment-Set 1 property
    
    //ENUM PROPERTY - value from SET
    //Type of product value should be decided as per 'customerOriginalMessageInput' but can refer chatHistory for reference.
    //Main context remains latest new message but history is just to make sure if we enough enformation related context
    //Value of this string can be strictly one of either Whatsapp, Telecommunication, PhysicalProduct,OtherService
    private String typeOfProduct;


    //ENUM PROPERTY - value from SET
    // Intent can be strictly one of either from : PRICING,PRODUCT_INQUIRY,CATALOG_REQUEST,PAYMENT_QUERY,PASSWORD_RESET,AGENT_REQUEST,FOLLOW_UP,FUTURE_UPDATE,SUPPORT_TICKET,COMPLAINT,PERSONAL_MESSAGE,GROUP_FORWARD,BLOCK_USER,GREETING,GENERAL_QUERY,ORDER_STATUS,SCHEDULE_CALL,REFUND,RETURN,UNKNOWN
    private String intent;
    

    //Check if user wants to stop AI messages
    //Decide its values primary  as per property 'customerOriginalMessageInput' (one property, no other at all)
    //Use history chat as context. History chat can be fund either in user prompt or assistant memory.
    //Fetch history chat data from assistant memory if its not in user prompt
    private boolean stopAIMessage;  //Requirment-Set 2 property, Mark 'stopAIMessage' true if user specifically asks to stop AI messages / user is Abusing / user is frustrated / wants to talk to real agent
    

    //##LLM response Rules
    //LLM response is crux of all response.
	//##If RAG DATA ONLY has links , rules for links response
	// Extract all links from Rag response first.
	// If no link present in RAG, then only extract links from general internet (but maximum two link)
	// Value of string will be as below
	// A symbol / emoji like this related to link, then *Links Description* which can be combination of normal text , bold, italics or strike through as per whats app semantics, then single //n for new line so as link moves just to next line, then finally link https://example.com/product/123.
	// The link description should be more than 5 words, maximum 8 words
    //##Rule for message stop response
    //If stopAIMessage is true , in 'llmResponse' tell user that AI messages will be stopped, tell in polite , few words without reason (Donot add reason why AI messaing stopped and do not give any time. Tell its stopped for now but for sometime (few hours)
    //Donot hesitate to get content from internet, make sure we get valid data. but do this only in case we do not have appropriate data in RAG
    //If rag data is not as per user input meaning , chat histoty do not use rag data
    //heck for is calculaton required
    //##Rule for calculation response
    //If calculation is required , we have this assistant full aware how calculation is to be found Just do it and add details to llmResponse accordingly
    //If user is asking for question such as development charges, cost as per 5 asterisk server , difference between on premise / cloud setup, cost of hiring resources etc.
    //This means if its not one to one mapping between price and product. Then it requires calculation.
    //Mark this as true.
    //For marking this use current user message and history chat messages both. Here history messages are important
    //Use history chat as context. History chat can be fund either in user prompt or assistant memory.
    //Fetch history chat data from assistant memory if its not in user prompt
    //##Rule for words limit & human like representation
    //LLM response should be crusk of all above properties. It should not be too big, as humans do not give too big response.
    //Try to cover info upto 50 words each message string in list
    //No String element in llmResponse can be greator than 30-50 words, if it is, divide into array
    //You can make use of punctutation such as '...' , human jargons as per language, use quotes , story to help user / customer understand product
    //##Rule for emoji
    //Use emoji's but just one / two maximum, not in all messages (that too at most important place)
    //##Rule for sales / self interpretation as individual. This BOT is sales agent.
    //This is sales response and should be customer centric , providing correct info , make user aware but not making him offended , always be polite and respectful.
    //##Rule for whats app. This is whats app bot so its important we use right way to bold , strike , new line which is respected by whats app. It does not allow all MARK DOWN Methods. So these responses should be as per whats app only
    // Use '\\n' for new line
    // Use Bold: *text* or Italic: _text_ or Strikethrough: ~text~, whee required to inform user and emphasize or show how much they save (using strike) where possible. Do not add too much. Its each frequency should be one per two / three messages
    /*
     Use list as pointers where possible
        * Point 1  
		* Point 2
		1. First  
		2. Second
     */
    //##Rule for forget password.
    //If user has forgot password, in '' tell user that password of any Employee can be reset by ADMIN.
    //Hence they should reach out to manager, In case password reset is required for admin, then they should reach out to {{organization}} admin. Mention {{organization}} name.
    //{{organization}}  should be in system prompt of assistant
    //##Rules for input data, user them to summarize. Say if its all time first message fir customer , greet customer well
    //If its new session only , say welcome back
    //Use customerName if its not null or is not invalid name. Or else user generic words
    //This tells if its all time response to customer.
    //private boolean isAllTimeFirstMessage;
    //This tells if its current customer this session / Thread firstMessage.
    //private boolean isSessionFirstMessage;
    //User all other input data such as chat history  etc all of it to generate this response.
    //##Rule for asking question from customer
    //You are sales agent , we do not assume data, we get it from RAG , get it from internet or else ask customer on his needs. We never assume. If we assume we should tell customer what we assumed and our analysis / response.
    //##Rule for llmResponse language
    //Input has customerOriginalMessageInput. This was originally , how customer interacted. Response should also be in same language
    //Even if it is Romanized Hindi, reply back same way. In short reply in same language as customerOriginalMessageInput
    //##Rule - If you get user input such as 'Yes', 'No' etc , see history to find relevant response
    private List<String> llmResponse;
    
}
