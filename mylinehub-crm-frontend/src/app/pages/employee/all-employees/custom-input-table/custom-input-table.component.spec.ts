import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CustomInputTableComponent } from './custom-input-table.component';

describe('CustomInputTableComponent', () => {
  let component: CustomInputTableComponent;
  let fixture: ComponentFixture<CustomInputTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CustomInputTableComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CustomInputTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
