import { TestBed } from '@angular/core/testing';

import { SshConnectionService } from './ssh-connection.service';

describe('SshConnectionService', () => {
  let service: SshConnectionService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SshConnectionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
