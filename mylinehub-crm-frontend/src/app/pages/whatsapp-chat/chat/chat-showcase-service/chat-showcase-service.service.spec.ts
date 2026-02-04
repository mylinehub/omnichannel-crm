import { TestBed } from '@angular/core/testing';

import { ChatShowcaseServiceService } from './chat-showcase-service.service';

describe('ChatShowcaseServiceService', () => {
  let service: ChatShowcaseServiceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ChatShowcaseServiceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
