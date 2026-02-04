import { TestBed } from '@angular/core/testing';

import { BrowserPhoneService } from './browser-phone.service';

describe('BrowserPhoneService', () => {
  let service: BrowserPhoneService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(BrowserPhoneService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
