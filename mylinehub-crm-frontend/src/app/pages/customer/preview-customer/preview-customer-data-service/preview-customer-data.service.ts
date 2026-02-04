import { Injectable } from '@angular/core';
import { Subject, takeUntil } from 'rxjs';
import { ProductService } from '../../../product/service/product.service';
import { ConstantsService } from '../../../../service/constants/constants.service';
import { CustomerService } from '../../service/customer.service';
import { NbDialogService } from '@nebular/theme';
import { DialogComponent } from '../../../employee/all-employees/dialog/dialog.component';

@Injectable({
  providedIn: 'root'
})
export class PreviewCustomerDataService {

  private destroy$: Subject<void> = new Subject<void>();
  
  currentRecord : any = {
    "business": "No Data",
    "city": "No Data",
    "country": "No Data",
    "coverted": false,
    "cronremindercalling": "No Data",
    "datatype": "No Data",
    "description": "No Data",
    "domain": "No Data",
    "email": "No Data",
    "firstname": "No Data",
    "id": -1,
    "imageData": "No Data",
    "imageName": "No Data",
    "imageType": "No Data",
    "iscalledonce": true,
    "lastname": "No Data",
    "organization": "No Data",
    "pesel": "No Data",
    "phoneContext": "No Data",
    "phoneNumber": "No Data",
    "remindercalling": true,
    "zipCode": "No Data",
    "interestedProducts":[]
  };

  dateReceived : any = "No Data";
  completeMessage = null;
  organization : string;

  interestedProducts:any = [];
  base64InterestedProductImageData:any=[];
  showInterestedProducts: any = true;
  interestedProductsLength = 0;

  currentByteImageData:any=null;
  base64ImageData:any=null;


  constructor(private productService : ProductService,
              private constantService : ConstantsService,
              private customerService : CustomerService,
              private dialogService: NbDialogService
            ) { 

              //console.log(ConstantsService.user);
              this.organization = localStorage.getItem("organization");
            }


  public setData(message:any)
  {
      let data = null;
      // console.log(JSON.parse(JSON.stringify(data)));
       //console.log(JSON.parse(JSON.stringify(data)).parkedchannel2);
       if(message == null)
        {
          // console.log("data is null");
          // this.showDialoge('Error','activity-outline','danger', "Customer not found for this email"); 
        }
        else
        {

          try{
            // console.log("data is not null");
            this.completeMessage = message;
  
                // private String autodialertype;
                // private Long campginId;
                // private String campginName;
                // private CustomerDTO customer;
                // private boolean remindercalling;
            
            let interimMessage:any = JSON.parse(message.body).message;

            interimMessage = JSON.parse(interimMessage);
            // console.log("interimMessage : ",interimMessage);
            this.dateReceived = interimMessage.currentDate;

            let date = new Date(this.dateReceived);
            this.dateReceived=date.toLocaleString();
            
            // console.log("dateReceived : ",this.dateReceived);  
            this.currentRecord =interimMessage.customer;
            // console.log("this.currentRecord : "+this.currentRecord);
            // this.currentImage = "data:image/png;base64,"+ JSON.parse(JSON.stringify(data)).imageData;
            this.setImageData();
            this.setProductInterestType();
           
          }
          catch(err)
          {
              console.log(err);
          }
        }
  }


  setProductInterestType()
  {

    // console.log("setProductInterestType this.currentRecord ", this.currentRecord);

    let data:any;
    if(JSON.parse(JSON.stringify(this.currentRecord)).interestedProducts != null )
    {
      data = {
        ids: JSON.parse(JSON.stringify(this.currentRecord)).interestedProducts.split(',').map(list => Number(list)),
        extension:this.currentRecord.extension,
        organization:this.organization
      }

      this.productService.getAllProductsByIdIn(data)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: allData => {
          // console.log("setAutodialerType");
          // console.log(JSON.stringify(allData));
          if(allData == null || JSON.parse(JSON.stringify(allData)).length ==0)
          {
            //console.log("all data is null");
            // console.log("Does not have products interested in")
            this.interestedProducts = [];
            this.showInterestedProducts = false;
          }
          else{
            // console.log("Has product interests")
            this.interestedProducts = [... JSON.parse(JSON.stringify(allData))];
            this.showInterestedProducts = true;
            this. interestedProductsLength = this.interestedProducts.length;

            this.interestedProducts.forEach((current:any,i:number)=>
            {
                try{
                      if(JSON.parse(JSON.stringify(current)).imageByteData != null)
                      {
                        let url = 'data:image/'+current.imageType+';base64,'+current.imageByteData;
                        this.base64InterestedProductImageData.push(url);
                      }
                      this.base64InterestedProductImageData[i] = this.base64InterestedProductImageData[i].replace("/image","");
                }
                catch(e)
                {
                  if((this.base64InterestedProductImageData.length-1)<i)
                  {
                    this.base64InterestedProductImageData.push(null);
                  }   
                }    
            });
  
          }
  
        },
        error: err => {
         console.log("Error : "+ JSON.stringify(err));
          // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
        }
      });

    }
    else{
      this.showInterestedProducts = false;
    }
  }

  setImageData()
  {
    this.customerService.getCustomerImage(this.currentRecord.phoneNumber,this.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: (allData:any) => {

        if(allData == null)
        {
          //  console.log("I am null data");
            this.currentByteImageData = null; 
            this.base64ImageData=null
        }
        else
        {
            this.currentByteImageData = allData; 
            try{
              if(this.currentByteImageData != null)
              {
                // console.log("this.currentByteImageData is not null");
                //  let uints = new Uint8Array(bytes);
                //  let base64 = btoa(String.fromCharCode(null,... uints));

                 let url = 'data:image/'+this.currentByteImageData.type+';base64,'+this.currentByteImageData.byteData;
                 this.base64ImageData = url;
                 this.base64ImageData = this.base64ImageData.replace("/image","");

                //  console.log("this.base64ImageData : ",this.base64ImageData);

              }
              else{
              }
            }
            catch(e)
            {
              // console.log(e);
              this.base64ImageData = null;  
            }

        }
      },
      error: err => {
       console.log("Error : "+ JSON.stringify(err));
        // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });
  }

  
  showDialoge(header: string,icon: string,status: string, message:string) {

    this.dialogService.open(DialogComponent, {
      context: {
        title: status,
        data: message,
        header: header,
        icon: icon
      },
    });
    }

}
