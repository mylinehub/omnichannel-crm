import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CellScrollComponent } from './cell-scroll.component';

describe('CellScrollComponent', () => {
  let component: CellScrollComponent;
  let fixture: ComponentFixture<CellScrollComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CellScrollComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CellScrollComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
