import { TestBed } from '@angular/core/testing';

import { WhatsappReportDataServiceService } from './whatsapp-report-data-service.service';

describe('WhatsappReportDataServiceService', () => {
  let service: WhatsappReportDataServiceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(WhatsappReportDataServiceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
