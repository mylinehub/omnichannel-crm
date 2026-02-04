import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CronCustomerDialogComponent } from './cron-customer-dialog.component';

describe('CronCustomerDialogComponent', () => {
  let component: CronCustomerDialogComponent;
  let fixture: ComponentFixture<CronCustomerDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CronCustomerDialogComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CronCustomerDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
