import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AllCostsComponent } from './all-costs.component';

describe('AllCostsComponent', () => {
  let component: AllCostsComponent;
  let fixture: ComponentFixture<AllCostsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AllCostsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AllCostsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
