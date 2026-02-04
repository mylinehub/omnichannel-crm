import { Injectable } from '@angular/core';
import { HeaderVariableService } from '../../header-variable/header-variable.service';
import { UtilsService } from '../utils/utils.service';

@Injectable({
  providedIn: 'root'
})
export class VideoService {

  constructor(protected headerVariableService : HeaderVariableService,
              protected utilsService : UtilsService,
            ) { }


      
  switchVideoSource(lang:any,sessionData:any, srcId:any){
    //console.log("I am in switchVideoSource");
    let session = sessionData;
    this.headerVariableService.browserPhoneTitle = lang.switching_video_source;

    let supportedConstraints = navigator.mediaDevices.getSupportedConstraints();
    let constraints = { 
        audio: false, 
        video: { deviceId: {} }
    }
    if(srcId != "default"){
        constraints.video.deviceId = { exact: srcId }
    }

    // Add additional Constraints
    if(supportedConstraints.frameRate && this.headerVariableService.maxFrameRate != "") {
        constraints.video["frameRate"] = this.headerVariableService.maxFrameRate;
    }
    if(supportedConstraints.height && this.headerVariableService.videoHeight != "") {
        constraints.video["height"] = this.headerVariableService.videoHeight;
    }
    if(supportedConstraints.aspectRatio && this.headerVariableService.videoAspectRatio != "") {
        constraints.video["aspectRatio"] = this.headerVariableService.videoAspectRatio;
    }
    session.data.VideoSourceDevice = srcId;
    let pc = session.sessionDescriptionHandler.peerConnection;
    let localStream = new MediaStream();

    navigator.mediaDevices.getUserMedia(constraints).then((newStream:any)=>{
        let newMediaTrack = newStream.getVideoTracks()[0];
        pc.getSenders().forEach((RTCRtpSender) =>{
            if(RTCRtpSender.track && RTCRtpSender.track.kind == "video") {
                //console.log("Switching Video Track : "+ RTCRtpSender.track.label + " to "+ newMediaTrack.label);
                RTCRtpSender.track.stop();
                RTCRtpSender.replaceTrack(newMediaTrack);
                localStream.addTrack(newMediaTrack);
            }
        });
    }).catch((e:any)=>{
        console.error("Error on getUserMedia", e, constraints);
    });

    // Restore Audio Stream is it was changed
    if(session.data.AudioSourceTrack && session.data.AudioSourceTrack.kind == "audio"){
        pc.getSenders().forEach((RTCRtpSender:any) =>{
            if(RTCRtpSender.track && RTCRtpSender.track.kind == "audio") {
                RTCRtpSender.replaceTrack(session.data.AudioSourceTrack).then(()=>{
                    if(session.data.ismute){
                        RTCRtpSender.track.enabled = false;
                    }
                    else {
                        RTCRtpSender.track.enabled = true;
                    }
                }).catch(()=>{
                    //console.error();
                });
                session.data.AudioSourceTrack = null;
            }
        });
    }

  //  Set Preview
  //console.log("Showing as preview...");
  let localVideo = this.headerVariableService.localVideo;
  localVideo.srcObject = localStream;
  localVideo.onloadedmetadata = (e:any)=> {
      localVideo.play();
 }
}

sendVideo(lang:any,sessionData:any, src:any){
    //console.log("I am in sendVideo");
    let session = sessionData;
    // Create Video Object
    let newVideo:any =this.headerVariableService.sharedVideo;
    newVideo.prop("src", src);
    newVideo.off("loadedmetadata");
    newVideo.on("loadedmetadata", ()=> {
        //console.log("Video can play now... ");

        // Resample Video
        let ResampleSize = 360;
        if(this.headerVariableService.videoResampleSize == "HD") ResampleSize = 720;
        if(this.headerVariableService.videoResampleSize == "FHD") ResampleSize = 1080;

        let videoObj = newVideo.get(0);
        let resampleCanvas:any = this.headerVariableService.resampleCanvas;

        let videoWidth = videoObj.videoWidth;
        let videoHeight = videoObj.videoHeight;

        if(videoWidth >= videoHeight){
            // Landscape / Square
            if(videoHeight > ResampleSize){
                let p = ResampleSize / videoHeight;
                videoHeight = ResampleSize;
                videoWidth = videoWidth * p;
            }
        }
        else {
            // Portrait... (phone turned on its side)
            if(videoWidth > ResampleSize){
                let p = ResampleSize / videoWidth;
                videoWidth = ResampleSize;
                videoHeight = videoHeight * p;
            }
        }

        resampleCanvas.width = videoWidth;
        resampleCanvas.height = videoHeight;
        let resampleContext = resampleCanvas.getContext("2d");

        window.clearInterval(session.data.videoResampleInterval);
        session.data.videoResampleInterval = window.setInterval(()=>{
           resampleContext.drawImage(videoObj, 0, 0, videoWidth, videoHeight);
        }, 40); // 25frames per second

        // Capture the streams
        let videoMediaStream = null;
        if('captureStream' in videoObj) {
            videoMediaStream = videoObj.captureStream();
        }
        else if('mozCaptureStream' in videoObj) {
            // This doesn't really work?
            // see: https://developer.mozilla.org/en-US/docs/Web/API/HTMLMediaElement/captureStream
            videoMediaStream = videoObj.mozCaptureStream();
        }
        else {
            // This is not supported??.
            videoMediaStream = videoObj.webkitCaptureStream();
            console.warn("Cannot capture stream from video, this will result in no audio being transmitted.")
        }

        let resampleVideoMediaStream = resampleCanvas.captureStream(25);

        // Get the Tracks
        let videoMediaTrack = resampleVideoMediaStream.getVideoTracks()[0];
        let audioTrackFromVideo = (videoMediaStream != null )? videoMediaStream.getAudioTracks()[0] : null;

        // Switch & Merge Tracks
        let pc = session.sessionDescriptionHandler.peerConnection;
        pc.getSenders().forEach((RTCRtpSender)=> {
            if(RTCRtpSender.track && RTCRtpSender.track.kind == "video") {
                //console.log("Switching Track : "+ RTCRtpSender.track.label);
                RTCRtpSender.track.stop();
                RTCRtpSender.replaceTrack(videoMediaTrack);
            }
            if(RTCRtpSender.track && RTCRtpSender.track.kind == "audio") {
                //console.log("Switching to mixed Audio track on session");
                
                session.data.AudioSourceTrack = RTCRtpSender.track;

                let mixedAudioStream = new MediaStream();
                if(audioTrackFromVideo) mixedAudioStream.addTrack(audioTrackFromVideo);
                mixedAudioStream.addTrack(RTCRtpSender.track);
                let mixedAudioTrack = this.utilsService.mixAudioStreams(mixedAudioStream).getAudioTracks()[0];
                mixedAudioTrack.IsMixedTrack = true;

                RTCRtpSender.replaceTrack(mixedAudioTrack);
            }
        });

        // Set Preview
        //console.log("Showing as preview...");
        let localVideo:any = '$("#line-" + lineNum + "-localVideo").get(0)';
       localVideo.srcObject = videoMediaStream;
        localVideo.onloadedmetadata = (e:any)=> {
           localVideo.play().then(()=>{
                //console.log("Playing Preview Video File");
            }).catch((e:any)=>{
                console.error("Cannot play back video", e);
            });
        }
        // Play the video
        //console.log("Starting Video...");
        videoObj.play();
    });
    //console.log("Video for Sharing created...");
}


shareScreen(lang:any,sessionData:any){
    //console.log("I am in shareScreen");
    let session = sessionData;
    this.headerVariableService.browserPhoneTitle = lang.switching_to_shared_screen;
    let localStream = new MediaStream();
    let pc = session.sessionDescriptionHandler.peerConnection;

    // TODO: Remove legacy ones
    if (navigator.mediaDevices.getDisplayMedia) {
        // EDGE, legacy support
        let screenShareConstraints = { video: true, audio: false }
        navigator.mediaDevices.getDisplayMedia(screenShareConstraints).then((newStream:any)=> {
            //console.log("navigator.getDisplayMedia")
            let newMediaTrack = newStream.getVideoTracks()[0];
            pc.getSenders().forEach((RTCRtpSender)=> {
                if(RTCRtpSender.track && RTCRtpSender.track.kind == "video") {
                    //console.log("Switching Video Track : "+ RTCRtpSender.track.label + " to Screen");
                    RTCRtpSender.track.stop();
                    RTCRtpSender.replaceTrack(newMediaTrack);
                    localStream.addTrack(newMediaTrack);
                }
            });

            // Set Preview
            // ===========
            //console.log("Showing as preview...");
            let localVideo :any = this.headerVariableService.localVideo;
            localVideo.srcObject = localStream;
            localVideo.onloadedmetadata = (e:any)=> {
                localVideo.play();
           }
        }).catch((err:any) =>{
            console.error("Error on getUserMedia");
            console.error(err);
        });
    } 
    else if (navigator.mediaDevices.getDisplayMedia) {
        // New standard
        let screenShareConstraints = { video: true, audio: false }
        navigator.mediaDevices.getDisplayMedia(screenShareConstraints).then((newStream:any)=> {
            //console.log("navigator.mediaDevices.getDisplayMedia")
            let newMediaTrack = newStream.getVideoTracks()[0];
            pc.getSenders().forEach((RTCRtpSender:any) =>{
                if(RTCRtpSender.track && RTCRtpSender.track.kind == "video") {
                    //console.log("Switching Video Track : "+ RTCRtpSender.track.label + " to Screen");
                    RTCRtpSender.track.stop();
                    RTCRtpSender.replaceTrack(newMediaTrack);
                    localStream.addTrack(newMediaTrack);
                }
            });

            // Set Preview
            // ===========
            //console.log("Showing as preview...");
            let localVideo :any=this.headerVariableService.localVideo;
            localVideo.srcObject = localStream;
            localVideo.onloadedmetadata = (e:any)=> {
                localVideo.play();
            }
        }).catch((err:any) =>{
            console.error("Error on getUserMedia");
            console.error(err);
        });
    } 
    else {
        // Firefox, apparently
        let screenShareConstraints:any = { video: { mediaSource: 'screen' }, audio: false }
        navigator.mediaDevices.getUserMedia(screenShareConstraints).then((newStream:any)=> {
            //console.log("navigator.mediaDevices.getUserMedia")
            let newMediaTrack = newStream.getVideoTracks()[0];
            pc.getSenders().forEach((RTCRtpSender:any)=> {
                if(RTCRtpSender.track && RTCRtpSender.track.kind == "video") {
                    //console.log("Switching Video Track : "+ RTCRtpSender.track.label + " to Screen");
                    RTCRtpSender.track.stop();
                    RTCRtpSender.replaceTrack(newMediaTrack);
                    localStream.addTrack(newMediaTrack);
                }
            });

            // Set Preview
            //console.log("Showing as preview...");
            let localVideo:any = this.headerVariableService.localVideo;
            localVideo.srcObject = localStream;
            localVideo.onloadedmetadata = (e:any)=> {
                localVideo.play();
            }
        }).catch((err:any)=> {
            console.error("Error on getUserMedia");
            console.error(err);
        });
    }

    // Restore Audio Stream is it was changed
    if(session.data.AudioSourceTrack && session.data.AudioSourceTrack.kind == "audio"){
        pc.getSenders().forEach((RTCRtpSender:any)=> {
            if(RTCRtpSender.track && RTCRtpSender.track.kind == "audio") {
                RTCRtpSender.replaceTrack(session.data.AudioSourceTrack).then(()=>{
                    if(session.data.ismute){
                        RTCRtpSender.track.enabled = false;
                    }
                    else {
                        RTCRtpSender.track.enabled = true;
                    }
                }).catch((e:any)=>{
                   console.error(e);
                });
                session.data.AudioSourceTrack = null;
            }
        });
    }

}

disableVideoStream(lang:any,sessionData:any){
    //console.log("I am in disableVideoStream");
    let session = sessionData;

    let pc = session.sessionDescriptionHandler.peerConnection;
    pc.getSenders().forEach((RTCRtpSender:any) =>{
        if(RTCRtpSender.track && RTCRtpSender.track.kind == "video") {
            //console.log("Disable Video Track : "+ RTCRtpSender.track.label + "");
            RTCRtpSender.track.enabled = false; //stop();
        }
        if(RTCRtpSender.track && RTCRtpSender.track.kind == "audio") {
            if(session.data.AudioSourceTrack && session.data.AudioSourceTrack.kind == "audio"){
                RTCRtpSender.replaceTrack(session.data.AudioSourceTrack).then(()=>{
                    if(session.data.ismute){
                        RTCRtpSender.track.enabled = false;
                    }
                    else {
                        RTCRtpSender.track.enabled = true;
                    }
                }).catch((e:any)=>{
                    console.error(e);
                });
                session.data.AudioSourceTrack = null;
            }
        }
    });

    // Set Preview
    //console.log("Showing as preview...");
    let localVideo :any= this.headerVariableService.localVideo;
    localVideo.pause();
    localVideo.removeAttribute('src');
    localVideo.load();
    this.headerVariableService.browserPhoneTitle = lang.video_disabled;
}


}
