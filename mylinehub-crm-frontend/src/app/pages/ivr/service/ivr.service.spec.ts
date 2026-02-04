import { TestBed } from '@angular/core/testing';

import { IvrService } from './ivr.service';

describe('IvrService', () => {
  let service: IvrService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(IvrService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
