import { TestBed } from '@angular/core/testing';

import { SipProviderService } from './sip-provider.service';

describe('SipProviderService', () => {
  let service: SipProviderService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SipProviderService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
