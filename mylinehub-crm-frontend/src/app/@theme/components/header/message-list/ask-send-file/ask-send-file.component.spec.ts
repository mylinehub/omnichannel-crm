import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AskSendFileComponent } from './ask-send-file.component';

describe('AskSendFileComponent', () => {
  let component: AskSendFileComponent;
  let fixture: ComponentFixture<AskSendFileComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AskSendFileComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AskSendFileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
