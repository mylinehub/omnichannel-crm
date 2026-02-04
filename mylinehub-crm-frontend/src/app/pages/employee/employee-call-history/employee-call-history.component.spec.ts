import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EmployeeCallHistoryComponent } from './employee-call-history.component';

describe('EmployeeCallHistoryComponent', () => {
  let component: EmployeeCallHistoryComponent;
  let fixture: ComponentFixture<EmployeeCallHistoryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EmployeeCallHistoryComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EmployeeCallHistoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
