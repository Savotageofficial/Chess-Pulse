package com.example.chesspulse

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chesspulse.AuthRepository
import com.example.chesspulse.MainActivity
import com.example.chesspulse.SignUpActivity
import com.example.chesspulse.ui.theme.ChessPulseTheme
import com.example.chesspulse.ui.theme.appColors
import com.example.chesspulse.R

class LoginActivity : ComponentActivity() {

    private lateinit var repository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = AuthRepository()

        // Check if already signed in
        if (repository.isUserSignedIn()) {
            repository.getCurrentUserType { userType ->
                navigateToHome(userType ?: "Patient")
            }
            return
        }

        setContent {
            ChessPulseTheme {

                // Loading state
                var isLoading by remember { mutableStateOf(false) }

                LoginScreen(
                    onLoginClick = { email, password ->
                        if (email.isBlank() || password.isBlank()) {
                            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                        } else {
                            isLoading = true
                            signIn(email, password) {
                                isLoading = false
                            }
                        }
                    },
                    onSignUpClick = {
                        startActivity(Intent(this, SignUpActivity::class.java))
                    },
                    onForgotPasswordClick = { email ->
                        if (email.isBlank()) {
                            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                        } else {
                            repository.sendPasswordResetEmail(email,
                                onSuccess = {
                                    Toast.makeText(this, "Email sent", Toast.LENGTH_SHORT).show()
                                },
                                onFailure = { error ->
                                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    },
                    isLoading = isLoading
                )
            }
        }
    }

    private fun signIn(email: String, password: String, onFinish: () -> Unit) {
        repository.signIn(email, password,
            onSuccess = { userType ->
                onFinish()
                navigateToHome(userType)
            },
            onFailure = { error ->
                onFinish()
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun navigateToHome(userType: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("userType", userType)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}

@Composable
fun LoginScreen(
    onLoginClick: (email: String, password: String) -> Unit = { _, _ -> },
    onSignUpClick: () -> Unit = {},
    onForgotPasswordClick: (email: String) -> Unit = {},
    isLoading: Boolean = false
) {
    val inputBgColor = appColors().inputBg
    val inputPlaceholderColor = appColors().inputPlaceholder
    val buttonColor = appColors().inputCursor
    val buttonTextColor = Color.White
    val textGrayColor = appColors().toggleUnselectedText

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            appColors().gradientTop,
            appColors().gradientBottom
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        Spacer(modifier = Modifier.height(24.dp))

        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(60.dp))

        Column(modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color = appColors().surface)
            .padding(30.dp)

        ) {

            Text(
                text = "Welcome Back , Master",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = appColors().textPrimary,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(60.dp))

            // EMAIL FIELD (unchanged)
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = {
                    Text(
                        text = "Email",
                        color = inputPlaceholderColor,
                        fontSize = 16.sp
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = inputBgColor,
                    unfocusedContainerColor = inputBgColor,
                    cursorColor = buttonColor,
                    focusedTextColor = appColors().inputText,
                    unfocusedTextColor = appColors().inputText,
                    focusedIndicatorColor = appColors().inputIndicator,
                    unfocusedIndicatorColor = appColors().inputIndicator
                ),
                shape = RoundedCornerShape(20.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // ✅ PASSWORD FIELD WITH VISIBILITY TOGGLE (same as SignUp)
            var passwordVisible by remember { mutableStateOf(false) }

            // Password input
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = {
                    Text(
                        text = "Password",
                        color = inputPlaceholderColor,
                        fontSize = 16.sp
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = inputBgColor,
                    unfocusedContainerColor = inputBgColor,
                    cursorColor = buttonColor,
                    focusedTextColor = appColors().inputText,
                    unfocusedTextColor = appColors().inputText,
                    focusedIndicatorColor = appColors().inputIndicator,
                    unfocusedIndicatorColor = appColors().inputIndicator
                ),
                shape = RoundedCornerShape(20.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Forgot password
            Text(
                text = "Forgot Password?",
                modifier = Modifier.align(Alignment.End).clickable { onForgotPasswordClick(email) },
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                textDecoration = TextDecoration.Underline
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Login button
            Button(
                onClick = { onLoginClick(email, password) },
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                shape = RoundedCornerShape(20.dp),
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Login",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = buttonTextColor
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            buildAnnotatedString {
                append("Don't have an account? ")
                withStyle(
                    SpanStyle(
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append("Sign Up")
                }
            },
            fontSize = 15.sp,
            color = textGrayColor,
            modifier = Modifier.clickable { onSignUpClick() }
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Preview
@Composable
fun LoginScreenPreview(){
    LoginScreen(
        onLoginClick = { email, password ->
            Log.d("trace" , "hello")
        },
        onSignUpClick = {
            Log.d("trace" , "hello")
        },
        onForgotPasswordClick = { email ->
            Log.d("trace" , "hello")
        },
        isLoading = false
    )

}