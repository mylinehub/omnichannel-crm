import { TestBed } from '@angular/core/testing';

import { WhatsappSpeechRecognitionService } from './whatsapp-speech-recognition.service';

describe('WhatsappSpeechRecognitionService', () => {
  let service: WhatsappSpeechRecognitionService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(WhatsappSpeechRecognitionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
