import { TestBed } from '@angular/core/testing';

import { WhatsappPromptService } from './whatsapp-prompt.service';

describe('WhatsappPromptService', () => {
  let service: WhatsappPromptService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(WhatsappPromptService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
