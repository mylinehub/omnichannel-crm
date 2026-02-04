import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WhatsappChatComponent } from './whatsapp-chat.component';

describe('WhatsappChatComponent', () => {
  let component: WhatsappChatComponent;
  let fixture: ComponentFixture<WhatsappChatComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ WhatsappChatComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(WhatsappChatComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
