import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Asesores } from './asesores';

describe('Asesores', () => {
  let component: Asesores;
  let fixture: ComponentFixture<Asesores>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Asesores],
    }).compileComponents();

    fixture = TestBed.createComponent(Asesores);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
