import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CallHistoryComponent } from './call-history.component';

describe('CallHistoryComponent', () => {
  let component: CallHistoryComponent;
  let fixture: ComponentFixture<CallHistoryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CallHistoryComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CallHistoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
