import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DetalleInmueble } from './detalle-inmueble';

describe('DetalleInmueble', () => {
  let component: DetalleInmueble;
  let fixture: ComponentFixture<DetalleInmueble>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DetalleInmueble],
    }).compileComponents();

    fixture = TestBed.createComponent(DetalleInmueble);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
