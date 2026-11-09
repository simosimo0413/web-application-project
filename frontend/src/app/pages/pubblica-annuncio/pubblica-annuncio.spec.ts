import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PubblicaAnnuncio } from './pubblica-annuncio';

describe('PubblicaAnnuncio', () => {
  let component: PubblicaAnnuncio;
  let fixture: ComponentFixture<PubblicaAnnuncio>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PubblicaAnnuncio],
    }).compileComponents();

    fixture = TestBed.createComponent(PubblicaAnnuncio);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
