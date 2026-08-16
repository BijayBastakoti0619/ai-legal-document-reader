// src/app/features/auth/login/login.component.ts

import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './login.component.html', // Pointing to the new HTML file
  styleUrl: './login.component.css'      // Pointing to the new CSS file
})
export class LoginComponent {
  // Inject our dependencies
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  // Initialize the form with strict validation rules
  loginForm: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });

  onSubmit(): void {
    // Stop immediately if the form is invalid (e.g., bad email format)
    if (this.loginForm.invalid) {
      return;
    }

    // Extract the raw values (which perfectly match our LoginRequest DTO)
    const credentials = this.loginForm.getRawValue();

    // Call our service and redirect on success
    this.authService.login(credentials).subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        console.error('Login failed', err);
        // We will handle displaying this error to the user in the HTML template soon
      }
    });
  }
}
