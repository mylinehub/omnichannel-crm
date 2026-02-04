import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminResetExtensionPasswordComponent } from './admin-reset-extension-password.component';

describe('AdminResetExtensionPasswordComponent', () => {
  let component: AdminResetExtensionPasswordComponent;
  let fixture: ComponentFixture<AdminResetExtensionPasswordComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AdminResetExtensionPasswordComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminResetExtensionPasswordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
