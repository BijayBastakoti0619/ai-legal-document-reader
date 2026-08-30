// src/app/features/auth/login/login.component.ts

import { Component, inject, signal } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {

  registrationSuccess = history.state?.registrationSuccess === true;

  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  // Signals for UI state
  isSubmitting = signal(false);
  errorMessage = signal('');

  loginForm: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });

  onSubmit(): void {

    // Clear previous error
    this.errorMessage.set('');

    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    // Start progress bar
    this.isSubmitting.set(true);

    const credentials = this.loginForm.getRawValue();

    this.authService
      .login(credentials)
      .pipe(
        finalize(() => {
          console.log('LOGIN FINALIZE CALLED');

          // Stop progress bar
          this.isSubmitting.set(false);
        })
      )
      .subscribe({

        next: () => {
          console.log('LOGIN SUCCESS');

          this.router.navigate(['/dashboard']);
        },

        error: (err) => {
          console.error('LOGIN ERROR:', err);

          if (err.status === 401 || err.status === 403) {

            this.errorMessage.set(
              'Invalid email or password.'
            );

          } else if (err.status === 0) {

            this.errorMessage.set(
              'Unable to connect to the server. Please try again.'
            );

          } else if (err.error?.message) {

            this.errorMessage.set(
              err.error.message
            );

          } else {

            this.errorMessage.set(
              'Login failed. Please try again.'
            );
          }
        }
      });
  }

  clearError(): void {
    this.errorMessage.set('');
  }
}
