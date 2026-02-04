import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WhatsappProjectComponent } from './whatsapp-project.component';

describe('WhatsappProjectComponent', () => {
  let component: WhatsappProjectComponent;
  let fixture: ComponentFixture<WhatsappProjectComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ WhatsappProjectComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(WhatsappProjectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
