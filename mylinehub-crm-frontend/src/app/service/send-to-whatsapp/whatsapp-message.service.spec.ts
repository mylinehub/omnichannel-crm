import { TestBed } from '@angular/core/testing';

import { WhatsappMessageService } from './whatsapp-message.service';

describe('WhatsappMessageService', () => {
  let service: WhatsappMessageService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(WhatsappMessageService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
