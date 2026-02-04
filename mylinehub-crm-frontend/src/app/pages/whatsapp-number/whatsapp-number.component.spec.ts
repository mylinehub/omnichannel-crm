import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WhatsappNumberComponent } from './whatsapp-number.component';

describe('WhatsappNumberComponent', () => {
  let component: WhatsappNumberComponent;
  let fixture: ComponentFixture<WhatsappNumberComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ WhatsappNumberComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(WhatsappNumberComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
