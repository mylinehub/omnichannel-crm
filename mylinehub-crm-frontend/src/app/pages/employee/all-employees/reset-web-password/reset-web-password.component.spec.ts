import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ResetWebPasswordComponent } from './reset-web-password.component';

describe('ResetWebPasswordComponent', () => {
  let component: ResetWebPasswordComponent;
  let fixture: ComponentFixture<ResetWebPasswordComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ResetWebPasswordComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ResetWebPasswordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
