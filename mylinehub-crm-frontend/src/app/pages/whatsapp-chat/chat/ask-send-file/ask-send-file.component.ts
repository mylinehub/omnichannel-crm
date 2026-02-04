import { Component, Input, OnInit } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';
import { Subject } from 'rxjs';
import { WhatsappDataServiceService } from '../whatsapp-data-service/whatsapp-data-service.service';

@Component({
  selector: 'ngx-ask-send-file',
  templateUrl: './ask-send-file.component.html',
  styleUrls: ['./ask-send-file.component.scss']
})
export class AskSendFileComponent implements OnInit {

  private destroy$: Subject<void> = new Subject<void>();
  @Input() message: String;
  
  constructor(protected ref: NbDialogRef<AskSendFileComponent>,
               protected whatsappDataServiceService:WhatsappDataServiceService,
  ) { }

  ngOnInit(): void {
  }

  yesPressed()
  {
    this.dismiss('yes');
  }

  noPressed()
  {

    this.dismiss('no');
  }

  dismiss(value:String) {
    this.ref.close(value);
  }

}


