import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NSecDialogStopComponent } from './n-sec-dialog-stop.component';

describe('NSecDialogComponent', () => {
  let component: NSecDialogStopComponent;
  let fixture: ComponentFixture<NSecDialogStopComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NSecDialogStopComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NSecDialogStopComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
