import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CallPostComponent } from './call-post.component';

describe('CallPostComponent', () => {
  let component: CallPostComponent;
  let fixture: ComponentFixture<CallPostComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CallPostComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CallPostComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
