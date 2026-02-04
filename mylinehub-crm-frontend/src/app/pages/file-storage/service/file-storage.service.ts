import { Injectable } from '@angular/core';
import { NbDialogService } from '@nebular/theme';
import { ApiHttpService } from '../../../service/http/api-http.service';
import { ConstantsService } from '../../../service/constants/constants.service';
import { Observable, Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class FileStorageService {
  private destroy$: Subject<void> = new Subject<void>();

  //Donot confuse with word Category or Folder. They are used interchangable here. Category is nothing but is folder.
  currentCategory:any = 'none';
  currentFile:any = 'none';
  currentFolderSize:any = 0;
  currentFileSize:any = 0;
  showBackButton: any = false;
  currentCategoryIndex:any = -1;
  currentFileIndex:any = -1;

  allSearchableFiles:any=[];
  allFiles:any=[];

  allFolders:any=[];
  fileUploadStatus = { status: 'done', requestType: '',loaded: 0 ,total: 0, percent: 30 };
  multipleFileDownloadStatus = { status: 'done', requestType: '',loaded: 0 ,total: 0, percent: 30 };
  // fileDownloadStatus = [];
  folderLadder: string[] = [];
  currentCategoryLadder: any = [];
  // filteredOptions$: Observable<string[]>;
  filteredOptionsList: any=[];
  bulkDownload:boolean = false;
  bulkDelete:boolean = false;
  tickMarkAll:boolean = false;

  searchFileNameString:any = "";
  searchFileTypeString:any = "";
  isFileUploadDialogOpen: boolean = false;
  isFileDownloadDialogOpen: boolean = false;
  totalDownloadSize:any = 0;
  uploadSubscription:any;
  downloadSubscription:any;
  totalUploadedFiles = 0;
  storeType = ConstantsService.userStore;

  constructor(private dialogService: NbDialogService,
              protected constService : ConstantsService,
              protected httpService : ApiHttpService,
              ) 
  {

  }

  // setCurrentCategoryName(value:string)
  // {
  //     this.currentCategory.name = value;
  //     this.folderLadder = value.split("/");
  // }

  public updateUploadStatus(loaded: number, total: number, requestType: string): void {

    console.log("updateUploadStatus");
    
    this.fileUploadStatus.status = 'progress';
    this.fileUploadStatus.requestType = requestType;
    this.fileUploadStatus.total = Math.trunc((total / 1024) / 1024);
    this.fileUploadStatus.loaded = Math.trunc((loaded / 1024) / 1024);
    this.fileUploadStatus.percent = Math.round(100 * loaded / total);

    console.log("this.fileUploadStatus : ",this.fileUploadStatus);

  }

  public getAllFileCategoryByExtensionAndOrganization(extension: string,organization: string) { 

    var endpoint: string = this.constService.API_GET_ALL_FILE_CATEGORIES;

    endpoint = endpoint.replace("{{organization}}",organization);
    endpoint = endpoint.replace("{{extension}}",extension);
    return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 

  public getAllFileNamesOfUserByOrganizationAndCategory(requestOrigin:string) { 

    var endpoint: string = this.constService.API_GET_ALL_FILE_NAMES_BY_CATEGORY;
    endpoint = endpoint.replace("{{requestOrigin}}",requestOrigin);
    endpoint = endpoint.replace("{{category}}",this.currentCategory.name);
    return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 

  public downloadUserFileByOrganizationAndCategory(requestOrigin:string,filename: string) { 

    var endpoint: string = this.constService.API_DOWNLOAD_USER_FILE;
    endpoint = endpoint.replace("{{requestOrigin}}",requestOrigin);
    endpoint = endpoint.replace("{{category}}",this.currentCategory.name);
    endpoint = endpoint.replace("{{filename}}",filename);
    return this.httpService.getWithTokenHandlededAndEvent(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 

  public deleteUserFileByOrganizationAndCategory(requestOrigin:string,filename: string) { 

    var endpoint: string = this.constService.API_DELETE_USER_FILE;
    endpoint = endpoint.replace("{{requestOrigin}}",requestOrigin);
    endpoint = endpoint.replace("{{category}}",this.currentCategory.name);
    endpoint = endpoint.replace("{{filename}}",filename);
    return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 


  
  public deleteMultipleUserFileByOrganizationAndCategory(requestOrigin:string,data:any) { 

    var endpoint: string = this.constService.API_DELETE_MULTIPLE_USER_FILE;
    endpoint = endpoint.replace("{{category}}",this.currentCategory.name);
    endpoint = endpoint.replace("{{requestOrigin}}",requestOrigin);
    return this.httpService.postWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
  } 

  public downloadMultipleUserFileByOrganizationAndCategory(requestOrigin:string) { 

    var endpoint: string = this.constService.API_DOWNLOAD_MULTIPLE_USER_FILE;
    endpoint = endpoint.replace("{{category}}",this.currentCategory.name);
    endpoint = endpoint.replace("{{requestOrigin}}",requestOrigin);

    this.allFiles.forEach((element:any) => {
      if(element.tickMark)
        {
          endpoint = endpoint + "&fileNames="+element.name;
        }
    });

    return this.httpService.getWithTokenHandlededAndEvent(this.constService.API_BASE_ENDPOINT + endpoint); 
  } 

 public deleteFileCategoryByExtensionAndNameAndOrganization(requestOrigin:string,data:any) { 
          var endpoint: string = this.constService.API_DELETE_FILE_CATEGORY;
          endpoint = endpoint.replace("{{requestOrigin}}",requestOrigin);
          return this.httpService.postWithTokenAndMultipartHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 

 public createFileCategoryByExtensionAndNameAndOrganization(requestOrigin:string,data:FormData) { 
          var endpoint: string = this.constService.API_CREATE_FILE_CATEGORY;
          endpoint = endpoint.replace("{{requestOrigin}}",requestOrigin);
          return this.httpService.postWithTokenAndMultipartHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        }
        
public updateFileCategoryByExtensionAndNameAndOrganization(requestOrigin:string,data:FormData) { 
          var endpoint: string = this.constService.API_UPDATE_FILE_CATEGORY;
          endpoint = endpoint.replace("{{requestOrigin}}",requestOrigin);
          return this.httpService.postWithTokenAndMultipartHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        }  


public updateFileCategoryByExtensionAndNameAndOrganizationWithoutImage(requestOrigin:string,data:FormData) { 
          var endpoint: string = this.constService.API_UPDATE_FILE_CATEGORY_WITHOUT_IMAGE;
          endpoint = endpoint.replace("{{requestOrigin}}",requestOrigin);
          return this.httpService.postWithTokenAndMultipartHandleded(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        } 

public uploadUserFilesByOrganizationAndCategory(requestOrigin:string,data:FormData) { 
          var endpoint: string = this.constService.API_UPLOAD_USER_FILES;
          endpoint = endpoint.replace("{{requestOrigin}}",requestOrigin);
          endpoint = endpoint.replace("{{category}}",this.currentCategory.name);
          return this.httpService.postWithTokenHandlededAndEvent(this.constService.API_BASE_ENDPOINT + endpoint,data); 
        }  
}
