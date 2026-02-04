import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CronDialogStopComponent } from './cron-dialog-stop.component';

describe('CronDialogComponent', () => {
  let component: CronDialogStopComponent;
  let fixture: ComponentFixture<CronDialogStopComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CronDialogStopComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CronDialogStopComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
