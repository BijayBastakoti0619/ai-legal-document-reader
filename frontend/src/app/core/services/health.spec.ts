// src/app/core/services/health.spec.ts

import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import { HealthService } from './health'; // <-- Corrected import name

describe('HealthService', () => {
  let service: HealthService; // <-- Corrected type

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),        // <-- Provides the HttpClient
        provideHttpClientTesting()  // <-- Mocks the HTTP requests for testing
      ]
    });
    service = TestBed.inject(HealthService); // <-- Corrected injection
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
