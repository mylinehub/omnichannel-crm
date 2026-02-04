import { TestBed } from '@angular/core/testing';

import { AbsenteeismService } from './absenteeism.service';

describe('AbsenteeismService', () => {
  let service: AbsenteeismService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AbsenteeismService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
