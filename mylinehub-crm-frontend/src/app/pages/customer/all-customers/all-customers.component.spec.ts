import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AllCustomersComponent } from './all-customers.component';

describe('AllCustomersComponent', () => {
  let component: AllCustomersComponent;
  let fixture: ComponentFixture<AllCustomersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AllCustomersComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AllCustomersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
