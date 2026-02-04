import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ResetExtensionPasswordComponent } from './reset-extension-password.component';

describe('ResetExtensionPasswordComponent', () => {
  let component: ResetExtensionPasswordComponent;
  let fixture: ComponentFixture<ResetExtensionPasswordComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ResetExtensionPasswordComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ResetExtensionPasswordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
