import { TestBed } from '@angular/core/testing';

import { DialLineService } from './dial-line.service';

describe('DialLineService', () => {
  let service: DialLineService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DialLineService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
