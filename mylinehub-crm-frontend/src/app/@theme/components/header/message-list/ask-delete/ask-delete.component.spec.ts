import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AskDeleteComponent } from './ask-delete.component';

describe('AskDeleteComponent', () => {
  let component: AskDeleteComponent;
  let fixture: ComponentFixture<AskDeleteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AskDeleteComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AskDeleteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
