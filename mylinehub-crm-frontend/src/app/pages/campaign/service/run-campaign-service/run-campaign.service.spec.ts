import { TestBed } from '@angular/core/testing';

import { RunCampaignService } from './run-campaign.service';

describe('RunCampaignService', () => {
  let service: RunCampaignService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(RunCampaignService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
