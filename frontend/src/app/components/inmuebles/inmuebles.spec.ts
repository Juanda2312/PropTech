import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Inmuebles } from './inmuebles';

describe('Inmuebles', () => {
  let component: Inmuebles;
  let fixture: ComponentFixture<Inmuebles>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Inmuebles],
    }).compileComponents();

    fixture = TestBed.createComponent(Inmuebles);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
