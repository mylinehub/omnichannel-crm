import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MonitorEmployeesComponent } from './monitor-employees.component';

describe('MonitorEmployeesComponent', () => {
  let component: MonitorEmployeesComponent;
  let fixture: ComponentFixture<MonitorEmployeesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MonitorEmployeesComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MonitorEmployeesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
