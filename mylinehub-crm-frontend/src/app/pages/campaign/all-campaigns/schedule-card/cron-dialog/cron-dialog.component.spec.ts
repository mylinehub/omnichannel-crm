import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CronDialogComponent } from './cron-dialog.component';

describe('CronDialogComponent', () => {
  let component: CronDialogComponent;
  let fixture: ComponentFixture<CronDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CronDialogComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CronDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
