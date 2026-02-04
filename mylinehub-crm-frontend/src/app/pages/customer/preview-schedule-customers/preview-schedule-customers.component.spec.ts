import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PreviewScheduleCustomersComponent } from './preview-schedule-customers.component';

describe('PreviewScheduleCustomersComponent', () => {
  let component: PreviewScheduleCustomersComponent;
  let fixture: ComponentFixture<PreviewScheduleCustomersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PreviewScheduleCustomersComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PreviewScheduleCustomersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
