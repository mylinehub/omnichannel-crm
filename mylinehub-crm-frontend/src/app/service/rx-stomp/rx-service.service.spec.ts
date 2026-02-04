import { TestBed } from '@angular/core/testing';

import { RxServiceService } from './rx-service.service';

describe('RxServiceService', () => {
  let service: RxServiceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(RxServiceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
