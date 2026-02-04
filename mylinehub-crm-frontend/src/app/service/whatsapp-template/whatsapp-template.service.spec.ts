import { TestBed } from '@angular/core/testing';

import { WhatsappTemplateService } from './whatsapp-template.service';

describe('WhatsappTemplateService', () => {
  let service: WhatsappTemplateService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(WhatsappTemplateService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
