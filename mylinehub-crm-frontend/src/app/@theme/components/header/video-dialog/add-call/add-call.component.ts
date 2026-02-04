import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'ngx-add-call',
  templateUrl: './add-call.component.html',
  styleUrls: ['./add-call.component.scss']
})
export class AddCallComponent implements OnInit {

  private destroy$: Subject<void> = new Subject<void>();
  dialValue:any = "";
  
  constructor(protected ref: NbDialogRef<AddCallComponent>) { }

  ngOnInit(): void {
  }

  call()
  {
    this.dismiss("tryingCall");
  }

  cancel()
  {

    this.dismiss(undefined);
  }

  dismiss(value:String) {
    this.ref.close(value);
  }

}
