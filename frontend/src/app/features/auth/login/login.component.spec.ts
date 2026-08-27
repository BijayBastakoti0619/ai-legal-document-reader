// src/app/features/auth/login/login.component.spec.ts

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { provideRouter } from '@angular/router';
import { LoginComponent } from './login.component';
import { AuthService } from '../../../core/services/auth.service';
import { of } from 'rxjs';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let mockAuthService: Partial<AuthService>;

  beforeEach(async () => {
    // Mock only the AuthService so we don't make real network calls
    mockAuthService = {
      login: () => of({ accessToken: 'mock', refreshToken: 'mock', tokenType: 'Bearer', expiresIn: 900 })
    };

    await TestBed.configureTestingModule({
      imports: [LoginComponent, ReactiveFormsModule],
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        // FIX: Provide a dummy route so the router doesn't panic on success!
        provideRouter([{ path: 'dashboard', children: [] }])
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should initialize an empty, invalid form', () => {
    expect(component.loginForm.value).toEqual({ email: '', password: '' });
    expect(component.loginForm.valid).toBe(false);
  });

  it('should invalidate incorrect email formats', () => {
    const emailControl = component.loginForm.get('email');

    emailControl?.setValue('not-an-email');
    expect(emailControl?.valid).toBe(false);

    emailControl?.setValue('user@example.com');
    expect(emailControl?.valid).toBe(true);
  });

  it('should call AuthService.login when the form is valid and submitted', () => {
    // Track if the login method gets called
    let serviceCalled = false;
    mockAuthService.login = () => {
      serviceCalled = true;
      return of({ accessToken: 'mock', refreshToken: 'mock', tokenType: 'Bearer', expiresIn: 900 });
    };

    // Fill out the form correctly
    component.loginForm.setValue({ email: 'user@example.com', password: 'StrongPassword123!' });

    // Trigger the submission
    component.onSubmit();

    expect(serviceCalled).toBe(true);
  });
});
