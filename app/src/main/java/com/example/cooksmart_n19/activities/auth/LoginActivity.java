package com.example.cooksmart_n19.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.activities.MainActivity;
import com.example.cooksmart_n19.models.User;
import com.facebook.CallbackManager;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_GOOGLE_SIGN_IN = 9001;
    private static final int RC_FIREBASE_UI = 123;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Social Login
    private CallbackManager mCallbackManager;

    // UI Components
    private EditText editTextUsername, editTextEmail, editTextPassword;
    private MaterialButton buttonAction;
    private MaterialTextView textViewToggle, textViewForgotPassword;
    private ProgressBar progressBar;
    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeFirebase();
        initializeUIComponents();
        setupEventListeners();
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mCallbackManager = CallbackManager.Factory.create();
    }

    private void initializeUIComponents() {
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonAction = findViewById(R.id.buttonAction);
        textViewToggle = findViewById(R.id.textViewToggle);
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupEventListeners() {
        findViewById(R.id.imageViewGoogle).setOnClickListener(v -> signInWithGoogle());
        findViewById(R.id.imageViewPhone).setOnClickListener(v -> startFirebaseUIAuth());
        textViewToggle.setOnClickListener(v -> toggleAuthMode());
        buttonAction.setOnClickListener(v -> performAuthAction());
        textViewForgotPassword.setOnClickListener(v -> handleForgotPassword());
    }

    // ================== FIREBASE UI PHONE AUTH ==================
    private void startFirebaseUIAuth() {
        List<AuthUI.IdpConfig> providers = Collections.singletonList(
                new AuthUI.IdpConfig.PhoneBuilder().build()
        );

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setLogo(R.drawable.logo_2)
                        .setTosAndPrivacyPolicyUrls(
                                "https://your-domain.com/terms",
                                "https://your-domain.com/privacy"
                        )
                        .build(),
                RC_FIREBASE_UI
        );
    }

    // ================== EXISTING AUTH METHODS ==================
    private void toggleAuthMode() {
        isLoginMode = !isLoginMode;
        updateUIMode();
    }

    private void updateUIMode() {
        editTextUsername.setVisibility(isLoginMode ? View.GONE : View.VISIBLE);
        textViewForgotPassword.setVisibility(isLoginMode ? View.VISIBLE : View.GONE);
        buttonAction.setText(isLoginMode ? R.string.login : R.string.register);
        textViewToggle.setText(isLoginMode ? R.string.no_account : R.string.have_account);
    }

    private void performAuthAction() {
        if (isLoginMode) {
            loginWithEmail();
        } else {
            registerWithEmail();
        }
    }

    private void registerWithEmail() {
        String username = editTextUsername.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (validateRegistration(username, email, password)) {
            showProgress(true);
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            createUserProfile(task.getResult().getUser(), username);
                        } else {
                            showError(task.getException().getMessage());
                        }
                        showProgress(false);
                    });
        }
    }

    private void loginWithEmail() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (validateLogin(email, password)) {
            showProgress(true);
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            navigateToMain();
                        } else {
                            showError(task.getException().getMessage());
                        }
                        showProgress(false);
                    });
        }
    }

    // ================== GOOGLE SIGN-IN ==================
    private void signInWithGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient client = GoogleSignIn.getClient(this, gso);
        startActivityForResult(client.getSignInIntent(), RC_GOOGLE_SIGN_IN);
    }

    // ================== FACEBOOK SIGN-IN ==================

    // ================== AUTH RESULT HANDLING ==================
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_FIREBASE_UI) {
            handleFirebaseUIResult(resultCode, data);
        } else if (requestCode == RC_GOOGLE_SIGN_IN) {
            handleGoogleSignInResult(data);
        } else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleFirebaseUIResult(int resultCode, Intent data) {
        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (resultCode == RESULT_OK) {
            FirebaseUser firebaseUser = mAuth.getCurrentUser();
            if (firebaseUser != null) {
                handleSuccessfulAuth(firebaseUser);
            } else {
                showError("Authentication failed");
            }
        } else if (response != null) {
            showError(response.getError().getMessage());
        }
    }

    private void handleGoogleSignInResult(Intent data) {
        try {
            GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            showError(e.getMessage());
        }
    }

    // ================== AUTH PROCESSING ==================
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        processAuthCredential(credential);
    }

    private void processAuthCredential(AuthCredential credential) {
        showProgress(true);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    showProgress(false);
                    if (task.isSuccessful()) {
                        handleSuccessfulAuth(task.getResult().getUser());
                    } else {
                        showError(task.getException().getMessage());
                    }
                });
    }

    private void handleSuccessfulAuth(FirebaseUser user) {
        db.collection("users").document(user.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot doc = task.getResult();
                        if (!doc.exists()) {
                            createUserProfile(user, user.getDisplayName());
                        }
                    }
                    navigateToMain();
                });
    }

    // ================== USER MANAGEMENT ==================
    private void createUserProfile(FirebaseUser firebaseUser, String username) {
        User user = new User(
                firebaseUser.getUid(),
                username != null ? username : "User",
                firebaseUser.getEmail(),
                new ArrayList<>(),
                Timestamp.now(),
                Timestamp.now()
        );

        // Thêm phone number nếu có
        if (firebaseUser.getPhoneNumber() != null) {
            user.setPhoneNumber(firebaseUser.getPhoneNumber());
        }

        // Thêm profile image nếu có
        if (firebaseUser.getPhotoUrl() != null) {
            user.setProfileImageUrl(firebaseUser.getPhotoUrl().toString());
        }

        db.collection("users").document(user.getUserId())
                .set(user.toFirestoreMap())
                .addOnFailureListener(e -> showError("Error creating profile: " + e.getMessage()));
    }

    // ================== UTILITY METHODS ==================
    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        buttonAction.setEnabled(!show);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void navigateToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private boolean validateRegistration(String username, String email, String password) {
        if (TextUtils.isEmpty(username)) {
            editTextUsername.setError(getString(R.string.username_required));
            return false;
        }
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError(getString(R.string.valid_email_required));
            return false;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            editTextPassword.setError(getString(R.string.password_length_error));
            return false;
        }
        return true;
    }

    private boolean validateLogin(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError(getString(R.string.email_required));
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError(getString(R.string.password_required));
            return false;
        }
        return true;
    }

    // ================== PASSWORD RESET ==================
    private void handleForgotPassword() {
        String email = editTextEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError(getString(R.string.email_required));
            return;
        }

        showProgress(true);
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    showProgress(false);
                    if (task.isSuccessful()) {
                        showError(getString(R.string.reset_email_sent));
                    } else {
                        showError(task.getException().getMessage());
                    }
                });
    }
}