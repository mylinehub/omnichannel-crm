import { TestBed } from '@angular/core/testing';

import { PhoneMusicService } from './phone-music.service';

describe('PhoneMusicService', () => {
  let service: PhoneMusicService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PhoneMusicService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
