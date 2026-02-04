import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProductInterestDialogComponent } from './product-interest-dialog.component';

describe('ProductInterestDialogComponent', () => {
  let component: ProductInterestDialogComponent;
  let fixture: ComponentFixture<ProductInterestDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ProductInterestDialogComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProductInterestDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
