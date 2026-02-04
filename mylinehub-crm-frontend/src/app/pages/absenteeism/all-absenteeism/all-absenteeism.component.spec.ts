import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AllAbsenteeismComponent } from './all-absenteeism.component';

describe('AllAbsenteeismComponent', () => {
  let component: AllAbsenteeismComponent;
  let fixture: ComponentFixture<AllAbsenteeismComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AllAbsenteeismComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AllAbsenteeismComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
