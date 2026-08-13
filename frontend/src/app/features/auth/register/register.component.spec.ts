import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { provideRouter,Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { RegisterComponent } from './register.component';
import { AuthService } from '../../../core/services/auth.service';
import { RegisterResponse } from '../../../shared/models/register-response';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;

  const authServiceMock = {
    register: vi.fn()
  };

  const routerMock = {
    navigate: vi.fn()
  };

  const successfulResponse: RegisterResponse = {
    id: 1,
    email: 'user@example.com',
    displayName: 'Sample User',
    role: 'USER'
  };

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [RegisterComponent],
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: authServiceMock
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  function fillValidForm(): void {
    component.registrationForm.setValue({
      displayName: 'Sample User',
      email: 'user@example.com',
      password: 'StrongPassword123!',
      confirmPassword: 'StrongPassword123!'
    });
  }

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should show an invalid-email error', () => {
    const emailControl = component.registrationForm.controls.email;

    emailControl.setValue('invalid-email');
    emailControl.markAsTouched();

    fixture.detectChanges();

    expect(emailControl.hasError('email')).toBe(true);

    const errorElements: NodeListOf<HTMLElement> =
      fixture.nativeElement.querySelectorAll('.field-error');

    const errorMessages = Array.from(errorElements).map(
      element => element.textContent?.trim()
    );

    expect(errorMessages).toContain('Enter a valid email address.');
  });

  it('should mark the form invalid when passwords do not match', () => {
    component.registrationForm.patchValue({
      displayName: 'Sample User',
      email: 'user@example.com',
      password: 'StrongPassword123!',
      confirmPassword: 'DifferentPassword123!'
    });

    component.registrationForm.markAllAsTouched();
    fixture.detectChanges();

    expect(
      component.registrationForm.hasError('passwordMismatch')
    ).toBe(true);

    expect(component.registrationForm.invalid).toBe(true);
  });

  it('should disable submit when the form is invalid', () => {
    fixture.detectChanges();

    const button: HTMLButtonElement =
      fixture.nativeElement.querySelector('button[type="submit"]');

    expect(component.registrationForm.invalid).toBe(true);
    expect(button.disabled).toBe(true);
  });

  it('should call AuthService with normalized form values', () => {
    authServiceMock.register.mockReturnValue(of(successfulResponse));
    routerMock.navigate.mockResolvedValue(true);

    component.registrationForm.setValue({
      displayName: '  Sample User  ',
      email: '  USER@EXAMPLE.COM  ',
      password: 'StrongPassword123!',
      confirmPassword: 'StrongPassword123!'
    });

    component.onSubmit();

    expect(authServiceMock.register).toHaveBeenCalledWith({
      displayName: 'Sample User',
      email: 'user@example.com',
      password: 'StrongPassword123!'
    });
  });

  it('should not send confirmPassword to the backend', () => {
    authServiceMock.register.mockReturnValue(of(successfulResponse));
    routerMock.navigate.mockResolvedValue(true);

    fillValidForm();
    component.onSubmit();

    const request = authServiceMock.register.mock.calls[0][0];

    expect(request).not.toHaveProperty('confirmPassword');
  });

  it('should redirect to login after successful registration', () => {
    authServiceMock.register.mockReturnValue(of(successfulResponse));
    routerMock.navigate.mockResolvedValue(true);

    fillValidForm();
    component.onSubmit();

    expect(routerMock.navigate).toHaveBeenCalledWith(
      ['/login'],
      {
        state: {
          registrationSuccess: true
        }
      }
    );
  });

  it('should display the duplicate-email error', () => {
    const duplicateError = new HttpErrorResponse({
      status: 409,
      error: {
        timestamp: '2026-08-05T18:00:00Z',
        status: 409,
        code: 'EMAIL_ALREADY_EXISTS',
        message:
          'An account already exists with this email address.',
        path: '/api/v1/auth/register',
        correlationId: 'example-correlation-id',
        fieldErrors: []
      }
    });

    authServiceMock.register.mockReturnValue(
      throwError(() => duplicateError)
    );

    fillValidForm();
    component.onSubmit();
    fixture.detectChanges();

    expect(component.backendError).toBe(
      'An account already exists with this email address.'
    );

    const errorElement: HTMLElement | null =
      fixture.nativeElement.querySelector('.form-error');

    expect(errorElement?.textContent).toContain(
      'An account already exists with this email address.'
    );
  });

  it('should not submit when the form is invalid', () => {
    component.registrationForm.patchValue({
      email: 'invalid-email'
    });

    component.onSubmit();

    expect(authServiceMock.register).not.toHaveBeenCalled();
  });

  it('should disable the button while submitting', () => {
    fillValidForm();
    component.isSubmitting = true;

    fixture.detectChanges();

    const button: HTMLButtonElement =
      fixture.nativeElement.querySelector('button[type="submit"]');

    expect(button.disabled).toBe(true);
    expect(button.textContent).toContain('Creating account');
  });
});
