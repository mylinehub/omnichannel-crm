import { TestBed } from '@angular/core/testing';

import { MessageListDataService } from './message-list-data.service';

describe('MessageListDataService', () => {
  let service: MessageListDataService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MessageListDataService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
