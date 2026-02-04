import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BrowserPhoneActionComponent } from './browser-phone-action.component';

describe('BrowserPhoneActionComponent', () => {
  let component: BrowserPhoneActionComponent;
  let fixture: ComponentFixture<BrowserPhoneActionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ BrowserPhoneActionComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BrowserPhoneActionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
