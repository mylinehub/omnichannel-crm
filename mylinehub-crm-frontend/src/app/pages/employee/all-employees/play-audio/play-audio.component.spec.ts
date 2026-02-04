import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlayAudioComponent } from './play-audio.component';

describe('PlayAudioComponent', () => {
  let component: PlayAudioComponent;
  let fixture: ComponentFixture<PlayAudioComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PlayAudioComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PlayAudioComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
