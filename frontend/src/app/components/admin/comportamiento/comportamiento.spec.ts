import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Comportamiento } from './comportamiento';

describe('Comportamiento', () => {
  let component: Comportamiento;
  let fixture: ComponentFixture<Comportamiento>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Comportamiento],
    }).compileComponents();

    fixture = TestBed.createComponent(Comportamiento);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
