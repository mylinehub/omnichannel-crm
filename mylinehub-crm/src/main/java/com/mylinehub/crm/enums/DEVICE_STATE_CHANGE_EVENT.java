package com.mylinehub.crm.enums;


public enum DEVICE_STATE_CHANGE_EVENT {
	dateReceived,
	privilege,
	server,
	calleridname,
	systemname,
	connectedlinename,
	priority,
	sequencenumber,
	exten,
	channelstate,
	calleridnum,
	context,
	state,  // state='UNAVAILABLE' , state='NOT_INUSE' , state=null , state='INUSE'
	device, //extension  , device='PJSIP/203' , device='confbridge:1234' , device='CBAnn/1234'
	connectedlinenum,
	timestamp,
	channelstatedesc,
	systemHashcode
}
		
		
		
		