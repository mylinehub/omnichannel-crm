import { Time } from '@angular/common';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ConstantsService {
public static searchContext: string = "dashboard"; 
public static TitleOfSite: string = "Constants Service"; 
// public static readonly PORT: number = 8081;
// public static readonly PROTOCOL: string = 'http://';
// public static readonly SERVER_IP: string = 'localhost';
public static readonly PORT: number = ;
public static readonly PROTOCOL: string = 'https://';
public static readonly SERVER_IP: string = 'app.mylinehub.com';
public readonly API_BASE_ENDPOINT: string = ConstantsService.PROTOCOL+ConstantsService.SERVER_IP+':'+ConstantsService.PORT+'/'; 
// public readonly API_BASE_ENDPOINT: string = ' https://'+ConstantsService.SERVER_IP+':'+ConstantsService.PORT+'/'; 
//public readonly BASE_ENDPOINT: string = 'http://'+ConstantsService.SERVER_IP+':4200';

//Web Socket
public static webSocketEndPoint:any = ConstantsService.PROTOCOL+ConstantsService.SERVER_IP+':'+ConstantsService.PORT+'/chat';
public static event:any = "/event/";

//WhatsApp Embedded Signup
// Meta / WhatsApp Embedded Signup
public static readonly META_GRAPH_VERSION: string = 'v24.0';
public static readonly META_APP_ID: string = '';
public static readonly META_LOGIN_FOR_BUSINESS_CONFIG_ID: string = '';

// API endpoint (Spring Boot) - query params
public readonly API_WHATSAPP_EMBEDDED_SIGNUP_COMPLETE_ENDPOINT: string =
  '/api/v1/whatsappphonenumber/embedded-signup/complete?organization={{organization}}';


//usrRoles
public static admin: string = 'ADMIN'; 
public static manager: any = 'MANAGER'; 
public static employee: string = 'EMPLOYEE'; 

//FileStoreTypes
public static userStore: string = 'FILESTORE'; 
public static whatsAppStore: any = 'WHATSAPP'; 
public static whatsAppStoreSend: any = 'WHATSAPP_SEND'; 
public static whatsAppStoreReceive: any = 'WHATSAPP_RECEIVE'; 

//Variales to encrypt data which is send in API without JWT
public static SECRET_KEY = ""; // Must match Angular key
public static INIT_VECTOR = ""; // Must match Angular IV
public static  DELIMITER = "";
public static VERIFYTOKEN = "";
public static whatsAppSupportlink = "https://wa.me/+919625048379";

//usrDetails
public static token: string = ''; 
public static organizationData: any = null; 
public static user: any = ''; 
public static organization: string = ''; 
public static isAuthenticated: boolean = false; 
public static role: string = ''; 
public static extension: string = ''; 
public static decodedToken: string = '';
public static email: string = '';
public static password: string = ''; 
public static fetchTime: string = '' ;

//Internal
public static readonly DIRECT_REFRESH_PAGE_TIME_MS: number = 1.5;
public readonly DASHBOARD_ENDPOINT: string = 'pages/employee/profile';
public readonly LOGIN_ENDPOINT: string = 'auth/login'; 
public readonly REFRESH_TOKEN_ENDPOINT: string = 'api/v1/auth/refreshToken?oldToken={{oldToken}}'; 
public readonly GOOGLE_LOGIN_ENDPOINT: string = 'api/v1/auth/googleLogin?googleToken={{googleToken}}'; 
public readonly RESET_PASSWORD_ENDPOINT: string = 'auth/reset-password'; 
public static searchNow: boolean = true;

public readonly ABSENT_EMPLOYEE_ENDPOINT: string =  'pages/absenteeism/employees';
public readonly AMI_REGISTRIES_ENDPOINT: string  = 'pages/ami-connection/registries';
public readonly All_Call_DETAIL_ENDPOINT: string  = 'pages/call-detail/all-calls';
public readonly All_COST_DETAIL_ENDPOINT: string  = 'pages/calling-cost/all-costs';
public readonly All_CAMPAIGN_DETAIL_ENDPOINT: string  = 'pages/campaign/all-campaigns';
public readonly All_CONFERENCE_DETAIL_ENDPOINT: string  = 'pages/conference/all-conferences';
public readonly All_CUSTOMER_DETAIL_ENDPOINT: string  = 'pages/customer/all-customers';
public readonly All_DEPARTMENT_DETAIL_ENDPOINT: string  = 'pages/department/all-departments';
public readonly PROFILE_ENDPOINT: string = 'pages/employee/profile';
public readonly All_ERROR_DETAIL_ENDPOINT: string  = 'pages/error/all-errors';
public readonly All_IVR_DETAIL_ENDPOINT: string  = 'pages/ivr/all-ivrs';
public readonly All_LOG_DETAIL_ENDPOINT: string  = 'pages/log/all-logs';
public readonly All_PRODUCT_DETAIL_ENDPOINT: string  = 'pages/product/all-products';
public readonly All_PURCHASE_DETAIL_ENDPOINT: string  = 'pages/purchase/all-purchases';
public readonly All_QUEUE_DETAIL_ENDPOINT: string  = 'pages/queue/all-queues';
public readonly All_SEARCH_DETAIL_ENDPOINT: string  = 'pages/search/search-result';
public readonly All_SIP_PROVIDER_DETAIL_ENDPOINT: string  = 'pages/sip-provider/registries';
public readonly All_SSH_PROVIDER_DETAIL_ENDPOINT: string  = 'pages/ssh-connection/registries';
public readonly All_SUPPLIER_DETAIL_ENDPOINT: string  = 'pages/supplier/all-suppliers';

//To be added
public readonly All_WHATS_APP_DASHBOARD_ENDPOINT: string  = '';

public readonly en :any = 
{
  create_group : "Create Group",
add_someone : "Add Someone",
find_someone : "Find someone...",
refresh_registration : "Refresh Registration",
configure_extension : "Configure Extension",
auto_answer : "Auto Answer",
do_no_disturb : "Do Not Disturb",
call_waiting : "Call Waiting",
record_all_calls : "Record All Calls",
extension_number : "Extension Number",
email : "Email",
mobile : "Mobile",
alternative_contact : "Alternate Contact",
full_name : "Full Name",
eg_full_name : "eg: Keyla James",
title_description : "Title / Description",
eg_general_manager : "eg: General Manager",
internal_subscribe_extension : "Subscribe Extension (Internal)",
eg_internal_subscribe_extension : "eg: 100 or john",
mobile_number : "Mobile Number",
eg_mobile_number : "eg: +44 123-456 7890",
eg_email : "eg: Keyla.James@innovateasterisk.com",
contact_number_1 : "Contact Number 1",
eg_contact_number_1 : "eg: +1 234 567 8901",
contact_number_2 : "Contact Number 2",
eg_contact_number_2 : "eg: +441234567890",
add : "Add",
cancel : "Cancel",
save : "Save",
reload_required : "Reload Required",
alert_settings : "In order to apply these settings, the page must reload, OK?",
account : "Account",
audio_video : "Audio & Video",
appearance : "Appearance",
notifications : "Notifications",
asterisk_server_address : "Secure WebSocket Server (TLS)",
eg_asterisk_server_address : "eg: ws.innovateasterisk.com",
websocket_port : "WebSocket Port",
eg_websocket_port : "eg: 4443",
websocket_path : "WebSocket Path",
eg_websocket_path : "/ws",
sip_domain: "Domain",
eg_sip_domain : "eg: innovateasterisk.com",
sip_username : "SIP Username",
eg_sip_username : "eg: webrtc",
sip_password : "SIP Password",
eg_sip_password : "eg: 1234",
speaker : "Speaker",
microphone : "Microphone",
camera : "Camera",
frame_rate : "Frame Rate (per second)",
quality : "Quality",
image_orientation : "Image Orientation",
image_orientation_normal : "Normal",
image_orientation_mirror : "Mirror",
aspect_ratio : "Aspect Ratio",
preview : "Preview",
ringtone : "Ringtone",
ring_device : "Ring Device",
auto_gain_control : "Auto Gain Control",
echo_cancellation : "Echo Cancellation",
noise_suppression : "Noise Suppression",
enable_onscreen_notifications : "Enabled Onscreen Notifications",
alert_notification_permission : "You need to accept the permission request to allow Notifications",
permission : "Permission",
error : "Error",
alert_media_devices : "MediaDevices was null -  Check if your connection is secure (HTTPS)",
alert_error_user_media : "Error getting User Media.",
alert_file_size : "The file is bigger than 50MB, you cannot upload this file",
alert_single_file : "Select a single file",
alert_not_found : "This item was not found",
edit : "Edit",
welcome : "Welcome",
accept : "Accept",
registered : "Browser Phone Registered",
registration_failed : "Registration Failed",
unregistered : "Unregistered, Bye!",
connected_to_web_socket : "Connected to Web Socket!",
disconnected_from_web_socket : "Disconnected from Web Socket!",
web_socket_error : "Web Socket Error",
connecting_to_web_socket : "Connecting to Web Socket...",
error_connecting_web_socket : "Error connecting to the server on the WebSocket port",
sending_registration : "Sending Registration...",
unsubscribing : "Unsubscribing...",
disconnecting : "Disconnecting...",
incoming_call : "Incoming Call",
incoming_call_from : "Incoming call from:",
answer_call : "Answer Call",
answer_call_with_video : "Answer Call with Video",
reject_call : "Reject Call",
call_failed : "Call Failed",
alert_no_microphone : "Sorry, you don't have any Microphone connected to this computer. You cannot receive calls.",
call_in_progress : "Call in Progress!",
call_rejected : "Call Rejected",
trying : "Trying...",
ringing : "Ringing...",
call_cancelled : "Call Cancelled",
call_ended : "Call ended, bye!",
yes : "Yes",
no : "No",
receive_kilobits_per_second : "Receive Kilobits per second",
receive_packets_per_second : "Receive Packets per second",
receive_packet_loss : "Receive Packet Loss",
receive_jitter : "Receive Jitter",
receive_audio_levels : "Receive Audio Levels",
send_kilobits_per_second : "Send Kilobits Per Second",
send_packets_per_second : "Send Packets Per Second",
state_not_online : "Not online",
state_ready : "Ready",
state_on_the_phone : "On the phone",
state_ringing : "Ringing",
state_on_hold : "On hold",
state_unavailable : "Unavailable",
state_unknown : "Unknown",
alert_empty_text_message : "Please enter something into the text box provided and click send",
no_message : "No Message",
message_from : "Message from",
starting_video_call : "Starting Video Call...",
call_extension : "Call Extension",
call_mobile : "Call Mobile",
call_number : "Call Number",
call_group : "Call Group",
starting_audio_call : "Starting Audio Call...",
call_recording_started : "Call Recording Started",
call_recording_stopped : "Call Recording Stopped",
confirm_stop_recording : "Are you sure you want to stop recording this call?",
stop_recording : "Stop Recording?",
width : "Width",
height : "Height",
extension : "Extension",
call_blind_transfered : "Call Blind Transferred",
connecting : "Connecting...",
attended_transfer_call_started : "Attended Transfer Call Started...",
attended_transfer_call_cancelled : "Attended Transfer Call Cancelled",
attended_transfer_complete_accepted : "Attended Transfer Complete (Accepted)",
attended_transfer_complete : "Attended Transfer complete",
attended_transfer_call_ended : "Attended Transfer Call Ended",
attended_transfer_call_rejected : "Attended Transfer Call Rejected",
attended_transfer_call_terminated : "Attended Transfer Call Terminated",
conference_call_started : "Conference Call Started...",
conference_call_cancelled : "Conference Call Cancelled",
conference_call_in_progress : "Conference Call In Progress",
conference_call_ended : "Conference Call Ended",
conference_call_rejected : "Conference Call Rejected",
conference_call_terminated : "Conference Call Terminated",
null_session : "Session Error, Null",
call_on_hold : "Call on Hold",
send_dtmf : "Sent DTMF",
switching_video_source : "Switching video source",
switching_to_canvas : "Switching to canvas",
switching_to_shared_video : "Switching to Shared Video",
switching_to_shared_screen : "Switching to Shared Screen",
video_disabled : "Video Disabled",
line : "Line",
back : "Back",
audio_call : "Audio Call",
video_call : "Video Call",
find_something : "Find Something",
remove : "Remove",
present : "Present",
scratchpad : "Scratchpad",
screen : "Screen",
video : "Video",
blank : "Blank",
show_key_pad : "Show Key Pad",
mute : "Mute",
unmute : "Unmute",
start_call_recording : "Start Call Recording",
stop_call_recording : "Stop Call Recording",
transfer_call : "Transfer Call",
cancel_transfer : "Cancel Transfer",
conference_call : "Conference Call",
cancel_conference : "Cancel Conference",
hold_call : "Hold Call",
resume_call : "Resume Call",
end_call : "End Call",
search_or_enter_number : "Search or enter number",
blind_transfer : "Blind Transfer",
attended_transfer : "Attended Transfer",
complete_transfer : "Complete Transfer",
end_transfer_call : "End Transfer Call",
call : "Call",
cancel_call : "Cancel Call",
join_conference_call : "Join Conference Call",
end_conference_call : "End Conference Call",
microphone_levels : "Microphone Levels",
speaker_levels : "Speaker Levels",
send_statistics : "Send Statistics",
receive_statistics : "Receive Statistics",
find_something_in_the_message_stream : "Find something in the message stream...",
type_your_message_here : "Type your message here...",
menu : "Menu",
confirm_remove_buddy : "This buddy will be removed from your list. Confirm remove?",
remove_buddy : "Remove Buddy",
read_more : "Read More",
started : "Started",
stopped : "Stopped",
recording_duration : "Recording Duration",
a_video_call : "a video call",
an_audio_call : "an audio call",
you_tried_to_make : "You tried to make",
you_made : "You made",
and_spoke_for : "and spoke for",
you_missed_a_call : "You missed a call",
you_received : "You received",
second_single : "second",
seconds_plural : "seconds",
minute_single : "minute",
minutes_plural : "minutes",
hour_single : "hour",
hours_plural : "hours",
bytes : "Bytes",
kb : "KB",
mb : "MB",
gb : "GB",
tb : "TB",
pb : "PB",
eb : "EB",
zb : "ZB",
yb : "YB",
call_on_mute : "Call on Mute",
call_off_mute : "Call off Mute",
tag_call : "Tag Call",
clear_flag : "Clear Flag",
flag_call : "Flag Call",
edit_comment : "Edit Comment",
copy_message : "Copy Message",
quote_message : "Quote Message",
select_expression : "Select Expression",
dictate_message : "Dictate Message",
alert_speech_recognition : "Your browser does not support this function, sorry",
speech_recognition : "Speech Recognition",
im_listening : "I'm listening...",
msg_silence_detection: "You were quiet for a while so voice recognition turned itself off.",
msg_no_speech: "No speech was detected. Try again.",
loading: "Loading...",
select_video: "Select Video",
ok: "OK",
device_settings : "Device Settings",
call_stats : "Call Stats",
you_received_a_call_from : "You received a call from",
you_made_a_call_to : "You made a call to",
you_answered_after : "You answered after",
they_answered_after : "They answered after",
with_video : "with video",
you_started_a_blind_transfer_to : "You started a blind transfer to",
you_started_an_attended_transfer_to : "You started an attended transfer to",
the_call_was_completed : "The call was completed.",
the_call_was_not_completed : "The call was not completed.",
you_put_the_call_on_mute : "You put the call on mute.",
you_took_the_call_off_mute : "You took the call off mute.",
you_put_the_call_on_hold : "You put the call on hold.",
you_took_the_call_off_hold : "You took the call off hold.",
you_ended_the_call : "You ended the call.",
they_ended_the_call : "They ended the call.",
call_is_being_recorded : "Call is being recorded.",
now_stopped : "Now Stopped",
you_started_a_conference_call_to : "You started a conference call to",
show_call_detail_record : "Show Call Detail Record",
call_detail_record : "Call Detail Record",
call_direction : "Call Direction",
call_date_and_time : "Call Date & Time",
ring_time : "Ring Time",
talk_time : "Talk Time",
call_duration : "Call Duration",
flagged : "Flagged",
call_tags : "Call Tags",
call_notes : "Call Notes",
activity_timeline : "Activity Timeline",
call_recordings : "Call Recordings",
save_as : "Save As",
right_click_and_select_save_link_as : "Right click and select Save Link As",
send : "Send",
set_status : "Set Status",
default_status : "(No Status)",
is_typing : "is typing",
chat_engine : "Chat Engine",
xmpp_server_address : "Secure XMPP Server (TLS)",
eg_xmpp_server_address : "eg: xmpp.innovateasterisk.com",
allow_calls_on_dnd : "Allow calls during Do Not Disturb",
basic_extension : "Basic Extension",
extension_including_xmpp : "Extension including Message Exchange",
addressbook_contact : "Address Book Contact",
subscribe_to_dev_state : "Subscribe to Device State Notifications",
default_video_src : "Default",
subscribe_voicemail : "Subscribe to VoiceMail (MWI)",
voicemail_did : "VoiceMail Management Number",
filter_and_sort : "Filter and Sort",
sort_type : "Type (then Last Activity)",
sort_type_cex : "Contacts, SIP then XMPP",
sort_type_cxe : "Contacts, XMPP then SIP",
sort_type_xec : "XMPP, SIP then Contacts",
sort_type_xce : "XMPP, Contacts then SIP",
sort_type_exc : "SIP, XMPP then Contacts",
sort_type_ecx : "SIP, Contacts then XMPP",
sort_exten : "Extension or Number (then Last Activity)",
sort_alpha : "Alphabetically (then Last Activity)",
sort_activity : "Only Last Activity",
sort_auto_delete_at_end : "Show Auto Delete at the end",
sort_auto_delete_hide : "Hide Auto Delete Buddies",
sort_show_exten_num : "Show Extension Numbers",
sort_no_showing : "Not showing {0} Auto Delete buddies",
delete_buddy : "Delete",
delete_duddy_data : "Delete History",
pin_to_top : "Pinned",
voice_mail : "VoiceMail",
you_have_new_voice_mail : "You have {0} new VoiceMail messages.",
new_voice_mail : "New VoiceMail Message"
};

public readonly searchPosts : any [] = [
  {
    "title": "Employee Absemteeism",
    "link": this.ABSENT_EMPLOYEE_ENDPOINT,
    "text": "Looking out to find who all were absent today or few days back. Check out our ansenteeism module for this purpose. In case this does not resolve this issues get in touch with our team at mylinehub.com"
  },
  {
    "title": "AMI Connections",
    "link": this.AMI_REGISTRIES_ENDPOINT,
    "text": "Looking out to find how we connect with your asterisk server. It is done via AMI Connections. Check out asterisk manual to know more about this. In case this does not resolve this issues get in touch with our team at mylinehub.com"
  },
  {
    "title": "Call Details",
    "link": this.All_Call_DETAIL_ENDPOINT,
    "text": "Looking out to find all call details across your organization. Check out our call details module for this purpose. In case this does not resolve this issues get in touch with our team at mylinehub.com"
  },
  {
    "title": "Calling Costs",
    "link": this.All_COST_DETAIL_ENDPOINT,
    "text": "Looking out to find all calling costs across your organization. Check out our calling cost module for this purpose. In case this does not resolve this issues get in touch with our team at mylinehub.com"
  },
  {
    "title": "Campaigns",
    "link": this.All_CAMPAIGN_DETAIL_ENDPOINT,
    "text": "Looking out to setup autodialer. It can be done by setting up campaigns. Check out our campaign module for this purpose. In case this does not resolve this issues get in touch with our team at mylinehub.com"
  },
  {
    "title": "Conferences",
    "link": this.All_CONFERENCE_DETAIL_ENDPOINT,
    "text": "For management we can setup personal conferences. This extension is static and may be protected by passwords. Check out our conference module for this purpose. In case this does not resolve this issues get in touch with our team at mylinehub.com"
  },
  {
    "title": "Customers",
    "link": this.All_CUSTOMER_DETAIL_ENDPOINT,
    "text": "Find details & reports for your customers. Verify if they got converted or diverted. Check what type of business they own. Check out our customer module for this purpose. In case this does not resolve this issues get in touch with our team at mylinehub.com"
  },
  {
    "title": "Departments",
    "link": this.All_DEPARTMENT_DETAIL_ENDPOINT,
    "text": "Maintain different departmentss your organization own. Check out our department module for this purpose. In case this does not resolve this issues get in touch with our team at mylinehub.com"
  },
  {
    "title": "Employee Profile",
    "link": this.PROFILE_ENDPOINT,
    "text": "Check out profile for your account. This is to update self information. Although a manager can update details of employee but this has to be done at different link. Check out our employee module for this purpose. In case this does not resolve this issues get in touch with our team at mylinehub.com"
  },
  {
    "title": "Errors",
    "link": this.All_ERROR_DETAIL_ENDPOINT,
    "text": "Check out errors within application. Check out our error module for this purpose. In case this does not resolve this issues get in touch with our team at mylinehub.com"
  },
  {
    "title": "IVR",
    "link": this.All_IVR_DETAIL_ENDPOINT,
    "text": "Interactive voice response. Automate them with mylinehub. It can be done using campaign module. But what IVR to execute, this information will need to be here. Check out our IVR module for this purpose. In case this does not resolve this issues get in touch with our team at mylinehub.com"
  },
  {
    "title": "Logs",
    "link": this.All_LOG_DETAIL_ENDPOINT,
    "text": "Check out logs within application. Check out our log module for this purpose. In case this does not resolve this issues get in touch with our team at mylinehub.com"
  },
  {
    "title": "Products",
    "link": this.All_PRODUCT_DETAIL_ENDPOINT,
    "text": "Maintain your products within mylinehub tool. Check out our product module for this purpose. In case this does not resolve this issues get in touch with our team at mylinehub.com"
  },
  {
    "title": "Purchases",
    "link": this.All_PURCHASE_DETAIL_ENDPOINT,
    "text": "Maintain your purchases within mylinehub tool. Check out our purchase module for this purpose. In case this does not resolve this issues get in touch with our team at mylinehub.com"
  },
  {
    "title": "Queues",
    "link": this.All_QUEUE_DETAIL_ENDPOINT,
    "text": "'Ringall', 'Round Robin'. We own capability to produce queues with different strategies or custom requirements for your team. Automate calling them with mylinehub. It can be done using campaign module. But what Queue to execute, this information will need to be here. Check out our IVR module for this purpose. In case this does not resolve this issues get in touch with our team at mylinehub.com"
  },
  {
    "title": "SIP Providers",
    "link": this.All_SIP_PROVIDER_DETAIL_ENDPOINT,
    "text": "Looking out to find phone numbers associated to your employees. Check out our sip provider module for this purpose. In case this does not resolve this issues get in touch with our team at mylinehub.com"
  },
  {
    "title": "SSH Connections",
    "link": this.All_SSH_PROVIDER_DETAIL_ENDPOINT,
    "text": "This is how we extract employee call recordings from asterisk server. Check out our ssh connection module for this purpose. In case this does not resolve this issues get in touch with our team at mylinehub.com"
  },
  {
    "title": "Suppliers",
    "link": this.All_SUPPLIER_DETAIL_ENDPOINT,
    "text": "Maintain your suppliers within mylinehub tool. Check out our supplier module for this purpose. In case this does not resolve this issues get in touch with our team at mylinehub.com"
  },
];


//Allow document formats Whats App. Same variables list is in backend.
public static text ="text";
public static audio ="audio";
public static video ="video";
public static sticker ="sticker";
public static image ="image";
public static document ="document";
public static url = "url";
public static reaction = "reaction";
public static contacts = "contacts";
public static location = "location";
public static interactive = "interactive";
public static whatsAppText = [ "text" ,"message"];
public static whatsAppAudio = [ "aac","mp4","mpeg","amr","ogg" ];
public static whatsAppVideo = [ "mp4","3sp" ];
public static whatsAppDocument = [ "plain","pdf","vnd.ms-powerpoint","msword","vnd.ms-excel","vnd.openxmlformats-officedocument.wordprocessingml.document","vnd.openxmlformats-officedocument.presentationml.presentation","vnd.openxmlformats-officedocument.spreadsheetml.sheet" ];
public static whatsAppSticker = [ "webp" ];
public static whatsAppImage = [ "png","jpeg" ];
//Below whats app variablels are kept in this file where as are verified and checked only at backend. They are kept here just for reference so as front end developer can get information without connecting backend.
public static whatsAppAudioCodec = [ "AAC" ];
public static whatsAppVideoCodec = [ "H.264" ];
public static whatsAppAudioMaxSizeBytes = 16777210;
public static whatsAppVideoMaxSizeBytes = 16777210;
public static whatsAppDocumentMaxSizeBytes = 104857600;
public static whatsAppStickerMaxSizeBytes = 102400;
public static whatsAppImageMaxSizeBytes = 5242880;


//Login & Reset Password
public readonly API_LOGIN_ENDPOINT: string = 'login'; 
public readonly API_SELF_RESET_PASSWORD_ENDPOINT: string = 'api/v1/employees/updateSelfWebPassword'; 


//******************** */
//** Organization Registration */

//Application Registration API's
//Post
public readonly API_REGISTER_BUSINESS_USING_GSTIN: string = 'api/v1/organization-app/verifyBusinessIdentificationAndCreate';


//**********************
//** Organizational Modules **
//**********************
//****Organization Data *******
//GET
public readonly API_ORG_DATA_ENDPOINT: string = 'api/v1/organization/getOrganizationalData?organization={{organization}}';


//**********************
//** WhatsApp Modules **
//**********************

//Customer Entity API but specific to Whats App
//***Whats App Customer Search API******/
// public readonly API_GETALL_CUSTOMER_BY_WHATSAPP_PHONE_ID_ENDPOINT: string = 'api/v1/customers/getCustomerByWhatsAppPhoneNumberId?whatsAppPhoneNumberId={{whatsAppPhoneNumberId}}&organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_GETALL_CUSTOMER_BY_WHATSAPP_PROJECT_ID_ENDPOINT: string = 'api/v1/customers/findAllBywhatsAppProjectId?organization={{organization}}&whatsAppProjectId={{whatsAppProjectId}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_GETALL_CUSTOMER_BY_WHATSAPP_REGISTERED_BY_ID_ENDPOINT: string = 'api/v1/customers/findAllByWhatsAppRegisteredByPhoneNumber?whatsAppRegisteredByPhoneNumber={{whatsAppRegisteredByPhoneNumber}}&organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';

//***Whats App Enum / Dictionaries******/
//GET
public readonly API_WHATSAPP_CURRENCY_CODE_ENDPOINT: string = 'api/v1/whatsappdict/getAllWhatsAppCurrencyCodes?organization={{organization}}';
public readonly API_WHATSAPP_CONVERSATION_TYPE_ENDPOINT: string = 'api/v1/whatsappdict/getAllWhatsAppConversationTypes?organization={{organization}}';
public readonly API_WHATSAPP_ADDRESS_TYPE_ENDPOINT: string = 'api/v1/whatsappdict/getAllWhatsAppAddressTypes?organization={{organization}}';
public readonly API_WHATSAPP_CALENDER_ENDPOINT: string = 'api/v1/whatsappdict/getAllWhatsAppCalender?organization={{organization}}';
public readonly API_WHATSAPP_COMPONENT_SUB_TYPE_ENDPOINT: string = 'api/v1/whatsappdict/getAllWhatsAppComponentSubType?organization={{organization}}';
public readonly API_WHATSAPP_COMPONENT_TYPE_ENDPOINT: string = 'api/v1/whatsappdict/getAllWhatsAppComponentType?organization={{organization}}';
public readonly API_WHATSAPP_DAY_OF_WEEK_ENDPOINT: string = 'api/v1/whatsappdict/getAllWhatsAppDayOfWeek?organization={{organization}}';
public readonly API_WHATSAPP_EMAIL_TYPE_ENDPOINT: string = 'api/v1/whatsappdict/getAllWhatsAppEmailType?organization={{organization}}';
public readonly API_WHATSAPP_LANGUAGE_CODE_ENDPOINT: string = 'api/v1/whatsappdict/getAllWhatsAppLanguageCode?organization={{organization}}';
public readonly API_WHATSAPP_LANGUAGE_POLICY_ENDPOINT: string = 'api/v1/whatsappdict/getAllWhatsAppLanguagePolicy?organization={{organization}}';
public readonly API_WHATSAPP_MEDIA_SELECTION_ENDPOINT: string = 'api/v1/whatsappdict/getAllWhatsAppMediaSelectedCriteria?organization={{organization}}';
public readonly API_WHATSAPP_MESSAGE_TYPE_ENDPOINT: string = 'api/v1/whatsappdict/getAllWhatsAppMessageType?organization={{organization}}';
public readonly API_WHATSAPP_MESSAGE_PRODUCT_ENDPOINT: string = 'api/v1/whatsappdict/getAllWhatsAppMessageProduct?organization={{organization}}';
public readonly API_WHATSAPP_PHONE_NUMBER_TYPE_ENDPOINT: string = 'api/v1/whatsappdict/getAllWhatsAppPhoneNumberType?organization={{organization}}';
public readonly API_WHATSAPP_RECEIPT_TYPE_ENDPOINT: string = 'api/v1/whatsappdict/getAllWhatsAppRecepientType?organization={{organization}}';
public readonly API_WHATSAPP_SEND_MESSAGE_KEYS_ENDPOINT: string = 'api/v1/whatsappdict/getAllWhatsAppSendMessageKeys?organization={{organization}}';
public readonly API_WHATSAPP_TEMPLATE_VARIABLES_ENDPOINT: string = 'api/v1/whatsappdict/getAllWhatsAppTemplateVariables?organization={{organization}}';
public readonly API_WHATSAPP_TEMPLATE_VARIABLES_TYPE_ENDPOINT: string = 'api/v1/whatsappdict/getAllWhatsAppTemplateVariablesType?organization={{organization}}';
public readonly API_WHATSAPP_URL_TYPE_ENDPOINT: string = 'api/v1/whatsappdict/getAllWhatsAppUrlType?organization={{organization}}';
public readonly API_WHATSAPP_AD_SOURCE_ENDPOINT: string = 'api/v1/whatsappdict/getAllWhatsAppAdSource?organization={{organization}}';
public readonly API_WHATSAPP_CHANGE_FIELD_TYPE_ENDPOINT: string = 'api/v1/whatsappdict/getAllWhatsAppChangeFieldType?organization={{organization}}';
public readonly API_WHATSAPP_CONVERSATION_CATEGORY_TYPE_ENDPOINT: string = 'api/v1/whatsappdict/getAllWhatsAppConversationCategory?organization={{organization}}';
public readonly API_WHATSAPP_MESSAGE_STATUS_TYPE_ENDPOINT: string = 'api/v1/whatsappdict/getAllWhatsAppMessageStatusType?organization={{organization}}';
public readonly API_WHATSAPP_PAYMENT_TYPE_ENDPOINT: string = 'api/v1/whatsappdict/getAllWhatsAppPaymentStatus?organization={{organization}}';
public readonly API_WHATSAPP_PRICING_MODEL_ENDPOINT: string = 'api/v1/whatsappdict/getAllWhatsAppPricingModel?organization={{organization}}';
public readonly API_WHATSAPP_INTERACTIVE_TYPE_ENDPOINT: string = 'api/v1/whatsappdict/getAllWhatsAppTypeOfInteractive?organization={{organization}}';
public readonly API_WHATSAPP_BLOCK_USER_PARAMETER_ENDPOINT: string = 'api/v1/whatsappdict/getAllWhatsAppBlockUserParameter?organization={{organization}}';

//***Whats App Chat History******/
//POST
public readonly API_GET_All_CHAT_HISTORY_FOR_PHONE_MAIN_ENDPOINT: string = 'api/v1/whatsapp-chat-history/getAllChatHistoryForPhoneNumberMain';
public readonly API_GET_All_CHAT_HISTORY_FOR_TWO_PHONE_ENDPOINT: string = 'api/v1/whatsapp-chat-history/getAllChatHistoryByTwoPhoneNumbersAndOrganization';
public readonly API_DELETE_All_CHAT_HISTORY_FOR_PHONE_MAIN_ENDPOINT: string = 'api/v1/whatsapp-chat-history/deleteAllChatHistoryByPhoneNumberMainAndOrganization';
public readonly API_DELETE_All_CHAT_HISTORY_FOR_TWO_PHONE_ENDPOINT: string = 'api/v1/whatsapp-chat-history/deleteAllChatHistoryByTwoPhoneNumbersAndOrganization';
public readonly API_UPDATE_LAST_READ_INDEX_FOR_TWO_PHONE_ENDPOINT: string = 'api/v1/whatsapp-chat-history/updateLastReadIndexByTwoPhoneNumbersAndOrganization';
public readonly API_DOWNLOAD_CHAT_HISTORY_ENDPOINT: string = 'api/v1/whatsapp-chat-history/exportChatHistoryExcelDbOnly';


//****Whats App Report *******
//GET
public readonly API_WHATSAPP_ALL_REPORT_ENDPOINT: string = 'api/v1/whatsappphonereport/getAllByOrganization?organization={{organization}}';
//POST
public readonly API_WHATSAPP_All_BY_ONLY_PHONE_REPORT_ENDPOINT: string = 'api/v1/whatsappphonereport/findAllByWhatsAppPhoneNumberAndOrganization';
public readonly API_WHATSAPP_All_BY_DAY_GREATOR_REPORT_ENDPOINT: string = 'api/v1/whatsappphonereport/findAllByDayUpdatedGreaterThanEqualAndWhatsAppPhoneNumberAndOrganization';
public readonly API_WHATSAPP_BY_DAY_GREATOR_AND_TYPE_REPORT_ENDPOINT: string = 'api/v1/whatsappphonereport/findAllByDayUpdatedGreaterThanEqualAndWhatsAppPhoneNumberAndTypeOfReportAndOrganization';

public readonly API_WHATSAPP_DASHBOARD_COUNT_ENDPOINT: string = 'api/v1/whatsappphonereport/getReportCountForDashboard';
public readonly API_WHATSAPP_DASHBOARD_COUNT_BY_NUMBER_ENDPOINT: string = 'api/v1/whatsappphonereport/getReportCountForDashboardForNumber';
public readonly API_WHATSAPP_DASHBOARD_COUNT_BY_NUMBER_AND_TIME_ENDPOINT: string = 'api/v1/whatsappphonereport/getReportCountForDashboardForNumberByTime';


//****Whats App Project *******
//GET
public readonly API_WHATSAPP_GET_ALL_PROJECT_ENDPOINT: string = 'api/v1/whatsappproject/getAllByOrganization?organization={{organization}}';
//POST
public readonly API_WHATSAPP_CREATE_PROJECT_ENDPOINT: string = 'api/v1/whatsappproject/create';
public readonly API_WHATSAPP_UPDATE_PROJECT_ENDPOINT: string = 'api/v1/whatsappproject/update';
//DELETE
public readonly API_WHATSAPP_DELETE_PROJECT_ENDPOINT: string = 'api/v1/whatsappproject/delete?organization={{organization}}&id={{id}}';


//****Whats App Open AI Account *******
//GET
public readonly API_WHATSAPP_GET_ALL_OPENAPI_ENDPOINT: string = 'api/v1/whatsappopenaiaccount/getAllByOrganization?organization={{organization}}';
//POST
public readonly API_WHATSAPP_CREATE_OPENAPI_ENDPOINT: string = 'api/v1/whatsappopenaiaccount/create';
public readonly API_WHATSAPP_UPDATE_OPENAPI_ENDPOINT: string = 'api/v1/whatsappopenaiaccount/update';
//DELETE
public readonly API_WHATSAPP_DELETE_OPENAPI_ENDPOINT: string = 'api/v1/whatsappopenaiaccount/delete?organization={{organization}}&id={{id}}';

//****Whats App Phone Number *******
//GET
public readonly API_WHATSAPP_GET_ALL_PHONE_ENDPOINT: string = 'api/v1/whatsappphonenumber/getAllByOrganization?organization={{organization}}';
public readonly API_WHATSAPP_GET_ALL_BY_ADMIN_PHONE_ENDPOINT: string = 'api/v1/whatsappphonenumber/getAllByOrganizationAndAdmin?organization={{organization}}&adminEmployeeId={{adminEmployeeId}}';
public readonly API_WHATSAPP_GET_ALL_BY_EMPLOYEE_AND_ORG_PHONE_ENDPOINT: string = 'api/v1/whatsappphonenumber/findAllByEmployeeInExtensionAccessListOrAdmin?employeeExtension={{employeeExtension}}&organization={{organization}}';

//POST
public readonly API_WHATSAPP_UPDATE_ADMIN_FOR_PHONE_ENDPOINT: string = 'api/v1/whatsappphonenumber/updateAdminEmployeeForWhatsAppNumberByOrganization';
public readonly API_WHATSAPP_UPDATE_EMPLOYEE_ACCESS_REQUEST_PHONE_ENDPOINT: string = 'api/v1/whatsappphonenumber/updateEmployeeAccessListByOrganization';
public readonly API_WHATSAPP_CREATE_PHONE_ENDPOINT: string = 'api/v1/whatsappphonenumber/create';
public readonly API_WHATSAPP_UPDATE_PHONE_ENDPOINT: string = 'api/v1/whatsappphonenumber/update?oldPhone={{oldPhone}}';
public readonly API_WHATSAPP_GET_ALL_BY_PROJECT_PHONE_ENDPOINT: string = 'api/v1/whatsappphonenumber/getAllByOrganizationAndWhatsAppProject';
public readonly API_WHATSAPP_GET_ALL_BY_PROJECT__AND_ACTIVE_PHONE_ENDPOINT: string = 'api/v1/whatsappphonenumber/getAllByOrganizationAndWhatsAppProjectAndActive';
//DELETE
public readonly API_WHATSAPP_DELETE_PHONE_ENDPOINT: string = 'api/v1/whatsappphonenumber/delete?organization={{organization}}&id={{id}}';

//****Whats App Templates *******
//GET
public readonly API_WHATSAPP_GET_ALL_TEMPLATE_ENDPOINT: string = 'api/v1/whatsappphonenumbertemplate/getAllByOrganization?organization={{organization}}';
//POST
public readonly API_WHATSAPP_GET_ALL_BY_PHONE_TEMPLATE_ENDPOINT: string = 'api/v1/whatsappphonenumbertemplate/getAllByOrganizationAndWhatsAppPhoneNumber';
public readonly API_WHATSAPP_CREATE_TEMPLATE_ENDPOINT: string = 'api/v1/whatsappphonenumbertemplate/create';
public readonly API_WHATSAPP_UPDATE_TEMPLATE_ENDPOINT: string = 'api/v1/whatsappphonenumbertemplate/update';
//DELETE
public readonly API_WHATSAPP_DELETE_TEMPLATE_ENDPOINT: string = 'api/v1/whatsappphonenumbertemplate/delete?organization={{organization}}&id={{id}}';

//****Whats App Templates Variables*******
//GET
public readonly API_WHATSAPP_GET_ALL_TEMPLATE_VARIABLE_ENDPOINT: string = 'api/v1/whatsappnumbertemplatevariable/findAllByWhatsAppNumberTemplateAndOrganization?templateId={{templateId}}&organization={{organization}}';
//POST
public readonly API_WHATSAPP_UPDATE_TEMPLATE_VARIABLE_ENDPOINT: string = 'api/v1/whatsappnumbertemplatevariable/update?templateId={{templateId}}&organization={{organization}}';

//****Whats App Prompt *******
//GET
public readonly API_WHATSAPP_GET_ALL_PROMPT_ENDPOINT: string = 'api/v1/whatsappprompt/getAllByOrganization?organization={{organization}}';
//POST
public readonly API_WHATSAPP_GET_ALL__BY_PHONE_PROMPT_ENDPOINT: string = 'api/v1/whatsappprompt/getAllByOrganizationAndWhatsAppPhoneNumber';
public readonly API_WHATSAPP_GET_ALL__BY_PHONE_AND_ACTIVE_PROMPT_ENDPOINT: string = 'api/v1/whatsappprompt/getAllByOrganizationAndWhatsAppPhoneNumberAndActive';
public readonly API_WHATSAPP_GET_ALL_BY_CATEGORY_PROMPT_ENDPOINT: string = 'api/v1/whatsappprompt/getAllByOrganizationAndCategory';
public readonly API_WHATSAPP_GET_ALL_BY_CATEGORY_AND_ACTIVE_PROMPT_ENDPOINT: string = 'api/v1/whatsappprompt/getAllByOrganizationAndCategoryAndActive';
public readonly API_WHATSAPP_CREATE_PROMPT_ENDPOINT: string = 'api/v1/whatsappprompt/create';
public readonly API_WHATSAPP_UPDATE_PROMPT_ENDPOINT: string = 'api/v1/whatsappprompt/update';
//DELETE
public readonly API_WHATSAPP_DELETE_PROMPT_ENDPOINT: string = 'api/v1/whatsappprompt/delete?organization={{organization}}&id={{id}}';

//****Whats App Prompt Variable*******
//GET
public readonly API_WHATSAPP_PROMPT_GET_ALL_VARIABLE_ENDPOINT: string = 'api/v1/whatsapppromptvariable/getAllByOrganization?organization={{organization}}';
//POST
public readonly API_WHATSAPP_CREATE_PROMPT_VARIABLE_ENDPOINT: string = 'api/v1/whatsapppromptvariable/create';
public readonly API_WHATSAPP_UPDATE_PROMPT_VARIABLE_ENDPOINT: string = 'api/v1/whatsapppromptvariable/update';
public readonly API_WHATSAPP_PROMPT_GET_ALL_BY_PROMPT_VARIABLE_ENDPOINT: string = 'api/v1/whatsapppromptvariable/getAllByOrganizationAndWhatsAppPrompt';
public readonly API_WHATSAPP_PROMPT_GET_ALL_BY_PROMPT_AND_ACTIVE_VARIABLE_ENDPOINT: string = 'api/v1/whatsapppromptvariable/getAllByOrganizationAndWhatsAppPromptAndActive';
//DELETE
public readonly API_WHATSAPP_PROMPT_DELETE_VARIABLE_ENDPOINT: string = 'api/v1/whatsapppromptvariable/delete?organization={{organization}}&id={{id}}';

//****Whats App Mesage*******
//GET
public readonly API_WHATSAPP_GET_MEDIA_URL_ENDPOINT: string = 'api/v1/messagetoWhatsApp/getWhatsAppMediaUrl';


//**********************
//** Calling Modules **
//**********************
//****CALL DETAILS *******
public readonly API_REFRESH_CONNECTION_ENDPOINT: string = 'api/v1/calldetail/refreshConnections?organization={{organization}}&phoneNumber={{phoneNumber}}';
public readonly API_CALL_DETAIL_ENDPOINT: string = 'api/v1/calldetail/getAllCallDetailsOnOrganization?organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_CALL_DETAIL_BY_CONTEXT_ENDPOINT: string = 'api/v1/calldetail/findAllByPhoneContextAndOrganization?phoneContext={{phoneContext}}&organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_CALL_DETAIL_BY_TIMEZONE_ENDPOINT: string = 'api/v1/calldetail/findAllByTimezoneAndOrganization?organization={{organization}}&timezone={{timezone}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_CALL_DETAIL_BY_ISCONFERENCE_ENDPOINT: string = 'api/v1/calldetail/findAllByIsconferenceAndOrganization?isconference={{isconference}}&organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_CALL_DETAIL_BY_ISIVR_ENDPOINT: string = 'api/v1/calldetail/findAllByIsIvrAndOrganization?ivr={{ivr}}&organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_CALL_DETAIL_BY_ISQUEUE_ENDPOINT: string = 'api/v1/calldetail/findAllByIsQueueAndOrganization?queue={{queue}}&organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_CALL_DETAIL_BY_ISPREDICTIVE_ENDPOINT: string = 'api/v1/calldetail/findAllByIsPridictiveAndOrganization?pridictive={{pridictive}}&organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_CALL_DETAIL_BY_ISPROGRESSIVE_ENDPOINT: string = 'api/v1/calldetail/findAllByIsProgressiveAndOrganization?progressive={{progressive}}&organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';

public readonly API_CALL_DETAIL_FOR_EMPLOYEE_HISTORY_ENDPOINT: string = 'api/v1/calldetail/findAllForEmployeeHistory?dateRange={{dateRange}}&callerid={{callerid}}&organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_CALL_DETAIL_BY_CUSTOMER_ENDPOINT: string = 'api/v1/calldetail/findAllByCustomeridAndOrganization?customerid={{customerid}}&organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_CALL_DETAIL_BY_COUNTRY_ENDPOINT: string = 'api/v1/calldetail/findAllByCountryAndOrganization?country={{country}}&organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_CALL_DETAIL_BY_CALL_ON_MOBILE_ENDPOINT: string = 'api/v1/calldetail/findAllByCallonmobileAndOrganization?callonmobile={{callonmobile}}&organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_CALL_DETAIL_BY_CALLERID_ENDPOINT: string = 'api/v1/calldetail/findAllByCalleridAndOrganization?callerid={{callerid}}&organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_CALL_DETAIL_START_DATE_GREATOR_ENDPOINT: string = 'api/v1/calldetail/findAllByStartdateGreaterThanEqualAndOrganization?organization={{organization}}&startDate={{startDate}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_CALL_DETAIL_CALL_DURATION_LESS_ENDPOINT: string = 'api/v1/calldetail/findAllByCalldurationsecondsLessThanEqualAndOrganization?calldurationseconds={{calldurationseconds}}&organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_CALL_DETAIL_CALL_DURATION_GREATOR_ENDPOINT: string = 'api/v1/calldetail/findAllByCalldurationsecondsGreaterThanEqualAndOrganization?calldurationseconds={{calldurationseconds}}&organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_CALL_DETAIL_BY_CALL_DURATION_GREATOR_CALLERID_ENDPOINT: string = 'api/v1/calldetail/findAllByCalldurationsecondsGreaterThanEqualAndCalleridAndOrganization?calldurationseconds={{calldurationseconds}}&callerid={{callerid}}&organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_CALL_DETAIL_BY_CALL_DURATION_GREATOR_CUSTOMER_ENDPOINT: string = 'api/v1/calldetail/findAllByCalldurationsecondsGreaterThanEqualAndCustomeridAndOrganization?calldurationseconds={{calldurationseconds}}&customerid={{customerid}}&organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_CALL_DETAIL_BY_CALL_DURATION_GREATOR_CONFERENCE_ENDPOINT: string = 'api/v1/calldetail/findAllByCalldurationsecondsGreaterThanEqualAndIsconferenceAndOrganization?calldurationseconds={{calldurationseconds}}&isconference={{isconference}}&organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_CALL_DETAIL_BY_CALL_DURATION_GREATOR_PHONECONTEXT_ENDPOINT: string = 'api/v1/calldetail/findAllByCalldurationsecondsGreaterThanEqualAndPhoneContextAndOrganization?calldurationseconds={{calldurationseconds}}&organization={{organization}}&phoneContext={{phoneContext}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_CALL_DETAIL_BY_CALL_DURATION_GREATOR_TIMEZONE_ENDPOINT: string = 'api/v1/calldetail/findAllByCalldurationsecondsGreaterThanEqualAndTimezoneAndOrganization?calldurationseconds={{calldurationseconds}}&organization={{organization}}&timezone={{timezone}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_CALL_DETAIL_BY_CALL_DURATION_LESS_CALLERID_ENDPOINT: string = 'api/v1/calldetail/findAllByCalldurationsecondsLessThanEqualAndCalleridAndOrganization?calldurationseconds={{calldurationseconds}}&callerid={{callerid}}&organization={{organization}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}}';
public readonly API_CALL_DETAIL_BY_CALL_DURATION_LESS_TIMEZONE_ENDPOINT: string = 'api/v1/calldetail/findAllByCalldurationsecondsLessThanEqualAndTimezoneAndOrganization?calldurationseconds={{calldurationseconds}}&organization={{organization}}&timeZone={{timeZone}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_CALL_DETAIL_BY_CALL_DURATION_LESS_PHONECONTEXT_ENDPOINT: string = 'api/v1/calldetail/findAllByCalldurationsecondsLessThanEqualAndPhoneContextAndOrganization?calldurationseconds={{calldurationseconds}}&organization={{organization}}&phoneContext={{phoneContext}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_CALL_DETAIL_BY_CALL_DURATION_LESS_ISCONFERENCE_ENDPOINT: string = 'api/v1/calldetail/findAllByCalldurationsecondsLessThanEqualAndIsconferenceAndOrganization?calldurationseconds={{calldurationseconds}}&isconference={{isconference}}&organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_CALL_DETAIL_BY_CALLONMOBILE_ISCONFERENCE_ENDPOINT: string = 'api/v1/calldetail/findAllByCallonmobileAndIsconferenceAndOrganization?callonmobile={{callonmobile}}&isconference={{isconference}}&organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_CALL_DETAIL_BY_CALL_DURATION_LESS_CUSTOMER_ENDPOINT: string = 'api/v1/calldetail/findAllByCalldurationsecondsLessThanEqualAndCustomeridAndOrganization?calldurationseconds={{calldurationseconds}}&customerid={{customerid}}&organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_CALL_DETAIL_IN_MEMORY_ALL_ENDPOINT: string = 'api/v1/calldetail/findAllInMemoryDataByOrganization?organization={{organization}}';
public readonly API_CALL_DETAIL_IN_MEMORY_ALL_EXTENSION_ENDPOINT: string = 'api/v1/calldetail/findAllInMemoryDataByOrganizationAndExtension?extension={{extension}}&organization={{organization}}';


//POST
public readonly API_CALL_DETAIL_COUNT_ENDPOINT: string = 'api/v1/calldetail/getCallCountForDashboard';
public readonly API_CALL_DETAIL_COUNT_BY_EMPLOYEE_ENDPOINT: string = 'api/v1/calldetail/getCallCountForDashboardForEmployee';
public readonly API_CALL_DETAIL_COUNT_BY_EMPLOYEE_AND_TIME_ENDPOINT: string = 'api/v1/calldetail/getCallCountForDashboardForEmployeeByTime';
public readonly API_CALL_DETAIL_ADD_AND_CHANGE_CUSTOMER_CONVERTED_ENDPOINT: string = 'api/v1/calldetail/addCustomerIfRequiredAndConvert';
public readonly API_CALL_DETAIL_ADD_AND_CHANGE_CUSTOMER_DESCRIPTION_ENDPOINT: string = 'api/v1/calldetail/addCustomerIfRequiredAndUpdateRemark';



//****CALLING COST *****
public readonly API_CALLING_COST_ENDPOINT: string = 'api/v1/callingcost/getAllCallingCostOnOrganization?organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_CALLING_COST_BY_EXTENSION_ENDPOINT: string = 'api/v1/callingcost/getAllCallingCostByExtensionAndOrganization?extension={{extension}}&organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_CALLING_COST_BY_CALL_CALCULATION_ENDPOINT: string = 'api/v1/callingcost/findAllByCallcalculationAndOrganization?callcalculation={{callcalculation}}&organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_CALLING_COST_BY_AMOUNT_LESS_THAN_ENDPOINT: string = 'api/v1/callingcost/findAllByAmountLessThanEqualAndOrganization?amount={{amount}}&organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_CALLING_COST_BY_AMOUNT_MORE_THAN_ENDPOINT: string = 'api/v1/callingcost/findAllByAmountGreaterThanEqualAndOrganization?amount={{amount}}&organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_CALLING_COST__SEND_VIA_EMAIL_ENDPOINT: string = 'api/v1/callingcost/getAllCallingCostOnOrganizationViaEmail?email={{email}}&organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_CALLING_COST_BY_AMOUNT_LESS_AND_COST_CALCULATION_ENDPOINT: string = 'api/v1/callingcost/findAllByAmountLessThanEqualAndCallcalculationAndOrganization?amount={{amount}}&callcalculation={{callcalculation}}&organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_CALLING_COST_BY_AMOUNT_GREATOR_AND_COST_CALCULATION_ENDPOINT: string = 'api/v1/callingcost/findAllByAmountGreaterThanEqualAndCallcalculationAndOrganization?amount={{amount}}&callcalculation={{callcalculation}}&organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';


//*****CAMPAIGN *******
public readonly API_GET_ALL_AUTODIALER_TYPE_ENDPOINT: string = 'api/v1/campaign/getAllAutodialerTypes?organization={{organization}}';
public readonly API_GET_ALL_CAMPAIGNS_ENDPOINT: string = 'api/v1/campaign/getAllCampaignsOnOrganization?organization={{organization}}';
public readonly API_GET_CAMPAIGN_BY_NAME_ENDPOINT: string = 'api/v1/campaign/getCampaignByNameAndOrganization?campaignName={{campaignName}}&organization={{organization}}';
public readonly API_GET_CAMPAIGN_BY_ID_ENDPOINT: string = 'api/v1/campaign/getCampaignByIdAndOrganization?campaignId={{campaignId}}&organization={{organization}}';
public readonly API_GET_CAMPAIGN_BY_PHONECONTEXT_ENDPOINT: string = 'api/v1/campaign/findAllByPhonecontextAndOrganization?organization={{organization}}&phonecontext={{phonecontext}}';
public readonly API_GET_CAMPAIGN_BY_ISMOBILE_ENDPOINT: string = 'api/v1/campaign/findAllByIsonmobileAndOrganization?isonmobile={{isonmobile}}&organization={{organization}}';
public readonly API_GET_CAMPAIGN_BY_COUNTRY_ENDPOINT: string = 'api/v1/campaign/findAllByCountryAndOrganization?country={{country}}&organization={{organization}}';
public readonly API_GET_CAMPAIGN_BY_BUSINESS_ENDPOINT: string = 'api/v1/campaign/findAllByBusinessAndOrganization?business={{business}}&organization={{organization}}';
public readonly API_GET_CAMPAIGN_BY_AUTODIALER_TYPE_ENDPOINT: string = 'api/v1/campaign/findAllByAutodialertypeAndOrganization?autodialertype={{autodialertype}}&organization={{organization}}';
public readonly API_GET_CAMPAIGN_BY_MANAGER_ENDPOINT: string = 'api/v1/campaign/findAllByManagerAndOrganization?managerId={{managerId}}&organization={{organization}}';
public readonly API_GET_ALL_CAMPAIGNS_BY_STARTDATE_ENDPOINT: string = 'api/v1/campaign/findAllByStartdateGreaterThanEqualAndOrganization?startdate={{startdate}}&organization={{organization}}';
public readonly API_GET_ALL_EMPLOYEES_OF_CAMPAIGN_ENDPOINT: string = 'api/v1/employeetocampaign/findAllByCampaignAndOrganization?id={{id}}&organization={{organization}}';
public readonly API_GET_ALL_CUSTOMERS_OF_CAMPAIGN_ENDPOINT: string = 'api/v1/customertocampaign/findAllByCampaignAndOrganization?id={{id}}&organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_GET_ALL_CAMPAIGNS_FOR_EMPLOYEE_ENDPOINT: string = 'api/v1/employeetocampaign/findAllByEmployeeAndOrganization?extension={{extension}}&organization={{organization}}';
public readonly API_GET_ALL_CAMPAIGN_FOR_CUSTOMER_ENDPOINT: string = 'api/v1/customertocampaign/findAllByCustomerAndOrganization?organization={{organization}}&phoneNumber={{phoneNumber}}';
public readonly API_GET_ALL_REMINDER_CALLING_TYPE_ENDPOINT: string = 'api/v1/campaign/getAllReminderCallingType?organization={{organization}}';


//POST
public readonly API_UPDATE_CAMPAIGN_ENDPOINT: string = 'api/v1/campaign/updateCampaignByOrganization?organization={{organization}}';
public readonly API_CREATE_CAMPAIGN_ENDPOINT: string = 'api/v1/campaign/createCampaignByOrganization?organization={{organization}}';
public readonly API_CREATE_EMPLOYEE_TO_CAMPAIGN_ENDPOINT: string = 'api/v1/employeetocampaign/createEmployeeToCampaignByOrganization?organization={{organization}}';
public readonly API_UPDATE_EMPLOYEE_TO_CAMPAIGN_ENDPOINT: string = 'api/v1/employeetocampaign/updateEmployeeToCampaignByOrganization?organization={{organization}}';
public readonly API_DELETE_EMPLOYEE_TO_CAMPAIGN_ENDPOINT: string = 'api/v1/employeetocampaign/deleteEmployeeToCampaignByOrganization?organization={{organization}}';
public readonly API_CREATE_CUSTOMER_TO_CAMPAIGN_ENDPOINT: string = 'api/v1/customertocampaign/createCustomerToCampaignByOrganization?organization={{organization}}';
public readonly API_GET_COUNT_CUSTOMER_TO_CAMPAIGN_ENDPOINT: string = 'api/v1/customertocampaign/getCountForCustomerToCampaignByOrganization?organization={{organization}}';
public readonly API_UPDATE_CUSTOMER_TO_CAMPAIGN_ENDPOINT: string = 'api/v1/customertocampaign/updateCustomerToCampaignByOrganization?organization={{organization}}';
public readonly API_DELETE_CUSTOMER_TO_CAMPAIGN_ENDPOINT: string = 'api/v1/customertocampaign/deleteCustomerToCampaignByOrganization?organization={{organization}}';
public readonly API_PAUSE_CAMPAIGN_ENDPOINT: string = 'api/v1/campaign/pauseCampaignByOrganization?organization={{organization}}';
public readonly API_START_CAMPAIGN_ENDPOINT: string = 'api/v1/campaign/startCampaignByOrganization?organization={{organization}}';
public readonly API_STOP_CAMPAIGN_ENDPOINT: string = 'api/v1/campaign/stopCampaignByOrganization?organization={{organization}}';
public readonly API_UNPAUSE_CAMPAIGN_ENDPOINT: string = 'api/v1/campaign/unpauseCampaignByOrganization?organization={{organization}}';
public readonly API_RESET_CAMPAIGN_ENDPOINT: string = 'api/v1/campaign/resetCampaignByOrganization?organization={{organization}}';


//*****CAMPAIGN VIEW*******
public readonly API_List_ATLEAST_ONCE_RUN_CAMPAIGN_ID_ENDPOINT: string = 'api/v1/campaign-run/listCampaignIdsMerged?organization={{organization}}';
public readonly API_List__CAMPAIGN_Run_ID_ENDPOINT: string = 'api/v1/campaign-run/listRunIdsForCampaignMerged?campaignId={{campaignId}}&organization={{organization}}';
public readonly API_CAMPAIGN_Run_ID_CALL_LOGS_OLD_ENDPOINT: string = 'api/v1/campaign-run/getCallLogsMergedForRun?campaignId={{campaignId}}&runId={{runId}}&organization={{organization}}&pageNumber={{pageNumber}}&size={{size}}&searchText={{searchText}}';
public readonly API_CAMPAIGN_Run_ID_CALL_LOGS_CURRENT_MEMORY_ENDPOINT: string = 'api/v1/campaign-run/getCurrentRunLiveLogsMemoryOnly?campaignId={{campaignId}}&organization={{organization}}&searchText={{searchText}}';
public readonly API_CAMPAIGN_RUN_EXCEL_EXPORT_DBONLY_ENDPOINT: string = 'api/v1/campaign-run/exportRunExcelDbOnly?campaignId={{campaignId}}&runId={{runId}}&organization={{organization}}';
public readonly API_CAMPAIGN_RUN_RECORDINGS_ENDPOINT: string = 'api/v1/campaign-run/exportRunRecordings?campaignId={{campaignId}}&runId={{runId}}&organization={{organization}}';

//DELETE
public readonly API_DELETE_CAMPAIGN_ENDPOINT: string = 'api/v1/campaign/deleteCampaignByIdAndOrganization?id={{id}}&organization={{organization}}';
public readonly API_DELETE_ALL_EMPLOYEE_TO_CAMPAIGN_ENDPOINT: string = 'api/v1/employeetocampaign/deleteAllByCampaignAndOrganization?id={{id}}&organization={{organization}}';
public readonly API_DELETE_ALL__CUSTOMER_TO_CAMPAIGN_ENDPOINT: string = 'api/v1/customertocampaign/deleteAllByCampaignAndOrganization?id={{id}}&organization={{organization}}';



//*****CONFERENCE *******
public readonly API_ALL_CONFERENCE_ENDPOINT: string = 'api/v1/conference/getAllConferenceByOrganization?organization={{organization}}';
public readonly API_ALL_CONFERENCE_BY_EXTENSION_ENDPOINT: string = 'api/v1/conference/getConferenceByExtensionAndOrganization?extension={{extension}}&organization={{organization}}';
public readonly API_ALL_CONFERENCE_BY_PHONECONTEXT_ENDPOINT: string = 'api/v1/conference/getAllConferenceOnPhoneContextAndOrganization?organization={{organization}}&phoneContext={{phoneContext}}';
public readonly API_ALL_CONFERENCE_BY_ISENABLED_ENDPOINT: string = 'api/v1/conference/getAllConferenceOnIsEnabledAndOrganization?isEnabled={{isEnabled}}&organization={{organization}}';
//POST
public readonly API_CREATE_CONFERENCE_ENDPOINT: string = 'api/v1/conference/createConferenceByOrganization?organization={{organization}}';
public readonly API_DISABLE_CONFERENCE_ENDPOINT: string = 'api/v1/conference/disableConferenceOnExtensionAndOrganization';
public readonly API_ENABLE_CONFERENCE_ENDPOINT: string = 'api/v1/conference/enableConferenceOnExtensionAndOrganization';
public readonly API_UPDATE_CONFERENCE_ENDPOINT: string = 'api/v1/conference/updateConferenceByOrganization?organization={{organization}}';
public readonly API_UPLOAD_CONFERENCE_ENDPOINT: string = 'api/v1/conference/upload?organization={{organization}}';
//DELETE
public readonly API_DELETE_CONFERENCE_ENDPOINT: string = 'api/v1/conference/deleteConferenceByExtensionAndOrganization?extension={{extension}}&organization={{organization}}';


//*****IVR *******
public readonly API_ALL_IVR_ENDPOINT: string = 'api/v1/ivr/getAllIvrsByOrganization?organization={{organization}}';
public readonly API_ALL_IVR__BY_EXTENSION_ENDPOINT: string = 'api/v1/ivr/getIvrByExtensionAndOrganization?extension={{extension}}&organization={{organization}}';
public readonly API_ALL_IVR__BY_PHONECONTEXT_ENDPOINT: string = 'api/v1/ivr/getAllIvrsOnPhoneContextAndOrganization?organization={{organization}}&phoneContext={{phoneContext}}';
public readonly API_ALL_IVR_BY_ISENABLEDENDPOINT: string = 'api/v1/ivr/getAllIvrOnIsEnabledAndOrganization?isEnabled={{isEnabled}}&organization={{organization}}';
//POST
public readonly API_CREATE_IVR_ENDPOINT: string = 'api/v1/ivr/createIvrByOrganization?organization={{organization}}';
public readonly API_DIABLE_IVR_ENDPOINT: string = 'api/v1/ivr/disableIvrOnEmailAndOrganization';
public readonly API_ENABLE_IVR_ENDPOINT: string = 'api/v1/ivr/enableIvrOnExtensionAndOrganization';
public readonly API_UPDATE_IVR_ENDPOINT: string = 'api/v1/ivr/updateIvrByOrganization?organization={{organization}}';
public readonly API_UPLOAD_IVR_ENDPOINT: string = 'api/v1/ivr/upload?organization={{organization}}';
//DELETE
public readonly API_DELETE_IVR_ENDPOINT: string = 'api/v1/ivr/deleteIvrByExtensionAndOrganization?extension={{extension}}&organization={{organization}}';


//*****QUEUE *******
public readonly API_GET_ALL_QUEUE_ENDPOINT: string = 'api/v1/queue/getAllQueuesByOrganization?organization={{organization}}';
public readonly API_GET_ALL_QUEUE_BY_EXTENSION_ENDPOINT: string = 'api/v1/queue/getQueueByExtensionAndOrganization?extension={{extension}}&organization={{organization}}';
public readonly API_GET_ALL_QUEUE_BY_PHONECONTEXT_ENDPOINT: string = 'api/v1/queue/getAllQueuesOnPhoneContextAndOrganization?organization={{organization}}&phoneContext={{phoneContext}}';
public readonly API_GET_ALL_QUEUE_BY_ISENABLED_ENDPOINT: string = 'api/v1/queue/getAllQueueOnIsEnabledAndOrganization?isEnabled={{isEnabled}}&organization={{organization}}';
//POST
public readonly API_UPLOAD_QUEUE_ENDPOINT: string = 'api/v1/queue/upload?organization={{organization}}';
public readonly API_UPDATE_QUEUE_ENDPOINT: string = 'api/v1/queue/updateQueueByOrganization?organization={{organization}}';
public readonly API_ENABLE_QUEUE_ENDPOINT: string = 'api/v1/queue/enableQueueOnExtensionAndOrganization';
public readonly API_DISABLE_QUEUE_ENDPOINT: string = 'api/v1/queue/disableQueueOnEmailAndOrganization';
public readonly API_CREATE_QUEUE_ENDPOINT: string = 'api/v1/queue/createQueueByOrganization?organization={{organization}}';
//DELETE
public readonly API_DELETE_QUEUE_ENDPOINT: string = 'api/v1/queue/deleteQueueByExtensionAndOrganization?extension={{extension}}&organization={{organization}}';


//**********************
//** Organization Module
//**********************
//Employee
public readonly API_GETALL_COST_CALCULATION_ENDPOINT: string = 'api/v1/employees/getAllCostCalcultationType?organization={{organization}}';
public readonly API_GETALL_UNLIMITED_PLAN_ENDPOINT: string = 'api/v1/employees/getAllUnlimitedPlanAmount?organization={{organization}}';
public readonly API_GETALL_METERED_PLAN_ENDPOINT: string = 'api/v1/employees/getAllMeteredPlanAmount?organization={{organization}}';

public readonly API_GETALL_EMPLOYEE_DETAIL_ENDPOINT: string = 'api/v1/employees/getAllEmployeesByOrganization?organization={{organization}}';
public readonly API_EMPLOYEE_DETAIL_ENDPOINT: string = 'api/v1/employees/getEmployeeByEmailAndOrganization?email={{email}}&organization={{organization}}';
public readonly API_EMPLOYEE_DETAIL_BY_PHONENUMBER_ENDPOINT: string = 'api/v1/employees/getEmployeeByPhonenumberAndOrganization?organization={{organization}}&phonenumber={{phonenumber}}';
public readonly API_EMPLOYEE_DETAIL_BY_EXTENSION_ENDPOINT: string = 'api/v1/employees/getEmployeeByExtensionAndOrganization?extension={{extension}}&organization={{organization}}';
public readonly API_EMPLOYEE_DETAIL_BY_USERROLE_ENDPOINT: string = 'api/v1/employees/getAllEmployeesOnUserRoleAndOrganization?organization={{organization}}&role={{role}}';
public readonly API_EMPLOYEE_DETAIL_BY_SEX_ENDPOINT: string = 'api/v1/employees/getAllEmployeesOnSexAndOrganization?organization={{organization}}&sex={{sex}}';
public readonly API_EMPLOYEE_DETAIL_BY_PHONECONTEXT_ENDPOINT: string = 'api/v1/employees/getAllEmployeesOnPhoneContextAndOrganization?organization={{organization}}&phoneContext={{phoneContext}}';
public readonly API_EMPLOYEE_DETAIL_BY_ISENABLED_ENDPOINT: string = 'api/v1/employees/getAllEmployeesOnIsEnabledAndOrganization?isEnabled={{isEnabled}}&organization={{organization}}';
public readonly API_GETALL_EMPLOYEE_DETAIL_BY_COSTCALCULATION_ENDPOINT: string = 'api/v1/employees/findAllBycostCalculationAndOrganization?costCalculation={{costCalculation}}&organization={{organization}}';
public readonly API_GET_ALL_IMAGES_EMPLOYEE_ENDPOINT: string = 'api/v1/employees/getEmployeeImages?email={{email}}&organization={{organization}}';


//POST
public readonly API_UPLOAD_EMPLOYEE_ENDPOINT: string = 'api/v1/employees/upload?organization={{organization}}';
public readonly API_UPDATE_EMPLOYEE_ENDPOINT: string = 'api/v1/employees/updateEmployeeByOrganization?oldEmail={{oldEmail}}&organization={{organization}}';
public readonly API_UPLOAD_EMPLOYEE_PROFILE_PIC_ENDPOINT: string = 'api/v1/employees/uploadProfilePicByEmailAndOrganization?email={{email}}&organization={{organization}}';
public readonly API_UPLOAD_EMPLOYEE_DOC1_ENDPOINT: string = 'api/v1/employees/uploadDocOneByEmailAndOrganization?email={{email}}&organization={{organization}}';
public readonly API_UPLOAD_EMPLOYEE_DOC2_ENDPOINT: string = 'api/v1/employees/uploadDocTwoByEmailAndOrganization?email={{email}}&organization={{organization}}';
public readonly API_CREATE_EMPLOYEE_ENDPOINT: string = 'api/v1/employees/createEmployeeByOrganization?organization={{organization}}';
public readonly API_DIASBLE_EMPLOYEE_CALL_ON_MOBILE_ENDPOINT: string = 'api/v1/employees/disableEmployeeCallOnMobile';
public readonly API_ENABLE_EMPLOYEE_CALL_ON_MOBILE_ENDPOINT: string = 'api/v1/employees/enableEmployeeCallOnMobile';
public readonly API_DIASBLE_EMPLOYEE__ENDPOINT: string = 'api/v1/employees/disableUserOnEmailAndOrganization';
public readonly API_ENABLE_EMPLOYEE__ENDPOINT: string = 'api/v1/employees/enableUserOnEmailAndOrganization';
public readonly API_ENABLE_EMPLOYEE_SECOND_ALLOTED_LINE_ENDPOINT: string = 'api/v1/employees/enableUseAllotedSecondLineByOrganization';
public readonly API_DIASBLE_EMPLOYEE_SECOND_ALLOTED_LINE_ENDPOINT: string = 'api/v1/employees/disableUseAllotedSecondLineByOrganization';
public readonly API_UPDATE_SELF_EXTENSION_PASSWORD_EMPLOYEE_ENDPOINT: string = 'api/v1/employees/updateSelfExtensionPassword';
public readonly API_ENABLE_EMPLOYEE_SELF_CALL_ON_MOBILE_ENDPOINT: string = 'api/v1/employees/enableSelfCallOnMobile';
public readonly API_DIASBLE_EMPLOYEE_SELF_CALL_ON_MOBILE_ENDPOINT: string = 'api/v1/employees/disableSelfCallOnMobile';
public readonly API_UPDATE_SELF_EMPLOYEE_ENDPOINT: string = 'api/v1/employees/updateSelfByOrganization?oldEmail={{oldEmail}}&organization={{organization}}';
public readonly API_UPDATE_WEB_PASSWORD_EMPLOYEE_ENDPOINT: string = 'api/v1/employees/updateWebPassword';
public readonly API_UPDATE_EXTENSION_PASSWORD_EMPLOYEE_ENDPOINT: string = 'api/v1/employees/updateExtensionPassword';

public readonly API_GET_ALL_RECORDING__EMPLOYEE_ENDPOINT: string = 'api/v1/employees/getAllRecordingDataForEmployee';
public readonly API_DOWNLOAD_RECORDING_EMPLOYEE_ENDPOINT: string = 'api/v1/employees/downloadRecordingForEmployee';
public readonly API_UPDATE_SELF_THEME_EMPLOYEE_ENDPOINT: string = 'api/v1/employees/updateSelfUiThemeByOrganization';
public readonly API_UPDATE_SELF_AUTO_ANSWER_EMPLOYEE_ENDPOINT: string = 'api/v1/employees/updateSelfAutoAnswerByOrganization';
public readonly API_UPDATE_SELF_AUTO_CONFERENCE_EMPLOYEE_ENDPOINT: string = 'api/v1/employees/updateSelfAutoConferenceByOrganization';
public readonly API_UPDATE_SELF_AUTO_VIDEO_EMPLOYEE_ENDPOINT: string = 'api/v1/employees/updateSelfAutoVideoByOrganization';
public readonly API_UPDATE_SELF_MIC_DEVICE_EMPLOYEE_ENDPOINT: string = 'api/v1/employees/updateSelfMicDeviceByOrganization';
public readonly API_UPDATE_SELF_SPEAKER_DEVICE_EMPLOYEE_ENDPOINT: string = 'api/v1/employees/updateSelfSpeakerDeviceByOrganization';
public readonly API_UPDATE_SELF_VIDEO_DEVICE_EMPLOYEE_ENDPOINT: string = 'api/v1/employees/updateSelfVideoDeviceByOrganization';
public readonly API_UPDATE_SELF_VIDEO_ORIENTATION_EMPLOYEE_ENDPOINT: string = 'api/v1/employees/updateSelfVideoOrientationByOrganization';
public readonly API_UPDATE_SELF_VIDEO_QUALITY_EMPLOYEE_ENDPOINT: string = 'api/v1/employees/updateSelfVideoQualityByOrganization';
public readonly API_UPDATE_SELF_VIDEO_FRAME_RATE_EMPLOYEE_ENDPOINT: string = 'api/v1/employees/updateSelfVideoFrameRateByOrganization';
public readonly API_UPDATE_SELF_AUTO_GAIN_CONTROL_EMPLOYEE_ENDPOINT: string = 'api/v1/employees/updateSelfAutoGainControlByOrganization';
public readonly API_UPDATE_SELF_ECHO_CANCELLATION_EMPLOYEE_ENDPOINT: string = 'api/v1/employees/updateSelfEchoCancellationByOrganization';
public readonly API_UPDATE_SELF_NOISE_SUPRESSION_EMPLOYEE_ENDPOINT: string = 'api/v1/employees/updateSelfNoiseSupressionByOrganization';
public readonly API_UPDATE_CONTROL_AI_EMPLOYEE_ENDPOINT: string = 'api/v1/employees/updateUserAllowedToSwitchOffWhatsAppAIByOrganization';
public readonly API_UPDATE_RECORD_ALL_CALLS_EMPLOYEE_ENDPOINT: string = 'api/v1/employees/updateEmployeeRecordAllCallsByOrganization';
public readonly API_UPDATE_Notification_Dot_Status_EMPLOYEE_ENDPOINT: string = 'api/v1/employees/updateNotificationDotStatusByOrganization';
public readonly API_UPDATE_SELF_DO_NOT_DISTURB_EMPLOYEE_ENDPOINT: string = 'api/v1/employees/updateSelfDoNotDisturbByOrganization';
public readonly API_UPDATE_SELF_START_FULL_SCREEN_VIDEO_EMPLOYEE_ENDPOINT: string = 'api/v1/employees/updateSelfStartVideoFullScreenByOrganization';
public readonly API_UPDATE_SELF_CALL_WAITING_EMPLOYEE_ENDPOINT: string = 'api/v1/employees/updateSelfCallWaitingByOrganization';
public readonly API_UPDATE_INTERCOM_POLICY_EMPLOYEE_ENDPOINT: string = 'api/v1/employees/updateEmployeeIntercomPolicyByOrganization';
public readonly API_UPDATE_FREE_DIAL_OPTION_EMPLOYEE_ENDPOINT: string = 'api/v1/employees/updateEmployeeFreeDialOptionByOrganization';
public readonly API_UPDATE_TEXT_DICTATION_EMPLOYEE_ENDPOINT: string = 'api/v1/employees/updateEmployeeTextDictationByOrganization';
public readonly API_UPDATE_TEXT_MESSAGING_EMPLOYEE_ENDPOINT: string = 'api/v1/employees/updateEmployeeTextMessagingByOrganization';


//******Absenteeism******
public readonly API_ALL_ABSENTEEISM_DETAIL_ENDPOINT: string = 'api/v1/absenteeisms/getAllAbsenteeismOnOrganization?organization={{organization}}';
public readonly API_GET_ALL_ABSENTEEISM_BY_DATE_ENDPOINT: string = 'api/v1/absenteeisms/findAllByDateFromGreaterThanEqualAndDateToLessThanEqualOrganization?dateFrom={{dateFrom}}&dateTo={{dateTo}}&organization={{organization}}';
public readonly API_GET_ALL_ABSENTEEISM_BY_EMPLOYEE_ENDPOINT: string = 'api/v1/absenteeisms/findAllByEmployeeAndOrganization?employeeID={{employeeID}}&organization={{organization}}';
public readonly API_GET_ALL_ABSENTEEISM_BY_ROA_ENDPOINT: string = 'api/v1/absenteeisms/findAllByReasonForAbsenseAndOrganization?reasonForAbsense={{reasonForAbsense}}&organization={{organization}}';
//POST
public readonly API_CREATE_ABSENTEEISM__ENDPOINT: string = 'api/v1/absenteeisms/createAbsenteeismByOrganization?organization={{organization}}';
public readonly API_UPDATE_ABSENTEEISM__ENDPOINT: string = 'api/v1/absenteeisms/updateAbsenteeismByOrganization?organization={{organization}}';
//DELETE
public readonly API_DELETE_ABSENTEEISM__ENDPOINT: string = 'api/v1/absenteeisms/deleteAbsenteeismByIdAndOrganization?id={{id}}&organization={{organization}}';



//******Customer******
public readonly API_GETALL_CUSTOMER_BY_ID_ENDPOINT: string = 'api/v1/customers/getCustomerByIdAndOrganization?customerId={{customerId}}&organization={{organization}}';
public readonly API_GET_IMAGE_CUSTOMER_ENDPOINT: string = 'api/v1/customers/getCustomerImage?phoneNumber={{phoneNumber}}&organization={{organization}}';
public readonly API_GETALL_CUSTOMER_BY_PHONE_NUMBER_ENDPOINT: string = 'api/v1/customers/getByPhoneNumberAndOrganization?organization={{organization}}&phoneNumber={{phoneNumber}}';

public readonly API_GETALL_CUSTOMER_BY_EMAIL_ENDPOINT: string = 'api/v1/customers/getCustomerByEmailAndOrganization?email={{email}}&organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_GETALL_CUSTOMER_BY_PESEL_ENDPOINT: string = 'api/v1/customers/getCustomerByPeselAndOrganization?organization={{organization}}&pesel={{pesel}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_GETALL_CUSTOMER_ENDPOINT: string = 'api/v1/customers/getAllCustomersOnOrganization?organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_GETALL_CUSTOMER_BY_ZIP_ENDPOINT: string = 'api/v1/customers/findAllByZipCodeAndOrganization?organization={{organization}}&zipCode={{zipCode}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_GETALL_CUSTOMER_BY_PHONECONTEXT_ENDPOINT: string = 'api/v1/customers/findAllByPhoneContextAndOrganization?organization={{organization}}&phoneContext={{phoneContext}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_GETALL_CUSTOMER_BY_ISCONVERTED_ENDPOINT: string = 'api/v1/customers/findAllByCovertedAndOrganization?coverted={{coverted}}&organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_GETALL_CUSTOMER_BY_COUNTRY_ENDPOINT: string = 'api/v1/customers/findAllByCountryAndOrganization?country={{country}}&organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_GETALL_CUSTOMER_BY_CITY_ENDPOINT: string = 'api/v1/customers/findAllByCityAndOrganization?city={{city}}&organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_GETALL_CUSTOMER_BY_BUSINESS_ENDPOINT: string = 'api/v1/customers/findAllByBusinessAndOrganization?business={{business}}&organization={{organization}}&searchText={{searchText}}&pageNumber={{pageNumber}}&size={{size}}';

//******Customer Property Inventory******
public readonly API_GETALL_INVENTORY_ENDPOINT: string = 'api/v1/property-inventory/findAllByOrganization?organization={{organization}}&searchText={{searchText}}&available={{available}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_GET_INVENTORY_BY_CUSTOMER__ENDPOINT: string = 'api/v1/property-inventory/getByCustomerIdAndOrganization?customerId={{customerId}}&organization={{organization}}';
public readonly API_DOWNLOAD_CUSTOMER_INVENTORY_ENDPOINT: string = 'api/v1/property-inventory/fetchExcelAfterListedDate?organization={{organization}}&fromListedDateIso={{fromListedDateIso}}&available={{available}}';

// ****** Customer Franchise Inventory ******
public readonly API_GETALL_FRANCHISE_INVENTORY_ENDPOINT: string = 'api/v1/franchise-inventory/findAllByOrganization?organization={{organization}}&searchText={{searchText}}&available={{available}}&pageNumber={{pageNumber}}&size={{size}}';
public readonly API_GET_FRANCHISE_INVENTORY_BY_CUSTOMER_ENDPOINT: string ='api/v1/franchise-inventory/getByCustomerIdAndOrganization?customerId={{customerId}}&organization={{organization}}';
public readonly API_GET_FRANCHISE_INVENTORY_BY_ID_ENDPOINT: string ='api/v1/franchise-inventory/getByIdAndOrganization?id={{id}}&organization={{organization}}';
public readonly API_DOWNLOAD_FRANCHISE_INVENTORY_ENDPOINT: string ='api/v1/franchise-inventory/fetchExcelAfterCreatedDate?organization={{organization}}&fromCreatedDateIso={{fromCreatedDateIso}}&available={{available}}';


//POST
public readonly API_UPDATE_CUSTOMER_DESCRIPTION_ENDPOINT: string = 'api/v1/customers/updateCustomerDescription';
public readonly API_UPLOAD_CUSTOMER_ENDPOINT: string = 'api/v1/customers/upload?organization={{organization}}';
public readonly API_UPDATE_CUSTOMER_ENDPOINT: string = 'api/v1/customers/updateCustomerByOrganization?oldPhone={{oldPhone}}&organization={{organization}}';
public readonly API_MARK_DIVERTED_CUSTOMER_ENDPOINT: string = 'api/v1/customers/customerGotDiverted';
public readonly API_MARK_CONVERTED_CUSTOMER_ENDPOINT: string = 'api/v1/customers/customerGotConverted';
public readonly API_CREATE_CUSTOMER_ENDPOINT: string = 'api/v1/customers/createCustomerByOrganization?organization={{organization}}';
public readonly API_Update_Customer_Product_Interest_ENDPOINT: string = 'api/v1/customers/updateCustomerProductInterests';
public readonly API_UPLOAD_CUSTOMER_PIC_ENDPOINT: string = 'api/v1/customers/uploadCustomerPicByIdAndOrganization?id={{id}}&organization={{organization}}';
public readonly API_UPDATE_AUTO_WHATSAPP_CUSTOMER_ENDPOINT: string = 'api/v1/customers/updateWhatsAppAIAutoMessage';

//DELETE
public readonly API_DELETE_CUSTOMER_ENDPOINT: string = 'api/v1/customers/deleteCustomerByIdAndOrganization?id={{id}}&organization={{organization}}';


//******Department******
public readonly API_GETALL_DEPARTMENT__ENDPOINT: string = 'api/v1/departments/getAllDepartmentsByOrganization?organization={{organization}}';
public readonly API_GET_DEPARTMENT_BY_ID_ENDPOINT: string = 'api/v1/departments/getDepartmentByIdAndOrganization?id={{id}}&organization={{organization}}';
//POST
public readonly API_UPLOAD_DEPARTMENT__ENDPOINT: string = 'api/v1/departments/upload?organization={{organization}}';
public readonly API_UPDATE_DEPARTMENT__ENDPOINT: string = 'api/v1/departments/updateDepartmentByOrganization?organization={{organization}}';
public readonly API_CREATE_DEPARTMENT__ENDPOINT: string = 'api/v1/departments/createDepartmentByOrganization?organization={{organization}}';
//DELETE
public readonly API_DELETE_DEPARTMENT__ENDPOINT: string = 'api/v1/departments/deleteDepartmentByIdAndOrganization?id={{id}}&organization={{organization}}';



//******Product******
public readonly API_GETALL_PRODUCT_ENDPOINT: string = 'api/v1/products/getAllproductsByOrganization?organization={{organization}}';
public readonly API_GETALL_PRODUCT_BY_ID_ENDPOINT: string = 'api/v1/products/getProductByIdAndOrganization?id={{id}}&organization={{organization}}';
public readonly API_GETALL_PRODUCT_BY_PRODUCT_TYPE_ENDPOINT: string = 'api/v1/products/getAllproductsOnProductTypeAndOrganization?organization={{organization}}&productType={{productType}}';
public readonly API_GET_IMAGE_PRODUCT_ENDPOINT: string = 'api/v1/products/getProductImage?id={{id}}&organization={{organization}}';

//POST
public readonly API_UPLOAD_PRODUCT_ENDPOINT: string = 'api/v1/products/upload?organization={{organization}}';
public readonly API_UPDATE_PRODUCT_ENDPOINT: string = 'api/v1/products/updateProductByOrganization?organization={{organization}}';
public readonly API_CREATE_PRODUCT_ENDPOINT: string = 'api/v1/products/createProductByOrganization?organization={{organization}}';
public readonly API_GETALL_PRODUCT_BY_IDS_IN_ENDPOINT: string = 'api/v1/products/findAllProductsByIdIn';
public readonly API_UPLOAD_PRODUCT_PIC_ENDPOINT: string = 'api/v1/products/uploadProductPicByEmailAndOrganization?id={{id}}&organization={{organization}}';


//DELETE
public readonly API_DELETE_PRODUCT_ENDPOINT: string = 'api/v1/products/deleteProductByIdAndOrganization?id={{id}}&organization={{organization}}';

//******Purchase******
public readonly API_GETALL_PURCHASE_BY_ENDPOINT: string = 'api/v1/purchases/getAllPurchasesOnOrganization?organization={{organization}}';
public readonly API_GET_PURCHASE_BY_PURCHASEID_ENDPOINT: string = 'api/v1/purchases/getPurchaseByPurchaseIDAndOrganization?customerID={{purchaseID}}&purchaseID={{organization}}';
public readonly API_GETALL_PURCHASE_BY_PURCHASEDATE_LESS_ENDPOINT: string = 'api/v1/purchases/findAllByPurchaseDateLessThanEqualAndOrganization?date={{date}}&organization={{organization}}';
public readonly API_GETALL_PURCHASE_BY_PURCHASEDATE_GREATOR_ENDPOINT: string = 'api/v1/purchases/findAllByPurchaseDateGreaterThanEqualAndOrganization?date={{date}}&organization={{organization}}';
public readonly API_GETALL_PURCHASE_BY_CUSTOMER_ENDPOINT: string = 'api/v1/purchases/findAllByCustomerAndOrganization?customerID={{customerID}}&organization={{organization}}';
//POST
public readonly API_UPDATE_PURCHASE_ENDPOINT: string = 'api/v1/purchases/updatePurchaseByOrganization?organization={{organization}}';
public readonly API_CREATE_PURCHASE_ENDPOINT: string = 'api/v1/purchases/createPurchaseByOrganization?organization={{organization}}';
//DELETE
public readonly API_DELETE_PURCHASE_ENDPOINT: string = 'api/v1/purchases/deletePurchaseByIdAndOrganization?organization={{organization}}&purchaseID={{purchaseID}}';


//******Suppler******
public readonly API_GETALL_SUPPLIER_ENDPOINT: string = 'api/v1/suppliers/getAllsuppliersByOrganization?organization={{organization}}';
public readonly API_GET_SUPPLIER_ENDPOINT: string = 'api/v1/suppliers/getSupplierByIdAndOrganization?id={{id}}&organization={{organization}}';
public readonly API_GETALL_SUPPLIER_BY_TRANSPORTCAPACITY_ENDPOINT: string = 'api/v1/suppliers/getAllsuppliersOnTransportcapacityAndOrganization?organization={{organization}}&transportcapacity={{transportcapacity}}';
public readonly API_GETALL_SUPPLIER_BY_SUPPLIER_ENDPOINT: string = 'api/v1/suppliers/getAllSupplierOnTypeAndOrganization?organization={{organization}}&supplierType={{supplierType}}';
//POST
public readonly API_UPLOAD_SUPPLIER_ENDPOINT: string = 'api/v1/suppliers/upload?organization={{organization}}';
public readonly API_UPDATE_SUPPLIER_ENDPOINT: string = 'api/v1/suppliers/updateSupplierByOrganization?organization={{organization}}';
public readonly API_CREATE_SUPPLIER_ENDPOINT: string = 'api/v1/suppliers/createSupplierByOrganization?organization={{organization}}';
//DELETE
public readonly API_DELETE_SUPPLIER_ENDPOINT: string = 'api/v1/suppliers/deleteSupplierByIdAndOrganization?id={{id}}&organization={{organization}}';


//**********************
//** Log Tracking **
//**********************

//******ERRORS******
public readonly API_GETALL_ERRORS_ENDPOINT: string = 'api/v1/error/getAllErrorsByOrganization?organization={{organization}}';

//******LOGS******
public readonly API_GETALL_LOGS_ENDPOINT: string = 'api/v1/logs/getAllLogsByOrganization?organization={{organization}}';


//**********************
//*** Settings ***
//**********************
//******AMI CONNECTION******
public readonly API_REFRESH_AMI_ENDPOINT: string = 'api/v1/amiconnection/refreshAmiConnectionForOrganization?domain={{domain}}&organization={{organization}}';
public readonly API_AMI_BY_AMIUSER_ENDPOINT: string = 'api/v1/amiconnection/getAmiConnectionByAmiuserAndOrganization?amiuser={{amiuser}}&organization={{organization}}';
public readonly API_GETALL_AMI_BY_PHONECONTEXT_ENDPOINT: string = 'api/v1/amiconnection/getAllAmiConnectionsOnPhoneContextAndOrganization?organization={{organization}}&phonecontext={{phonecontext}}';
public readonly API_GETALL_AMI_ENDPOINT: string = 'api/v1/amiconnection/getAllAmiConnectionsByOrganization?organization={{organization}}';
public readonly API_GETALL_AMI_BY_ISENABLED_ENDPOINT: string = 'api/v1/amiconnection/getAllAmiConnectionOnIsEnabledAndOrganization?isactive={{isactive}}&organization={{organization}}';
//POST
public readonly API_UPLOAD_AMI_ENDPOINT: string = 'api/v1/amiconnection/upload?organization={{organization}}';
public readonly API_UPDATE_AMI_ENDPOINT: string = 'api/v1/amiconnection/updateAmiConnectionByOrganization?organization={{organization}}';
public readonly API_ENABLE_AMI_ENDPOINT: string = 'api/v1/amiconnection/enableAmiConnectionOnAmiUserAndOrganization';
public readonly API_DISABLE_AMI_ENDPOINT: string = 'api/v1/amiconnection/disableAmiConnectionOnAmiUserAndOrganization';
public readonly API_CREATE_AMI_ENDPOINT: string = 'api/v1/amiconnection/createAmiConnectionByOrganization?organization={{organization}}';
public readonly API_CONNECT_AMI_ENDPOINT: string = 'api/v1/amiconnection/connectAmiConnectionOnAmiUserAndOrganization';
//DELETE
public readonly API_DELETE_AMI_ENDPOINT: string = 'api/v1/amiconnection/deleteAmiConnectionByAmiUserAndOrganization?amiuser={{amiuser}}&organization={{organization}}';

//******SIP PROVIDER******
public readonly API_GETALL_SIP_PROVIDER_ENDPOINT: string = 'api/v1/sipprovider/getAllSipProvidersByOrganization?organization={{organization}}';
public readonly API_GETALL_SIP_PROVIDER_BY_PHONECONTEXT_ENDPOINT: string = 'api/v1/sipprovider/getSipProviderByPhoneNumberAndOrganization?organization={{organization}}&phoneNumber={{phoneNumber}}';
//POST
public readonly API_UPDATE_SIP_PROVIDER_ENDPOINT: string = 'api/v1/sipprovider/updateSipProviderByOrganization?organization={{organization}}';
public readonly API_ENABLE_SIP_PROVIDER_ENDPOINT: string = 'api/v1/sipprovider/enableSipProviderOnIdAndOrganization';
public readonly API_DIABLE_SIP_PROVIDER_ENDPOINT: string = 'api/v1/sipprovider/disableSipProviderOnIdAndOrganization';
public readonly API_CREATE_SIP_PROVIDER_ENDPOINT: string = 'api/v1/sipprovider/createSipProviderByOrganization?organization={{organization}}';
//DELETE
public readonly API_DELETE_SIP_PROVIDER_ENDPOINT: string = 'api/v1/sipprovider/deleteSipProviderByPhoneNumberAndOrganization?organization={{organization}}&phoneNumber={{phoneNumber}}';


//******SSH CONNECTION******
public readonly API_GETALL_SSH_CONNECTION_ENDPOINT: string = 'api/v1/ssh/getAllSshConnectionsByOrganization?organization={{organization}}';
public readonly API_REFRESH_SSH_CONNECTION_ENDPOINT: string = 'api/v1/ssh/refreshSshConnectionForOrganization?domain={{domain}}&organization={{organization}}';
//POST
public readonly API_UPDATE_SSH_CONNECTION_ENDPOINT: string = 'api/v1/ssh/updateSshConnectionByOrganization?organization={{organization}}';
public readonly API_ENABLE_SSH_CONNECTION_ENDPOINT: string = 'api/v1/ssh/enableSshConnectionOnIdAndOrganization';
public readonly API_DISABLE_SSH_CONNECTION_ENDPOINT: string = 'api/v1/ssh/disableSshConnectionOnIdAndOrganization';
public readonly API_CREATE_SSH_CONNECTION_ENDPOINT: string = 'api/v1/ssh/createSshConnectionByOrganization?organization={{organization}}';
//DELETE
public readonly API_DELETE_SSH_CONNECTION_ENDPOINT: string = 'api/v1/ssh/deleteSshConnectionByDomainAndOrganization?domain={{domain}}&organization={{organization}}';


//*************CHAT HISTORY*********/
public readonly API_GET_CHAT_HISTORY_CANDIDATES: string = 'api/v1/chat-history/getAllChatHistoryCandidatesByExtensionAndOrganization';
public readonly API_GET_CHAT_HISTORY: string = 'api/v1/chat-history/getAllChatHistoryByTwoExtensionsAndOrganization';
public readonly API_DELETE_ALL_CHAT_HISTORY_FOR_ALL: string = 'api/v1/chat-history/deleteAllChatHistoryByExtensionAndOrganization';
public readonly API_DELETE_ALL_CHAT_HISTORY_FOR_ONE: string = 'api/v1/chat-history/deleteAllChatHistoryByTwoExtensionsAndOrganization';
public readonly API_APPEND_CHAT_HISTORY: string = 'api/v1/chat-history/appendChatHistoryByTwoExtensionsAndOrganization';
public readonly API_UPDATE_LASTREADINDEX: string = 'api/v1/chat-history/updateLastReadIndexByTwoExtensionsAndOrganization';


//***********Notification************ */
public readonly API_GET_ALL_NOTIFICATION_ENDPOINT: string = 'api/v1/notification/getAllNotificationsByExtensionAndOrganization';
public readonly API_DELETE_ALL_NOTIFICATION_ENDPOINT: string = 'api/v1/notification/deleteAllNotificationsByExtensionAndOrganization';
public readonly API_DELETE_FEW_NOTIFICATION_ENDPOINT: string = 'api/v1/notification/deleteNotificationByIdsAndExtensionsAndOrganization';

//**********Employee Basic Info For All ********* */
public readonly API_ALL_EMPLOYEE_BASIC_INFO_ENDPOINT: string = 'api/v1/employees/getAllEmployeesBasicInfoByOrganization?organization={{organization}}';


//*********Schedule Call ****************** */
public readonly API_SCHEDULE_CALL_FIXEDDATE_ENDPOINT: string = 'api/v1/scheduleJob/scheduleAFixedDateCallToCustomer';
public readonly API_SCHEDULE_CALL_AFTERNSEC_ENDPOINT: string = 'api/v1/scheduleJob/scheduleAfterNSecCallToCustomer';
public readonly API_SCHEDULE_CALL_CRON_ENDPOINT: string = 'api/v1/scheduleJob/scheduleACronCallToCustomer';
public readonly API_REMOVE_SCHEDULED_CALL_ENDPOINT: string = 'api/v1/scheduleJob/removeScheduledCallToCustomer?scheduleType={{scheduleType}}&phoneNumber={{phoneNumber}}&fromExtension={{fromExtension}}&organization={{organization}}';
public readonly API_Find_If_SCHEDULED_CALL_ENDPOINT: string = 'api/v1/scheduleJob/findIfScheduledCallJobToCustomer?scheduleType={{scheduleType}}&phoneNumber={{phoneNumber}}&fromExtension={{fromExtension}}&organization={{organization}}';


public readonly API_SCHEDULE_START_CAMPAIGN_FIXEDDATE_ENDPOINT: string = 'api/v1/scheduleJob/scheduleAFixedDateCampaignToStart';
public readonly API_SCHEDULE_START_CAMPAIGN_AFTERNSEC_ENDPOINT: string = 'api/v1/scheduleJob/scheduleAfterNSecCampaignToStart';
public readonly API_SCHEDULE_START_CAMPAIGN_CRON_ENDPOINT: string = 'api/v1/scheduleJob/scheduleACronCampaignToStart';
public readonly API_REMOVE_SCHEDULED_START_CAMPAIGN_ENDPOINT: string = 'api/v1/scheduleJob/removeStartedScheduledCampgin?scheduleType={{scheduleType}}&campaignId={{campaignId}}&organization={{organization}}';
public readonly API_Find_If_SCHEDULED_START_CAMPAIGN_ENDPOINT: string = 'api/v1/scheduleJob/findIfScheduledStartedCampaignJob?scheduleType={{scheduleType}}&campaignId={{campaignId}}&organization={{organization}}';


public readonly API_SCHEDULE_STOP_CAMPAIGN_FIXEDDATE_ENDPOINT: string = 'api/v1/scheduleJob/scheduleAFixedDateCampaignToStop';
public readonly API_SCHEDULE_STOP_CAMPAIGN_AFTERNSEC_ENDPOINT: string = 'api/v1/scheduleJob/scheduleAfterNSecCampaignToStop';
public readonly API_SCHEDULE_STOP_CAMPAIGN_CRON_ENDPOINT: string = 'api/v1/scheduleJob/scheduleACronCampaignToStop';
public readonly API_REMOVE_SCHEDULED_STOP_CAMPAIGN_ENDPOINT: string = 'api/v1/scheduleJob/removeScheduledStopedCampgin?scheduleType={{scheduleType}}&campaignId={{campaignId}}&organization={{organization}}';
public readonly API_Find_If_SCHEDULED_STOP_CAMPAIGN_ENDPOINT: string = 'api/v1/scheduleJob/findIfScheduledStoppedCampaignJob?scheduleType={{scheduleType}}&campaignId={{campaignId}}&organization={{organization}}';


//Type Of Schedules Tracked. Below variables should be same as in backend
//Types of runnable
public readonly  startCampaignRunnable: string = "startCampaignRunnable";
public readonly  stopCampaignRunnable: string = "stopCampaignRunnable";
public readonly  customerCallRunnable: string = "customerCallRunnable";
//Types of schedule prefix
public readonly cron: string = "cron";
public readonly fixeddate: string = "fixeddate";
public readonly afternseconds: string = "afternseconds";



//*********File Service ******************* */
//Get
public readonly API_GET_ALL_FILE_CATEGORIES: string = 'api/v1/file/getAllFileCategoryByExtensionAndOrganization?extension={{extension}}&organization={{organization}}';
public readonly API_GET_ALL_FILE_NAMES_BY_CATEGORY: string = 'api/v1/file/getAllFileNamesOfUserByOrganizationAndCategory?requestOrigin={{requestOrigin}}&category={{category}}';
public readonly API_DOWNLOAD_USER_FILE: string = 'api/v1/file/downloadUserFileByOrganizationAndCategory?requestOrigin={{requestOrigin}}&category={{category}}&filename={{filename}}';
public readonly API_DELETE_USER_FILE: string = 'api/v1/file/deleteUserFileByOrganizationAndCategory?requestOrigin={{requestOrigin}}&category={{category}}&filename={{filename}}';

//Post
public readonly API_DELETE_FILE_CATEGORY: string = 'api/v1/file/deleteFileCategoryByExtensionAndNameAndOrganization?requestOrigin={{requestOrigin}}';
public readonly API_CREATE_FILE_CATEGORY: string = 'api/v1/file/createFileCategoryByExtensionAndNameAndOrganization?requestOrigin={{requestOrigin}}';
public readonly API_UPDATE_FILE_CATEGORY: string = 'api/v1/file/updateFileCategoryByExtensionAndNameAndOrganization?requestOrigin={{requestOrigin}}';
public readonly API_UPDATE_FILE_CATEGORY_WITHOUT_IMAGE: string = 'api/v1/file/updateFileCategoryByExtensionAndNameAndOrganizationWithoutImage?requestOrigin={{requestOrigin}}';
public readonly API_UPLOAD_USER_FILES: string = 'api/v1/file/uploadUserFilesByOrganizationAndCategory?requestOrigin={{requestOrigin}}&category={{category}}';
public readonly API_DELETE_MULTIPLE_USER_FILE: string = 'api/v1/file/deleteMultipleUserFileByOrganizationAndCategory?requestOrigin={{requestOrigin}}&category={{category}}';
public readonly API_DOWNLOAD_MULTIPLE_USER_FILE: string = 'api/v1/file/downloadMultipleUserFileByOrganizationAndCategory?requestOrigin={{requestOrigin}}&category={{category}}';


//AMI API's
//POST
public readonly API_ATTEMPTED_TRANSFER_CALL: string = 'api/v1/asterisk/attemptedTransferCall';
public readonly API_BLIND_TRANSFER_CALL: string = 'api/v1/asterisk/blindTransferCall';
public readonly API_BRIDGE_TWO_ACTIVE_CALLS: string = 'api/v1/asterisk/bridgeTwoActiveCalls';
public readonly API_CHANGE_MONITOR_ACTION: string = 'api/v1/asterisk/changeMonitorAction';
public readonly API_CONF_KICK_MEMBER: string = 'api/v1/asterisk/confbridgeKickMember';
public readonly API_CONF_LIST_MEMBERS: string = 'api/v1/asterisk/confbridgeListMembers';
public readonly API_CONF_LIST_ROOMS: string = 'api/v1/asterisk/confbridgeListRooms';
public readonly API_CONF_LOCK_MEMBER: string = 'api/v1/asterisk/confbridgeLock';
public readonly API_CONF_MUTE_MEMBER: string = 'api/v1/asterisk/confbridgeMuteMember';
public readonly API_CONF_SET_SINGLE_VIDEO_SOURCE_MEMBER: string = 'api/v1/asterisk/confbridgeSetSingleVideoSrcMember';
public readonly API_CONF_START_RECORD_MEMBER: string = 'api/v1/asterisk/confbridgeStartRecord';
public readonly API_CONF_STOP_RECORD_MEMBER: string = 'api/v1/asterisk/confbridgeStopRecord';
public readonly API_CONF_UNLOCK_MEMBER: string = 'api/v1/asterisk/confbridgeUnlock';
public readonly API_CONF_UNMUTE_MEMBER: string = 'api/v1/asterisk/confbridgeUnmuteMember';
public readonly API_CONF_PUBLIC: string = 'api/v1/asterisk/publicConferenceRequest';
public readonly API_CREATE_CONFERENCE: string = 'api/v1/asterisk/customConferenceRequest';
public readonly API_DISCONNECT_CALL_AFTER_X_SECONDS: string = 'api/v1/asterisk/disconnectAfterXSeconds';
public readonly API_EXTENSION_STATE: string = 'api/v1/asterisk/extensionState';
public readonly API_HUNG_UP_CALL: string = 'api/v1/asterisk/hungUpCall';
public readonly API_LISTEN_QUIETLY: string = 'api/v1/asterisk/listenQuietly';
public readonly API_MONITOR_ACTION: string = 'api/v1/asterisk/monitorAction';
public readonly API_ORIGINATE_CALL: string = 'api/v1/asterisk/originateCall';
public readonly API_ORIGINATE_DATA_CALL: string = 'api/v1/asterisk/originateDataCall';
public readonly API_PARK_FOR_TIMEOUT: string = 'api/v1/asterisk/parkForTimeOut';
public readonly API_PAUSE_MONITOR_ACTION: string = 'api/v1/asterisk/pauseMonitorAction';
public readonly API_REQUEST_STATE_FOR_ALL_AGENTS: string = 'api/v1/asterisk/requestStateForAllAgents';
public readonly API_SEND_ANONYMOUS_TEXT_TO_CHANNEL: string = 'api/v1/asterisk/sendAnonymousTextToChannel';
public readonly API_STATUS_FOR_CHANNEL: string = 'api/v1/asterisk/statusForSpecificChannel';
public readonly API_STOP_MONITOR_ACTION: string = 'api/v1/asterisk/stopMonitorAction';
public readonly API_UNPAUSE_MONITOR_ACTION: string = 'api/v1/asterisk/unpauseMonitorAction';


constructor() { }
}
