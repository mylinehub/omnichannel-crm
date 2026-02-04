import { TestBed } from '@angular/core/testing';

import { NotificationDataService } from './notification-data.service';

describe('NotificationDataService', () => {
  let service: NotificationDataService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(NotificationDataService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
