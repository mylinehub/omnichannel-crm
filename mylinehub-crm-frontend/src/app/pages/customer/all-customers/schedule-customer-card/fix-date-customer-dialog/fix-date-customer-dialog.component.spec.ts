import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FixDateCustomerDialogComponent } from './fix-date-customer-dialog.component';

describe('FixDateCustomerDialogComponent', () => {
  let component: FixDateCustomerDialogComponent;
  let fixture: ComponentFixture<FixDateCustomerDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FixDateCustomerDialogComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FixDateCustomerDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
