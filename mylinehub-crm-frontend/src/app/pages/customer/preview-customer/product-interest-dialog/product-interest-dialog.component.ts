import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { NbDialogRef, NbDialogService } from '@nebular/theme';
import { Subject, takeUntil } from 'rxjs';
import { CustomerService } from '../../service/customer.service';
import { DialogComponent } from '../../../employee/all-employees/dialog/dialog.component';
import { ConstantsService } from '../../../../service/constants/constants.service';
import { ProductService } from '../../../product/service/product.service';

@Component({
  selector: 'ngx-product-interest-dialog',
  templateUrl: './product-interest-dialog.component.html',
  styleUrls: ['./product-interest-dialog.component.scss']
})
export class ProductInterestDialogComponent implements OnInit, OnDestroy {

  heading = 'Update Customer Products Interest';
  @Input() organization:string;
  @Input() currentRecord:string;
  selectedProductList : any = [];
  allProducts:any=[];
  // isSelected = [true,true,true,true,true,true,false,false,false,false,false,false,false];
  // isSelected = true;
  private destroy$: Subject<void> = new Subject<void>();


  constructor(protected ref: NbDialogRef<ProductInterestDialogComponent>,
              private dialogService: NbDialogService,
              private customerService: CustomerService,
              protected constantService : ConstantsService,
              private productService : ProductService,
              // private formBuilder: FormBuilder,
              ) {
                
              }
              
  cancel() {
    this.ref.close();
  }

  ngOnInit(): void {
    this.setAllProducts();
  }


  submit() {
    
    let data : any = {
      id:JSON.parse(JSON.stringify(this.currentRecord)).id,
      email:ConstantsService.user.email,
      organization:this.organization,  
      value:String(this.selectedProductList),
    };

    this.customerService.updateCustomerProductInterests(data)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: data => {
        console.log(data);
        if(String(data) == 'true')
        {
          this.ref.close();
             
        }
        else{
          this.showDialoge('Error','activity-outline','danger', "Please try again."); 
          this.ref.close();
        }

      },
      error: err => {
      // console.log("Error : "+ JSON.stringify(err));
        this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
        this.ref.close();
      }
    });
  }


  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  setAllProducts()
  {
    this.productService.getAllproductsByOrganization(ConstantsService.user.organization)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: allData => {

        if(allData == null)
        {
          //console.log("I am null data");
          this.allProducts = [];
          this.selectedProductList= [];
         
        }
        else
        {
            //console.log("Inside Data : "+ JSON.stringify(allData));
            var arr = JSON.parse(JSON.stringify(allData));
            this.allProducts = [... arr];

            if(JSON.parse(JSON.stringify(this.currentRecord)).interestedProducts != null)
            {
              console.log("JSON.parse(JSON.stringify(this.currentRecord)).interestedProducts.split(',')",JSON.parse(JSON.stringify(this.currentRecord)).interestedProducts.split(','));
              let list = JSON.parse(JSON.stringify(this.currentRecord)).interestedProducts.split(',').map(list => Number(list));
              this.selectedProductList = list;
            }
            else{
              this.selectedProductList= [];
            }
            
            // this.formBuilder.group({
            //   value: new FormControl(this.selectedProductList),
            //  });
          }
      },
      error: err => {
       // console.log("Error : "+ JSON.stringify(err));
        this.showDialoge('Error','activity-outline','danger', JSON.stringify(err)); 
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
