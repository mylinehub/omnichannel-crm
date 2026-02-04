import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FranchiseCallListingComponent } from './franchise-call-listing.component';

describe('FranchiseCallListingComponent', () => {
  let component: FranchiseCallListingComponent;
  let fixture: ComponentFixture<FranchiseCallListingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FranchiseCallListingComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FranchiseCallListingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
