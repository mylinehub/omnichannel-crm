import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AllEmployeesComponent } from './all-employees.component';

describe('AllEmployeesComponent', () => {
  let component: AllEmployeesComponent;
  let fixture: ComponentFixture<AllEmployeesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AllEmployeesComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AllEmployeesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
