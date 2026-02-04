import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'ngx-ask-delete',
  templateUrl: './ask-delete.component.html',
  styleUrls: ['./ask-delete.component.scss']
})
export class AskDeleteComponent implements OnInit {

  private destroy$: Subject<void> = new Subject<void>();
  @Input() message: String;
  @Input() type: String;
  
  constructor(protected ref: NbDialogRef<AskDeleteComponent>) { }

  ngOnInit(): void {
  }

  yes()
  {
    this.dismiss(this.type);
  }

  no()
  {

    this.dismiss(undefined);
  }

  dismiss(value:String) {
    this.ref.close(value);
  }

}
