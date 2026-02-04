import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateUpdateCategoryComponent } from './create-update-category.component';

describe('CreateUpdateCategoryComponent', () => {
  let component: CreateUpdateCategoryComponent;
  let fixture: ComponentFixture<CreateUpdateCategoryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CreateUpdateCategoryComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CreateUpdateCategoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
