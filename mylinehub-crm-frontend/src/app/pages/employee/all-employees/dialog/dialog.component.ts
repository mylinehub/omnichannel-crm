import { Component, Input, OnInit } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';


@Component({
  template: `
    <nb-card class="dialog-card" status = "{{ title }}">
      <nb-card-header><span>&nbsp;</span><nb-icon icon="{{ icon }}"></nb-icon><span>&nbsp;&nbsp;</span>{{ header }}</nb-card-header>
      <nb-card-body class="dialog-card-body-font-size">
        {{ data }}
      </nb-card-body>
      <nb-card-footer>
        <button nbButton outline status="{{ title }}" (click)="dismiss()">Close</button>
      </nb-card-footer>
    </nb-card>
  `,
})
export class DialogComponent implements OnInit {
  @Input() title: string;
  @Input() data: string;

  @Input() header: string;
  @Input() icon: string;

  constructor(protected ref: NbDialogRef<DialogComponent>) {

  }
  ngOnInit(): void {
    
    //console.log("Error Original String :"+this.data);

    const trimmed = this.data.trim();

      // Try to parse only if it looks like JSON (starts with '{' and ends with '}')
      if ((trimmed.startsWith('{') && trimmed.endsWith('}')) ||( trimmed.startsWith('[') && trimmed.endsWith(']'))) {
        try {

          let doneSkim:boolean = false;
          console.log("Inside json error format correction");

          const json = JSON.parse(trimmed);
          console.log("json");
          console.log(json);

          try {
            // JSON parsing + logic
                if (json && typeof json === 'object' && 'error' in json) {
                    const parsedData = json.error;
                    console.log("Error is present");
                    console.log(parsedData);
                    if(parsedData && typeof parsedData === 'object' && 'message' in parsedData){
                      console.log("Pulled message inide error");
                      doneSkim = true;
                      this.data = parsedData.message;
                    }
                    else{
                          console.log("Did not pull message inide error");
                    }
                }
                else{
                    console.log("Did not find error");
                }

          } catch (e) {
            // Parsing failed — keep data as-is
            console.log("found error in first check error --> message");
          }


          console.log("json again");
          console.log(json);
          console.log("doneSkim : "+ doneSkim);
          if(json && typeof json === 'object' && 'message' in json){

            console.log("Fetching message directly. Lets check doneSkim");

            if(!doneSkim){
              console.log("Pulled message directly");
              doneSkim = true;
              this.data = json.message;
            }
            else{
              console.log("doneSkim is true");
            }
          }

        } catch (e) {
          // Parsing failed — keep data as-is
          console.log(e);
        }
      }

    //console.log("Error Converted String :"+this.data);
  }

  dismiss() {
    this.ref.close();
  }
}