import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FixedDateDialogComponent } from './fixed-date-dialog.component';

describe('FixedDateDialogComponent', () => {
  let component: FixedDateDialogComponent;
  let fixture: ComponentFixture<FixedDateDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FixedDateDialogComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FixedDateDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
