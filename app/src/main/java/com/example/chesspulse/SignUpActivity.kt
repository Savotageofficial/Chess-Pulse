package com.example.chesspulse

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import androidx.core.content.ContextCompat
import com.example.chesspulse.R
import com.example.chesspulse.AuthRepository
import com.example.chesspulse.ui.theme.ChessPulseTheme
import com.example.chesspulse.ui.theme.appColors
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import java.nio.file.WatchEvent

class SignUpActivity : ComponentActivity() {

    private lateinit var repository: AuthRepository
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = AuthRepository()
        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current

            val notifPermissionLauncher =
                rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        Log.d("NOTIF", "Notifications granted")
                    } else {
                        Log.d("NOTIF", "Notifications denied")
                    }
                }

            ChessPulseTheme {
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    SplashScreen()
                    // Run the check in background
                    LaunchedEffect(Unit) {
                        delay(1000)
                        checkUserStatus(
                            onUserFound = { userType ->
                                // Navigate to MainActivity for both user types
                                val intent =
                                    Intent(this@SignUpActivity, MainActivity::class.java).apply {
                                        putExtra("userType", userType)
                                        flags =
                                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    }
                                startActivity(intent)
                                finish()
                            },
                            onUserNotFound = { showSplash = false }
                        )
                    }
                } else {

                    var isLoading by remember { mutableStateOf(false) }
                    SignUpScreen(
                        onSignUpClick = { name, email, password ->
                            isLoading = true
                            repository.createAccount(
                                email, password, name,
                                onSuccess = {
                                    isLoading = false
                                    Toast.makeText(this, "Check your email", Toast.LENGTH_SHORT)
                                        .show()
                                },
                                onFailure = { error ->
                                    isLoading = false
                                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        isLoading = isLoading
                    )
                }
            }
        }
    }

    private fun checkUserStatus(onUserFound: (String) -> Unit, onUserNotFound: () -> Unit) {
        val user = auth.currentUser

        if (user != null) {
            user.reload()
                .addOnSuccessListener {
                    if (auth.currentUser != null && auth.currentUser!!.isEmailVerified) {
                        db.collection("users").document(auth.currentUser!!.uid)
                            .get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val userType = document.getString("userType") ?: "Patient"
                                    onUserFound(userType)
                                } else {
                                    auth.signOut()
                                    onUserNotFound()
                                }
                            }
                            .addOnFailureListener {
                                auth.signOut()
                                onUserNotFound()
                            }
                    } else {
                        auth.signOut()
                        onUserNotFound()
                    }
                }
                .addOnFailureListener {
                    auth.signOut()
                    onUserNotFound()
                }
        } else onUserNotFound()
    }

}

@Composable
fun SplashScreen() {
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            appColors().gradientTop,
            appColors().gradientBottom
        )
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradientBrush),
         horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier.size(180.dp).clip(RoundedCornerShape(20.dp))
        )
        Spacer(modifier = Modifier.height(20.dpe ))
        CircularProgressIndicator()
    }
}

@Composable
fun SignUpScreen(
    onSignUpClick: (name: String, email: String, password: String) -> Unit = { _, _, _ -> },
    isLoading: Boolean = false
) {
    val toggleBgColor = appColors().toggleBg
    val toggleSelectedBg = appColors().toggleSelectedBg
    val toggleSelectedText = appColors().toggleSelectedText
    val toggleUnselectedText = appColors().toggleUnselectedText
    val buttonColor = appColors().inputCursor
    val buttonTextColor = Color.White
    val textGrayColor = appColors().toggleUnselectedText


    var userType by remember { mutableStateOf("Patient") }
    var loginSelected by remember { mutableStateOf("Sign Up") }

    var name by remember { mutableStateOf("") }
    var specialization by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val context = LocalContext.current

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            appColors().gradientTop,
            appColors().gradientBottom
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradientBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(200.dp).clip(RoundedCornerShape(20.dp))
            )



            Spacer(modifier = Modifier.height(40.dp))


            Spacer(modifier = Modifier.height(30.dp))
            Column(modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(color = appColors().surface)
                .padding(30.dp)

            ) {
                Text(
                    text = "Sign Up",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors().textPrimary
                )

                Spacer(modifier = Modifier.height(40.dp))

                InputField("Name", name) { name = it }

                InputField("Email", email) { email = it }

                PasswordField("Password", password) { password = it }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        // Validation before calling sign up
                        when {
                            name.isBlank() -> {
                                Toast.makeText(context, "Please enter name", Toast.LENGTH_SHORT)
                                    .show()
                            }

                            email.isBlank() -> {
                                Toast.makeText(context, "Please enter email", Toast.LENGTH_SHORT)
                                    .show()
                            }

                            password.isBlank() -> {
                                Toast.makeText(context, "Please enter password", Toast.LENGTH_SHORT)
                                    .show()
                            }

                            else -> {
                                onSignUpClick(
                                    name,
                                    email,
                                    password
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                    shape = RoundedCornerShape(20.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "Sign Up",
                            color = buttonTextColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
            Text(
                buildAnnotatedString {
                    append("Already have an account? ")
                    withStyle(
                        SpanStyle(
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append("Log In")
                    }
                },
                fontSize = 15.sp,
                color = textGrayColor,
                modifier = Modifier.clickable { context.startActivity(Intent(context, LoginActivity::class.java))}
            )
        }
    }
}

@Composable
fun InputField(
    placeholderText: String,
    text: String,
    onValueChange: (String) -> Unit
) {
    val inputBgColor = appColors().toggleBg
    val inputTextColor = appColors().inputPlaceholder
    val buttonColor = appColors().inputCursor

    OutlinedTextField(
        value = text,
        onValueChange = onValueChange,
        placeholder = { Text(text = placeholderText, color = inputTextColor, fontSize = 16.sp) },
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = inputBgColor,
            unfocusedContainerColor = inputBgColor,
            cursorColor = buttonColor,
            focusedTextColor = appColors().inputText,
            unfocusedTextColor = appColors().inputText,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(20.dp),
        singleLine = true,
    )
    Spacer(modifier = Modifier.height(14.dp))
}

@Composable
fun PasswordField(
    placeholder: String,
    text: String,
    onValueChange: (String) -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = text,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = appColors().inputPlaceholder, fontSize = 16.sp) },
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = appColors().toggleBg,
            unfocusedContainerColor = appColors().toggleBg,
            cursorColor = appColors().inputCursor,
            focusedTextColor = appColors().inputText,
            unfocusedTextColor = appColors().inputText,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(20.dp),
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (visible) "Hide password" else "Show password"
                )
            }
        }
    )
    Spacer(modifier = Modifier.height(14.dp))
}

@Preview
@Composable
fun SignUpPreview(){
    SignUpScreen(
        onSignUpClick = { name, email, password ->
            Log.d("trace" , "hello")
        },
        isLoading = false
    )

}