import { TestBed } from '@angular/core/testing';

import { NotificationListDataService } from './notification-list-data.service';

describe('NotificationListDataService', () => {
  let service: NotificationListDataService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(NotificationListDataService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
