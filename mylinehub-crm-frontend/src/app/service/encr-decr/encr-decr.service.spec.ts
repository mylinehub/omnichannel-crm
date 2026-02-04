import { TestBed } from '@angular/core/testing';

import { EncrDecrService } from './encr-decr.service';

describe('EncrDecrService', () => {
  let service: EncrDecrService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(EncrDecrService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
