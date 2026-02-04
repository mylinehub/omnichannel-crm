import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NSecCustomerDialogComponent } from './n-sec-customer-dialog.component';

describe('NSecCustomerDialogComponent', () => {
  let component: NSecCustomerDialogComponent;
  let fixture: ComponentFixture<NSecCustomerDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NSecCustomerDialogComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NSecCustomerDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
