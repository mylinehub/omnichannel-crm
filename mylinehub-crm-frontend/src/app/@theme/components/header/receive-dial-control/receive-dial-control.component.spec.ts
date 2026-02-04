import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReceiveDialControlComponent } from './receive-dial-control.component';

describe('ReceiveDialControlComponent', () => {
  let component: ReceiveDialControlComponent;
  let fixture: ComponentFixture<ReceiveDialControlComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ReceiveDialControlComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReceiveDialControlComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
