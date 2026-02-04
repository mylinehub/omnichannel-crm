import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RunCampaignViewComponent } from './run-campaign-view.component';

describe('RunCampaignViewComponent', () => {
  let component: RunCampaignViewComponent;
  let fixture: ComponentFixture<RunCampaignViewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RunCampaignViewComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RunCampaignViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
