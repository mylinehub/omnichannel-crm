import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PreviewCustomerComponent } from './preview-customer.component';

describe('PreviewCustomerComponent', () => {
  let component: PreviewCustomerComponent;
  let fixture: ComponentFixture<PreviewCustomerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PreviewCustomerComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PreviewCustomerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
