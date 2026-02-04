import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AmiConnectionComponent } from './ami-connection.component';

describe('AmiConnectionComponent', () => {
  let component: AmiConnectionComponent;
  let fixture: ComponentFixture<AmiConnectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AmiConnectionComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AmiConnectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
