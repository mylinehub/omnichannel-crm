import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AllDepartmentsComponent } from './all-departments.component';

describe('AllDepartmentsComponent', () => {
  let component: AllDepartmentsComponent;
  let fixture: ComponentFixture<AllDepartmentsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AllDepartmentsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AllDepartmentsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
