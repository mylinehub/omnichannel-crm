import { TestBed } from '@angular/core/testing';

import { WhatsappdictService } from './whatsappdict.service';

describe('WhatsappdictService', () => {
  let service: WhatsappdictService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(WhatsappdictService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
