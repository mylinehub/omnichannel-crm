import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AiAccountComponent } from './ai-account.component';

describe('AiAccountComponent', () => {
  let component: AiAccountComponent;
  let fixture: ComponentFixture<AiAccountComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AiAccountComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AiAccountComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
