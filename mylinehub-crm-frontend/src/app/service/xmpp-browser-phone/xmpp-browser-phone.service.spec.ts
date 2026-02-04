import { TestBed } from '@angular/core/testing';

import { XmppBrowserPhoneService } from './xmpp-browser-phone.service';

describe('XmppBrowserPhoneService', () => {
  let service: XmppBrowserPhoneService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(XmppBrowserPhoneService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
