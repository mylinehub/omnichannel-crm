import { TestBed } from '@angular/core/testing';

import { WhatsappDataServiceService } from './whatsapp-data-service.service';

describe('WhatsappDataServiceService', () => {
  let service: WhatsappDataServiceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(WhatsappDataServiceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
