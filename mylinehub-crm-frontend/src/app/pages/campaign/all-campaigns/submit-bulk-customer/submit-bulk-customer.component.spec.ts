import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SubmitBulkCustomerComponent } from './submit-bulk-customer.component';

describe('SubmitBulkCustomerComponent', () => {
  let component: SubmitBulkCustomerComponent;
  let fixture: ComponentFixture<SubmitBulkCustomerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SubmitBulkCustomerComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SubmitBulkCustomerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
