import { TestBed } from '@angular/core/testing';

import { MenuDataService } from './menu-data.service';

describe('MenuDataService', () => {
  let service: MenuDataService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MenuDataService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
