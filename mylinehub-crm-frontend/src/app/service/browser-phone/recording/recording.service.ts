import { Injectable } from '@angular/core';
import { HeaderVariableService } from '../../header-variable/header-variable.service';
import { UtilsService } from '../utils/utils.service';
import { VideoDialogDataService } from '../../../@theme/components/header/video-dialog/video-dialog-data-service/video-dialog-data.service';

@Injectable({
  providedIn: 'root'
})
export class RecordingService {

  constructor(protected headerVariableService : HeaderVariableService,
              protected utilsService : UtilsService,
              protected videoDialogDataService:VideoDialogDataService) { }


  

  startRecording(sessionData:any){
    //console.log("I am in startRecording");
    let session = sessionData;
    if(session == null){
        console.warn("Could not find session");
        return;
    }
  
    let id = this.utilsService.uID();
  
    if(!session.data.recordings) session.data.recordings = [];
    session.data.recordings.push({
        uID: id,
        startTime: this.utilsService.utcDateNow(),
        stopTime: this.utilsService.utcDateNow(),
    });
  
    if(session.data.mediaRecorder && session.data.mediaRecorder.state == "recording"){
        console.warn("Call Recording was somehow on... stopping call recording");
        this.stopRecording(session);
        // State should be inactive now, but the data available event will fire
        // Note: potential race condition here if someone hits the stop, and start quite quickly.
    }
    //console.log("Creating call recorder...");
  
    session.data.recordingAudioStreams = new MediaStream();
    let pc = session.sessionDescriptionHandler.peerConnection;
    pc.getSenders().forEach((RTCRtpSender:any) =>{
        if(RTCRtpSender.track && RTCRtpSender.track.kind == "audio") {
            //console.log("Adding sender audio track to record:", RTCRtpSender.track.label);
            session.data.recordingAudioStreams.addTrack(RTCRtpSender.track);
        }
    });
    pc.getReceivers().forEach((RTCRtpReceiver:any)=> {
        if(RTCRtpReceiver.track && RTCRtpReceiver.track.kind == "audio") {
            //console.log("Adding receiver audio track to record:", RTCRtpReceiver.track.label);
            session.data.recordingAudioStreams.addTrack(RTCRtpReceiver.track);
        }
    });
  
    // Resample the Video Recording
    if(session.data.withvideo){
        let recordingWidth = 640;
        let recordingHeight = 360;
        let pnpVideSize = 100;
        if(this.headerVariableService.recordingVideoSize == "HD"){
            recordingWidth = 1280;
            recordingHeight = 720;
            pnpVideSize = 144;
        }
        if(this.headerVariableService.recordingVideoSize == "FHD"){
            recordingWidth = 1920;
            recordingHeight = 1080;
            pnpVideSize = 240;
        }

        // Create Canvas
        session.data.recordingCanvas =this.headerVariableService.recordingCanvas;
        session.data.recordingCanvas.width = (this.headerVariableService.recordingLayout == "side-by-side")? (recordingWidth * 2) + 5: recordingWidth;
        session.data.recordingCanvas.height = recordingHeight;
        session.data.recordingContext = session.data.recordingCanvas.getContext("2d");
  
        // Capture Interval
        window.clearInterval(session.data.recordingRedrawInterval);
        session.data.recordingRedrawInterval = window.setInterval(()=>{
  
            // Video Source
            let pnpVideo = this.headerVariableService.localVideo;
  
            let mainVideo = null;
            let validVideos = [];
            let talkingVideos = [];
            let videoContainer:any = this.videoDialogDataService.allConferenceMembers.videoContainer;
            let potentialVideos =  videoContainer.find('video').length;
            if(potentialVideos == 0){
                // Nothing to render
                 //console.log("Nothing to render in this frame")
            }
            else if (potentialVideos == 1){
                mainVideo = videoContainer.find('video')[0];
                //console.log("Only one video element", mainVideo);
            }
            else if (potentialVideos > 1){
                // Decide what video to record
                videoContainer.find('video').each((video:any,i:any)=> {
                    let videoTrack = video.srcObject.getVideoTracks()[0];
                    if(videoTrack.readyState == "live" && video.videoWidth > 10 && video.videoHeight >= 10) {
                        if(video.srcObject.isPinned == true){
                            mainVideo = video;
                            //console.log("Multiple Videos using last PINNED frame");
                        }
                        if(video.srcObject.isTalking == true){
                            talkingVideos.push(video);
                        }
                        validVideos.push(video);
                    }
                });
  
                // Check if we found something
                if(mainVideo == null && talkingVideos.length >= 1){
                    // Nothing pinned use talking
                    mainVideo = talkingVideos[0];
                    //console.log("Multiple Videos using first talking frame");
                }
                if(mainVideo == null && validVideos.length >= 1){
                    // Nothing pinned or talking use valid
                    mainVideo = validVideos[0];
                    //console.log("Multiple Videos using first VALID frame");
                }
            }
  
            // Main Video
            let videoWidth = (mainVideo && mainVideo.videoWidth > 0)? mainVideo.videoWidth : recordingWidth ;
            let videoHeight = (mainVideo && mainVideo.videoHeight > 0)? mainVideo.videoHeight : recordingHeight ;
            if(videoWidth >= videoHeight){
                // Landscape / Square
                let scale = recordingWidth / videoWidth;
                videoWidth = recordingWidth;
                videoHeight = videoHeight * scale;
                if(videoHeight > recordingHeight){
                    let scale = recordingHeight / videoHeight;
                    videoHeight = recordingHeight;
                    videoWidth = videoWidth * scale;
                }
            } 
            else {
                // Portrait
                let scale = recordingHeight / videoHeight;
                videoHeight = recordingHeight;
                videoWidth = videoWidth * scale;
            }
            let offsetX = (videoWidth < recordingWidth)? (recordingWidth - videoWidth) / 2 : 0;
            let offsetY = (videoHeight < recordingHeight)? (recordingHeight - videoHeight) / 2 : 0;
            if(this.headerVariableService.recordingLayout == "side-by-side") offsetX = recordingWidth + 5 + offsetX;
  
            // Picture-in-Picture Video
            let pnpVideoHeight = pnpVideo.videoHeight;
            let pnpVideoWidth = pnpVideo.videoWidth;
            // let pnpVideoHeight = 50;
            // let pnpVideoWidth = 70;
            if(pnpVideoHeight > 0){
                if(pnpVideoWidth >= pnpVideoHeight){
                    let scale = pnpVideSize / pnpVideoHeight;
                    pnpVideoHeight = pnpVideSize;
                    pnpVideoWidth = pnpVideoWidth * scale;
                } 
                else{
                    let scale = pnpVideSize / pnpVideoWidth;
                    pnpVideoWidth = pnpVideSize;
                    pnpVideoHeight = pnpVideoHeight * scale;
                }
            }
            let pnpOffsetX = 10;
            let pnpOffsetY = 10;
            if(this.headerVariableService.recordingLayout == "side-by-side"){
                pnpVideoWidth = pnpVideo.videoWidth;
                pnpVideoHeight = pnpVideo.videoHeight;
            //   let pnpVideoHeight = 50;
            //   let pnpVideoWidth = 70;
                if(pnpVideoWidth >= pnpVideoHeight){
                    // Landscape / Square
                    let scale = recordingWidth / pnpVideoWidth;
                    pnpVideoWidth = recordingWidth;
                    pnpVideoHeight = pnpVideoHeight * scale;
                    if(pnpVideoHeight > recordingHeight){
                        let scale = recordingHeight / pnpVideoHeight;
                        pnpVideoHeight = recordingHeight;
                        pnpVideoWidth = pnpVideoWidth * scale;
                    }
                } 
                else {
                    // Portrait
                    let scale = recordingHeight / pnpVideoHeight;
                    pnpVideoHeight = recordingHeight;
                    pnpVideoWidth = pnpVideoWidth * scale;
                }
                pnpOffsetX = (pnpVideoWidth < recordingWidth)? (recordingWidth - pnpVideoWidth) / 2 : 0;
                pnpOffsetY = (pnpVideoHeight < recordingHeight)? (recordingHeight - pnpVideoHeight) / 2 : 0;
            }
  
            // Draw Background
            session.data.recordingContext.fillRect(0, 0, session.data.recordingCanvas.width, session.data.recordingCanvas.height);
  
            // Draw Main Video
            if(mainVideo && mainVideo.videoHeight > 0){
                session.data.recordingContext.drawImage(mainVideo, offsetX, offsetY, videoWidth, videoHeight);
            }
  
            // Draw PnP
            if(pnpVideo.videoHeight > 0 && (this.headerVariableService.recordingLayout == "side-by-side" || this.headerVariableService.recordingLayout == "them-pnp")){
                // Only Draw the Pnp Video when needed
                session.data.recordingContext.drawImage(pnpVideo, pnpOffsetX, pnpOffsetY, pnpVideoWidth, pnpVideoHeight);
            }
        }, Math.floor(1000/this.headerVariableService.recordingVideoFps));
  
        // Start Video Capture
        session.data.recordingVideoMediaStream = session.data.recordingCanvas.captureStream(this.headerVariableService.recordingVideoFps);
    }
  
    session.data.recordingMixedAudioVideoRecordStream = new MediaStream();
    session.data.recordingMixedAudioVideoRecordStream.addTrack(this.utilsService.mixAudioStreams(session.data.recordingAudioStreams).getAudioTracks()[0]);
    if(session.data.withvideo){
        session.data.recordingMixedAudioVideoRecordStream.addTrack(session.data.recordingVideoMediaStream.getVideoTracks()[0]);
    }
  
    let mediaType = "audio/webm"; // audio/mp4 | audio/webm;
    if(session.data.withvideo) mediaType = "video/webm";
    let options = {
        mimeType : mediaType
    }
    // Note: It appears that mimeType is optional, but... Safari is truly dreadful at recording in mp4, and doesn't have webm yet
    // You you can leave this as default, or force webm, however know that Safari will be no good at this either way.
    // session.data.mediaRecorder = new MediaRecorder(session.data.recordingMixedAudioVideoRecordStream, options);
    session.data.mediaRecorder = new MediaRecorder(session.data.recordingMixedAudioVideoRecordStream);
    session.data.mediaRecorder.data = {}
    session.data.mediaRecorder.data.id = ""+ id;
    session.data.mediaRecorder.data.sessionId = ""+ session.id;
    session.data.mediaRecorder.ondataavailable = (event:any)=> {
        //console.log("Got Call Recording Data: ", event.data.size +"Bytes", 'this.data.id', 'this.data.buddyId', 'this.data.sessionId');
        // Save the Audio/Video file
        this.saveCallRecording(event.data, 'this.data.id', 'this.data.buddyId', 'this.data.sessionId');
    }
  
    //console.log("Starting Call Recording", id);
    session.data.mediaRecorder.start(); // Safari does not support time slice
    session.data.recordings[session.data.recordings.length-1].startTime = this.utilsService.utcDateNow();
  
  //   $("#line-" + lineObj.LineNumber + "-msg").html(this.lang.call_recording_started);
  
   // this.updatelinescroll(lineNum);
  }

  
  saveCallRecording(blob:any, id:any, buddy:any, sessionid:any){
    //console.log("I am in saveCallRecording");
    let indexedDB = window.indexedDB;
    let request = indexedDB.open("CallRecordings", 1);
    request.onerror = (event:any)=> {
        console.error("IndexDB Request Error:", event);
    }
    request.onupgradeneeded = (event:any)=> {
        console.warn("Upgrade Required for IndexDB... probably because of first time use.");
        let IDB = request.result;
  
      //   // Create Object Store
        if(IDB.objectStoreNames.contains("Recordings") == false){
            let objectStore = IDB.createObjectStore("Recordings", { keyPath: "uID" });
            objectStore.createIndex("sessionid", "sessionid", { unique: false });
            objectStore.createIndex("bytes", "bytes", { unique: false });
            objectStore.createIndex("type", "type", { unique: false });
            objectStore.createIndex("mediaBlob", "mediaBlob", { unique: false });
        }
        else {
            console.warn("IndexDB requested upgrade, but object store was in place.");
        }
    }
    request.onsuccess = (event:any)=> {
        //console.log("IndexDB connected to CallRecordings");
  
        let IDB = request.result;
        if(IDB.objectStoreNames.contains("Recordings") == false){
            console.warn("IndexDB CallRecordings.Recordings does not exists, this call recoding will not be saved.");
            IDB.close();
            window.indexedDB.deleteDatabase("CallRecordings"); // This should help if the table structure has not been created.
            return;
        }
        IDB.onerror = (event:any)=> {
            console.error("IndexDB Error:", event);
        }
    
        // Prepare data to write
        let data = {
            uID: id,
            sessionid: sessionid,
            bytes: blob.size,
            type: blob.type,
            mediaBlob: blob
        }
        // Commit Transaction
        let transaction = IDB.transaction(["Recordings"], "readwrite");
        let objectStoreAdd = transaction.objectStore("Recordings").add(data);
        objectStoreAdd.onsuccess = (event:any)=> {
            //console.log("Call Recording Success: ", id, blob.size, blob.type, buddy, sessionid);
        }
    }
  }
  
  
  stopRecording(sessionData:any){
    //console.log("I am in stopRecording");
    let session = sessionData;
    // Called at the end of a call

    if(session.data.mediaRecorder){
        if(session.data.mediaRecorder.state == "recording"){
            //console.log("Stopping Call Recording");
            session.data.mediaRecorder.stop();
            session.data.recordings[session.data.recordings.length-1].stopTime = this.utilsService.utcDateNow();
            window.clearInterval(session.data.recordingRedrawInterval);

           // $("#line-" + lineObj.LineNumber + "-msg").html(this.lang.call_recording_stopped);
           // this.updatelinescroll(lineNum);
        } 
        else{
            console.warn("Recorder is in an unknown state");
        }
    }
    return;
  }



}
