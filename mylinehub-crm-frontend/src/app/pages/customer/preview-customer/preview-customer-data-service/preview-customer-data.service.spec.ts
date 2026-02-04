import { TestBed } from '@angular/core/testing';

import { PreviewCustomerDataService } from './preview-customer-data.service';

describe('PreviewCustomerDataService', () => {
  let service: PreviewCustomerDataService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PreviewCustomerDataService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
