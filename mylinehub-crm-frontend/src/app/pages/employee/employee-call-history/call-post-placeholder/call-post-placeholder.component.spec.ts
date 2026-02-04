import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CallPostPlaceholderComponent } from './call-post-placeholder.component';

describe('CallPostPlaceholderComponent', () => {
  let component: CallPostPlaceholderComponent;
  let fixture: ComponentFixture<CallPostPlaceholderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CallPostPlaceholderComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CallPostPlaceholderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
