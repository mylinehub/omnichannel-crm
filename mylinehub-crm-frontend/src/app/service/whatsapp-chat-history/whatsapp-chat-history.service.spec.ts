import { TestBed } from '@angular/core/testing';

import { WhatsappChatHistoryService } from './whatsapp-chat-history.service';

describe('WhatsappChatHistoryService', () => {
  let service: WhatsappChatHistoryService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(WhatsappChatHistoryService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
