import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AllFileStorageComponent } from './all-file-storage.component';

describe('AllFileStorageComponent', () => {
  let component: AllFileStorageComponent;
  let fixture: ComponentFixture<AllFileStorageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AllFileStorageComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AllFileStorageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
