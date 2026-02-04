import { TestBed } from '@angular/core/testing';

import { AmiConnectionService } from './ami-connection.service';

describe('AmiConnectionService', () => {
  let service: AmiConnectionService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AmiConnectionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
