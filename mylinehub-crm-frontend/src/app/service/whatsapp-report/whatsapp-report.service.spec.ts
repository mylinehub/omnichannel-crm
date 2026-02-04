import { TestBed } from '@angular/core/testing';

import { WhatsappReportService } from './whatsapp-report.service';

describe('WhatsappReportService', () => {
  let service: WhatsappReportService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(WhatsappReportService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
