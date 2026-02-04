import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ScheduleCustomerCardComponent } from './schedule-customer-card.component';

describe('ScheduleCustomerCardComponent', () => {
  let component: ScheduleCustomerCardComponent;
  let fixture: ComponentFixture<ScheduleCustomerCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ScheduleCustomerCardComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ScheduleCustomerCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
