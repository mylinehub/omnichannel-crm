import { TestBed } from '@angular/core/testing';

import { FranchiseManagementService } from './franchise-management.service';

describe('FranchiseManagementService', () => {
  let service: FranchiseManagementService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(FranchiseManagementService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
