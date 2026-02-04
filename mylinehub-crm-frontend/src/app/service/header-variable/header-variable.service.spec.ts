import { TestBed } from '@angular/core/testing';

import { HeaderVariableService } from './header-variable.service';

describe('HeaderVariableService', () => {
  let service: HeaderVariableService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(HeaderVariableService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
