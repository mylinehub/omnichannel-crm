import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InventoryListingComponent } from './inventory-listing.component';

describe('InventoryListingComponent', () => {
  let component: InventoryListingComponent;
  let fixture: ComponentFixture<InventoryListingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ InventoryListingComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InventoryListingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
