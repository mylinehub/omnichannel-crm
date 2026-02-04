package com.mylinehub.crm.whatsapp.api.cloud.wrapper.messages;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.mylinehub.crm.entity.Customers;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.Organization;
import com.mylinehub.crm.entity.Product;
import com.mylinehub.crm.entity.Purchases;
import com.mylinehub.crm.utils.LoggerUtils;
import com.mylinehub.crm.utils.okhttp.OkHttpResponseFunctions;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.SendMessageToWhatsApp;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.media.WhatsAppTemplateMediaIdService;
import com.mylinehub.crm.whatsapp.dto.WhatsAppPhoneNumberTemplateVariableDto;
import com.mylinehub.crm.whatsapp.dto.general.MediaDto;
import com.mylinehub.crm.whatsapp.dto.general.ParameterDto;
import com.mylinehub.crm.whatsapp.dto.general.TextDto;
import com.mylinehub.crm.whatsapp.dto.general.template.ComponentDto;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumberTemplates;
import com.mylinehub.crm.whatsapp.enums.COMPONENT_TYPE;
import com.mylinehub.crm.whatsapp.enums.MESSAGE_TYPE;
import com.mylinehub.crm.whatsapp.enums.RECEPIENT_TYPE;
import com.mylinehub.crm.whatsapp.enums.SEND_MESSAGE_KEYS;
import com.mylinehub.crm.whatsapp.enums.TEMPLATE_VARIABLES;
import com.mylinehub.crm.whatsapp.requests.WhatsAppTemplateVariableRequest;
import com.mylinehub.crm.whatsapp.service.WhatsAppNumberTemplateVariableService;

