import { TestBed } from '@angular/core/testing';

import { WhatsappTemplateVariableService } from './whatsapp-template-variable.service';

describe('WhatsappTemplateVariableService', () => {
  let service: WhatsappTemplateVariableService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(WhatsappTemplateVariableService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
