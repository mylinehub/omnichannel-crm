import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SipProviderComponent } from './sip-provider.component';

describe('SipProviderComponent', () => {
  let component: SipProviderComponent;
  let fixture: ComponentFixture<SipProviderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SipProviderComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SipProviderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
