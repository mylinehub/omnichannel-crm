import { TestBed } from '@angular/core/testing';

import { CallingCostService } from './calling-cost.service';

describe('CallingCostService', () => {
  let service: CallingCostService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CallingCostService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
