import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SubmitBulkEmployeeComponent } from './submit-bulk-employee.component';

describe('SubmitBulkEmployeeComponent', () => {
  let component: SubmitBulkEmployeeComponent;
  let fixture: ComponentFixture<SubmitBulkEmployeeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SubmitBulkEmployeeComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SubmitBulkEmployeeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
