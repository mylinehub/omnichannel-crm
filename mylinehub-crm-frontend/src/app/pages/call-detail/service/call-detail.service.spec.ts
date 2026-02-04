import { TestBed } from '@angular/core/testing';

import { CallDetailService } from './call-detail.service';

describe('CallDetailService', () => {
  let service: CallDetailService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CallDetailService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
