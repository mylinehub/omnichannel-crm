import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MyAbsenteeismComponent } from './my-absenteeism.component';

describe('MyAbsenteeismComponent', () => {
  let component: MyAbsenteeismComponent;
  let fixture: ComponentFixture<MyAbsenteeismComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MyAbsenteeismComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MyAbsenteeismComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
