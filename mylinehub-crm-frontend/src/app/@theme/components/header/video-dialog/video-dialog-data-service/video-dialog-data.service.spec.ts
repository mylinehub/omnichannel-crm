import { TestBed } from '@angular/core/testing';

import { VideoDialogDataService } from './video-dialog-data.service';

describe('VideoDialogDataService', () => {
  let service: VideoDialogDataService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(VideoDialogDataService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
