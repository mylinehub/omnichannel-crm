import { TestBed } from '@angular/core/testing';

import { WhatsappPromptVariableService } from './whatsapp-prompt-variable.service';

describe('WhatsappPromptVariableService', () => {
  let service: WhatsappPromptVariableService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(WhatsappPromptVariableService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
