// src/app/app.spec.ts

import { TestBed } from '@angular/core/testing';
import { App } from './app'; // <-- Now importing the exact 'App' class
import { provideRouter } from '@angular/router';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App], // <-- Updated here
      providers: [provideRouter([])]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App); // <-- Updated here
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render the router-outlet', () => {
    const fixture = TestBed.createComponent(App); // <-- Updated here
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;

    // Assert that our dynamic routing placeholder exists
    expect(compiled.querySelector('router-outlet')).toBeTruthy();
  });
});
