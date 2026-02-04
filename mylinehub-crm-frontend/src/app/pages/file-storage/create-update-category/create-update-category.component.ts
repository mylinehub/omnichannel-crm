import { Component, Input, OnInit } from '@angular/core';
import { NbDialogRef, NbDialogService } from '@nebular/theme';
import { FileStorageService } from '../service/file-storage.service';
import { DialogComponent } from '../../employee/all-employees/dialog/dialog.component';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'ngx-create-update-category',
  templateUrl: './create-update-category.component.html',
  styleUrls: ['./create-update-category.component.scss']
})
export class CreateUpdateCategoryComponent implements OnInit {
  private destroy$: Subject<void> = new Subject<void>();
  @Input() type: string;
  @Input() fileStorageService: FileStorageService;
  @Input() storeType: string;
  name:any = "";
  oldName:any = "";
  businessType:any = "";
  imageFile:any;
  formParams:FormData = null;
  namePrefix:any = "";
  folderName:any;
  folderBusinessType:any;
  
  constructor(protected ref: NbDialogRef<CreateUpdateCategoryComponent>,
              private dialogService: NbDialogService,
             ) {
  }
  
  ngOnInit(): void {

    if(this.type == "Update")
    {
        // console.log("this.fileStorageService.currentCategory : ",this.fileStorageService.currentCategory);

        let completeName = this.fileStorageService.currentCategory.name;

        // console.log("completeName : ",completeName);

        if(String(this.fileStorageService.currentCategory.name).includes("/"))
          {
            // console.log("String include /");
            this.namePrefix = String(completeName).substring(0,String(completeName).lastIndexOf("/")+1);
            // console.log("this.namePrefix : ",this.namePrefix);
            this.name = String(completeName).substring(String(completeName).lastIndexOf("/")+1,String(completeName).length);
            this.oldName = this.name;
            // console.log("this.name : ",this.name);
          }
          else
          {
            // console.log("String does not include /");
            this.namePrefix="";
            this.name = String(completeName);
            this.oldName = this.name;
            // console.log("this.name : ",this.name);
          }
        //Add previous value to name and businessType
        
        this.businessType = this.fileStorageService.currentCategory.businessType;
    }

    // Get the input field
    this.folderName = document.getElementById("folder-name");

    // Execute a function when the user presses a key on the keyboard
    this.folderName.addEventListener("keypress", (event:any)=>{
       this.verifyAlphaNumericConstraint(this.name,event);
    });

    // Get the input field
    this.folderBusinessType = document.getElementById("folder-businesstype");

    // Execute a function when the user presses a key on the keyboard
    this.folderBusinessType.addEventListener("keypress", (event:any)=>{
       this.verifyAlphaNumericConstraint(this.businessType,event);
    });

    // console.log("OnIt create/update : ",JSON.stringify(this.fileStorageService.allFolders));

  }

  verifyAlphaNumericConstraint(variable:any,event:any)
  {
        let k;  
        k = event.charCode;  //         k = event.keyCode;  (Both can be used)
        // console.log("Input character : ", event);
        // console.log("Input character char code : ",k)
        if((k > 64 && k < 91) || (k > 96 && k < 123) || k == 95 || k == 32 || (k >= 48 && k <= 57))
          {
              //Its fine
          }
        else{
          variable = variable.slice(0, -1) ;
          this.showDialoge('Error','activity-outline','danger', "Only alphabet, number, underscore, space are allowed"); 
        }  
  }

