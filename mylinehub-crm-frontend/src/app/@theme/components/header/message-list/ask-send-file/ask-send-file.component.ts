import { Component, Input, OnInit } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';
import { Subject } from 'rxjs';

@Component({
  selector: 'ngx-ask-send-file',
  templateUrl: './ask-send-file.component.html',
  styleUrls: ['./ask-send-file.component.scss']
})
export class AskSendFileComponent implements OnInit {

  private destroy$: Subject<void> = new Subject<void>();
  @Input() message: String;
  @Input() type: String;
  
  constructor(protected ref: NbDialogRef<AskSendFileComponent>) { }

  ngOnInit(): void {
  }

  directlyToPeer()
  {
    this.dismiss('peer');
  }

  viaServer()
  {

    this.dismiss('server');
  }

  dismiss(value:String) {
    this.ref.close(value);
  }

}

