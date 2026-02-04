import { Component, OnChanges, OnDestroy, OnInit, SimpleChanges } from '@angular/core';
import {NbTrigger,NbPosition,NbMediaBreakpointsService, NbMenuService, NbSidebarService, NbThemeService,NbSearchService, NbDialogService, NbMenuItem } from '@nebular/theme';
import { LayoutService } from '../../../@core/utils';
import { filter, map, takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs';
import { ConstantsService } from './../../../service/constants/constants.service';
import { Router } from '@angular/router';
import { JwtHelperService } from '@auth0/angular-jwt'
import { DatePipe } from '@angular/common';
import { LoginService } from './../../../auth/service/login/login.service';
import { HttpHeaders } from '@angular/common/http';
import { BrowserPhoneService } from '../../../service/browser-phone/browser-phone.service';
import { BrowserPhoneComponent } from './browser-phone/browser-phone.component';
import { BrowserPhoneActionComponent } from './browser-phone-action/browser-phone-action.component';
import { VideoDialogComponent } from './video-dialog/video-dialog.component';
import { ReceiveDialControlComponent } from './receive-dial-control/receive-dial-control.component';
import { HeaderVariableService } from '../../../service/header-variable/header-variable.service';
import { EmployeeService } from '../../../pages/employee/service/employee.service';
import { DialogComponent } from '../../../pages/employee/all-employees/dialog/dialog.component';
import { StompService } from '../../../service/stomp/stomp.service';
import { NotificationListComponent } from './notification-list/notification-list.component';
import { MessageListComponent } from './message-list/message-list.component';
import { NotificationListDataService } from './notification-list/notification-list-data-service/notification-list-data.service';
import { MessageListDataService } from './message-list/message-list-data-service/message-list-data.service';
import { ProductService } from '../../../pages/product/service/product.service';
import { RegisterService } from '../../../service/browser-phone/register/register.service';
import { UtilsService } from '../../../service/browser-phone/utils/utils.service';
import { MenuDataService } from '../../../service/menu-data/menu-data.service';
import { OrganizationService } from '../../../service/organization/organization.service';
import { WhatsappNumberService } from '../../../service/whatsapp-number/whatsapp-number.service';
import { WhatsappDataServiceService } from '../../../pages/whatsapp-chat/chat/whatsapp-data-service/whatsapp-data-service.service';
import { FileStorageService } from '../../../pages/file-storage/service/file-storage.service';
import { JwtTokenService } from '../../../service/JWTToken/jwt-token.service';
//import { SearchResultComponent } from '../../../pages/search/search-result/search-result.component';

@Component({
  selector: 'ngx-header',
  styleUrls: ['./header.component.scss'],
  templateUrl: './header.component.html',
})
export class HeaderComponent implements OnInit,OnChanges,OnDestroy {
  private destroy$: Subject<void> = new Subject<void>();
  userPictureOnly: boolean = false;
  user: any;
  redirectDelay: number = 0;
  generalNotification: any = NotificationListComponent;
  messageNotification: any = MessageListComponent;
  trigger = NbTrigger.HOVER;
  position = NbPosition.BOTTOM;
  phoneComponent: any = BrowserPhoneComponent;
  browserPhoneActionComponent: any = BrowserPhoneActionComponent;
  receiveDialControlComponent: any = ReceiveDialControlComponent;

 
 
  videoButtonStatus = 'success';
  shareDataButtonStatus = 'success';
 
  hasBackdrop :  any = false;
  videoDialogOpen = false;
  messageDialogOpen = false;

  isWhite:boolean = false;


  //isVideoCall = false;
  // isScreenShared = false;
  // isConferenceCall = false;
  // isTransferAttendedCall = false;

  themes = [
    {
      value: 'default',
      name: 'Light',
    },
    {
      value: 'dark',
      name: 'Dark',
    },
    {
      value: 'cosmic',
      name: 'Cosmic',
    },
    {
      value: 'corporate',
      name: 'Corporate',
    },
  ];

  currentTheme = 'default';

  userMenu = [ { title: 'Profile' }, { title: 'Log out' } ];


  constructor(private sidebarService: NbSidebarService,
              private menuService: NbMenuService,
              private themeService: NbThemeService,
              private layoutService: LayoutService,
              private breakpointService: NbMediaBreakpointsService,
              protected router: Router,
              protected constantService : ConstantsService,
              protected jwtHelperService:JwtHelperService,
              protected datepipe: DatePipe,
              protected loginService : LoginService,
              private searchService: NbSearchService,
              private dialogService: NbDialogService,
              private employeeService : EmployeeService,
              private browserPhoneService:BrowserPhoneService,
              protected headerVariableService:HeaderVariableService,
              protected stompService:StompService,
              protected notificationListDataService:NotificationListDataService,
              protected messageListDataService:MessageListDataService,
              protected registerService: RegisterService,
              protected utilsService : UtilsService,
              private menuDataService: MenuDataService,
              private organizationService : OrganizationService,
              private whatsappNumberService : WhatsappNumberService,
              private whatsappDataServiceService : WhatsappDataServiceService,
              private fileStorageService:FileStorageService,
              ){
              //console.log("header-constructor");
              //private searchResultComponent:SearchResultComponent) {
              //Setting Borwser Phone variables values

              
    // this.menuService.onItemClick().subscribe((event) => {
    //   console.log("Menu Item Clicked");
    //   console.log(JSON.stringify(event));
    //    // The 'event' object contains information about the clicked item
    //       console.log('Menu item clicked:', event.item);

    //       // You can access properties of the clicked item, for example:
    //       if (event.item.title === 'All-Media') {
    //         console.log('All-Media button was clicked!');
    //         this.fileStorageService.isWhatsApp = true;
    //       } else if (event.item.title === 'All Files') {
    //         console.log('All Files button was clicked!');
    //         this.fileStorageService.isWhatsApp = false;
    //       }

    // });
                 
     if(localStorage.getItem("token") != null)
     {
       
      // console.log("token not null in header while constantservice.user was null");
       
       const headers = new HttpHeaders({
               'Content-Type': 'application/json',
               'Authorization': `Bearer ${localStorage.getItem("token")}`
             });

             const requestOptions = { headers: headers };

             this.loginService.getEmployeeData(localStorage.getItem("organization"),localStorage.getItem("email"),requestOptions)
             .pipe(takeUntil(this.destroy$))
             .subscribe({
               next: data => {

               //console.log("Fetched user details from server");
              ConstantsService.user = JSON.parse(JSON.stringify(data));

              //console.log(ConstantsService.user.firstName + " " + ConstantsService.user.lastName);
              //console.log(JSON.stringify(data));
              //  console.log("ConstantsService.user : ",ConstantsService.user);
                // this.user = {name : ConstantsService.user.firstName + " " + ConstantsService.user.lastName, picture : "data:image/png;base64,"+ConstantsService.user.imageData};
                let imageURL = null;
                
                if(ConstantsService.user.iconImageByteData !== null){
                    console.log("image icon data is not null");
                    imageURL='data:image/'+ConstantsService.user.imageType+';base64,'+ConstantsService.user.iconImageByteData;
                    imageURL = imageURL.replace("/image","");      
                }
                
                console.log("imageURL : ",imageURL);
                this.user = {name : ConstantsService.user.firstName.trim() + " " + ConstantsService.user.lastName.trim(), picture : imageURL};
                
                console.log("UserName:",this.user.name);

                 const { xl } = this.breakpointService.getBreakpointsMap();

                 this.themeService.onMediaQueryChange()
                   .pipe(
                     map(([, currentBreakpoint]) => currentBreakpoint.width < xl),
                     takeUntil(this.destroy$),
                   )
                   .subscribe((isLessThanXl: boolean) => this.userPictureOnly = isLessThanXl);
             
                 this.themeService.onThemeChange()
                   .pipe(
                     map(({ name }) => name),
                     takeUntil(this.destroy$),
                   )
                   .subscribe(themeName => this.currentTheme = themeName);

               //console.log('ConstantsService.user.uiTheme',ConstantsService.user.uiTheme);

                 if(ConstantsService.user.uiTheme != "" || ConstantsService.user.uiTheme != undefined)
                 {
                   //this.themeService.changeTheme(ConstantsService.user.uiTheme);

                     let setDefaultTheme = true;
                     //setting theme as per user prefrence
                     this.themes.forEach( (element:any,index:number) => {
                     console.log(element.value);
                     console.log(ConstantsService.user.uiTheme);
                       if(element.value === ConstantsService.user.uiTheme)
                       {
                       //console.log('I am setting theme : ',ConstantsService.user.uiTheme);
                         setDefaultTheme = false;
                         this.themeService.changeTheme(ConstantsService.user.uiTheme);

                         if(ConstantsService.user.uiTheme.toLowerCase() === 'dark'){
                          console.log("Changing logo to white");
                          this.isWhite= false;
                          this.headerVariableService.userBackgroundColor = "#414b66";
                         }
                         else if(ConstantsService.user.uiTheme.toLowerCase() === 'cosmic'){
                          console.log("Changing logo to white");
                          this.isWhite= false;
                          this.headerVariableService.userBackgroundColor = "#686891";
                         }
                         else{
                          this.isWhite= true;
                          this.headerVariableService.userBackgroundColor = "#efefef";
                         }

                       }
                     });

                     if (setDefaultTheme){
                     this.themeService.changeTheme('default');
                      this.isWhite= true;
                      this.headerVariableService.userBackgroundColor = "#efefef";
                     }

                 }
                 else
                 {
                   this.themeService.changeTheme('default');
                   this.isWhite= true;
                   this.headerVariableService.userBackgroundColor = "#efefef";
                 }

                // console.log('ConstantsService.user.startVideoFullScreen',ConstantsService.user.startVideoFullScreen);
                                
                 this.headerVariableService.autoAnswer = ConstantsService.user.autoAnswer;
                 this.headerVariableService.autoConference = ConstantsService.user.autoConference;
                 this.headerVariableService.autoVideo = ConstantsService.user.autoVideo;

                 this.headerVariableService.doNotDisturb = ConstantsService.user.doNotDisturb;
                 this.headerVariableService.startVideoFullScreen = ConstantsService.user.startVideoFullScreen;
                 this.headerVariableService.callWaiting = ConstantsService.user.callWaiting;

                 this.headerVariableService.intercomPolicy = ConstantsService.user.intercomPolicy;
                 this.headerVariableService.freeDial = ConstantsService.user.freeDialOption;
                 this.headerVariableService.textDictate = ConstantsService.user.textDictateOption;
                 this.headerVariableService.textMessaging = ConstantsService.user.textMessagingOption;

                 this.headerVariableService.currentMicStringValue =  ConstantsService.user.micDevice;
                 this.headerVariableService.currentVideoStringValue =   ConstantsService.user.videoDevice;
                 this.headerVariableService.currentSpeakerStringValue =   ConstantsService.user.speakerDevice;
                //  console.log('this.headerVariableService.currentMicStringValue',this.headerVariableService.currentMicStringValue)
                //  console.log('this.headerVariableService.currentVideoStringValue',this.headerVariableService.currentVideoStringValue)
                //  console.log('this.headerVariableService.currentSpeakerStringValue',this.headerVariableService.currentSpeakerStringValue)
                 
                 this.headerVariableService.currentVideoOrientationStringValue =   ConstantsService.user.videoOrientation;
                 this.headerVariableService.currentVideoQualityStringValue =   ConstantsService.user.videoQuality;
                 this.headerVariableService.currentVideoFrameRateStringValue =  ConstantsService.user.videoFrameRate;
                 this.headerVariableService.currentAutoGainControlStringValue =  ConstantsService.user.autoGainControl;
                 this.headerVariableService.currentEchoCancellationStringValue =   ConstantsService.user.echoCancellation;
                 this.headerVariableService.currentNoiseSeperationStringValue =  ConstantsService.user.noiseSupression;
                 this.headerVariableService.notificationBadgeDot = ConstantsService.user.notificationDot
                // console.log('this.headerVariableService.startVideoFullScreen',this.headerVariableService.startVideoFullScreen);
                // currentMic:any = [false,false,true];
                // currentVideo:any = [true,false,false,false];
                // currentSpeaker:any = [false,true,false,false];
                // currentVideoOrientation : any = [true,false];
                // currentVideoQuality : any = [true,false,false,false];
                // currentVideoFrameRate : any = [true,false,false,false,false];
                // currentAutoGainControl:any = [true,false];
                // currentEchoCancellation:any = [true,false];
                // currentNoiseSeperation:any = [true,false];
                // console.log("User Name : "+this.user.name);
                // console.log("User Image : "+this.user.picture);
                // console.log("Fetched user details from server");

                this.notificationListDataService.loadNotifications();
                this.loadAllEmployeesData();         
                //Setting menu data after we get Whats App data
                this.setMenuData();

                // if (state == "terminated") this.headerVariableService.selfPresence =  "Ready";
                // if (state == "trying") this.headerVariableService.selfPresence =  "On the phone";
                // if (state == "proceeding") this.headerVariableService.selfPresence =  "On the phone";
                // if (state == "early") this.headerVariableService.selfPresence =  "Ringing";
                // if (state == "confirmed") this.headerVariableService.selfPresence =  "On the phone";
               },
               error: err => {
                 //  console.log("Error while setting up header: "+ JSON.stringify(err));
                 //  console.log('Routing To Login Page');
                 
                 ConstantsService.token = '';
                 ConstantsService.organization = '';
                 ConstantsService.user = '';
                 ConstantsService.role = '';
                 ConstantsService.extension = '';
                 ConstantsService.isAuthenticated = false;
                 ConstantsService.decodedToken = '';
                 ConstantsService.email = '';
                 ConstantsService.password = '';
                 ConstantsService.fetchTime = '';
       
               //  console.log('Setting Up Local Storage To Empty It');
                 localStorage.removeItem("token");
                 localStorage.removeItem("organization");
                 localStorage.removeItem("role");
                 localStorage.removeItem("extension");
                 localStorage.removeItem("fetchTime");
                 localStorage.removeItem("email");

                 if(localStorage.getItem("zunu")!= null)
                   {
                     localStorage.removeItem("zunu");
                   }

                 setTimeout(() => {
                   return this.router.navigateByUrl(this.constantService.LOGIN_ENDPOINT);
                 }, this.redirectDelay);
                         }
           });
       
     }
              
  }


  ngOnInit() {

  //console.log("header-ng-onit")

    this.currentTheme = this.themeService.currentTheme;
   
   // console.log("Header : NGOnIt : Setting Menu Onlick Event");
    //console.log("User: "+ ConstantsService.user);

    this.headerVariableService.remoteAudio = document.getElementById('remoteAudio');
    
    this.searchService.onSearchSubmit()
    .subscribe((data: any) => {
      
    //console.log("search button is clicked ");
      //console.log(JSON.stringify(data));
      //console.log(JSON.stringify(data.term));
     // ConstantsService.searchContext = "";
      ConstantsService.searchContext = data.term;
      //console.log("Profile : menuService.onItemClick");
 
      ConstantsService.searchNow = true;
      
      setTimeout(() => {
        return this.router.navigateByUrl(this.constantService.All_SEARCH_DETAIL_ENDPOINT);
      }, this.redirectDelay);

      //this.searchResultComponent.loadNext();

    })


    this.menuService.onItemClick()
    .pipe(
      filter(({ tag }) => tag === 'my-context-menu'),
      map(({ item: { title } }) => title),
      takeUntil(this.destroy$)
    )
    .subscribe(title => 
      {
      //  console.log(`${title} was clicked!`)
        if(title == 'Log out')
        {
        //  console.log("Logout : menuService.onItemClick");
          

         this.themeService.changeTheme('default');

          ConstantsService.token = '';
          ConstantsService.organization = '';
          ConstantsService.user = '';
          ConstantsService.role = '';
          ConstantsService.extension = '';
          ConstantsService.isAuthenticated = false;
          ConstantsService.decodedToken = '';
          ConstantsService.email = '';
          ConstantsService.password = '';
          ConstantsService.fetchTime = '';

        //  console.log('Setting Up Local Storage To Empty It');
          localStorage.removeItem("token");
          localStorage.removeItem("organization");
          localStorage.removeItem("role");
          localStorage.removeItem("extension");
          localStorage.removeItem("fetchTime");
          localStorage.removeItem("email");

          // this.browserPhoneService.unregister(false);
          // this.stompService.disconnect();

          if(localStorage.getItem("zunu")!= null)
          {
            localStorage.removeItem("zunu");
          }
          

        //  console.log('Routing To Login Page');

          setTimeout(() => {
            return this.router.navigateByUrl(this.constantService.LOGIN_ENDPOINT);
          }, this.redirectDelay);

        }
        else if(title == 'Profile')
        {
          //console.log("Profile : menuService.onItemClick");
          setTimeout(() => {
            return this.router.navigateByUrl(this.constantService.PROFILE_ENDPOINT);
          }, this.redirectDelay);
        }
      }
      );

 
      //console.log("Header : NGOnIt : Checking values");
      //console.log(localStorage.getItem("token"));
      //console.log(localStorage.getItem("email"));
      //console.log(localStorage.getItem("organization"));
   
    
  }

  loadAllEmployeesData()
  {

    let destroy$ = new Subject<void>();
    // console.log("loadAllEmployeesData");

    this.employeeService.getAllEmployeesBasicInfoByOrganization(localStorage.getItem("organization"))
    .pipe(takeUntil(destroy$))
    .subscribe({
      next: (result) => {
               //What should happen when it is saved
               if (result != null)
               {
                this.messageListDataService.allEmployeesData = [... JSON.parse(JSON.stringify(result))];

                
                  // this.messageListDataService.allEmployeesData.forEach((value:any)=>{
                  //   value.presence = "danger";
                  //   value.state = "terminated";
                  //   value.dotClass = "dotOffline";
                  //   value.channel = "none";
                  // });

               }
               else{
                this.messageListDataService.allEmployeesData = [];
               }
               destroy$.next();
               destroy$.complete();
      },
      error: err => {
      // console.log("Error : "+ JSON.stringify(err));
        this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
      });
    
  }

  loadAllAssignedWhatsAppNumber(orgData)
  {

    let destroy$ = new Subject<void>();
    console.log("loadAllAssignedWhatsAppNumber in header component");

    this.whatsappNumberService.findAllByEmployeeInExtensionAccessListOrAdmin(localStorage.getItem("organization"),ConstantsService.user.extension)
    .pipe(takeUntil(destroy$))
    .subscribe({
      next: (result) => {
               //What should happen when it is saved
               if (result != null)
               {
                this.whatsappDataServiceService.allNumbersData = [... JSON.parse(JSON.stringify(result))];
               }
               else{
                this.whatsappDataServiceService.allNumbersData = [];
               }

                //  console.log("Inside header component, starting stomp")
                this.stompService.createClient(orgData); 
                
               destroy$.next();
               destroy$.complete();
      },
      error: err => {
      // console.log("Error : "+ JSON.stringify(err));
        this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
      });
    
  }


  setMenuData()
  {
    this.organizationService.getOrganizationalData(ConstantsService.user.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: allData => {
      //  console.log("getOrganizationalData");
        // console.log('allData : '+JSON.stringify(allData));
       
        let parsedData = JSON.parse(JSON.stringify(allData));
        ConstantsService.organizationData = parsedData;
        let fetchedMenu = parsedData.menuAccess;
        let triggerBrowserPhone: boolean = parsedData.enableCalling;
        // console.log('fetchedMenu : '+JSON.parse(JSON.stringify(fetchedMenu)));
        const menu:NbMenuItem[] = JSON.parse(fetchedMenu);

        this.menuDataService.menu = menu;
        // console.log('triggerBrowserPhone : '+triggerBrowserPhone);

        if(triggerBrowserPhone)
        {
          this.browserPhoneService.sendAync.next('createSipUserAgent');
          //Detects few variables in headerVariable service
          this.utilsService.detectDevices();

          //Setting up varible values aysn. This is an example on how observers are created to do parallel job
          this.headerVariableService.setVideoOrientationVariable();
          this.headerVariableService.setVideoQualityVariable();
          this.headerVariableService.setVideoFrameRateVariable();
          this.headerVariableService.setAutoGainControlVariable();
          this.headerVariableService.setEchoCancellationVariable();
          this.headerVariableService.setNoiseSeperationVariable();
        }
        else{
          console.log("Calling is disabled, hence not starting SIP Agent, also not asking permissions");
        }

        this.loadAllAssignedWhatsAppNumber(parsedData);

      },
      error: err => {
       // console.log("Error : "+ JSON.stringify(err));
        this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
    });
  }

  openMessageDialog()
  {
    const hasBackdrop = this.hasBackdrop;
    if(!this.messageDialogOpen)
    {
      this.dialogService.open(MessageListComponent, { hasBackdrop,
        context: {
        },
      }).onClose.subscribe(response => {
        this.messageDialogOpen = false;
      });
      this.messageDialogOpen = true;
    }
  }

  openVideoDialog()
  {
    const hasBackdrop = this.hasBackdrop;
    if(!this.videoDialogOpen)
    {
      this.dialogService.open(VideoDialogComponent, { hasBackdrop,
        context: {
        },
      }).onClose.subscribe(response => {
        this.videoDialogOpen = false;
      });
      this.videoDialogOpen = true;
    }
    
  }

  ngOnChanges(changes: SimpleChanges): void {
  //console.log("header-ng-onChanges");
  }

  ngOnDestroy() {

    try{
        console.log("header-OnDestroy");
        console.log('Unregister browser phone');
        this.registerService.unregister(this.browserPhoneService.userAgent,this.browserPhoneService.lang,false);
    }
    catch(e)
    {
      console.log("Error while deregistring browser phone")
      console.log(e);
    }

    try{
        console.log('Disconnect stomp service');
        this.stompService.disconnect();
    }
    catch(e)
    {
      console.log("Error while deregistring stomp connection")
      console.log(e);
    }

    this.destroy$.next();
    this.destroy$.complete();
  }



  changeTheme(themeName: string) {

    if(themeName != "" || themeName != undefined)
    this.themeService.changeTheme(themeName);
    else
    this.themeService.changeTheme("default");
    
    if(themeName.toLowerCase() === 'dark'){
      console.log("Changing logo to dark");
      this.isWhite= false;
      this.headerVariableService.userBackgroundColor = "#414b66";
    }
    else if(themeName.toLowerCase() === 'cosmic'){
      console.log("Changing logo to cosmic");
      this.isWhite= false;
      this.headerVariableService.userBackgroundColor = "#686891";
    }
    else{
      console.log("Changing logo to white");
      this.isWhite= true;
      this.headerVariableService.userBackgroundColor = "#efefef";
    }


    this.employeeService.updateSelfThemeByOrganization({email:localStorage.getItem("email"),organization:localStorage.getItem("organization"),value:themeName})
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: result => {
               //What should happen when it is saved
      },
      error: err => {
      // console.log("Error : "+ JSON.stringify(err));
        this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
      }
      });

  }

  toggleSidebar(): boolean {
    this.sidebarService.toggle(true, 'menu-sidebar');
    this.layoutService.changeLayoutSize();

    return false;
  }

  navigateHome() {
    console.log("navigateHome");
    setTimeout(() => {
            return this.router.navigateByUrl(this.constantService.PROFILE_ENDPOINT);
          }, this.redirectDelay);
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
