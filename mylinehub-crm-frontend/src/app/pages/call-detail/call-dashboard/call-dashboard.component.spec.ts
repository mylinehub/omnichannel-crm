import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CallDashboardComponent } from './call-dashboard.component';

describe('CallDashboardComponent', () => {
  let component: CallDashboardComponent;
  let fixture: ComponentFixture<CallDashboardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CallDashboardComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CallDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
