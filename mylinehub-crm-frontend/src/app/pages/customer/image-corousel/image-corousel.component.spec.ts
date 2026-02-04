import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ImageCorouselComponent } from './image-corousel.component';

describe('ImageCorouselComponent', () => {
  let component: ImageCorouselComponent;
  let fixture: ComponentFixture<ImageCorouselComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ImageCorouselComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ImageCorouselComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
