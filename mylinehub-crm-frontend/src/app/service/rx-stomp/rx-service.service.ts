import { Injectable } from '@angular/core';
import { RxStomp } from '@stomp/rx-stomp';

@Injectable({
  providedIn: 'root'
})
export class RxServiceService extends RxStomp {

  constructor() {
    // console.log("Stomp service Constructor")
    super();
    // console.log("Stomp service Constructor after super")
  }
}
