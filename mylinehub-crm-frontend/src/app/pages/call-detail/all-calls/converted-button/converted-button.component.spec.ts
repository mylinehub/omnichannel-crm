import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConvertedButtonComponent } from './converted-button.component';

describe('ConvertedButtonComponent', () => {
  let component: ConvertedButtonComponent;
  let fixture: ComponentFixture<ConvertedButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ConvertedButtonComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ConvertedButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
