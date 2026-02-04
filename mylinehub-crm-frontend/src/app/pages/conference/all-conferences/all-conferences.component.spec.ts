import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AllConferencesComponent } from './all-conferences.component';

describe('AllConferencesComponent', () => {
  let component: AllConferencesComponent;
  let fixture: ComponentFixture<AllConferencesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AllConferencesComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AllConferencesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
