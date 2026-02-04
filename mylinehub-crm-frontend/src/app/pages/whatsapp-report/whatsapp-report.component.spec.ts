import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WhatsappReportComponent } from './whatsapp-report.component';

describe('WhatsappReportComponent', () => {
  let component: WhatsappReportComponent;
  let fixture: ComponentFixture<WhatsappReportComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ WhatsappReportComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(WhatsappReportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
