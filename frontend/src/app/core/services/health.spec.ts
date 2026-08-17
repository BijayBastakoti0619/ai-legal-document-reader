// src/app/core/services/health.spec.ts

import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import { HealthService } from './health'; // Make sure the actual file is named health.ts (or update to './health.service' if it is health.service.ts)

describe('HealthService', () => {
  let service: HealthService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),        // Provides the HttpClient
        provideHttpClientTesting()  // Mocks the HTTP requests for testing
      ]
    });
    service = TestBed.inject(HealthService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
