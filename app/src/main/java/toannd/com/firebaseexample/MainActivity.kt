package toannd.com.firebaseexample

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private val email: String = "email"
    private val callbackManager = CallbackManager.Factory.create()
    private var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = FirebaseAuth.getInstance()

        loginButton.setOnClickListener {
            val btnLogin = LoginButton(this)
            btnLogin.performClick()
            btnLogin.setReadPermissions(listOf(email))
            // If you are using in a fragment, call loginButton.setFragment(this);

            // Callback registration
            btnLogin.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    // App code
                    Toast.makeText(baseContext, "Success", Toast.LENGTH_SHORT).show()
                    val accessToken = AccessToken.getCurrentAccessToken()
                    handleFacebookAccessToken(accessToken)
                }

                override fun onCancel() {
                    // App code
                    updateUI(null)
                    Toast.makeText(baseContext, "Cancel", Toast.LENGTH_SHORT).show()
                }

                override fun onError(exception: FacebookException) {
                    // App code
                    updateUI(null)
                    Toast.makeText(baseContext, "Error", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth?.currentUser
                    updateUI(user)
                    user?.let { createUserDB(it) }
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateUI(null)
                }
            }
    }

    private fun createUserDB(user: FirebaseUser) {
        val userId = user.uid
        val email = user.email
        val phoneNumber = user.phoneNumber
        val database = FirebaseDatabase.getInstance()
        val mUser = database.getReference("profile")
        userId.let { mUser.child(it).child("user_id").setValue(userId) }
        userId.let { mUser.child(it).child("email").setValue(email) }
        userId.let { mUser.child(it).child("phone_number").setValue(phoneNumber) }
    }


    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth?.currentUser
        updateUI(currentUser)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            loginButton.text = "Sign Out"
        } else {
            loginButton.text = "Continue with facebook"
        }
    }
}