import lombok.AllArgsConstructor;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class OkHttpSendTemplateMessageClient {

	private final SendMessageToWhatsApp sendMessageToWhatsApp;
	private final WhatsAppNumberTemplateVariableService whatsAppNumberTemplateVariableService;
	private final WhatsAppTemplateMediaIdService whatsAppTemplateMediaIdService;
	
	public JSONObject sendMessage(String messagingProduct,String recipientPhoneNumber,WhatsAppTemplateVariableRequest whatsAppTemplateVariableRequest, String languageCode,String version,String phoneNumberID, String token) throws Exception
	{
		JSONObject toReturn = null;
		try {
			
				System.out.println("OkHttpSendTemplateMessageClient sendMessage");

				MediaType mimeType = MediaType.parse("application/json");
				RequestBody body;
				JSONArray componentArray = new JSONArray();

				WhatsAppPhoneNumberTemplates template = whatsAppTemplateVariableRequest.getWhatsAppPhoneNumberTemplate();
				
				// 1) Inject HEADER MEDIA if template has mediaPath (or mediaId)
				if (template.getMediaPath() != null && !template.getMediaPath().trim().isEmpty()) {
				
				    // ensures mediaId exists and is valid (uploads if needed)
				    String mediaId = whatsAppTemplateMediaIdService.getOrUploadMediaId(template);
				
				    String mediaType = template.getMediaType(); // new column
				    if (mediaType == null || mediaType.trim().isEmpty()) {
				        throw new Exception("Template has mediaPath but mediaType is null/blank. template=" + template.getTemplateName());
				    }
				
				    componentArray.put(buildHeaderMediaComponent(mediaType, mediaId));
				}

				
				JSONObject languageObject = new JSONObject();
				languageObject.put(SEND_MESSAGE_KEYS.code.name(), languageCode);
				
				JSONObject templateObject = new JSONObject();
				String tname = whatsAppTemplateVariableRequest.getWhatsAppPhoneNumberTemplate().getTemplateName();
				if (tname == null || tname.trim().isEmpty()) {
				    throw new Exception("WhatsApp template name is null/blank");
				}
				templateObject.put(SEND_MESSAGE_KEYS.name.name(), tname.trim());
				templateObject.put(SEND_MESSAGE_KEYS.language.name(), languageObject);
				
				LoggerUtils.log.debug("JSON component Array, template & language Object created in java");
				System.out.println("TemplateObject before component: "+templateObject);
				
				System.out.println("*** Generating template variables ... ***");
				List<ComponentDto> variables = generateTemplateVariables(whatsAppTemplateVariableRequest);
				
				if(variables != null && variables.size() >0)
				{
					System.out.println("*** Generating components ... ***");
					componentArray = generateComponents(variables,whatsAppTemplateVariableRequest.getWhatsAppPhoneNumberTemplate().isFollowOrder(),componentArray);
					
					if (componentArray != null) {
						LoggerUtils.log.debug("Embedding components");
						templateObject.put(SEND_MESSAGE_KEYS.components.name(), componentArray);
					}
					else {
						throw new Exception("Template component values are not correct. This template is not supported by application. Contact Admin.");
					}
				}
				else {
					LoggerUtils.log.debug("Found no variables for this template : "+whatsAppTemplateVariableRequest.getWhatsAppPhoneNumberTemplate().getTemplateName());
				}
				
				System.out.println("componentArray : "+componentArray);
				System.out.println("TemplateObject after component : "+templateObject);
				System.out.println("languageObject : "+languageObject);
				
				JSONObject bodyObject = new JSONObject();
				bodyObject.put(SEND_MESSAGE_KEYS.messaging_product.name(), messagingProduct);
				bodyObject.put(SEND_MESSAGE_KEYS.recipient_type.name(), RECEPIENT_TYPE.individual.name());
				bodyObject.put(SEND_MESSAGE_KEYS.to.name(), recipientPhoneNumber);
				bodyObject.put(SEND_MESSAGE_KEYS.type.name(), MESSAGE_TYPE.template.name());
				bodyObject.put(SEND_MESSAGE_KEYS.template.name(), templateObject);
				
				LoggerUtils.log.debug("JSON body object created in java");
				LoggerUtils.log.debug("Mime Type : "+mimeType);
				System.out.println("*****************************************************************");
				System.out.println("Body Before Sending");
				System.out.println("*****************************************************************");
				System.out.println(bodyObject);
				
				body= RequestBody.create(mimeType,bodyObject.toString());
				
				System.out.println("Sending normal whats app message");
				
				try (Response response = sendMessageToWhatsApp.sendNormalMessage(body, version, phoneNumberID, token)) {
					toReturn = new OkHttpResponseFunctions().extractJSONObjectFromResponseAndCloseBuffer(response);
		        }
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw e;
		}
		
		return toReturn;
	}
	
	
	
	private JSONArray generateComponents(List<ComponentDto> variables,boolean followOrder,JSONArray componentArray) throws Exception {
		// TODO Auto-generated method stub
		
		try {
			
			    LoggerUtils.log.debug("******* Generate Components Started ... *********");
				LoggerUtils.log.debug("Staring variable loop ...");
				
				for(int i =0; i<variables.size(); i++)
				{
					LoggerUtils.log.debug("Variable : "+ (i+1) + " : INDEX : "+variables.get(i).getIndex()+" : SUB-TYPE : "+variables.get(i).getSub_type()+" : TYPE : "+variables.get(i).getType());
					ComponentDto componentDto  = variables.get(i);
					
					JSONObject mainArrayObjectValue	= new JSONObject();
					JSONArray objectValueParameterArray = new JSONArray();
					
					LoggerUtils.log.debug("Adding type of component ...");
					mainArrayObjectValue.put(SEND_MESSAGE_KEYS.type.name(), componentDto.getType());
					
					LoggerUtils.log.debug("Adding sub type of component ...");
					if(componentDto.getSub_type() != null && componentDto.getSub_type() != "") {
						mainArrayObjectValue.put(SEND_MESSAGE_KEYS.sub_type.name(), componentDto.getSub_type());
					}
					
					LoggerUtils.log.debug("Adding index of component ...");
					if(componentDto.getIndex() != null && componentDto.getIndex() != "") {
						mainArrayObjectValue.put(SEND_MESSAGE_KEYS.index.name(), componentDto.getIndex());
					}
					
					
					if(componentDto.getType().equals(COMPONENT_TYPE.header.name())||
							componentDto.getType().equals(COMPONENT_TYPE.body.name())||
								componentDto.getType().equals(COMPONENT_TYPE.footer.name()))
					{
						LoggerUtils.log.debug("Adding parameter of component (having type out of header/body/footer) ...");
						if(componentDto.getParameters() != null && componentDto.getParameters().size()>0)
						{
							JSONObject parameterValue = null;
							
							if(!followOrder) {
								LoggerUtils.log.debug("Staring parameter loop ...");
								for(int j = 0 ; j< componentDto.getParameters().size();j++)
								{
									ParameterDto parameterDto = componentDto.getParameters().get(j);
									parameterValue = putSingleNonButtonParameter(componentDto,parameterDto,followOrder);
									objectValueParameterArray.put(parameterValue);
									LoggerUtils.log.debug("Before second loop ends(First If), latest value of objectValueParameterArray : "+objectValueParameterArray);
								}
							}
							else {
								 //Follow order
								 LoggerUtils.log.debug("Follow order while creating template");
								 List<ParameterDto> parameters = componentDto.getParameters();
								 Map<Integer,ParameterDto> parameterMap = new HashMap<>();
								 
								 parameters.forEach((element)->{
									 parameterMap.put(element.getIndex(), element);
								 });
								 
								 for(int k = 0 ; k< parameterMap.size();k++) {
									 ParameterDto parameterDto = parameterMap.get(k+1);
									 
									 if(parameterDto== null) {
										 throw new Exception("Order of template is not correct. Whats-app message will not be send. It should have integer : "+(k+1));
									 }
									 
									 parameterValue = putSingleNonButtonParameter(componentDto,parameterDto,followOrder);
									 objectValueParameterArray.put(parameterValue);
									 LoggerUtils.log.debug("Before second loop ends(First If), latest value of objectValueParameterArray : "+objectValueParameterArray);
					
								 }
							}
							
							//Second loop ends here for first If
							mainArrayObjectValue.put(SEND_MESSAGE_KEYS.parameters.name(), objectValueParameterArray);
							componentArray.put(mainArrayObjectValue);
							LoggerUtils.log.debug("Before first loop ends (first If for inside loop),, latest value of componentArray : "+componentArray);
							
						}
						else
						{
							LoggerUtils.log.debug("Considering parameters list size is zero, we do not need to add this to return array.");
						}
					}
					else if(componentDto.getType().equals(COMPONENT_TYPE.button.name())){
						LoggerUtils.log.debug("Adding parameter of component (having type button)");
						
						JSONObject parameterValue = null;
						
						if(!followOrder) {
							
							for(int k =0 ; k< componentDto.getParameters().size();k++)
							{
								ParameterDto parameterDto = componentDto.getParameters().get(k);
								parameterValue = putSingleButtonParameter(parameterDto,followOrder);
								objectValueParameterArray.put(parameterValue);
								LoggerUtils.log.debug("Before second loop ends(Second If), latest value of objectValueParameterArray : "+objectValueParameterArray);
							}
							
						}
						else {
							//Follow order
							 LoggerUtils.log.debug("Follow order while creating template");
							 List<ParameterDto> parameters = componentDto.getParameters();
							 Map<Integer,ParameterDto> parameterMap = new HashMap<>();
							 
							 parameters.forEach((element)->{
								 parameterMap.put(element.getIndex(), element);
							 });
							 
							 for(int k = 0 ; k< parameterMap.size();k++) {
								 ParameterDto parameterDto = parameterMap.get(k+1);
								 
								 if(parameterDto== null) {
									 throw new Exception("Order of template is not correct. Whats-app message will not be send. It should have integer : "+(k+1));
								 }

								 parameterValue = putSingleButtonParameter(parameterDto,followOrder);
								 objectValueParameterArray.put(parameterValue);
								 LoggerUtils.log.debug("Before second loop ends(Second If), latest value of objectValueParameterArray : "+objectValueParameterArray);
				
							 }
						}
						
						
						//second loop ends here for second If
						mainArrayObjectValue.put(SEND_MESSAGE_KEYS.parameters.name(), objectValueParameterArray);
						componentArray.put(mainArrayObjectValue);
						LoggerUtils.log.debug("Before first loop ends (second If for inside loop), latest value of componentArray : "+componentArray);
					}
					else if(componentDto.getType().equals(COMPONENT_TYPE.action.name())){
						
					}
					else {
						throw new Exception ("Component type : "+componentDto.getType()+" not supported.");
					}
				}
				//First loop ends here
				
				LoggerUtils.log.debug("Generate Component method execution complete");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw e;
		}
		
		return componentArray;
	}
	
	JSONObject putSingleButtonParameter(ParameterDto parameterDto, boolean followOrder) throws Exception{
		JSONObject parameterValue = new JSONObject();
		parameterValue.put(SEND_MESSAGE_KEYS.type.name(), parameterDto.getType());
		if(parameterDto.getType().equals(SEND_MESSAGE_KEYS.text.name()))
		{
			parameterValue.put(SEND_MESSAGE_KEYS.text.name(), parameterDto.getText().getBody());
		}
		else if(parameterDto.getType().equals(SEND_MESSAGE_KEYS.payload.name()))
		{
			parameterValue.put(SEND_MESSAGE_KEYS.payload.name(), parameterDto.getText().getBody());
		}
		else if(parameterDto.getType().equals(SEND_MESSAGE_KEYS.action.name()))
		{
			JSONObject actionData = new JSONObject();
			actionData.put(SEND_MESSAGE_KEYS.thumbnail_product_retailer_id.name(), parameterDto.getId());
			parameterValue.put(SEND_MESSAGE_KEYS.action.name(), actionData);
		}	
		else {
			throw new Exception("Button Type :"+parameterDto.getType()+" is not supported.");
		}
		
		return parameterValue;
	}
	
	JSONObject putSingleNonButtonParameter(ComponentDto componentDto,ParameterDto parameterDto, boolean followOrder) throws Exception{
		
		LoggerUtils.log.debug("Parameter Name : "+parameterDto.getParameter_name());
		LoggerUtils.log.debug("Parameter Type : "+parameterDto.getType());
		LoggerUtils.log.debug("Parameter Sub Type : "+parameterDto.getSub_type());
		LoggerUtils.log.debug("Parameter Index : "+parameterDto.getIndex());
		LoggerUtils.log.debug("Parameter Language Code : "+parameterDto.getLanguageCode());
		
		JSONObject parameterValue = new JSONObject();
		parameterValue.put(SEND_MESSAGE_KEYS.type.name(), parameterDto.getType());
		
		if(!followOrder) {
			parameterValue.put(SEND_MESSAGE_KEYS.parameter_name.name(), parameterDto.getParameter_name());
		}
		else {
			LoggerUtils.log.debug("It is ordered template, name is not passed to okhttp request");
		}
		
		LoggerUtils.log.debug("After creating parameterValue, now put actual data in parameterValue");
		
		if(parameterDto.getType().equals(SEND_MESSAGE_KEYS.text.name()))
		{
			LoggerUtils.log.debug("Type Text");
			if(componentDto.getType().equals(COMPONENT_TYPE.header.name())||
					componentDto.getType().equals(COMPONENT_TYPE.body.name()))
			{
				parameterValue.put(SEND_MESSAGE_KEYS.text.name(), parameterDto.getText().getBody());
				LoggerUtils.log.debug("parameterValue: "+parameterValue);
				
			}
			else if(componentDto.getType().equals(COMPONENT_TYPE.button.name()))
			{
				throw new Exception("Text is not allowed in button");
			}
		}
		else if(parameterDto.getType().equals(SEND_MESSAGE_KEYS.image.name()))
		{
			LoggerUtils.log.debug("Type image");
			if(componentDto.getType().equals(COMPONENT_TYPE.header.name()))
			{
				JSONObject data = new JSONObject();
				if(parameterDto.getMedia().getLink()!=null && parameterDto.getMedia().getLink() != "")
					data.put(SEND_MESSAGE_KEYS.link.name(), parameterDto.getMedia().getLink());
				else
					data.put(SEND_MESSAGE_KEYS.id.name(), parameterDto.getMedia().getId());
				
				if(parameterDto.getMedia().getCaption() != null && parameterDto.getMedia().getCaption() != "")
					data.put(SEND_MESSAGE_KEYS.caption.name(), parameterDto.getMedia().getCaption());
				if(parameterDto.getMedia().getFileName() != null && parameterDto.getMedia().getFileName() != "")
					data.put(SEND_MESSAGE_KEYS.filename.name(), parameterDto.getMedia().getFileName());
				parameterValue.put(SEND_MESSAGE_KEYS.image.name(),data);
				LoggerUtils.log.debug("parameterValue: "+parameterValue);
			}
			else if(componentDto.getType().equals(COMPONENT_TYPE.body.name())||
					componentDto.getType().equals(COMPONENT_TYPE.button.name()))
			{
				throw new Exception("Image is not allowed in button or body");
			}
		}
		else if(parameterDto.getType().equals(SEND_MESSAGE_KEYS.audio.name()))
		{
			LoggerUtils.log.debug("Type audio");
			if(componentDto.getType().equals(COMPONENT_TYPE.header.name()))
			{
				JSONObject data = new JSONObject();
				if(parameterDto.getMedia().getLink()!=null && parameterDto.getMedia().getLink() != "")
					data.put(SEND_MESSAGE_KEYS.link.name(), parameterDto.getMedia().getLink());
				else
					data.put(SEND_MESSAGE_KEYS.id.name(), parameterDto.getMedia().getId());
				
				if(parameterDto.getMedia().getCaption() != null && parameterDto.getMedia().getCaption() != "")
					data.put(SEND_MESSAGE_KEYS.caption.name(), parameterDto.getMedia().getCaption());
				if(parameterDto.getMedia().getFileName() != null && parameterDto.getMedia().getFileName() != "")
					data.put(SEND_MESSAGE_KEYS.filename.name(), parameterDto.getMedia().getFileName());
				parameterValue.put(SEND_MESSAGE_KEYS.audio.name(),data);
				
				LoggerUtils.log.debug("parameterValue: "+parameterValue);
			}
			else if(componentDto.getType().equals(COMPONENT_TYPE.body.name())||
					componentDto.getType().equals(COMPONENT_TYPE.button.name()))
			{
				throw new Exception("Audio is not allowed in body or button");
			}
		}
		else if(parameterDto.getType().equals(SEND_MESSAGE_KEYS.video.name()))
		{
			LoggerUtils.log.debug("Type video");
			if(componentDto.getType().equals(COMPONENT_TYPE.header.name()))
			{
				JSONObject data = new JSONObject();
				if(parameterDto.getMedia().getLink()!=null || parameterDto.getMedia().getLink() != "")
				{
					data.put(SEND_MESSAGE_KEYS.link.name(), parameterDto.getMedia().getLink());
				}
				else
				{
					data.put(SEND_MESSAGE_KEYS.id.name(), parameterDto.getMedia().getId());
				}
				if(parameterDto.getMedia().getCaption() != null && parameterDto.getMedia().getCaption() != "")
					data.put(SEND_MESSAGE_KEYS.caption.name(), parameterDto.getMedia().getCaption());
				if(parameterDto.getMedia().getFileName() != null && parameterDto.getMedia().getFileName() != "")
					data.put(SEND_MESSAGE_KEYS.filename.name(), parameterDto.getMedia().getFileName());
				parameterValue.put(SEND_MESSAGE_KEYS.video.name(),data);
				LoggerUtils.log.debug("parameterValue: "+parameterValue);
			}
			else if(componentDto.getType().equals(COMPONENT_TYPE.body.name())||
					componentDto.getType().equals(COMPONENT_TYPE.button.name()))
			{
				throw new Exception("Video is not allowed in body or button");
			}
		}
		else if(parameterDto.getType().equals(SEND_MESSAGE_KEYS.document.name()))
		{
			LoggerUtils.log.debug("Type document");
			if(componentDto.getType().equals(COMPONENT_TYPE.header.name()))
			{
				JSONObject data = new JSONObject();
				if(parameterDto.getMedia().getLink()!=null || parameterDto.getMedia().getLink() != "")
				{
					data.put(SEND_MESSAGE_KEYS.link.name(), parameterDto.getMedia().getLink());
				}
				else
				{
					data.put(SEND_MESSAGE_KEYS.id.name(), parameterDto.getMedia().getId());
				}
				
				if(parameterDto.getMedia().getCaption() != null && parameterDto.getMedia().getCaption() != "")
					data.put(SEND_MESSAGE_KEYS.caption.name(), parameterDto.getMedia().getCaption());
				if(parameterDto.getMedia().getFileName() != null && parameterDto.getMedia().getFileName() != "")
					data.put(SEND_MESSAGE_KEYS.filename.name(), parameterDto.getMedia().getFileName());
				parameterValue.put(SEND_MESSAGE_KEYS.document.name(),data);
				LoggerUtils.log.debug("parameterValue: "+parameterValue);
			}
			else if(componentDto.getType().equals(COMPONENT_TYPE.body.name())||
					componentDto.getType().equals(COMPONENT_TYPE.button.name()))
			{
				throw new Exception("Document is not allowed in body or button");
			}
		}
		else if(parameterDto.getType().equals(SEND_MESSAGE_KEYS.sticker.name()))
		{
			LoggerUtils.log.debug("Type sticker");
			if(componentDto.getType().equals(COMPONENT_TYPE.header.name()))
			{
				JSONObject data = new JSONObject();
				if(parameterDto.getMedia().getLink()!=null || parameterDto.getMedia().getLink() != "")
					data.put(SEND_MESSAGE_KEYS.link.name(), parameterDto.getMedia().getLink());
				else
					data.put(SEND_MESSAGE_KEYS.id.name(), parameterDto.getMedia().getId());
				
				if(parameterDto.getMedia().getCaption() != null && parameterDto.getMedia().getCaption() != "")
					data.put(SEND_MESSAGE_KEYS.caption.name(), parameterDto.getMedia().getCaption());
				if(parameterDto.getMedia().getFileName() != null && parameterDto.getMedia().getFileName() != "")
					data.put(SEND_MESSAGE_KEYS.filename.name(), parameterDto.getMedia().getFileName());
				
				parameterValue.put(SEND_MESSAGE_KEYS.sticker.name(),data);
				LoggerUtils.log.debug("parameterValue: "+parameterValue);
			}
			else if(componentDto.getType().equals(COMPONENT_TYPE.body.name())||
					componentDto.getType().equals(COMPONENT_TYPE.button.name()))
			{
				throw new Exception("Sticker is not allowed in body or button");
			}
		}
		else if(parameterDto.getType().equals(SEND_MESSAGE_KEYS.currency.name()))
		{
			LoggerUtils.log.debug("Type currency");
			if(componentDto.getType().equals(COMPONENT_TYPE.header.name())||
					componentDto.getType().equals(COMPONENT_TYPE.button.name()))
			{
				throw new Exception("Currency is not allowed in header or button");
			}
			else if (componentDto.getType().equals(COMPONENT_TYPE.body.name()))
			{
				JSONObject data = new JSONObject();
				data.put(SEND_MESSAGE_KEYS.fallback_value.name(), parameterDto.getCurrency().getFallback_value());
				data.put(SEND_MESSAGE_KEYS.code.name(),parameterDto.getCurrency().getCode());
				data.put(SEND_MESSAGE_KEYS.amount_1000.name(), parameterDto.getCurrency().getAmount_1000());
				
				parameterValue.put(SEND_MESSAGE_KEYS.currency.name(),data);
				LoggerUtils.log.debug("parameterValue: "+parameterValue);
			}
		}
		else if(parameterDto.getType().equals(SEND_MESSAGE_KEYS.date_time.name()))
		{
			LoggerUtils.log.debug("Type date_time");
			if(componentDto.getType().equals(COMPONENT_TYPE.header.name())||
					componentDto.getType().equals(COMPONENT_TYPE.button.name()))
			{
				throw new Exception("Date Time is not allowed in header or button");
			}
			else if (componentDto.getType().equals(COMPONENT_TYPE.body.name()))
			{
				JSONObject data = new JSONObject();
				data.put(SEND_MESSAGE_KEYS.fallback_value.name(), parameterDto.getDateTime().getFallback_value());
				data.put(SEND_MESSAGE_KEYS.day_of_week.name(),parameterDto.getDateTime().getDay_of_week());
				data.put(SEND_MESSAGE_KEYS.year.name(), parameterDto.getDateTime().getYear());
				data.put(SEND_MESSAGE_KEYS.month.name(), parameterDto.getDateTime().getMonth());
				data.put(SEND_MESSAGE_KEYS.day_of_month.name(), parameterDto.getDateTime().getDay_of_month());
				data.put(SEND_MESSAGE_KEYS.hour.name(), parameterDto.getDateTime().getHour());
				data.put(SEND_MESSAGE_KEYS.minute.name(), parameterDto.getDateTime().getMinute());
				data.put(SEND_MESSAGE_KEYS.calendar.name(), parameterDto.getDateTime().getCalendar());

				parameterValue.put(SEND_MESSAGE_KEYS.date_time.name(),data);
				LoggerUtils.log.debug("parameterValue: "+parameterValue);
			}
		}
		
		return parameterValue;
	}
	
	
	List<ComponentDto> generateTemplateVariables(WhatsAppTemplateVariableRequest whatsAppTemplateVariableRequest) throws Exception
	{
		List<ComponentDto> toReturn = new ArrayList<>();
		Map<String,List<ParameterDto>> allParameterValues = new HashMap<>();
		
		LoggerUtils.log.debug("***** OkHttpSendTemplateMessageClient generateTemplateVariables *******");
		
		try {
				List<WhatsAppPhoneNumberTemplateVariableDto> allVariablesAppPhoneNumberTemplateVariableDtos = whatsAppNumberTemplateVariableService.findAllByWhatsAppNumberTemplateAndOrganization(whatsAppTemplateVariableRequest.getWhatsAppPhoneNumberTemplate().getId(), whatsAppTemplateVariableRequest.getWhatsAppPhoneNumberTemplate().getOrganization());
				
				if(allVariablesAppPhoneNumberTemplateVariableDtos == null || allVariablesAppPhoneNumberTemplateVariableDtos.isEmpty()) {
				    return new ArrayList<>(); // no variables, but still allow template send
				}
				
				LoggerUtils.log.debug("**** Number of Template variables : "+allVariablesAppPhoneNumberTemplateVariableDtos.size());
				
				WhatsAppPhoneNumberTemplates template = whatsAppTemplateVariableRequest.getWhatsAppPhoneNumberTemplate();
				LoggerUtils.log.debug("Fetched template");
				Product product = template.getProduct();
				LoggerUtils.log.debug("Fetched template product");
				Customers customer = whatsAppTemplateVariableRequest.getCustomer();
				LoggerUtils.log.debug("Fetched template customer");
				Purchases purchase = whatsAppTemplateVariableRequest.getPurchase();
				LoggerUtils.log.debug("Fetched template purchase");
				Employee employee = whatsAppTemplateVariableRequest.getEmployee();
				LoggerUtils.log.debug("Fetched template employee");
				Organization organization = whatsAppTemplateVariableRequest.getOrganization();
				LoggerUtils.log.debug("Fetched template organization");
				
				String code = whatsAppTemplateVariableRequest.getCode();
				LoggerUtils.log.debug("Code : "+code);
				String amount = whatsAppTemplateVariableRequest.getAmount();
				LoggerUtils.log.debug("Amount : "+amount);
				String inputText = whatsAppTemplateVariableRequest.getText();
				LoggerUtils.log.debug("Input Text : "+inputText);
				
				Date date = whatsAppTemplateVariableRequest.getDate();
				LoggerUtils.log.debug("Date : "+date);
				Date today_date = whatsAppTemplateVariableRequest.getToday_date();
				LoggerUtils.log.debug("Today Date : "+today_date);
				Date yesterday_date = whatsAppTemplateVariableRequest.getYesterday_date();
				LoggerUtils.log.debug("Yesterday Date : "+yesterday_date);
				
				String currency = whatsAppTemplateVariableRequest.getCurrency();
				LoggerUtils.log.debug("Currency : "+currency);
				String name = whatsAppTemplateVariableRequest.getName();
				LoggerUtils.log.debug("Name : "+name);
				String email = whatsAppTemplateVariableRequest.getEmail();
				LoggerUtils.log.debug("Email : "+email);
				String parentorg = whatsAppTemplateVariableRequest.getParentorg();
				LoggerUtils.log.debug("Parent Org : "+parentorg);
				String reason = whatsAppTemplateVariableRequest.getReason();
				LoggerUtils.log.debug("Reason : "+reason);
				String retailer_id = whatsAppTemplateVariableRequest.getRetailer_id();
				LoggerUtils.log.debug("retailer_id : "+retailer_id);
				
				LoggerUtils.log.debug("Entering into loop for template variables");
				for(int i =0 ; i< allVariablesAppPhoneNumberTemplateVariableDtos.size() ; i++)
					{
						WhatsAppPhoneNumberTemplateVariableDto current = allVariablesAppPhoneNumberTemplateVariableDtos.get(i);
		
						// HEADER MEDIA is handled from WhatsAppPhoneNumberTemplates (static per template)
						// Ignore header media variables in DB so they don't overwrite header component
						if (COMPONENT_TYPE.header.name().equals(current.getVariableHeaderType())
						        && (TEMPLATE_VARIABLES.image.name().equals(current.getVariableName())
						            || TEMPLATE_VARIABLES.video.name().equals(current.getVariableName())
						            || TEMPLATE_VARIABLES.audio.name().equals(current.getVariableName())
						            || TEMPLATE_VARIABLES.document.name().equals(current.getVariableName())
						            || TEMPLATE_VARIABLES.sticker.name().equals(current.getVariableName()))) {
						    continue;
						}

						
						List<ParameterDto> values = allParameterValues.get(current.getVariableHeaderType());
						ParameterDto parameterDto = null;
						if(values == null) {
							LoggerUtils.log.debug("This loop iteration will adding first variable to header type : "+current.getVariableHeaderType());
							values =  new ArrayList<>();
						}
						parameterDto = new ParameterDto();
						parameterDto.setType(current.getVariableType());
						parameterDto.setParameter_name(current.getVariableName());
						
						LoggerUtils.log.debug("Variable Type : "+current.getVariableType());
						LoggerUtils.log.debug("Variable Header Type : "+current.getVariableHeaderType());
						LoggerUtils.log.debug("Variable Name : "+current.getVariableName());
						MediaDto mediaDto = null;	
						TextDto text = null;
						
						if(current.getVariableName().equals(TEMPLATE_VARIABLES.audio.name())||current.getVariableName().equals(TEMPLATE_VARIABLES.video.name())||current.getVariableName().equals(TEMPLATE_VARIABLES.document.name())||current.getVariableName().equals(TEMPLATE_VARIABLES.sticker.name())||current.getVariableName().equals(TEMPLATE_VARIABLES.image.name()))
						{
							if(current.getVariableHeaderType().equals(COMPONENT_TYPE.header.name()))
							{
								LoggerUtils.log.debug("Setting media ...");
								mediaDto = new MediaDto();
								if(current.getMediaID() != null)
									mediaDto.setId(current.getMediaID());
								if(current.getMediaUrl() != null)
									mediaDto.setLink(current.getMediaUrl());
								if(current.getFileName() != null)
									mediaDto.setFileName(current.getFileName());
								if(current.getCaption() != null)
									mediaDto.setCaption(current.getCaption());
							}
							else {
								throw new Exception("Media content only allowed in header of template. Kindly do not use them in body or button");
							}
						}
						else if(current.getVariableName().equals(TEMPLATE_VARIABLES.emp_name.name()))
						{
							if(employee != null)
							{
								LoggerUtils.log.debug("Setting employee name ...");
								text = new TextDto();
								text.setBody(employee.getFirstName()+" "+employee.getLastName());
							}
							else {
								throw new Exception("Employee we are trying to send Template \""+template.getTemplateName()+"\" message does not exist in system");
							}
						}
						else if(current.getVariableName().equals(TEMPLATE_VARIABLES.emp_email.name()))
						{
							if(employee != null)
							{
								LoggerUtils.log.debug("Setting employee email ...");
								text = new TextDto();
								text.setBody(employee.getEmail());
							}
							else {
								throw new Exception("Employee we are trying to send Template \""+template.getTemplateName()+"\" message does not exist in system");
							}
						}
						else if(current.getVariableName().equals(TEMPLATE_VARIABLES.emp_phone.name()))
						{
							if(employee != null)
							{
								LoggerUtils.log.debug("Setting employee phone ...");
								text = new TextDto();
								text.setBody(employee.getPhonenumber());
							}
							else {
								throw new Exception("Employee we are trying to send Template \""+template.getTemplateName()+"\" message does not exist in system");
							}
						}
						else if(current.getVariableName().equals(TEMPLATE_VARIABLES.cust_name.name()))
						{
							if(customer != null)
							{
								LoggerUtils.log.debug("Setting customer name ...");
								text = new TextDto();
								String fn = customer.getFirstname();
								String ln = customer.getLastname();

								boolean badFn = (fn == null) || fn.trim().isEmpty()
								        || fn.trim().equalsIgnoreCase("N/A")
								        || fn.trim().equalsIgnoreCase("NA")
								        || fn.trim().equalsIgnoreCase("Not Applicable")
								        || fn.trim().equalsIgnoreCase("Not applicable");

								String body;
								if (badFn) {
								    body = customer.getPhoneNumber(); // fallback as per your requirement
								} else {
								    String full = (fn + " " + (ln == null ? "" : ln)).trim();
								    body = full.isEmpty() ? customer.getPhoneNumber() : full;
								}
								
								text.setBody(cleanCustomerName(body));

							}
							else {
								throw new Exception("Customer we are trying to send Template \""+template.getTemplateName()+"\" message does not exist in system");
							}
						}
						else if(current.getVariableName().equals(TEMPLATE_VARIABLES.cust_email.name()))
						{
							if(customer != null)
							{
								LoggerUtils.log.debug("Setting customer email ...");
								text = new TextDto();
								text.setBody(customer.getEmail());
							}
							else {
								throw new Exception("Customer we are trying to send Template \""+template.getTemplateName()+"\" message does not exist in system");
							}
						}
						else if(current.getVariableName().equals(TEMPLATE_VARIABLES.cust_phone.name()))
						{
							if(customer != null)
							{
								LoggerUtils.log.debug("Setting customer phone ...");
								text = new TextDto();
								text.setBody(customer.getPhoneNumber());
							}
							else {
								throw new Exception("Customer we are trying to send Template \""+template.getTemplateName()+"\" message does not exist in system");
							}
						}
						else if(current.getVariableName().equals(TEMPLATE_VARIABLES.cust_city.name()))
						{
							if(customer != null)
							{
								LoggerUtils.log.debug("Setting customer city ...");
								text = new TextDto();
								text.setBody(customer.getCity());
							}
							else {
								throw new Exception("Customer we are trying to send Template \""+template.getTemplateName()+"\" message does not exist in system");
							}
						}
						else if(current.getVariableName().equals(TEMPLATE_VARIABLES.cust_country.name()))
						{
							if(customer != null)
							{
								LoggerUtils.log.debug("Setting customer country ...");
								text = new TextDto();
								text.setBody(customer.getCountry());
							}
							else {
								throw new Exception("Customer we are trying to send Template \""+template.getTemplateName()+"\" message does not exist in system");
							}
						}
						else if(current.getVariableName().equals(TEMPLATE_VARIABLES.product_name.name()))
						{
							if(product != null)
							{
								LoggerUtils.log.debug("Setting product name ...");
								text = new TextDto();
								text.setBody(product.getName());
							}
							else {
								throw new Exception("Product for which we are trying to send Template \""+template.getTemplateName()+"\" message does not exist in system");
							}
						}
						else if(current.getVariableName().equals(TEMPLATE_VARIABLES.product_price.name()))
						{
							if(product != null)
							{
								LoggerUtils.log.debug("Setting product price ...");
								text = new TextDto();
								text.setBody(String.valueOf(product.getSellingPrice()));
							}
							else {
								throw new Exception("Product for which we are trying to send Template \""+template.getTemplateName()+"\" message does not exist in system");
							}
						}
						else if(current.getVariableName().equals(TEMPLATE_VARIABLES.purchase_name.name()))
						{
							if(purchase != null)
							{
								LoggerUtils.log.debug("Setting purchase name ...");
								text = new TextDto();
								text.setBody(purchase.getPurchaseName());
							}
							else {
								throw new Exception("Product for which we are trying to send Template \""+template.getTemplateName()+"\" message does not exist in system");
							}
						}
						else if(current.getVariableName().equals(TEMPLATE_VARIABLES.purchase_price.name()))
						{
							if(purchase != null)
							{
								LoggerUtils.log.debug("Setting purchase price ...");
								text = new TextDto();
								text.setBody(String.valueOf(purchase.getInvoice().getGrossValue()));
							}
							else {
								throw new Exception("Purchase for which we are trying to send Template \""+template.getTemplateName()+"\" message does not exist in system");
							}
						}
						else if(current.getVariableName().equals(TEMPLATE_VARIABLES.purchase_quantity.name()))
						{
							if(purchase != null)
							{
								LoggerUtils.log.debug("Setting purchase quantity ...");
								text = new TextDto();
								text.setBody(String.valueOf(purchase.getQuantity()));
							}
							else {
								throw new Exception("Purchase for which we are trying to send Template \""+template.getTemplateName()+"\" message does not exist in system");
							}
						}
						else if(current.getVariableName().equals(TEMPLATE_VARIABLES.purchase_date.name()))
						{
							if(purchase != null)
							{
								LoggerUtils.log.debug("Setting purchase date ...");
								text = new TextDto();
								text.setBody(String.valueOf(purchase.getPurchaseDate()));
							}
							else {
								throw new Exception("Purchase for which we are trying to send Template \""+template.getTemplateName()+"\" message does not exist in system");
							}
						}
						else if(current.getVariableName().equals(TEMPLATE_VARIABLES.today_date.name()))
						{
							LoggerUtils.log.debug("Setting today_date ...");
							text = new TextDto();
							text.setBody(String.valueOf(today_date));
						}
						else if(current.getVariableName().equals(TEMPLATE_VARIABLES.yesterday_date.name()))
						{
							LoggerUtils.log.debug("Setting yesterday_date ...");
							text = new TextDto();
							text.setBody(String.valueOf(yesterday_date));
						}
						else if(current.getVariableName().equals(TEMPLATE_VARIABLES.currency.name()))
						{
							LoggerUtils.log.debug("Setting currency ...");
							text = new TextDto();
							text.setBody(String.valueOf(currency));
						}
						else if(current.getVariableName().equals(TEMPLATE_VARIABLES.code.name()))
						{
							LoggerUtils.log.debug("Setting code ...");
							text = new TextDto();
							text.setBody(String.valueOf(code));
						}
						else if(current.getVariableName().equals(TEMPLATE_VARIABLES.text.name()))
						{
							LoggerUtils.log.debug("Setting text ...");
							text = new TextDto();
							text.setBody(String.valueOf(inputText));
						}
						else if(current.getVariableName().equals(TEMPLATE_VARIABLES.date.name()))
						{
							LoggerUtils.log.debug("Setting date ...");
							text = new TextDto();
							text.setBody(String.valueOf(date));
						}
						else if(current.getVariableName().equals(TEMPLATE_VARIABLES.organization.name()))
						{
							LoggerUtils.log.debug("Setting organization ...");
							text = new TextDto();
							text.setBody(String.valueOf(organization.getOrganization()));
						}
						else if(current.getVariableName().equals(TEMPLATE_VARIABLES.businessdesc.name()))
						{
							LoggerUtils.log.debug("Setting businessdesc ...");
							text = new TextDto();
							text.setBody(String.valueOf(organization.getNatureOfBusiness()));
						}
						else if(current.getVariableName().equals(TEMPLATE_VARIABLES.name.name()))
						{
							LoggerUtils.log.debug("Setting name ...");
							text = new TextDto();
							text.setBody(String.valueOf(name));
						}
						else if(current.getVariableName().equals(TEMPLATE_VARIABLES.email.name()))
						{
							LoggerUtils.log.debug("Setting email ...");
							text = new TextDto();
							text.setBody(String.valueOf(email));
						}
						else if(current.getVariableName().equals(TEMPLATE_VARIABLES.amount.name()))
						{
							LoggerUtils.log.debug("Setting amount ...");
							text = new TextDto();
							text.setBody(String.valueOf(amount));
						}
						else if(current.getVariableName().equals(TEMPLATE_VARIABLES.parentorg.name()))
						{
							LoggerUtils.log.debug("Setting parentorg ...");
							text = new TextDto();
							text.setBody(String.valueOf(parentorg));
						}
						else if(current.getVariableName().equals(TEMPLATE_VARIABLES.reason.name()))
						{
							LoggerUtils.log.debug("Setting reason ...");
							text = new TextDto();
							text.setBody(String.valueOf(reason));
						}
						else if(current.getVariableName().equals(TEMPLATE_VARIABLES.retailer_id.name()))
						{
							LoggerUtils.log.debug("Setting retailer_id ...");
							parameterDto.setId(retailer_id);
						}
						else {
							throw new Exception("Template has variables not suported by system");
						}
						
						LoggerUtils.log.debug("Setting common values ...");
						//Setting up common values
						if (text != null) {
						    text.setPreview_url(false);
						    parameterDto.setText(text);
						}
						if (mediaDto != null) {
						    parameterDto.setMedia(mediaDto);
						}

						parameterDto.setIndex(current.getOrderNumber());
						values.add(parameterDto);
						allParameterValues.put(current.getVariableHeaderType(), values);
						
						LoggerUtils.log.debug("Total Variable in this header type "+current.getVariableHeaderType()+" is : "+values.size());
						
					}
					//End of loop one
					
					//Covert map into list
					 LoggerUtils.log.debug("Covert map into list.");
					 for (Map.Entry<String,List<ParameterDto>> entry : allParameterValues.entrySet()) {
			                String key = entry.getKey();
			                List<ParameterDto> value = entry.getValue();
			                LoggerUtils.log.debug("Key: " + key + ", Value: " + value);
			                
			                ComponentDto current = new ComponentDto();
			                current.setType(key);
			                current.setParameters(value);
			                current.setSub_type(whatsAppTemplateVariableRequest.getSub_type());
			                current.setIndex(whatsAppTemplateVariableRequest.getIndex());
			                
			                toReturn.add(current);
			            }
					 
					 LoggerUtils.log.debug("After coverting map into list.");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw e;
		}
		
		return toReturn;
	}
	
	private String cleanCustomerName(String value) {
	    if (value == null) return null;

	    // Trim first
	    String v = value.trim();

	    // Remove trailing hyphen(s) with or without spaces
	    // Examples fixed:
	    // "7226876755 -" -> "7226876755"
	    // "Anand -"      -> "Anand"
	    // "Anand - "     -> "Anand"
	    // "Anand--"      -> "Anand"
	    v = v.replaceAll("[\\s-]+$", "");

	    return v.trim();
	}

	private JSONObject buildHeaderMediaComponent(String mediaType, String mediaId) throws Exception {

	    if (mediaType == null || mediaType.trim().isEmpty()) {
	        throw new Exception("mediaType is null/blank for header media");
	    }
	    if (mediaId == null || mediaId.trim().isEmpty()) {
	        throw new Exception("mediaId is null/blank for header media");
	    }

	    String type = mediaType.trim().toLowerCase();

	    // WhatsApp header supports: image, video, document (and in some cases audio)
	    // Your system supports more, so we map generically.
	    JSONObject header = new JSONObject();
	    header.put(SEND_MESSAGE_KEYS.type.name(), COMPONENT_TYPE.header.name());

	    JSONArray params = new JSONArray();

	    JSONObject p = new JSONObject();
	    p.put(SEND_MESSAGE_KEYS.type.name(), type);

	    JSONObject mediaObj = new JSONObject();
	    mediaObj.put(SEND_MESSAGE_KEYS.id.name(), mediaId);

	    // Put under correct key: "image":{id}, "video":{id}, "document":{id}, ...
	    p.put(type, mediaObj);

	    params.put(p);
	    header.put(SEND_MESSAGE_KEYS.parameters.name(), params);

	    return header;
	}

}
