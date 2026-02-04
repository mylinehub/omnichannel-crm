import { Component, ElementRef, Input, OnInit, ViewChild } from '@angular/core';

@Component({
  selector: 'ngx-image-corousel',
  templateUrl: './image-corousel.component.html',
  styleUrls: ['./image-corousel.component.scss']
})
export class ImageCorouselComponent implements OnInit {

  @Input() interestedProducts:any;
  @Input() currentRecord:any;
  @Input() base64InterestedProductImageData;

  // cards:any = [1, 2, 3, 4, 5, 6, 7, 8];
  innerTransform:any= "";
  innerTransition:any="";
  step:any= '';
  transitioning:any = false;
  outerCarousel:HTMLElement;
  carousel:HTMLElement;
  inner:HTMLElement;


  constructor() {
   }

  ngOnInit(): void {
    // //console.log("ImageCorouselComponent interestedProducts :",this.interestedProducts);
    // //console.log("ImageCorouselComponent currentRecord :",this.currentRecord);

    if(this.interestedProducts == undefined || this.interestedProducts == '' || this.interestedProducts == null || this.interestedProducts.length ==0)
    {
      document.getElementById('prev').setAttribute("disabled","disabled");
      document.getElementById('next').setAttribute("disabled","disabled");
    }

  }

  setStep () {

    //console.log("setStep");
    const innerWidth = this.inner.scrollWidth;
    //console.log("innerWidth :",innerWidth);
    const totalCards = this.interestedProducts.length
    //console.log("totalCards :",totalCards);
    this.step = (innerWidth / totalCards) + "px";
    //console.log("this.step :",this.step);
  }

  next () {    
      this.interestedProducts.unshift(this.interestedProducts.pop());
      this.base64InterestedProductImageData.unshift(this.base64InterestedProductImageData.pop());
  }
  prev () {
      this.interestedProducts.push(this.interestedProducts.shift());
      this.base64InterestedProductImageData.push(this.base64InterestedProductImageData.shift());
}

}