  createOrUpdate(){

    if(this.type == "Update")
    {
        if(this.name != "" && this.businessType != "") 
            {
                let data:any = {
                  name:this.namePrefix+this.name,
                  businessType:this.businessType,
                  organization:localStorage.getItem("organization"),
                };

                if(this.formParams == null)
                  {
                    this.formParams = new FormData();
                  }

                this.formParams.append('data', JSON.stringify(data));
                this.formParams.append('oldName', this.fileStorageService.currentCategory.name);

                if (!this.formParams.has('image'))
                {
                  this.fileStorageService.updateFileCategoryByExtensionAndNameAndOrganizationWithoutImage(this.storeType,this.formParams)
                  .pipe(takeUntil(this.destroy$))
                  .subscribe({
                    next: result => {
  
                      // console.log("Updating Folder with out image");
                      if(String(result) == 'true')
                          {
                            // console.log("Result is true");
                            this.fileStorageService.currentCategory.name = this.namePrefix+this.name;
                            this.fileStorageService.folderLadder = this.fileStorageService.currentCategory.name.split("/");
                            this.fileStorageService.currentCategory.businessType = this.businessType;
                            let length = this.fileStorageService.currentCategoryLadder.length;
                            length = length -1;
                            this.fileStorageService.currentCategoryLadder[length].name = this.namePrefix+this.name;
                            this.fileStorageService.currentCategoryLadder[length].businessType = this.businessType;

                            // console.log("oldName : ",this.oldName);
                            // console.log("name : ",this.name);

                            this.fileStorageService.allFolders.forEach((value:any)=>
                            {
                              value.name = value.name.replace(this.oldName,this.name);
                            });

                            // console.log("Inside update");
                            // console.log("this.fileStorageService.allFolders : ",this.fileStorageService.allFolders);


                            this.dismiss();
                          }
                          else{
                            //console.log("Result is not true");
                          // event.confirm.reject();
                          this.showDialoge('Unsuccess','done-all-outline','danger', `Updating folder is unsuccessful.`);
                          }
                          
                    },
                    error: err => {
                    // console.log("Error : "+ JSON.stringify(err));
                      this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
                    }
                  });
                }
                else
                {
                  this.fileStorageService.updateFileCategoryByExtensionAndNameAndOrganization(this.storeType,this.formParams)
                  .pipe(takeUntil(this.destroy$))
                  .subscribe({
                    next: result => {
  
                      // console.log("Updating Folder with image");
                      if(String(result) == 'true')
                          {
                            // console.log("Result is true");
                            this.fileStorageService.currentCategory.name = this.namePrefix+this.name;
                            this.fileStorageService.folderLadder = this.fileStorageService.currentCategory.name.split("/");
                            this.fileStorageService.currentCategory.businessType = this.businessType;
                            let length = this.fileStorageService.currentCategoryLadder.length;
                            length = length -1;
                            this.fileStorageService.currentCategoryLadder[length].name = this.namePrefix+this.name;
                            this.fileStorageService.currentCategoryLadder[length].businessType = this.businessType;
                            this.fileStorageService.allFolders.forEach((value:any)=>
                              {
                                value.name = value.name.replace(this.oldName,this.name);
                              });
                            this.dismiss();
                          }
                          else{
                            //console.log("Result is not true");
                          // event.confirm.reject();
                          this.showDialoge('Unsuccess','done-all-outline','danger', `Updating folder is unsuccessful.`);
                          }
                          
                    },
                    error: err => {
                    // console.log("Error : "+ JSON.stringify(err));
                      this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
                    }
                  });
                }
            }
            else
            {
                if (this.name == "")
                this.showDialoge('Error','activity-outline','danger', `It is mandatory to set name.`); 
                else if (this.businessType == "")
                this.showDialoge('Error','activity-outline','danger', `It is mandatory to set business type.`); 
            }
    }
    else if(this.type == "Create")
    {
      if(this.formParams != null && this.name != "" && this.businessType != "") 
        {
            if(this.fileStorageService.currentCategory.name != undefined)
              {
                this.namePrefix = this.fileStorageService.currentCategory.name+"/";
              }
            else{
                this.namePrefix = "";
            }

            let data:any = {
              name:this.namePrefix+this.name,
              businessType:this.businessType,
            };

            this.formParams.append('data', JSON.stringify(data));

            this.fileStorageService.createFileCategoryByExtensionAndNameAndOrganization(this.storeType,this.formParams)
            .pipe(takeUntil(this.destroy$))
            .subscribe({
              next: result => {

                // console.log("Creating Folder");
                if(String(result) == 'true')
                    {
                        // console.log("Result is true");
                        let image:any = this.formParams.get('image');
                        let lastIndex = image.name.lastIndexOf('.');
                        let type = image.slice(lastIndex + 1);
                        let reader = new FileReader();

                        reader.onload = () =>{

                            // console.log("Inside Reader Onload");
                            let base64String = String(reader.result).replace("data:", "")
                                          .replace(/^.+,/, "");
                            
                            // console.log("Before: ",JSON.stringify(this.fileStorageService.allFolders));
                            //Get image byte data to set it up here
                            this.fileStorageService.allFolders.unshift({
                                        name:this.namePrefix+this.name,
                                        businessType: this.businessType,
                                        iconImageByteData:base64String,
                                        iconImageType:type
                              });

                            // console.log("After: ",JSON.stringify(this.fileStorageService.allFolders));
                        }
                            
                        // console.log("Reading data as URL");
                        reader.readAsDataURL(image);
                        this.fileStorageService.currentFolderSize = this.fileStorageService.currentFolderSize + 1;
                        this.dismiss();
                    }
                    else{
                      //console.log("Result is not true");
                    // event.confirm.reject();
                    this.showDialoge('Unsuccess','done-all-outline','danger', `Creating folder is unsuccessful.`);
                    }
                    
              },
              error: err => {
              // console.log("Error : "+ JSON.stringify(err));
                this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
              }
            });
        }
        else
        {
            // console.log("this.name : ",this.name);
            // console.log("this.businessType : ",this.businessType);
            // console.log("this.formParams : ",this.formParams);
            if (this.name == "")
              {
                this.showDialoge('Error','activity-outline','danger', `It is mandatory to set name.`); 
              }
            else if (this.businessType == "")
              {
                this.showDialoge('Error','activity-outline','danger', `It is mandatory to set business type.`); 
              }
            else if (this.formParams == null)
              {
                this.showDialoge('Error','activity-outline','danger', `It is mandatory to select an image.`);
              }
        }
    }
  }

