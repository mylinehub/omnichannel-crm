import { TestBed } from '@angular/core/testing';

import { PropertyInventoryService } from './property-inventory.service';

describe('PropertyInventoryService', () => {
  let service: PropertyInventoryService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PropertyInventoryService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
