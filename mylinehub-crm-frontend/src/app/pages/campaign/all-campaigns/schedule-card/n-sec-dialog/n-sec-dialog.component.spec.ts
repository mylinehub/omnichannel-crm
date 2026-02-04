import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NSecDialogComponent } from './n-sec-dialog.component';

describe('NSecDialogComponent', () => {
  let component: NSecDialogComponent;
  let fixture: ComponentFixture<NSecDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NSecDialogComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NSecDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
