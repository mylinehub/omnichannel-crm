import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BrowserPhoneComponent } from './browser-phone.component';

describe('BrowserPhoneComponent', () => {
  let component: BrowserPhoneComponent;
  let fixture: ComponentFixture<BrowserPhoneComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ BrowserPhoneComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BrowserPhoneComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
