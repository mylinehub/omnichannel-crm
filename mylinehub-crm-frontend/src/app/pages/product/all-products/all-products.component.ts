import { Component, OnDestroy, OnInit } from '@angular/core';
import { ProductService } from './../service/product.service';
import { takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs';
import { LocalDataSource } from 'ng2-smart-table';
import {
  ExportAsService,
  ExportAsConfig,
  SupportedExtensions,
} from 'ngx-export-as';
import { NbToastrService,NbThemeService,NbStepChangeEvent,NbDialogService } from '@nebular/theme';
import { takeWhile } from 'rxjs/operators' ;
import { Router } from '@angular/router';
import { ConstantsService } from './../../../service/constants/constants.service';
import { DialogComponent } from '../../employee/all-employees/dialog/dialog.component';
import { CustomInputTableComponent } from '../../employee/all-employees/custom-input-table/custom-input-table.component';

interface CardSettings {
  title: string;
  iconClass: string;
  type: string;
}



@Component({
  selector: 'ngx-all-products',
  templateUrl: './all-products.component.html',
  styleUrls: ['./all-products.component.scss']
})
export class AllProductsComponent implements OnInit, OnDestroy {
  
  tableHeading = 'All Products';
  private destroy$: Subject<void> = new Subject<void>();
  downloadAs: SupportedExtensions = 'png';

  file: File = null;
  // currentImage : any;
  
  stepTwoNextButton = true;
  stepThreeNextButton = true;

  currentRecord:any = ' ';
  previousRecord:any = ' ';
  allRecords:any = [];

  //Step2
  keys: any = [];
  values: any = [];
  selectedOption;

  //Step3
  inputs: any = [];
  types: any = [];
  inputValues: any = ['','',''];
  organization = '';

  single = [
    {
      name: 'Products',
      value: 0,
    },
  ];

  colorScheme: any;
  themeSubscription: any;

  changeEvent: NbStepChangeEvent;
  private alive = true;
  

  view :any = [600, 200];
  gradient: boolean = false;
  on = true;
  solarValue: number;
 
  statusCards: string;

  commonStatusCardsSet: CardSettings[] = [
  ];

  statusCardsByThemes: {
    default: CardSettings[];
    cosmic: CardSettings[];
    corporate: CardSettings[];
    dark: CardSettings[];
  } = {
    default: this.commonStatusCardsSet,
    cosmic: this.commonStatusCardsSet,
    corporate: [
    ],
    dark: this.commonStatusCardsSet,
  };


  redirectDelay: number = 0;
  showDetail : boolean = false;
  showAction : boolean = false;
  showPicture : boolean = false;

  settings = {
    add: {
      addButtonContent: '<i class="nb-plus"></i>',
      createButtonContent: '<i class="nb-checkmark"></i>',
      cancelButtonContent: '<i class="nb-close"></i>',
      confirmCreate: true
    },
    pager: {
      display: true,
      perPage: 5
    },
    edit: {
      editButtonContent: '<i class="nb-edit"></i>',
      saveButtonContent: '<i class="nb-checkmark"></i>',
      cancelButtonContent: '<i class="nb-close"></i>',
      confirmSave: true
    },
    delete: {
      deleteButtonContent: '<i class="nb-trash"></i>',
      confirmDelete: true,
    },

    columns: {
      id: {
        title: 'ID',
        type: 'number',
        editable: false,
        addable: false,
      },
      name: {
        title: 'Name',
        type: 'custom',
        valuePrepareFunction: (cell,row) => {
                // DATA FROM HERE GOES TO renderComponent
                console.log('valuePrepareFunction');
                console.log(cell);
                return cell;
        },
        renderComponent: CustomInputTableComponent,
      },
      imageData: {
        title: 'Image Data',
        type: 'string',
        hide: true
      },
      imageName: {
        title: 'Image Name',
        type: 'string',
        hide: true
      },
      imageType: {
        title: 'Image Type',
        type: 'string',
        hide: true
      },
      productStringType: {
        title: 'Product Type',
        type: 'custom',
        valuePrepareFunction: (cell,row) => {
                // DATA FROM HERE GOES TO renderComponent
                console.log('valuePrepareFunction');
                console.log(cell);
                return cell;
        },
        renderComponent: CustomInputTableComponent,
      },
      productType: {
        title: 'Product Type Old',
        type: 'string',
        hide: true
      },
      units: {
        title: 'Measure Unit',
        type: 'custom',
        valuePrepareFunction: (cell,row) => {
                // DATA FROM HERE GOES TO renderComponent
                console.log('valuePrepareFunction');
                console.log(cell);
                return cell;
        },
        renderComponent: CustomInputTableComponent,
      },
      purchasePrice: {
        title: 'Purchase Price (INR)',
        type: 'number',
      },
      sellingPrice: {
        title: 'Selling Price (INR)',
        type: 'number',
      },
      taxRate: {
        title: 'Tax Rate (%)',
        type: 'number',
      },
      organization: {
        title: 'Organization',
        type: 'string',
        hide:true,
      }
    },
  };

 exportAsConfig: ExportAsConfig = {
        type: 'xlsx', // the type you want to download
        elementIdOrContent: 'lastTable', // the id of html/table element
      };

  radioOptions = [
    { value: '1-Column Search', label: '1-Property Search', checked: true },
    { value: '2-Column Search', label: '2-Property Search', disabled: true  },
    { value: '3-Column Search', label: '3-Property Search', disabled: true },
  ];

  radioOption;

  allDropDownOptions = [
    { column: 1, label: 'Product Type', properties:['productStringType']},
  ];

  dropDownOptions = [
    { column: 1, label: 'Product Type', properties:['productStringType']},
  ];
  currentDropDownOption: any;
  dropDownOption;

  source: LocalDataSource = new LocalDataSource();
  currentByteImageData:any=null;
  base64ImageData:any=null;
  
  constructor(private productService : ProductService,
              private exportAsService: ExportAsService,
              private themeService: NbThemeService,
              private nbToastrService:NbToastrService,
              protected router: Router,
              protected constantService : ConstantsService,
              private dialogService: NbDialogService) {

                //console.log("I am in constructor");

                if(localStorage.getItem("organization")!=null)
                 {
                  this.organization = localStorage.getItem("organization");
                 }
                 else{
                  
                  setTimeout(() => {
                    //   console.log('Routing to dashboard page');
                       return this.router.navigateByUrl(this.constantService.LOGIN_ENDPOINT);
                     }, this.redirectDelay);
                }
                
                this.themeService.getJsTheme()
                .pipe(takeWhile(() => this.alive))
                .subscribe(theme => {
                  this.statusCards = this.statusCardsByThemes[theme.name];
                  const colors: any = theme.variables;
                  this.colorScheme = {
                  domain: [colors.primaryLight, colors.infoLight, colors.successLight, colors.warningLight, colors.dangerLight],
                };
              });
            }


  setTable()
  {
    this.productService.getAllproductsByOrganization(this.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: allData => {

        if(allData == null)
        {
          //console.log("I am null data");
          //console.log("Inside Data : "+ JSON.stringify(allData));
            this.source.load([]);   
            this.allRecords = [];

            var output = [
              {
                name: 'Products',
                value: 0,
              },
            ];

            //console.log("After http");    
            this.single=[...output];
        }
        else
        {
            //console.log("Inside Data : "+ JSON.stringify(allData));
              this.source.load(<any[]>allData);  
              var arr = JSON.parse(JSON.stringify(allData));
              //console.log("Inside Data (arr) : "+ JSON.stringify(arr));
              this.allRecords = [...arr];

              var output = [
                {
                  name: 'Products',
                  value: 0,
                },
              ];

              arr.forEach((element) => {
                //console.log("Element"+ JSON.stringify(element));
                output[0].value = output[0].value +1;
              });
              
              //console.log("After http");    
              this.single=[...output];
        }
      },
      error: err => {
       console.log("Error : "+ JSON.stringify(err));
        // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });
  }

  onProductPicturechange (event: any)
  {

    console.log("onProfilePicUpload");
    let lastIndex = event.target.files[0].name.lastIndexOf('.');
    let name = event.target.files[0].name.slice(0, lastIndex);
    let type = event.target.files[0].name.slice(lastIndex + 1);
    let size = event.target.files[0].size;

    // console.log("Name : ",name);
    // console.log("Type : ",type);
    // console.log("Size : ",size);

    if(size >= 12000)
    {
      size = (((size)/1024)/1024).toFixed(2);
      console.log("Size In MB : ",size);
    }
    else
    {
      size = 0.001;
    }

    if(size < 520) 
    {
          // console.log("onProductPicturechange");
          if(event.target.files[0].name.endsWith(".png") ||event.target.files[0].name.endsWith(".PNG") || event.target.files[0].name.endsWith(".JPG") ||event.target.files[0].name.endsWith(".jpg") || event.target.files[0].name.endsWith(".jpeg")|| event.target.files[0].name.endsWith(".JPG") ||event.target.files[0].name.endsWith(".jpg") || event.target.files[0].name.endsWith(".JPEG")) 
            {
        
              this.file = event.target.files[0];
              
              let formParams = new FormData();
              formParams.append('image', event.target.files[0])
        
              this.productService.uploadPicByIdAndOrganization(formParams,this.currentRecord.id,this.organization)
              .pipe(takeUntil(this.destroy$))
              .subscribe({
                next: result => {
                  if(String(result) == 'true')
                      {
                          // this.currentRecord.imageData;
                          event.target.value = "";
                          this.showDialoge('Success','done-all-outline','success', `Product pic upload process is successful.`);
                          // event.confirm.resolve();
                          this.setImageData();
                          }
                      else{
                        //console.log("Result is not true");
                      // event.confirm.reject();
                      this.showDialoge('Unsuccess','done-all-outline','danger', `Product pic upload process is unsuccessful.`);
                      }
                      event.target.value = "";
                },
                error: err => {
                console.log("Error : "+ JSON.stringify(err));
                  // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
                  event.target.value = "";
                }
              });
            }
            else
            {
              //console.log("error");
                this.file = null;
                this.showDialoge('Error','activity-outline','danger', `PNG/JPEG/JPG are supported formats.`); 
                event.target.value = "";
            }
 
    }
    else
    {
        //console.log("error");
        this.showDialoge('Error','activity-outline','danger', `Max size supported is 500KB.`); 
        event.target.value = "";
    }
  }

  onFilechange(event: any) {
   // console.log("onFilechange");
    //console.log(event);
    //console.log(event.target.files[0].name.endsWith(".xlsx"));
    //console.log(event.target.files[0].name.endsWith(".xls"));

    if(event.target.files[0].name.endsWith(".xlsm") || event.target.files[0].name.endsWith(".xlsx") || event.target.files[0].name.endsWith(".xls")) 
    {
      this.file = event.target.files[0];
      
      let formParams = new FormData();
      formParams.append('file', event.target.files[0])

      this.showDialoge('Success','done-all-outline','success', `Upload process is started. It will take some time.`);
      

      event.target.value = "";

      this.productService.upload(formParams,this.organization)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: result => {
          // console.log("Result : "+ JSON.stringify(result));
           this.setTable();
          },
          error: err => {
          console.log("Error : "+ JSON.stringify(err));
            // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
            event.target.value = "";
          }
        });
    }
    else
    {
        this.file = null;
        this.showDialoge('Error','activity-outline','danger', `Only excel can be uploaded`); 
        event.target.value = "";
    }
    
  }

  
  onDeleteConfirm(event): void {
    console.log("User Deleted a row. Row data is  : ");
    //console.log(event);
    
    this.previousRecord =  this.currentRecord;
    this.currentRecord = JSON.parse(JSON.stringify(event.data));

    this.showDetail = false;
    this.showAction = false;
    this.showPicture = false;

    if (window.confirm('Are you sure you want to delete?')) {

   //console.log("Starting Delete API");

   this.productService.deleteProductByIdAndOrganization(this.currentRecord.id,this.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: result => {

        //console.log("API returned result");

        if(String(result) == 'true')
        {
              //console.log("Result is true");
              event.confirm.resolve();
              var output = JSON.parse(JSON.stringify(this.single));
              //console.log("Chart intial value");
             //console.log(output);
             output[0].value = output[0].value - 1;
              
              this.single = [...output];
              //console.log("I am coming out from delete")
              }
        else{
          //console.log("Result is not true");
          event.confirm.reject();
        }
      },
      error: err => {
        console.log("Error : "+ JSON.stringify(err));
        // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
        event.confirm.reject();
      }
    });
    } 
    else {
     // console.log("User rejected delete");
      event.confirm.reject();
    }
  }

  onSaveConfirm(event): void {

    //console.log("User Edited a row. Row data is  : ");
    //console.log(event);
    //console.log(event);

    this.previousRecord =  JSON.parse(JSON.stringify(event.data));
    this.currentRecord = JSON.parse(JSON.stringify(event.newData));
    // this.currentImage = "data:image/png;base64,"+ this.previousRecord.imageData;
    this.currentRecord.imageData = this.previousRecord.imageData;

    var columns = JSON.parse(JSON.stringify(this.settings.columns));
    var keys = [];
    Object.keys(this.currentRecord).map(function (key) { //console.log(key);
      //console.log(key);
      //console.log(columns[key].title);
      keys.push(columns[key].title);

    });
    this.keys = keys;
    this.values = Object.values(this.currentRecord);

    this.showDetail = true;
    this.showAction = true;
    this.showPicture = true;

    if (window.confirm('Are you sure you want to edit?')) {
    
     // console.log("Starting Update API");
      this.productService.updateProductByOrganization(JSON.stringify(this.currentRecord),this.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: result => {

        //console.log("API returned result");

        if(String(result) == 'true')
        {
             event.confirm.resolve();
            }
        else{
          //console.log("Result is not true");
          event.confirm.reject();
        }
      },
      error: err => {
       console.log("Error : "+ JSON.stringify(err));
        // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
        event.confirm.reject();
      }
    });
    } 
    else {
      //console.log("User rejected delete");
      event.confirm.reject();
    }
  }

  onCreateConfirm(event): void {
    //console.log("User Created a row. Row data is  : ");
    //console.log(event);

    this.currentRecord = JSON.parse(JSON.stringify(event.newData));
    this.currentRecord.organization = this.organization;
    
    // this.currentImage = "data:image/png;base64,"+ this.currentRecord.imageData;
    var columns = JSON.parse(JSON.stringify(this.settings.columns));
    var keys = [];
    Object.keys(this.currentRecord).map(function (key) { //console.log(key);
      console.log(key);
      console.log(columns[key].title);
      keys.push(columns[key].title);

    });
    this.keys = keys;
    this.values = Object.values(this.currentRecord);

    this.showDetail = false;
    this.showAction = false;
    this.showPicture = false;

    if (window.confirm('Are you sure you want to create?')) {

      //console.log("Starting Create API");
   
      this.productService.createProductByOrganization(JSON.stringify(this.currentRecord),this.organization)
       .pipe(takeUntil(this.destroy$))
       .subscribe({
         next: result => {
   
           //console.log("API returned result");
   
           if(String(result) == 'true')
           {
                //console.log("Result is true");
                event.confirm.resolve();
                this.setTable();
           }
           else{
             //console.log("Result is not true");
             this.showDialoge('Error','activity-outline','danger', "You should create records for your organization. Record not created."); 
             event.confirm.reject();
           }
         },
         error: err => {
           console.log("Error : "+ JSON.stringify(err));
          //  this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
           event.confirm.reject();
         }
       });
       } 
       else {
         //console.log("User rejected delete");
         event.confirm.reject();
       }
  }

  
  onSearch()
  {
    this.showAction = false;
    this.showDetail = false;
    this.showPicture = false;

   //console.log("On Search is clicked");

    if(this.currentDropDownOption.label == 'Product Type')
    {
      //console.log("Phone Context Search Started");
      this.productService.getAllproductsOnProductTypeAndOrganization(this.inputValues[0],this.organization)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: allData => {
  
          if(allData == null)
          {
           // console.log("I am null data");
            this.source.load([]);   
            this.allRecords = [];

            var output = [
              {
                name: 'Products',
                value: 0,
              },
            ];

            //console.log("After http");    
            this.single=[...output];
          }
          else
          {
              //console.log("Inside Data : "+ JSON.stringify(allData));
          this.source.load(<any[]>allData);   
          var arr = JSON.parse(JSON.stringify(allData));
          //console.log("Inside Data (arr) : "+ JSON.stringify(arr));
          this.allRecords = [...arr];
          var output = [
            {
              name: 'Products',
              value: 0,
            },
          ];

          arr.forEach((element) => {
            //console.log("Element"+ JSON.stringify(element));
            output[0].value = output[0].value +1;
          });
          
          //console.log("After http");    
          this.single=[...output];
          }
        },
        error: err => {
        console.log("Error : "+ JSON.stringify(err));
          // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
        }
      });
    }
  }
  
  getChildData(event){
    
  }

  onUserRowSelect(event): void {
    //console.log("User Selected a row. Row data is  : ");
    //console.log(event);
    this.currentRecord = JSON.parse(JSON.stringify(event.data));
    //console.log(JSON.stringify(this.currentRecord));

    this.productService.getProductByIdAndOrganization(this.currentRecord.id,this.organization)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: data => {
  
          console.log(data);

          if(data == null)
          {
            //console.log("data is null");
            this.showDialoge('Error','activity-outline','danger', "Product not found for this id"); 
          }
          else
          {
            //console.log("data is not null");
            // this.currentImage = "data:image/png;base64,"+ JSON.parse(JSON.stringify(data)).imageData;
            this.setImageData();
          }
        },
        error: err => {
        console.log("Error : "+ JSON.stringify(err));
          // this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
        }
      });

  
      var columns = JSON.parse(JSON.stringify(this.settings.columns));
      var keys = [];
      Object.keys(this.currentRecord).map(function (key) { //console.log(key);
        //console.log(key);
        //console.log(columns[key].title);
        keys.push(columns[key].title);

      });
      this.keys = keys;
      this.values = Object.values(this.currentRecord);
      console.log("assigned value to current record");
      this.showDetail = true;
      this.showAction = true;
      this.showPicture = true;

  }

  
  export() {
    this.exportAsConfig.type = this.downloadAs;
    this.exportAsService
      .save(this.exportAsConfig, 'Products')
      .subscribe(() => {
        // save started
      });
  }


  handleStepChange(e: NbStepChangeEvent): void {
    //console.log('Handle event');
    this.changeEvent = e;
    if(e.index == 2)
    {
      var currentSearch = this.allDropDownOptions.find( record => record.label === this.currentDropDownOption.label);
      var currentProperties = currentSearch.properties;
      var columns = JSON.parse(JSON.stringify(this.settings.columns));
      var inputs = [];
      var types = [];
      var count =0;
      //console.log(currentSearch)
      //console.log(currentProperties)
      //console.log(columns)

      currentProperties.forEach( (element) => {
        inputs.push(columns[element].title);


        if(columns[element].hasOwnProperty("editor") && (columns[element].editor.type == 'list' && columns[element].editor.config.list[0].hasOwnProperty("value") && columns[element].editor.config.list[0].value == 'true'))
        {
          types.push('boolean');
          this.inputValues[count] = true;
        }
        else if(columns[element].hasOwnProperty("editor") && columns[element].editor.type == 'list'){
            types.push('string');
            this.inputValues[count] = '';
        }
        else{
          types.push(columns[element].type);
          if(columns[element].type == 'boolean' || (columns[element].type == 'list' && columns[element].config.list[0].value == 'true'))
            {
              this.inputValues[count] = true;
            }
            else{
              this.inputValues[count] = '';
            }
        }
        count = count+ 1;
     });

     this.inputs = inputs;
     this.types = types;

     for (let i = 0; i < this.inputs.length; i++) {

      if(this.types[i] == 'boolean')
      {
        //console.log('I am boolean');
      }
      else{
        if(this.inputValues[i] == '' || this.inputValues[i] == ' ' || this.inputValues[i] == null)
        {
          this.stepThreeNextButton = true;
          break;
        }
      }

      this.stepThreeNextButton = false;
     
    }
   }
   if(e.index == 3)
   {
    //console.log(JSON.stringify(this.inputValues));
   }
  }
  
  bulkUpload(){
    //console.log("Bulk Upload AMI");
    const val = document.getElementById('file-input');

   /* val.addEventListener("change", function() {

    });*/

    val.click();
  }

  picUpload(){
    //console.log("Bulk Upload AMI");
    const val = document.getElementById('pic-input');
    val.click();
  }

  setImageData()
  {
    this.productService.getProductImage(this.currentRecord.id,this.organization)
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

  /*showToast(status: NbComponentStatus, message:string) {
    const toastRef: NbToastRef =  this.nbToastrService.show(status,message, { status });
   // toastRef.close();
  }*/

  onRadioChange(event)
  {
    this.selectedOption = '';
    this.stepTwoNextButton = true;
    var allValues: any = this.allDropDownOptions;
    var requiredValues: any = [];

    if(JSON.stringify(event) == "\"1-Column Search\"")
    {
      allValues.forEach( (element) => {
        if(element.column == 1)
        {
          requiredValues.push(element);
        }
      });
      this.dropDownOptions = requiredValues;
    }
    else if(JSON.stringify(event) == "\"2-Column Search\""){
      allValues.forEach( (element) => {
        if(element.column == 2)
        {
          requiredValues.push(element);
        }
      });
      this.dropDownOptions = requiredValues;
    }
    else if(JSON.stringify(event) == "\"3-Column Search\""){
      allValues.forEach( (element) => {
        if(element.column == 3)
        {
          requiredValues.push(element);
        }
      });
      this.dropDownOptions = requiredValues;
    }

  }

  dropDownChange(event)
  {
    //console.log("Drop Down Button changed");
    this.currentDropDownOption = this.dropDownOptions[event];
    this.stepTwoNextButton = false;
  }

 stepInputChange(event)
  {
    //console.log('stepInputChange');
    for (let i = 0; i < this.inputs.length; i++) {
      //console.log(this.inputValues[i]);

      if(this.types[i] == 'boolean')
      {

      }
      else{
        if(this.inputValues[i] == '' || this.inputValues[i] == ' ' || this.inputValues[i] == null)
        {
         // console.log('break');
          this.stepThreeNextButton = true;
          break;
        }  
      }
     
     // console.log('passed');
      this.stepThreeNextButton = false;
    }
  }

  valueChanged(event){
    //console.log('date value changed');
    var track = 0;

    for (let i = 0; i < this.inputs.length; i++) {

      if(this.types[i] == 'boolean')
      {
       // console.log('valueChanged : boolean');
      }
      else{
        if(this.inputValues[i] == '' || this.inputValues[i] == ' ' || this.inputValues[i] == null)
        {
          if(track==0)
          { // Do Nothing
           // console.log('track0');
          }
          else{
            track = 1;
            this.stepThreeNextButton = true;
            break;
          } 
        }
      }
      this.stepThreeNextButton = false;
    }
  }

  ngOnInit(){
    // console.log("I am in ngOnIt");
    if(ConstantsService.user.firstName !== undefined){
               console.log("Loading empoloyee data");
               this.setupNgOnInitData();
          }
      else{
                setTimeout(() => {
                  console.log("Delay employee load due to unavailability of data");
                  this.setupNgOnInitData();
                }, ConstantsService.DIRECT_REFRESH_PAGE_TIME_MS); // 2000 milliseconds = 2 seconds
          }
  }

  setupNgOnInitData(){

    if(ConstantsService.user.role === ConstantsService.employee)
      {
        document.getElementById("downloadMainTableButton").hidden = true;
      }

    this.setTable();  
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.themeSubscription) this.themeSubscription.unsubscribe();
  }
}

function getBase64(file) {

  //console.log("inside base 64");
  return new Promise((resolve, reject) => {
    //console.log("creating promise");
    const reader = new FileReader();
    //console.log("reading file");
    reader.readAsDataURL(file);
   // console.log("on load if resolved");
    reader.onload = () => resolve(reader.result);

   // console.log("reject on error");
    reader.onerror = error => reject(error);
  });
}