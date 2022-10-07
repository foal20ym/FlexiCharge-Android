package com.flexicharge.bolt.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.flexicharge.bolt.R
import com.flexicharge.bolt.api.flexicharge.RetrofitInstance
import com.flexicharge.bolt.api.flexicharge.UserDetails
import com.flexicharge.bolt.databinding.ActivityLoginBinding
import com.flexicharge.bolt.databinding.ActivityRegisterBinding
import com.flexicharge.bolt.helpers.TextInputType
import com.flexicharge.bolt.helpers.Validator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException


class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val validateHelper = Validator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        registerBtn              = binding.buttonRegisterConfirm
        registerUserEmail        = binding.loginActivityEditTextEmail
        registerUserPass         = binding.loginActivityEditTextPassword
        registerUserRepeatPass   = binding.editTextPasswordRepeat
        agreeCheckBox            = binding.checkBoxTosAgreement

        confirmRegistration()
    }

    // first take the input from user
    lateinit var agreeCheckBox : CheckBox
    lateinit var registerBtn : Button
    lateinit var registerUserEmail: EditText
    lateinit var registerUserPass: EditText
    lateinit var registerUserRepeatPass: EditText


    private fun confirmRegistration() {
        validateHelper.validateUserInput(registerUserEmail, TextInputType.isEmail)
        validateHelper.validateUserInput(registerUserPass, TextInputType.isPassword)
        checkRepeatPass()
        makeBtnUnclickable()

        registerBtn.setOnClickListener {
            sendDataToBackend()
        }

    }

    //function to send users' data to backend
    private fun sendUserData(userEmail: String, userPass: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            // handle request to backend.
            try {
                val requestBody = UserDetails(userEmail, userPass)
                val response = RetrofitInstance.flexiChargeApi.registerNewUser(requestBody)
                if (response.isSuccessful) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        val intent =
                            Intent(this@RegisterActivity, VerifyActivity::class.java)
                        startActivity(intent)
                    }
                } else {
                    lifecycleScope.launch(Dispatchers.Main) {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Something went wrong",
                            Toast.LENGTH_LONG
                        )
                    }
                }
            } catch (e: HttpException) {
                lifecycleScope.launch(Dispatchers.Main) {
                    Toast.makeText(
                        this@RegisterActivity,
                        e.message,
                        Toast.LENGTH_LONG
                    )
                }
            } catch (e: IOException) {
                lifecycleScope.launch(Dispatchers.Main) {
                    Toast.makeText(this@RegisterActivity, e.message, Toast.LENGTH_LONG)
                }
            }
        }
    }

    fun checkRepeatPass() {
        registerUserRepeatPass.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {
                    if (registerUserRepeatPass.text.toString() != registerUserPass.text.toString()) {
                        registerUserRepeatPass.error = "does not match"
                    } else if (registerUserRepeatPass.text.toString() != registerUserPass.text.toString()) {
                        registerUserRepeatPass.error = null
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        })
    }

    fun sendDataToBackend () {
        if (agreeCheckBox.isChecked()) {
            if (registerUserRepeatPass.error == null) {
                if (registerUserEmail.error == null) {
                    if (registerUserPass.error == null) {
                        sendUserData(
                            registerUserEmail.text.toString(),
                            registerUserPass.text.toString()
                        )
                    }
                }
            }
        }
        else {
            Toast.makeText(applicationContext, "You have to agree to terms and conditions", Toast.LENGTH_LONG).show()
        }
    }

    fun makeBtnUnclickable () {
        if (validateHelper.isValidEmail && validateHelper.isValidPassword && registerUserRepeatPass.error == null) {
            registerBtn.setEnabled(true)
        }
        else {
            registerBtn.setEnabled(false)
        }
    }


    fun goToSignIn(view: View) {
        //Go to sign in activity
        startActivity(Intent(this, LoginActivity::class.java))
    }
    fun continueAsGuest(view: View) {
        //Continue to MainActivity
        val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.apply { putBoolean("isGuest", true) }.apply()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
