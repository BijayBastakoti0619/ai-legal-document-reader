import { Component, inject } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';

import { AuthService } from '../../../core/services/auth.service';
import { RegisterRequest } from '../../../shared/models/register-request';
import { ApiErrorResponse } from '../../../shared/models/api-error-response';
import { passwordMatchValidator } from '../../../shared/validator/passwordMatchValidator';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  isSubmitting = false;
  backendError = '';
  successMessage = '';

  readonly registrationForm = this.formBuilder.nonNullable.group(
    {
      displayName: [
        '',
        [
          Validators.required,
          Validators.minLength(2),
          Validators.maxLength(100)
        ]
      ],
      email: [
        '',
        [
          Validators.required,
          Validators.email,
          Validators.maxLength(320)
        ]
      ],
      password: [
        '',
        [
          Validators.required,
          Validators.minLength(8),
          Validators.maxLength(72),
          Validators.pattern(
            /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9\s]).+$/
          )
        ]
      ],
      confirmPassword: ['', Validators.required]
    },
    {
      validators: passwordMatchValidator
    }
  );

  onSubmit(): void {
    this.backendError = '';
    this.successMessage = '';

    if (this.registrationForm.invalid) {
      this.registrationForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;

    const formValue = this.registrationForm.getRawValue();

    const request: RegisterRequest = {
      email: formValue.email.trim().toLowerCase(),
      password: formValue.password,
      displayName: formValue.displayName.trim()
    };

    this.authService
      .register(request)
      .pipe(
        finalize(() => {
          this.isSubmitting = false;
        })
      )
      .subscribe({
        next: () => {
          this.successMessage =
            'Account created successfully. Redirecting to login...';

          this.router.navigate(['/login'], {
            state: {
              registrationSuccess: true
            }
          });
        },
        error: (error: HttpErrorResponse) => {
          this.handleBackendError(error);
        }
      });
  }

  private handleBackendError(error: HttpErrorResponse): void {
    const apiError = error.error as ApiErrorResponse;

    if (
      error.status === 409 &&
      apiError?.code === 'EMAIL_ALREADY_EXISTS'
    ) {
      this.backendError =
        'An account already exists with this email address.';
      return;
    }

    if (
      error.status === 400 &&
      apiError?.fieldErrors?.length
    ) {
      for (const fieldError of apiError.fieldErrors) {
        const control = this.registrationForm.get(fieldError.field);

        control?.setErrors({
          ...(control.errors ?? {}),
          backend: fieldError.message
        });
      }

      this.backendError = apiError.message;
      return;
    }

    this.backendError =
      'Registration could not be completed. Please try again.';
  }

  fieldHasError(fieldName: string, errorName: string): boolean {
    const control = this.registrationForm.get(fieldName);

    return Boolean(
      control &&
      control.touched &&
      control.hasError(errorName)
    );
  }
}
