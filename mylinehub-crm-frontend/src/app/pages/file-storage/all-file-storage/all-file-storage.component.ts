import { Component, HostListener, OnDestroy, OnInit } from '@angular/core';
import { NbDialogRef, NbDialogService, NbMenuService, NbThemeService } from '@nebular/theme';
import { FileStorageService } from '../service/file-storage.service';
import { DialogComponent } from '../../employee/all-employees/dialog/dialog.component';
import { CreateUpdateCategoryComponent } from '../create-update-category/create-update-category.component';
import { Subject, takeUntil, takeWhile } from 'rxjs';
import { HttpEvent, HttpEventType } from '@angular/common/http';
import { saveAs } from 'file-saver';
import { AskDeleteComponent } from '../ask-delete/ask-delete.component';
import { UploadStatusComponent } from '../upload-status/upload-status.component';
import { DownloadStatusComponent } from '../download-status/download-status.component';
import { ConstantsService } from '../../../service/constants/constants.service';
import { Router } from '@angular/router';
import { OrganizationService } from '../../../service/organization/organization.service';

@Component({
  selector: 'ngx-all-file-storage',
  templateUrl: './all-file-storage.component.html',
  styleUrls: ['./all-file-storage.component.scss']
})
export class AllFileStorageComponent implements OnInit, OnDestroy {

  private destroy$: Subject<void> = new Subject<void>();
  fileDeleteIndex: any;
  uploadStatusValuePercent: number = 30;
  fileDeleteName: string;
  private uploadStatusRef: NbDialogRef<UploadStatusComponent>;
  private downloadStatusRef: NbDialogRef<DownloadStatusComponent>;

  folderMaxInteger = 8;
  fileMaxInteger = 15;
  fileTypeMaxInteger = 10;
  screenWidth: number;
  screenHeight: number;
  doNotShowUploadDownloadFeatures: boolean = false;
  whatsAppSupportlink: string;
  folderManagementActionSize : string = "medium";
  fileManagementActionSize : string = "medium";
  itsMobile = false;

  single = [
    {
      name: 'Used',
      value: 23.5,
    },
    {
      name: 'Free',
      value: 26.5,
    },
  ];
  colorScheme: any;
   view :any = [600, 200];
  gradient: boolean = false;
  private alive = true;
  folderClickedOutOfArray: any;
  themeSubscription: any;

