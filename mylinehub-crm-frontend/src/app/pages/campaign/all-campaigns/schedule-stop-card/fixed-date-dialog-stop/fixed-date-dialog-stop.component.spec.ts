import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FixedDateDialogStopComponent } from './fixed-date-dialog-stop.component';

describe('FixedDateDialogComponent', () => {
  let component: FixedDateDialogStopComponent;
  let fixture: ComponentFixture<FixedDateDialogStopComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FixedDateDialogStopComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FixedDateDialogStopComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
