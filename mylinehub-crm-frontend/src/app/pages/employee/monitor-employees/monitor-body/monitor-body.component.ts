import { Component, Input, OnInit } from '@angular/core';
import { HeaderVariableService } from '../../../../service/header-variable/header-variable.service';

@Component({
  selector: 'ngx-monitor-body',
  templateUrl: './monitor-body.component.html',
  styleUrls: ['./monitor-body.component.scss']
})
export class MonitorBodyComponent implements OnInit {

  @Input() record:any;
  @Input() type:any;
  @Input() status:any;
  recording:any = false;
  callStarted:any = false;

  constructor(protected headerVariableService:HeaderVariableService,) { }

  ngOnInit(): void {
    // console.log('MonitorBodyComponent');
    // console.log(this.record); 
    // console.log(this.type); 
    // console.log(this.status); 
  }


  get hasButtons(): boolean {
    return (this.type === 'engaged' || (this.type === 'idle'));
  }

  bridgeConference(){

  }

  quietly(){
    this.callStarted = false;
  }

  hungUpCall(){
    
  }

  startRecord(){

  }

  stopRecord(){

  }

  makeAudioCall(){

  }

  makeVideoCall(){

  }

}