  constructor(private dialogService: NbDialogService,
              protected fileStorageService:FileStorageService,
              private themeService: NbThemeService,
              private router: Router,
              private organizationService:OrganizationService) {

                this.whatsAppSupportlink = ConstantsService.whatsAppSupportlink;
                
                 let allowInitialization:boolean = true;
                 console.log("Executing Init after 150ms delay");
                 console.log(this.router.url);

                 if(this.router.url.includes("whatsapp")){
                          this.doNotShowUploadDownloadFeatures = true;
                          this.fileStorageService.storeType = ConstantsService.whatsAppStore;   
                          if(ConstantsService.user.role === ConstantsService.employee)
                            {
                              allowInitialization = false;
                              this.showDialoge('Not-Allowed','activity-outline','danger', "Only admin or manager can view this page.");
                            }
                          else
                            { }
                        }
                        else{
                          this.doNotShowUploadDownloadFeatures = false;
                          this.fileStorageService.storeType = ConstantsService.userStore;
                        }

                        if (allowInitialization){
                          console.log('Initializing Filestore');
                          this.getAllInitialCategories();
                        }
                        console.log("filestore type: "+this.fileStorageService.storeType);

                        this.themeSubscription = this.themeService.getJsTheme()
                                        .pipe(takeWhile(() => this.alive))
                                        .subscribe(theme => {
                                          const colors: any = theme.variables;
                                          this.colorScheme = {
                                          domain: [colors.primaryLight, colors.infoLight, colors.successLight, colors.warningLight, colors.dangerLight],
                                        };
                        });

               }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.themeSubscription) this.themeSubscription.unsubscribe();
  }

  ngOnInit(): void {

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
    this.getScreenSize();
    setTimeout(() => {
          console.log("Setting org data");
          this.setOrganizationStorageData();
       }, 800); // 2000 milliseconds = 2 seconds
  }

  toggleSingleFileCheckBox(file:any,j:number)
   {
      console.log("toggleSingleFileCheckBox");
      console.log("file : ",file);
      console.log("j : ",j);

      let thisTickMarkValue = this.fileStorageService.filteredOptionsList[j].tickMark;

      console.log("thisTickMarkValue : ",thisTickMarkValue);

      let allFileIndex = -1;
      allFileIndex = this.fileStorageService.allFiles.findIndex(obj => obj.name == file.name);

      if(allFileIndex != -1)
        {
          this.fileStorageService.allFiles[allFileIndex].tickMark = !thisTickMarkValue;

          if(this.fileStorageService.allFiles[allFileIndex].tickMark)
            {
              this.fileStorageService.bulkDelete = true;
              this.fileStorageService.bulkDownload = true;
              this.fileStorageService.totalDownloadSize = this.fileStorageService.totalDownloadSize + this.fileStorageService.allFiles[allFileIndex].size;
              console.log("************************* Total Size *************************");
              console.log(this.fileStorageService.totalDownloadSize);
            }
          else{

            this.fileStorageService.totalDownloadSize = this.fileStorageService.totalDownloadSize - this.fileStorageService.allFiles[allFileIndex].size;

            let downBulkdownloadDelete = true;
            for(let i=0; i< this.fileStorageService.allFiles.length;i++)
            {
              if(this.fileStorageService.allFiles[i].tickMark){
                downBulkdownloadDelete = false;
                break;
              }
            };

            if(downBulkdownloadDelete)
              {
                this.fileStorageService.bulkDelete = false;
                this.fileStorageService.bulkDownload = false;
                this.fileStorageService.totalDownloadSize = 0;
              }

            console.log("************************* Total Size *************************");
            console.log(this.fileStorageService.totalDownloadSize);

          }
          // this.fileStorageService.filteredOptionsList[j].tickMark = !this.fileStorageService.filteredOptionsList[j].tickMark;
        }
      else{
        this.showDialoge('Error','activity-outline','danger', "File not found. Refresh Please");
      }
   }

  toggleMultipleFileCheckBox()
   {
        console.log("toggleMultipleFileCheckBox");
        console.log("this.fileStorageService.tickMarkAll : ",this.fileStorageService.tickMarkAll);
        let value = this.fileStorageService.tickMarkAll;

        if(!value)
          {
            this.fileStorageService.allFiles.forEach((element:any) => {
              element.tickMark = false;
            });

            this.fileStorageService.totalDownloadSize = 0;

            this.fileStorageService.filteredOptionsList.forEach((element:any) => {
         
              if(!this.fileStorageService.bulkDelete)
                {
                  this.fileStorageService.bulkDelete = true;
                }
              
              if(!this.fileStorageService.bulkDownload)
                {
                    this.fileStorageService.bulkDownload = true;
                }

              element.tickMark = true;
              let allFileIndex = -1;
              allFileIndex = this.fileStorageService.allFiles.findIndex(obj => obj.name == element.name);
  
              if(allFileIndex != -1)
                {
                  this.fileStorageService.allFiles[allFileIndex].tickMark = true;
                  this.fileStorageService.totalDownloadSize = this.fileStorageService.totalDownloadSize + this.fileStorageService.allFiles[allFileIndex].size;
                }
              else{
                this.showDialoge('Error','activity-outline','danger', "File may not deleted. Refresh please.");
              }        
    
            });

            console.log("************************* Total Size *************************");
            console.log(this.fileStorageService.totalDownloadSize);

          }
        else{

          if(this.fileStorageService.bulkDelete)
            {
              this.fileStorageService.bulkDelete = false;
            }
          
          if(this.fileStorageService.bulkDownload)
            {
                this.fileStorageService.bulkDownload = false;
            }

          this.fileStorageService.filteredOptionsList.forEach((element:any) => {
            element.tickMark = false;
          });

          this.fileStorageService.allFiles.forEach((element:any) => {
            element.tickMark = false;
          });

          this.fileStorageService.totalDownloadSize = 0;

          console.log("************************* Total Size *************************");
          console.log(this.fileStorageService.totalDownloadSize);

        }  
   }

  bulkDownload()
   {
        //Call bulk download api
       this.fileStorageService.downloadSubscription = this.fileStorageService.downloadMultipleUserFileByOrganizationAndCategory(this.fileStorageService.storeType)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: event => {
          console.log("************************* Multiple File download event *************************");
          console.log(event);
          this.reportProgress(event,0,-2,0);
        },
        error: err => {
        console.log("Error : "+ JSON.stringify(err));
          //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
        }
      });
   }

  bulkDelete()
  {
      console.log("bulkDelete");
      this.showDeleteDialoge("All selected files will be deleted ?","bulkDeleteFile"); 

  }

  bulkActualDelete()
  {
      let data = [];

      this.fileStorageService.allFiles.forEach((element:any) => {
        if(element.tickMark)
          {
            data.push(element.name);
          }
      });

      //Call bulk delete api
      this.fileStorageService.deleteMultipleUserFileByOrganizationAndCategory(this.fileStorageService.storeType,data)
     .pipe(takeUntil(this.destroy$))
     .subscribe({
       next: result => {
        // console.log("File deleted result : ",result);
         if(result == 1)
             {
              this.getAllFilesAndFolders();
              this.setOrganizationStorageData();
             }
             
       },
       error: err => {
       // console.log("Error : "+ JSON.stringify(err));
         //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
       }
     });
  }


  onSearchChange()
  {
    //  console.log("Search input changed");
    //  console.log("this.fileStorageService.searchFileNameString :",this.fileStorageService.searchFileNameString);
    //  console.log("this.fileStorageService.searchFileTypeString :",this.fileStorageService.searchFileTypeString);
    this.getFilteredOptions(this.fileStorageService.searchFileNameString,this.fileStorageService.searchFileTypeString);
  }

  getAllFilesAndFolders()
  {
    console.log("getAllFilesAndFolders");
    if(this.fileStorageService.currentCategory!=null)
      {
        this.fileStorageService.showBackButton = true;
      }
    else{
      this.fileStorageService.showBackButton = false;
    }

    this.fileStorageService.getAllFileNamesOfUserByOrganizationAndCategory(this.fileStorageService.storeType)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: (result:any) => {

            console.log("result : ",result);
            if(result.files != null)
              {
                console.log("file is not null");
                this.fileStorageService.allFiles = [... JSON.parse(result.files)];
                this.fileStorageService.currentFileSize = this.fileStorageService.allFiles.length;
                this.fileStorageService.searchFileNameString = "";
                this.fileStorageService.searchFileTypeString = "";
                this.fileStorageService.bulkDelete = false;
                this.fileStorageService.bulkDownload = false;

                // console.log("this.fileStorageService.allFiles : ",this.fileStorageService.allFiles);
                // console.log("this.fileStorageService.currentFileSize : ",this.fileStorageService.currentFileSize);

                this.fileStorageService.allFiles.forEach((element:any) => {
                  element.tickMark=false;
                  element.status='done'; 
                  element.requestType='';
                  element.loaded=0;
                  element.total=0; 
                  element.percent=0;
                  element.size = parseFloat((element.size/(1024*1024)).toFixed(2));
                });

                this.fileStorageService.filteredOptionsList = [... this.fileStorageService.allFiles];

              }
              else
              {
                // console.log("file is null");
                this.fileStorageService.allFiles = [];
                this.fileStorageService.filteredOptionsList = [];
                this.fileStorageService.currentFileSize = 0;
              }
            
            if(result.folders != null)
              {
                // console.log("folder is not null");
                // console.log(JSON.parse(result.folders)[0]);

                // console.log("folder size : ",JSON.parse(result.folders).length);

                this.fileStorageService.allFolders = [... JSON.parse(result.folders)];

                this.fileStorageService.currentFolderSize = this.fileStorageService.allFolders.length;
              }
              else{
                // console.log("folder is null");
                this.fileStorageService.allFolders = [];
                this.fileStorageService.currentFolderSize = 0;
              }
            
      },
      error: err => {
      console.log("Error : "+ JSON.stringify(err));
        //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });

  }

  fileDownload(fileName:string,fileType:string,j:number)
  {
    console.log("Download file : "+j);
    this.fileStorageService.downloadUserFileByOrganizationAndCategory(this.fileStorageService.storeType,fileName)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: event => {
            // console.log(event);
            this.reportProgress(event,j,-2,-2);
      },
      error: err => {
      // console.log("Error : "+ JSON.stringify(err));
        //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });

  }

  fileDelete(fileName:string,fileType:string,j:number)
  {
    this.fileDeleteName = fileName;
    this.fileDeleteIndex = j;
    this.showDeleteDialoge("Do you really want to delete this file ?","file"); 
  }

  deleteActualFile()
  {
     console.log("Delete file");
     console.log("this.fileDeleteName : ",this.fileDeleteName);

     this.fileStorageService.deleteUserFileByOrganizationAndCategory(this.fileStorageService.storeType,this.fileDeleteName)
     .pipe(takeUntil(this.destroy$))
     .subscribe({
       next: result => {
        console.log("File deleted result : ",result);
         if(result == 1)
             {
                let allFileIndex = -1;
                allFileIndex = this.fileStorageService.allFiles.findIndex(obj => obj.name == this.fileDeleteName);

                if(allFileIndex != -1)
                  {
                    this.fileStorageService.allFiles.splice(allFileIndex, 1);
                    this.fileStorageService.filteredOptionsList.splice(this.fileDeleteIndex, 1);
                    this.setOrganizationStorageData();
                  }
                else{
                  this.showDialoge('Error','activity-outline','danger', "File may not deleted. Refresh please.");
                }
                
                this.fileStorageService.currentFileSize = this.fileStorageService.currentFileSize - 1;
             }
             else{
               //console.log("Result is not true");
               this.showDialoge('Error','activity-outline','danger', "File was not deleted. Try again."); 
             }
             
       },
       error: err => {
       // console.log("Error : "+ JSON.stringify(err));
         //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
       }
     });
  }
  

  onUploadFiles(files:File[]){
    // console.log("On Upload Files");
    const formData = new FormData();

    this.fileStorageService.totalUploadedFiles = files.length;

    for (const file of files) 
      { 
          // Add to current list
          console.log("file.name : ", file.name);
          console.log("file.type : ", file.type);

          let size:any = file.size;

          // console.log("Name : ",name);
          // console.log("Type : ",type);
          // console.log("Size : ",size);

          size = (((size)/1024)/1024).toFixed(2);

          this.fileStorageService.allFiles.unshift(
            {
              bulkDownload:false,
              bulkDelete: false,
              status: 'done', 
              requestType: '',
              loaded: 0,
              total: 0, 
              percent: 0,
              name:file.name,
              type:file.type,
              size:size,
	            byteData: null
            }
          );

          this.fileStorageService.filteredOptionsList.unshift(
            {
              bulkDownload:false,
              bulkDelete: false,
              status: 'done', 
              requestType: '',
              loaded: 0,
              total: 0, 
              percent: 0,
              name:file.name,
              type:file.type,
              size:size,
	            byteData: null
            }
          );

          this.fileStorageService.currentFileSize = this.fileStorageService.currentFileSize + 1;

          formData.append('files', file, file.name); 
      }

    this.fileStorageService.searchFileNameString = "";
    this.fileStorageService.searchFileTypeString = "";
    

    this.fileStorageService.uploadSubscription = this.fileStorageService.uploadUserFilesByOrganizationAndCategory(this.fileStorageService.storeType,formData)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: event => {
            // console.log(event);
            this.reportProgress(event,0,-1,-2);
            //Add files to list of files
      },
      error: err => {
      // console.log("Error : "+ JSON.stringify(err));

        this.getAllFilesAndFolders();

        try{
               if(this.uploadStatusRef)
                {
                  this.uploadStatusRef.close();
                }

                if(this.downloadStatusRef)
                {
                  this.downloadStatusRef.close();
                }

          }
          catch(e)
          {
              console.log(e);
          }

        this.showDialoge('Refresh Page','activity-outline','danger', JSON.stringify(err)); 
      }
    });
  }
  
  uploadFiles() {
    // console.log("Upload Files");
    const val = document.getElementById('upload-files');
    val.click();
  }

  goToPreviousFolder()
  {
    // console.log("Go To Previous Folder");
    if(this.fileStorageService.currentCategory!=null)
      {
          if(String(this.fileStorageService.currentCategory.name).includes("/"))
            {
              // console.log("this.fileStorageService.currentCategory : ",this.fileStorageService.currentCategory);
              // console.log("this.fileStorageService.currentCategoryLadder : ",this.fileStorageService.currentCategoryLadder);
              let length = this.fileStorageService.currentCategoryLadder.length;
              length = length-1;
              this.fileStorageService.currentCategory = this.fileStorageService.currentCategoryLadder[length-1];
              this.fileStorageService.currentCategoryLadder.pop();
              this.fileStorageService.folderLadder = this.fileStorageService.currentCategory.name.split("/");
              this.getAllFilesAndFolders();
            }
            else{
              // console.log("Removing event listner");
              // this.removeFileSearchFilterListner();
              this.getAllInitialCategories();
            }
          
      }
    else{
          this.fileStorageService.currentCategoryLadder = [];
          this.fileStorageService.currentCategory = 'none';
          this.fileStorageService.folderLadder=[];
          this.fileStorageService.currentCategoryLadder = [];
          this.fileStorageService.currentFile = 'none';
          this.fileStorageService.currentFileSize = 0;
          this.fileStorageService.allFiles = [];
          this.fileStorageService.currentCategoryIndex = -1;
          this.fileStorageService.currentFileIndex = -1;
          this.fileStorageService.showBackButton = false;
          this.showDialoge('Unsuccess','done-all-outline','danger', `You are already at root folder.`);
    }  
  }

  folderLadderClicked(k:number)
  {
    // console.log("Folder ladder is clicked : "+k);
    let category:string="";
    for(var i : number = 0; i <= k; i++)  
      {
        if(i!=k)
          {
            category=category+this.fileStorageService.folderLadder[i]+"/";
          }
        else{
            category=category+this.fileStorageService.folderLadder[i];
        }
      }

      let diff = this.fileStorageService.currentCategoryLadder.length - (k+1);
      this.fileStorageService.currentCategory = this.fileStorageService.currentCategoryLadder[k];
      this.fileStorageService.currentCategoryLadder.splice((k+1), diff);
      this.fileStorageService.folderLadder = this.fileStorageService.currentCategory.name.split("/");
      this.getAllFilesAndFolders();

  }

  fileIsClicked(j:any){
    console.log("Clicked file : "+j);
    this.fileStorageService.currentFile = this.fileStorageService.allFiles[j];
    this.fileStorageService.currentFileIndex = j;
  }

  folderIsClicked(i:any){
    this.folderClickedOutOfArray = i ;
    console.log("Folder "+i+" is clicked");
    this.loadFilesAsPerFolderClicked();
  }

  loadFilesAsPerFolderClicked(){
    console.log('loadFilesAsPerFolderClicked');
    console.log(this.fileStorageService.allFolders[this.folderClickedOutOfArray]);
    this.fileStorageService.currentCategory = this.fileStorageService.allFolders[this.folderClickedOutOfArray];
    this.fileStorageService.currentCategoryLadder.push(this.fileStorageService.currentCategory);
    this.fileStorageService.folderLadder = this.fileStorageService.currentCategory.name.split("/");
    this.fileStorageService.currentCategoryIndex = this.folderClickedOutOfArray;

    this.getAllFilesAndFolders();
  }

  createCategory() {
    console.log("Create Category");
    this. createUpdateCategoryDialoge('Create');
  }

  updateCategory() {
    console.log("Update Category");
    this. createUpdateCategoryDialoge('Update');
  }


  getAllInitialCategories()
  {
    
    console.log("Executing Init after 150ms delay");
    let extensionVariable = ''
    if(this.router.url.includes("whatsapp")){
          extensionVariable = localStorage.getItem("organization");
        }
    else{
          extensionVariable = localStorage.getItem("extension");
        }

    this.fileStorageService.getAllFileCategoryByExtensionAndOrganization(extensionVariable,localStorage.getItem("organization"))
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: result => {
        if(result != null)
            {
              this.fileStorageService.allFolders = [... JSON.parse(JSON.stringify(result))];
              this.fileStorageService.showBackButton = false;
              this.fileStorageService.currentFolderSize = this.fileStorageService.allFolders.length;

              this.fileStorageService.currentCategory = 'none';
              this.fileStorageService.folderLadder=[];
              this.fileStorageService.currentCategoryLadder = [];
              this.fileStorageService.currentFile = 'none';
              this.fileStorageService.currentFileSize = 0;
              this.fileStorageService.allFiles = [];
              this.fileStorageService.currentCategoryIndex = -1;
              this.fileStorageService.currentFileIndex = -1;

              // console.log("Initial: ",JSON.stringify(this.fileStorageService.allFolders));
            }
            else{
              //console.log("Result is not true");
              // event.confirm.reject();
              this.fileStorageService.allFolders = [];
              this.fileStorageService.currentFolderSize = 0;
              this.fileStorageService.currentCategory = 'none';
              this.fileStorageService.folderLadder=[];
              this.fileStorageService.currentCategoryLadder = [];
              this.fileStorageService.currentFile = 'none';
              this.fileStorageService.currentFileSize = 0;
              this.fileStorageService.allFiles = [];
              this.fileStorageService.currentCategoryIndex = -1;
              this.fileStorageService.currentFileIndex = -1;
            }
            
      },
      error: err => {
      // console.log("Error : "+ JSON.stringify(err));
        //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });
  }

  deleteCategory() {
    this.showDeleteDialoge("This will delete all files and folders with-in. Want to proceed ?","category"); 
  }

  deleteActualCategory()
  {

    console.log("Delete Category");
    let data:any = {
      name:this.fileStorageService.currentCategory.name,
      businessType:this.fileStorageService.currentCategory.businessType
    };

    this.fileStorageService.deleteFileCategoryByExtensionAndNameAndOrganization(this.fileStorageService.storeType,data)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: result => {
        if(String(result) == 'true')
            {
              // Delete was successful;
              this.goToPreviousFolder();
              this.setOrganizationStorageData();
            }
            else{
              //console.log("Result is not true");
            // event.confirm.reject();
            this.showDialoge('Unsuccess','done-all-outline','danger', `Deleting folder is unsuccessful.`);
            }
            
      },
      error: err => {
      // console.log("Error : "+ JSON.stringify(err));
        //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });
  }


  public reportProgress(httpEvent: HttpEvent<string[] | Blob>,index:number,type:number,allFiles:number): void {
    console.log("Index Number : ",index);
    console.log("reportProgress : ",index);
    console.log("httpEvent : ",httpEvent);

    switch(httpEvent.type) {
      case HttpEventType.UploadProgress:
        if(type == -1)
          {
            if(this.fileStorageService.fileUploadStatus.status == 'done')
              {
                this.showUploadStatusDialoge();
              }
              
            this.fileStorageService.updateUploadStatus(httpEvent.loaded, httpEvent.total!, 'Uploading... ');
          }
        break;
      case HttpEventType.DownloadProgress:
        if(type == -2)
          {
            if(allFiles == -2)
              {
                this.updateDownloadStatus(httpEvent.loaded, httpEvent.total!, 'Downloading... ',index);
              }
              else{

                if(this.fileStorageService.multipleFileDownloadStatus.status == 'done')
                  {
                    this.showDownloadStatusDialoge();
                  }

                this.updateMultipleDownloadStatus(httpEvent.loaded, 'Downloading... ');
              }
          }
        break;
      case HttpEventType.ResponseHeader:
        // console.log('Response Header returned', httpEvent);
        break;
      case HttpEventType.Response:
        // console.log("Response Body");
        // console.log("httpEvent.body instanceof Array : ",httpEvent.body instanceof Array);

        if (httpEvent.body instanceof Array) {

          try{
               if(this.uploadStatusRef)
                {
                  this.uploadStatusRef.close();
                }
          }
          catch(e)
          {
              console.log(e);
          }

          this.fileStorageService.fileUploadStatus.status = 'done';
          this.setOrganizationStorageData();
        } 
        else 
        {

          // console.log("*********End Of Download***********");
          // console.log("httpEvent.headers : ",httpEvent.headers);
          // console.log("File-Name : ",httpEvent.headers.get('File-Name'));


          if(allFiles != -2)
            {
              saveAs(new File([httpEvent.body!], "download.zip", 
                {type: `${httpEvent.headers.get('Content-Type')};charset=utf-8`}));

                try{
                      if(this.downloadStatusRef)
                      {
                        this.downloadStatusRef.close();
                      }
                  }
                  catch(e)
                  {
                      console.log(e);
                  }

                this.fileStorageService.multipleFileDownloadStatus.status = 'done';
            }
            else{
              
              saveAs(new File([httpEvent.body!], this.fileStorageService.filteredOptionsList[index].name!, 
                {type: `${httpEvent.headers.get('Content-Type')};charset=utf-8`}));

              // saveAs(new Blob([httpEvent.body!], 
              //   { type: `${httpEvent.headers.get('Content-Type')};charset=utf-8`}),
              //    httpEvent.headers.get('File-Name'));

              this.fileStorageService.filteredOptionsList[index].status = 'done';
            }
        }
        break;
        default:
        // console.log(httpEvent);
        break;
      
    }
  }


  private updateMultipleDownloadStatus(loaded: number, requestType: string): void {

    let total = this.fileStorageService.totalDownloadSize;

    console.log("************************* Total Size *************************");
    console.log(total);

    this.fileStorageService.multipleFileDownloadStatus.status = 'progress';
    this.fileStorageService.multipleFileDownloadStatus.requestType = requestType;
    this.fileStorageService.multipleFileDownloadStatus.total = total;
    this.fileStorageService.multipleFileDownloadStatus.loaded = Math.trunc((loaded / 1024) / 1024);
    this.fileStorageService.multipleFileDownloadStatus.percent = Math.round(100 * this.fileStorageService.multipleFileDownloadStatus.loaded / total);

    console.log("************************* Multiple Download Status *************************");
    console.log(this.fileStorageService.multipleFileDownloadStatus);

  }

private updateDownloadStatus(loaded: number, total: number, requestType: string, index:number,): void {

    // console.log("updateUploadStatus for index : ",index);

    this.fileStorageService.filteredOptionsList[index].status = 'progress';
    this.fileStorageService.filteredOptionsList[index].requestType = requestType;
    this.fileStorageService.filteredOptionsList[index].total = Math.round((total / 1024) / 1024);
    this.fileStorageService.filteredOptionsList[index].loaded = Math.round((loaded / 1024) / 1024);
    this.fileStorageService.filteredOptionsList[index].percent = Math.round(100 * loaded / total);

    this.changeDownloadRequestType(index);

    console.log("this.fileStorageService.filteredOptionsList[index].percent : ",this.fileStorageService.filteredOptionsList[index].percent);

  }  

  changeDownloadRequestType(j:number) {
    if (this.fileStorageService.filteredOptionsList[j].percent <= 25) {
      this.fileStorageService.filteredOptionsList[j].requestType = 'danger';
    } else if (this.fileStorageService.filteredOptionsList[j].percent <= 50) {
      this.fileStorageService.filteredOptionsList[j].requestType = 'warning';
    } else if (this.fileStorageService.filteredOptionsList[j].percent <= 75) {
      this.fileStorageService.filteredOptionsList[j].requestType = 'info';
    } else {
      this.fileStorageService.filteredOptionsList[j].requestType = 'success';
    }
  }

  showUploadStatus()
  {
    console.log("showUploadStatus");
    this.showUploadStatusDialoge();
  }

  showDownloadStatus()
  {
    console.log("showUploadStatus");
    this.showDownloadStatusDialoge();
  }

  showDownloadStatusDialoge() {

    if(!this.fileStorageService.isFileDownloadDialogOpen)
      {

       this.downloadStatusRef = this.dialogService.open(DownloadStatusComponent, {
          context: {
            fileStorageService:this.fileStorageService,
          },
        });

       this.downloadStatusRef.onClose.subscribe((value) => {
        console.log("Upload dialog is closed");
        console.log("Value : ",value);
        this.fileStorageService.isFileDownloadDialogOpen = false;
       });
        
      }
  }

  showUploadStatusDialoge() {

    if(!this.fileStorageService.isFileUploadDialogOpen)
      {

       this. uploadStatusRef = this.dialogService.open(UploadStatusComponent, {
          context: {
            fileStorageService:this.fileStorageService,
          },
        });

       this.uploadStatusRef.onClose.subscribe((value) => {
        console.log("Upload dialog is closed");
        console.log("Value : ",value);
        this.fileStorageService.isFileUploadDialogOpen = false;
       });
        
      }
  }

  showDeleteDialoge(message:string,type:string) {

        this.dialogService.open(AskDeleteComponent, {
          context: {
            message: message,
            type: type
          },
        }).onClose.subscribe((type) => {
          console.log("Delete dialog is closed");
          console.log("Something of type is delete : ",type);
          if(type != undefined && type == "category")
            {   
               this. deleteActualCategory();    
            }
          else if(type != undefined && type == "file")
            {
               this.deleteActualFile();
            }
          else if(type != undefined && type == "bulkDeleteFile")
            {
               this.bulkActualDelete();
            }
        });
    }

  createUpdateCategoryDialoge(type:string)
  {
    console.log("Create Update Category Dialoge");
    
    this.dialogService.open(CreateUpdateCategoryComponent, {
      context: {
        type: type,
        fileStorageService:this.fileStorageService,
        storeType:this.fileStorageService.storeType,
      },
    });
  }
  

  private filter(nameValue: String,typeValue:String){
    const filterValueName = nameValue.toLowerCase();
    const filterValueType = typeValue.toLowerCase();
    console.log("filterValueName : ",filterValueName);
    console.log("filterValueType : ",filterValueType);

    if(this.fileStorageService.tickMarkAll)
      {
        this.fileStorageService.tickMarkAll = false;
        this.fileStorageService.allFiles.forEach((element:any) => {
          element.tickMark = false;
        });
      }
   
    this.fileStorageService.filteredOptionsList = this.fileStorageService.allFiles.filter(optionValue => (((optionValue.name).toLowerCase().includes(filterValueName))&&(optionValue.type).toLowerCase().includes(filterValueType)));
    this.fileStorageService.currentFileSize = this.fileStorageService.filteredOptionsList.length;
  }

  getFilteredOptions(nameValue: String,typeValue:String){
    this.filter(nameValue,typeValue);
  }

  // @HostListener('window:resize', ['$event'])
  //   onResize(event?: any) {
  //     console.log("screensize canged")
  //     this.getScreenSize(); // Update screen size on window resize
  //   }
  
    getScreenSize() {
      this.screenWidth = window.innerWidth;
      this.screenHeight = window.innerHeight;
      console.log(`Screen width: ${this.screenWidth}, Screen height: ${this.screenHeight}`);
      if(this.screenWidth<555){
        this.folderMaxInteger = 7;
        this.fileMaxInteger = 20;
        this.fileTypeMaxInteger = 5;
        this.folderManagementActionSize = "small";
        this.fileManagementActionSize = "small";
        this.itsMobile = true;
      }
      else if(this.screenWidth<650){
        this.folderMaxInteger = 9;
        this.fileMaxInteger = 15;
        this.fileTypeMaxInteger = 7;
      }
      else if(this.screenWidth<1025){
        this.folderMaxInteger = 10;
        this.fileMaxInteger = 30;
        this.fileTypeMaxInteger = 10;
      }
      else{
        this.folderMaxInteger = 12;
        this.fileMaxInteger = 50;
        this.fileTypeMaxInteger = 15;
      }
    }

  public formatValue(value: number): string {
    return `${value} MB`;
  }

  setOrganizationStorageData(){
    console.log("setOrganizationStorageData");
    this.organizationService.getOrganizationalData(ConstantsService.user.organization)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: allData => {
            // console.log('allData : '+JSON.stringify(allData));
           
            let parsedData = JSON.parse(JSON.stringify(allData));
            ConstantsService.organizationData = parsedData;

              var output = [
                {
                  name: 'Used',
                  value: 0,
                },
                {
                  name: 'Free',
                  value: 0,
                },
              ];

              if(parsedData.allowedUploadInMB !== -1){
                output[0].value = parsedData.currentUploadInMB;
                output[1].value = (parsedData.allowedUploadInMB - parsedData.currentUploadInMB);
              }
              else{
                output[0].value = 0;
                output[1].value = parsedData.currentUploadInMB;
              }
              
              console.log("parsedData.allowedUploadInMB : "+parsedData.allowedUploadInMB);
              console.log("parsedData.currentUploadInMB : "+parsedData.currentUploadInMB);

              //console.log("After http");    
              this.single=[...output];
    
          },
          error: err => {
           // console.log("Error : "+ JSON.stringify(err));
            //this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
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
