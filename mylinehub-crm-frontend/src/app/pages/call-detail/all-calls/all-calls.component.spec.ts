import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AllCallsComponent } from './all-calls.component';

describe('AllCallsComponent', () => {
  let component: AllCallsComponent;
  let fixture: ComponentFixture<AllCallsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AllCallsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AllCallsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
