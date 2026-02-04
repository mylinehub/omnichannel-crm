import { Component, OnInit, SimpleChanges } from '@angular/core';
import { ConstantsService } from './../../../service/constants/constants.service';
import { SearchService } from './../service/search.service';

@Component({
  selector: 'ngx-search-result',
  templateUrl: './search-result.component.html',
  styleUrls: ['./search-result.component.scss']
})
export class SearchResultComponent implements OnInit {

  searchCard = {
    news: [],
    placeholders: [],
    loading: false,
    pageToLoadNext: 1,
  };
  pageSize = 10;
  value :number = 500;
  noElement : boolean = true;

  constructor(private constantsService:ConstantsService,
              private searchService:SearchService) { }

              // ngOnChanges() {
              //   console.log(`ngOnChanges - data is`);
              // }

              ngDoCheck() {
                //console.log("ngDoCheck")
                if(ConstantsService.searchNow)
                {
                  ConstantsService.searchNow = false;
                  //console.log("Performing Search");
                  //console.log("Search Text : " +ConstantsService.searchContext);
                  this.loadNext();
                 // console.log("After Load Next");
                }
              }
            
              // ngAfterContentInit() {
              //   console.log("ngAfterContentInit");
              // }
            
              // ngAfterContentChecked() {
              //   console.log("ngAfterContentChecked");
              // }
            
              // ngAfterViewInit() {
              //   console.log("ngAfterViewInit");
              // }
            
              // ngAfterViewChecked() {
              //   console.log("ngAfterViewChecked");
              // }
            
              // ngOnDestroy() {
              //   console.log("ngOnDestroy");
              // }
              
  ngOnInit(): void {

   // console.log("Search button is clicked in search component ngOnIt");
    //console.log("Search Text : " +ConstantsService.searchContext);
    this.loadNext();

  }

  loadNext() {

   // console.log("Inside Load Next");

    // if (this. searchCard.loading) { 
    //   console.log("Search Card Loading True");
    //   return; }

   this. searchCard.loading = true;

   //console.log("After loading");

   this. searchCard.placeholders = [];

   //console.log("After place holder");

   var value : string  = this.searchService.load();
   
  // console.log("After fetching value from load service");

   //console.log('value',value);
  // console.log(JSON.stringify(this.searchCard.news));

   
   if(value != "[]")
   {
       //console.log("value is defined");
      this.searchCard.news =JSON.parse(value);
   }
   else{

   // console.log("value is undefined");

      this.searchCard.news =[
        {
          "title": "No data found",
          "link": "https://mylinehub.com",
          "text": "No result found. Try to search some other string In case this does not resolve this issues get in touch with our team at mylinehub.com"
        },
      ];
   }

  // console.log('this.searchCard.news',this.searchCard.news);
 
  // console.log("Search Values : " + this.searchService.load());
  // this. searchCard.loading = false;
   /* this.searchService.load(this. searchCard.pageToLoadNext, this.pageSize)
      .subscribe(next => {

        console.log("Search Starts");
        console.log(JSON.stringify(next));

        this. searchCard.placeholders = [];
        this. searchCard.news.push(...next);
        this. searchCard.loading = false;
        this. searchCard.pageToLoadNext++;
      });*/
    
   // console.log("cardData : " + JSON.stringify(cardData));
   // console.log("searchCard : " + JSON.stringify(this.searchCard));

   /*  if(this.noElement)
     {
        console.log("No value found for searching");

        cardData.news.push(
         [{
            "title":"No results found",
            "link": "https://mylinehub.com",
            "text": "The text you searched for was not found. Please try searcgning for related words. Or else please reach out to mylinehub administration team for support."
          }]);

        cardData.loading = false;
        cardData.pageToLoadNext++;

     }*/

  }

}
