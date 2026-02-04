import { ApiHttpService } from './../../../service/http/api-http.service';
import { ConstantsService } from './../../../service/constants/constants.service';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

const TOTAL_PAGES = 7;

export class SearchPost {
  title: string;
  link: string;
  text: string;
}

@Injectable({
  providedIn: 'root'
})
export class SearchService {


news : Array<SearchPost>  = []; 


 constructor( protected constService : ConstantsService,
               protected httpService : ApiHttpService,
               private http: HttpClient,) { }

    
load(): string {

  //console.log("Inside load service method");
    this.news = [];
    var words:any;
    words = ConstantsService.searchContext.split(" ");
   
    console.log("Number of search words : " ,words);

      this.constService.searchPosts.forEach( (element,index) => {

        var countThree : number = 0;
        var countFour : number = 0;
        var countFiveOrMore : number = 0;

       for (let i = 0; i < words.length; i++) {  

          if (((element.title.includes(words[i]))||(element.link.includes(words[i]))||(element.text.includes(words[i]))))
          {
            //console.log("3 letter word");
            //countThree = countThree + 1;
            // if(countThree > 3)
            // {
            //console.log("Adding Data To Array for 3 letter");
             
            // }

            this.news.push(JSON.parse(JSON.stringify(element)));
            break;
            
          }

         /* if (((element.title.includes(words[i]))||(element.link.includes(words[i]))||(element.text.includes(words[i]))) && words[i].length == 4)
          {
           // console.log("4 Letter work found");
            countFour = countFour + 1;
            if(countFour > 1)
            {
             // console.log("Adding Data To Array for 4 letter");
              this.news.push(JSON.parse(JSON.stringify(element)));
              break;
            }
          }

          if (((element.title.includes(words[i]))||(element.link.includes(words[i]))||(element.text.includes(words[i]))) && words[i].length >4)
          {
           // console.log("5 or more letter word");
            
            countFiveOrMore = countFiveOrMore + 1;
            if(countFiveOrMore > 0)
            {
           //   console.log("Adding Data To Array for 5 or more letter");

              this.news.push(JSON.parse(JSON.stringify(element)));
              break;
            }
          }*/

        };

        console.log("this.news : "+index+" : " ,this.news);

      }
      
      );

      return JSON.stringify(this.news);
  }
}








