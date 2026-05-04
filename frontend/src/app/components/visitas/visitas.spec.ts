import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Visitas } from './visitas';

describe('Visitas', () => {
  let component: Visitas;
  let fixture: ComponentFixture<Visitas>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Visitas],
    }).compileComponents();

    fixture = TestBed.createComponent(Visitas);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
