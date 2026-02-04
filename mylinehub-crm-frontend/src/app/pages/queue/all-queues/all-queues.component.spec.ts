import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AllQueuesComponent } from './all-queues.component';

describe('AllQueuesComponent', () => {
  let component: AllQueuesComponent;
  let fixture: ComponentFixture<AllQueuesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AllQueuesComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AllQueuesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
