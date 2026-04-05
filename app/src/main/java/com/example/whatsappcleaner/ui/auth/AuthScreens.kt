package com.example.whatsappcleaner.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

@Composable
fun PreparingCleanerScreen(
    message: String = "Preparing Cleaner…",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onSignupClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val auth = remember { FirebaseAuth.getInstance() }

    AuthFormLayout(
        title = "Welcome back",
        subtitle = "Sign in to continue",
        name = null,
        age = null,
        onNameChange = {},
        onAgeChange = {},
        email = email,
        password = password,
        onEmailChange = {
            email = it
            errorMessage = null
        },
        onPasswordChange = {
            password = it
            errorMessage = null
        },
        isPasswordVisible = isPasswordVisible,
        onPasswordVisibilityChange = { isPasswordVisible = !isPasswordVisible },
        isSubmitting = isSubmitting,
        primaryActionText = "Login",
        onPrimaryAction = {
            if (email.isBlank() || password.isBlank()) {
                errorMessage = "Email and password are required."
                return@AuthFormLayout
            }
            isSubmitting = true
            auth.signInWithEmailAndPassword(email.trim(), password)
                .addOnCompleteListener { task ->
                    isSubmitting = false
                    if (task.isSuccessful) {
                        errorMessage = null
                        onLoginSuccess()
                    } else {
                        errorMessage = task.exception?.localizedMessage ?: "Login failed. Try again."
                    }
                }
        },
        footer = {
            TextButton(
                onClick = onSignupClick,
                enabled = !isSubmitting
            ) {
                Text("New here? Create account")
            }
        },
        errorMessage = errorMessage,
        modifier = modifier
    )
}

@Composable
fun SignupScreen(
    onSignupSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val auth = remember { FirebaseAuth.getInstance() }

    AuthFormLayout(
        title = "Create account",
        subtitle = "Start cleaning smarter",
        name = name,
        age = age,
        onNameChange = {
            name = it
            errorMessage = null
        },
        onAgeChange = {
            age = it.filter { c -> c.isDigit() }.take(3)
            errorMessage = null
        },
        email = email,
        password = password,
        onEmailChange = {
            email = it
            errorMessage = null
        },
        onPasswordChange = {
            password = it
            errorMessage = null
        },
        isPasswordVisible = isPasswordVisible,
        onPasswordVisibilityChange = { isPasswordVisible = !isPasswordVisible },
        isSubmitting = isSubmitting,
        primaryActionText = "Sign up",
        onPrimaryAction = {
            if (name.isBlank() || age.isBlank() || email.isBlank() || password.length < 6) {
                errorMessage = "Please fill all fields correctly"
                return@AuthFormLayout
            }
            val parsedAge = age.toIntOrNull()
            if (parsedAge == null || parsedAge !in 13..120) {
                errorMessage = "Please fill all fields correctly"
                return@AuthFormLayout
            }
            isSubmitting = true
            auth.createUserWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener {
                    val profileUpdate = UserProfileChangeRequest.Builder()
                        .setDisplayName("${name.trim()} • ${parsedAge}y")
                        .build()
                    auth.currentUser
                        ?.updateProfile(profileUpdate)
                        ?.addOnCompleteListener {
                            isSubmitting = false
                            errorMessage = null
                            onSignupSuccess()
                        }
                        ?.addOnFailureListener { exception ->
                            Log.e("AUTH_ERROR", exception.toString())
                        }
                        ?: run {
                            isSubmitting = false
                            errorMessage = null
                            onSignupSuccess()
                        }
                }
                .addOnFailureListener { exception ->
                    Log.e("AUTH_ERROR", exception.toString())
                    isSubmitting = false
                    errorMessage = "Signup failed. Please try again."
                }
        },
        footer = {
            TextButton(
                onClick = onLoginClick,
                enabled = !isSubmitting
            ) {
                Text("Already have an account? Login")
            }
        },
        errorMessage = errorMessage,
        modifier = modifier
    )
}

@Composable
private fun AuthFormLayout(
    title: String,
    subtitle: String,
    name: String?,
    age: String?,
    onNameChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    email: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    isPasswordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
    isSubmitting: Boolean,
    primaryActionText: String,
    onPrimaryAction: () -> Unit,
    footer: @Composable () -> Unit,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        if (name != null) {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Full name") },
                singleLine = true,
                enabled = !isSubmitting
            )
        }

        if (age != null) {
            OutlinedTextField(
                value = age,
                onValueChange = onAgeChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                label = { Text("Age") },
                singleLine = true,
                enabled = !isSubmitting
            )
        }

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = if (name != null || age != null) 12.dp else 0.dp),
            label = { Text("Email") },
            singleLine = true,
            enabled = !isSubmitting
        )

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            label = { Text("Password") },
            singleLine = true,
            enabled = !isSubmitting,
            visualTransformation = if (isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                TextButton(onClick = onPasswordVisibilityChange, enabled = !isSubmitting) {
                    Text(if (isPasswordVisible) "Hide" else "Show")
                }
            }
        )

        errorMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            )
        }

        Button(
            onClick = onPrimaryAction,
            enabled = !isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            } else {
                Text(primaryActionText)
            }
        }

        footer()
    }
}
