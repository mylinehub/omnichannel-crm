import { TestBed } from '@angular/core/testing';

import { PreviewScheduleDataServiceService } from './preview-schedule-data-service.service';

describe('PreviewScheduleDataServiceService', () => {
  let service: PreviewScheduleDataServiceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PreviewScheduleDataServiceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
