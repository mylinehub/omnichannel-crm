import { TestBed } from '@angular/core/testing';

import { WhatsappProjectService } from './whatsapp-project.service';

describe('WhatsappProjectService', () => {
  let service: WhatsappProjectService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(WhatsappProjectService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
