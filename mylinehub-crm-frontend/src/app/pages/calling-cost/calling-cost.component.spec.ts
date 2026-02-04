import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CallingCostComponent } from './calling-cost.component';

describe('CallingCostComponent', () => {
  let component: CallingCostComponent;
  let fixture: ComponentFixture<CallingCostComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CallingCostComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CallingCostComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