  onCategoryImageFileUpload(event)
  {
    // console.log("onCategoryImageFileUpload");

      let lastIndex = event.target.files[0].name.lastIndexOf('.');
      let name = event.target.files[0].name.slice(0, lastIndex);
      let type = event.target.files[0].name.slice(lastIndex + 1);
      let size = event.target.files[0].size;

      // console.log("Name : ",name);
      // console.log("Type : ",type);
      // console.log("Size : ",size);

      if(size >= 12000)
      {
        size = (((size)/1024)).toFixed(2);
        // console.log("Size In KB : ",size);
      }
      else
      {
        size = 0.001;
      }

   if(size <110) 
    {
      if(event.target.files[0].name.endsWith(".png") ||event.target.files[0].name.endsWith(".PNG") || event.target.files[0].name.endsWith(".JPG") ||event.target.files[0].name.endsWith(".jpg") || event.target.files[0].name.endsWith(".jpeg")|| event.target.files[0].name.endsWith(".JPG") ||event.target.files[0].name.endsWith(".jpg") || event.target.files[0].name.endsWith(".JPEG")) 
        {
    
          this.imageFile = event.target.files[0];
          this.formParams = null;
          this.formParams = new FormData();
          this.formParams.append('image', event.target.files[0]);
        }
        else
        {
          //console.log("error");
            this.imageFile = null;
            this.formParams = null;
            this.showDialoge('Error','activity-outline','danger', `PNG/JPEG/JPG are supported formats.`); 
            event.target.value = "";
        }
    }
    else
    {
      //console.log("error");
      this.imageFile = null;
      this.formParams = null;
      this.showDialoge('Error','activity-outline','danger', `Max size supported is 100KB.`); 
      event.target.value = "";
    }
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
    
  dismiss() {
    this.ref.close();
  }

}
