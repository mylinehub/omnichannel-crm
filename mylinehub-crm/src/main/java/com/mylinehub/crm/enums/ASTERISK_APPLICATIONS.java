package com.mylinehub.crm.enums;

public enum ASTERISK_APPLICATIONS {
	
	//https://www.voip-info.org/asterisk-documentation-of-application-commands/
	//General Commands
	Authenticate,
	Bridge,
	ChannelRedirect,
	CheckGroup,
	ClearHash,
	Curl,
	DUNDiLookup,
	GetGroupCount,
	GetGroupMatchCount,
	KeepAlive,
	Log,
	Page,
	PickupChan,
	SendDTMF,
	SendImage,
	SendText,
	SendURL,
	SetGroup,
	System,
	Transfer,
	TrySystem,
	VMAuthenticate,
	Wait,
	WaitExten,
	WaitForRing,
	WaitMusicOnHold,


	//Billing
	AppendCDRUserField,
	ForkCDR,
	NoCDR,
	ResetCDR,
	SetAccount,
	Asterisk1cmd1SetAMAFlags,
	SetCDRUserField,


	//Call Management (Hangup, Answer, Dial, etc)
	AMD,
	Answer,
	Busy,
	ChanIsAvail,
	Congestion,
	Dial,
	DISA,
	Hangup,
	Incomplete,
	Originate,
	RetryDial,
	Ringing,


	//Caller Presentation (ID, Name, etc)
	CallingPres,
	LookupBlacklist,
	LookupCIDName,
	PrivacyManager,
	SetCallerID,
	SetCallerPres,
	SetCIDName,
	SetCIDNum,
	SoftHangup,
	Zapateller,


	//ADSI
	ADSIProg,
	GetCPEID,


	//Database Handling
	DBdel,
	DBdeltree,
	DBget,
	DBput,
	ODBCFinish,
	RealTime,
	RealTimeUpdate,


	//Application Integration
	AGI,
	DeadAGI,
	EAGI,
	EnumLookup,
	ExternalIVR,
	JabberJoin,
	JabberLeave,
	JabberSend,
	JabberSendGroup,
	JabberStatus,
	Asterisk1cmd1jack,
	Read,
	ReadFile,
	TXTCIDName,
	UserEvent,


	//Control Flow & Timeouts
	AbsoluteTimeout,
	AELSub,
	ContinueWhile,
	DigitTimeout,
	EndWhile,
	Exec,
	ExecIf,
	ExecIfTime,
	ExitWhile,
	Gosub,
	GosubIf,
	Goto,
	GotoIf,
	GotoIfTime,
	Macro,
	MacroExclusive,
	MacroExit,
	MacroIf,
	NoOp,
	Random,
	ResponseTimeout,
	Return,
	StackPop,
	While,


	//String & Variable Manipulation
	Cut,
	DumpChan,
	ImportVar,
	Math,
	MSet,
	SetGlobalVar,
	Set,
	Sounds,
	Background,
	BackgroundDetect,
	ControlPlayback,
	DateTime,
	Echo,
	Festival,
	Milliwatt,
	MP3Player,
	MusicOnHold,
	Playback,
	Playtones,
	Asterisk1cmd1proceeding,
	Progress,
	SayUnixTime,
	SayAlpha,
	SayCountedAdj,
	SayCountedNoun,
	SayDigits,
	SayNumber,
	SayPhonetic,
	SetMusicOnHold,
	SetLanguage,
	StopPlaytones,
	ChangeMonitor,
	ChanSpy,
	Dictate,
	ExtenSpy,
	MixMonitor,
	Monitor,
	Record,
	StopMonitor,
	StopMixMonitor,


	//SIP Commands
	Asterisk1cmd1SipAddHeader,
	SIPdtmfMode,
	SIPGetHeader,


	//DAHDI (was ZAP) Commands
	DAHDIBarge,
	DAHDIRAS,
	DAHDIScan,
	DAHDISendKeypadFacility,
	Flash,
	ZapCD,
	


	//Voicemail and Conferencing

	ConfBridge,
	Directory,
	HasNewVoicemail,
	HasVoicemail,
	MailboxExists,
	MeetMe,
	Asterisk1cmd1MeetmeAdmin,
	MeetMeChannelAdmin,
	MeetMeCount,
	MiniVM,
	MinivmAccMess,
	MinivmDelete,
	MinivmGreet,
	MinivmNotify,
	MinivmRecord,
	VoiceMail,
	VoiceMailMain,
	VMSayName,


	//Queue and ACD Management
	AddQueueMember,
	AgentCallbackLogin,
	AgentLogin,
	AgentMonitorOutgoing,
	Park,
	ParkAndAnnounce,
	ParkedCall,
	PauseQueueMember,
	Queue,
	RemoveQueueMember,
	UnpauseQueueMember,


	//Short Message Service (SMS)
	SMS,


	//Alarm Monitoring/Central Station
	AlarmReceiver,


	//Amateur Radio/Repeater Linking
	Rpt,


	//External Applications – Not in the Digium Distribution (Svn or Http Tarballs)
	ALSAMonitor,
	app1Prepaid,
	ASR,
	Asterisk1app_dbodbc,
	Backticks,
	DBRewrite,
	DBQuery,
	DTMFToText,
	DynExtenDB,
	Flite,
	ICES,
	Iconv,
	LDAPget,
	MYSQL,
	NBScat,
	
	Perl,
	PHP,
	PPPD,
	Asterisk1Cmd1Voximal,
	Vxml,


	//Bristuff Application
	//All of those are part of the Bristuff asterisk patch.
	Autoanswer,
	AutoanswerLogin,
	Devstate,
	PickUp,
	PickUpChan,
	PickupSIPuri,
	PickDown,
	Segfault,
	Steal,
	Asterisk1cmd1BristuffZapCD,
	ZapEC,
	ZapInband,


	//vISDN applications
	VISDNOverlapDial,


	//Applications for Sirrix channels
	SrxDeflect,
	SrxEchoCan,
	SrxMWI;
}
