import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AllErrorsComponent } from './all-errors.component';

describe('AllErrorsComponent', () => {
  let component: AllErrorsComponent;
  let fixture: ComponentFixture<AllErrorsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AllErrorsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AllErrorsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
