import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AllIvrsComponent } from './all-ivrs.component';

describe('AllIvrsComponent', () => {
  let component: AllIvrsComponent;
  let fixture: ComponentFixture<AllIvrsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AllIvrsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AllIvrsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
