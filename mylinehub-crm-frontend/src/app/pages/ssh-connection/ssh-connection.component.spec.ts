import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SshConnectionComponent } from './ssh-connection.component';

describe('SshConnectionComponent', () => {
  let component: SshConnectionComponent;
  let fixture: ComponentFixture<SshConnectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SshConnectionComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SshConnectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
