import { TestBed } from '@angular/core/testing';

import { WhatsappNumberService } from './whatsapp-number.service';

describe('WhatsappNumberService', () => {
  let service: WhatsappNumberService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(WhatsappNumberService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
