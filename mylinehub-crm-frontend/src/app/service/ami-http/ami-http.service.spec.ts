import { TestBed } from '@angular/core/testing';

import { AmiHttpService } from './ami-http.service';

describe('AmiHttpService', () => {
  let service: AmiHttpService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AmiHttpService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
