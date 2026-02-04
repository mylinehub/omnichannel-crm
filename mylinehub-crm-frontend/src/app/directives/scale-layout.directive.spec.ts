import { ScaleLayoutDirective } from './scale-layout.directive';
import { ElementRef, Renderer2 } from '@angular/core';

describe('ScaleLayoutDirective', () => {
  let elementRefMock: ElementRef;
  let rendererMock: Renderer2;

  beforeEach(() => {
    elementRefMock = new ElementRef(document.createElement('div'));
    rendererMock = jasmine.createSpyObj('Renderer2', ['setStyle']);
  });

  it('should create an instance', () => {
    const directive = new ScaleLayoutDirective(elementRefMock, rendererMock);
    expect(directive).toBeTruthy();
  });
});

