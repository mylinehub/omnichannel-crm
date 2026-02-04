import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NgxLoginComponent } from './login.component';

describe('LoginComponent', () => {
  let component: NgxLoginComponent;
  let fixture: ComponentFixture<NgxLoginComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NgxLoginComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NgxLoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
