import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddCallComponent } from './add-call.component';

describe('AddCallComponent', () => {
  let component: AddCallComponent;
  let fixture: ComponentFixture<AddCallComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AddCallComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddCallComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
