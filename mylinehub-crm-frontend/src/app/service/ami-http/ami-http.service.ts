import { Injectable } from '@angular/core';
import { ConstantsService } from '../constants/constants.service';
import { ApiHttpService } from '../http/api-http.service';

@Injectable({
  providedIn: 'root'
})
export class AmiHttpService {

  constructor( protected constService : ConstantsService,
    protected httpService : ApiHttpService,) { }

    public attemptedTransferCall(data:any) { 
      var endpoint: string = this.constService.API_ATTEMPTED_TRANSFER_CALL;
      return this.httpService.postWithTokenHandlededAndAudio(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    } 

    public blindTransferCall(data:any) { 
      var endpoint: string = this.constService.API_BLIND_TRANSFER_CALL;
      return this.httpService.postWithTokenHandlededAndAudio(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    } 


    public bridgeTwoActiveCalls(data:any) { 
      var endpoint: string = this.constService.API_BRIDGE_TWO_ACTIVE_CALLS;
      return this.httpService.postWithTokenHandlededAndAudio(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    } 

    public changeMonitorAction(data:any) { 
      var endpoint: string = this.constService.API_CHANGE_MONITOR_ACTION;
      return this.httpService.postWithTokenHandlededAndAudio(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    } 

    public confbridgeKickMember(data:any) { 
      var endpoint: string = this.constService.API_CONF_KICK_MEMBER;
      return this.httpService.postWithTokenHandlededAndAudio(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }

    public confbridgeListMembers(data:any) { 
      var endpoint: string = this.constService.API_CONF_LIST_MEMBERS;
      return this.httpService.postWithTokenHandlededAndAudio(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }

    public confbridgeListRooms(data:any) { 
      var endpoint: string = this.constService.API_CONF_LIST_ROOMS;
      return this.httpService.postWithTokenHandlededAndAudio(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }

    public confbridgeLock(data:any) { 
      var endpoint: string = this.constService.API_CONF_LOCK_MEMBER;
      return this.httpService.postWithTokenHandlededAndAudio(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }

    public confbridgeMuteMember(data:any) { 
      var endpoint: string = this.constService.API_CONF_MUTE_MEMBER;
      return this.httpService.postWithTokenHandlededAndAudio(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }

    public confbridgeSetSingleVideoSrcMember(data:any) { 
      var endpoint: string = this.constService.API_CONF_SET_SINGLE_VIDEO_SOURCE_MEMBER;
      return this.httpService.postWithTokenHandlededAndAudio(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }


    public confbridgeStartRecord(data:any) { 
      var endpoint: string = this.constService.API_CONF_START_RECORD_MEMBER;
      return this.httpService.postWithTokenHandlededAndAudio(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }


    public confbridgeStopRecord(data:any) { 
      var endpoint: string = this.constService.API_CONF_STOP_RECORD_MEMBER;
      return this.httpService.postWithTokenHandlededAndAudio(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }

    public confbridgeUnlock(data:any) { 
      var endpoint: string = this.constService.API_CONF_UNLOCK_MEMBER;
      return this.httpService.postWithTokenHandlededAndAudio(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }

    public confbridgeUnmuteMember(data:any) { 
      var endpoint: string = this.constService.API_CONF_UNMUTE_MEMBER;
      return this.httpService.postWithTokenHandlededAndAudio(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }

    public publicConferenceRequest(data:any) { 
      var endpoint: string = this.constService.API_CONF_PUBLIC;
      return this.httpService.postWithTokenHandlededAndAudio(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }


    public customConferenceRequest(data:any) { 
      var endpoint: string = this.constService.API_CREATE_CONFERENCE;
      return this.httpService.postWithTokenHandlededAndAudio(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }


    public disconnectAfterXSeconds(data:any) { 
      var endpoint: string = this.constService.API_DISCONNECT_CALL_AFTER_X_SECONDS;
      return this.httpService.postWithTokenHandlededAndAudio(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }


    public extensionState(data:any) { 
      var endpoint: string = this.constService.API_EXTENSION_STATE;
      return this.httpService.postWithTokenHandlededAndAudio(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }


    public hungUpCall(data:any) { 
      var endpoint: string = this.constService.API_HUNG_UP_CALL;
      return this.httpService.postWithTokenHandlededAndAudio(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }

    public listenQuietly(data:any) { 
      var endpoint: string = this.constService.API_LISTEN_QUIETLY;
      return this.httpService.postWithTokenHandlededAndAudio(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }

    public monitorAction(data:any) { 
      var endpoint: string = this.constService.API_MONITOR_ACTION;
      return this.httpService.postWithTokenHandlededAndAudio(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }


    public originateCall(data:any) { 
      var endpoint: string = this.constService.API_ORIGINATE_CALL;
      return this.httpService.postWithTokenHandlededAndAudio(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }


    public originateDataCall(data:any) { 
      var endpoint: string = this.constService.API_ORIGINATE_DATA_CALL;
      return this.httpService.postWithTokenHandlededAndAudio(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }


    public parkForTimeOut(data:any) { 
      var endpoint: string = this.constService.API_PARK_FOR_TIMEOUT;
      return this.httpService.postWithTokenHandlededAndAudio(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }


    public pauseMonitorAction(data:any) { 
      var endpoint: string = this.constService.API_PAUSE_MONITOR_ACTION;
      return this.httpService.postWithTokenHandlededAndAudio(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }


    public requestStateForAllAgents(data:any) { 
      var endpoint: string = this.constService.API_REQUEST_STATE_FOR_ALL_AGENTS;
      return this.httpService.postWithTokenHandlededAndAudio(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }

    public sendAnonymousTextToChannel(data:any) { 
      var endpoint: string = this.constService.API_SEND_ANONYMOUS_TEXT_TO_CHANNEL;
      return this.httpService.postWithTokenHandlededAndAudio(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }


    public statusForSpecificChannel(data:any) { 
      var endpoint: string = this.constService.API_STATUS_FOR_CHANNEL;
      return this.httpService.postWithTokenHandlededAndAudio(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }

    public stopMonitorAction(data:any) { 
      var endpoint: string = this.constService.API_STOP_MONITOR_ACTION;
      return this.httpService.postWithTokenHandlededAndAudio(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }

    public unpauseMonitorAction(data:any) { 
      var endpoint: string = this.constService.API_UNPAUSE_MONITOR_ACTION;
      return this.httpService.postWithTokenHandlededAndAudio(this.constService.API_BASE_ENDPOINT + endpoint,data); 
    }
}
