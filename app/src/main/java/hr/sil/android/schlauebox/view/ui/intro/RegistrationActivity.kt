package hr.sil.android.schlauebox.view.ui.intro


import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.core.content.res.getColorOrThrow
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.databinding.ActivityRegistrationBinding
import hr.sil.android.schlauebox.util.SettingsHelper
import hr.sil.android.schlauebox.util.backend.UserUtil
import hr.sil.android.schlauebox.util.connectivity.NetworkChecker
import hr.sil.android.schlauebox.view.ui.BaseActivity
import hr.sil.android.schlauebox.view.ui.LoginActivity
import hr.sil.android.schlauebox.view.ui.MainActivity
import hr.sil.android.schlauebox.view.ui.home.activities.TtcActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class RegistrationActivity : BaseActivity() {

    private val log = logger()

    var nameValue: String? = ""
    var phoneValue: String? = ""
    var addressValue: String? = ""
    var emailValue: String? = ""
    var passwordValue: String? = ""
    var confirmPasswordValue: String? = ""
    var termsAndConditionValue: Boolean? = false

    var correctPassword: Boolean = true

    private var isGroupNameEdittextTouched = false
    private var programaticSetText = false
    private var togetherGroupName = ""

    private val LESS_THAN_TOTAL_ALLOWED_CHARACTERS = 31
    private val MAX_ROW_LENGTH = 15
    private val NO_CHARATER_IN_ROW_IN_EPAPER = 0
    private val ROW_SWITCH_POSITION = MAX_ROW_LENGTH + 1
    private val FIRST_INDEX_IN_LIST_SPLITED_BY_SPACE = 0

    private val STARTING_FROM_FIRST_CHARACTER = 0

    private val SECOND_ROW_LAST_INDEX = MAX_ROW_LENGTH * 2

    private val INCREASE_BY_ONE_BECAUSE_OF_EMPTY_SPACE = 1

    private val ROW_POSITION = 0
    private val INDEX_POSITION = 1
    private var cursorPosition = IntArray(2)

    private lateinit var binding: ActivityRegistrationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cursorPosition[ROW_POSITION] = 0
        cursorPosition[INDEX_POSITION] = 0

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.tvShowPasswords.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG)
        binding.tvRegisterAcceptTermsLink.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG)

        //binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        //binding.etRepeatPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        binding.btnRegister.setOnClickListener {

            if (validate()) {

                binding.progressBar.visibility = View.VISIBLE

                val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
//                params.setMargins(0, dip(-20), 0, 0)
//                params.below(binding.clRepeatPassword)
//                params.centerHorizontally()
                binding.tvShowPasswords.layoutParams = params
                GlobalScope.launch {
                    if (NetworkChecker.isInternetConnectionAvailable()) {

                        correctPassword = false

                        val groupName = binding.groupNameFirstRow.text.toString().trim() + " " + binding.groupNameSecondRow.text.toString().trim()

//                        val result = UserUtil.register(
//                            binding.etName.text.toString(),
//                            binding.etAddress.text.toString(),
//                            binding.etPhone.text.toString(),
//                            binding.etEmail.text.toString(),
//                            binding.etPassword.text.toString(),
//                            groupName,
//                            binding.reducedMobilityCheckBox.isChecked
//                        )
//                        if( result )
//                            UserUtil.acceptedTerms()
//
//                        withContext(Dispatchers.Main) {
//                            if (result) {
//
//                                SettingsHelper.userPasswordWithoutEncryption = binding.etPassword.text.toString()
//                                SettingsHelper.userRegisterOrLogin = true
//
//                                val intent =
//                                        Intent(this@RegistrationActivity, MainActivity::class.java)
//                                startActivity(intent)
//                                finish()
//                            } else
//                                App.ref.toast(R.string.register_error)
//                            binding.progressBar.visibility = View.GONE
//                        }
                    } else {
                        //App.ref.toast(R.string.app_generic_no_network)
                    }
                }
            } else {

                log.error("Error while registering the user")
                if (correctPassword) {
//                    val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
//                    params.setMargins(0, dip(-20), 0, 0)
//                    params.below(binding.clRepeatPassword)
//                    params.centerHorizontally()
//                    binding.tvShowPasswords.layoutParams = params
//                } else {
//                    val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
//                    params.setMargins(0, 0, 0, 0)
//                    params.below(binding.clRepeatPassword)
//                    params.centerHorizontally()
//                    binding.tvShowPasswords.layoutParams = params
//                }
            }
        }

//        binding.etName.run {
//
//            requestFocus()
//
//            addTextChangedListener(object : TextWatcher {
//
//                var fRInsertedCharLength = 0
//                override fun afterTextChanged(p0: Editable?) {
//                }
//
//                override fun beforeTextChanged(p0: CharSequence?, start: Int, before: Int, count: Int) {
//                    fRInsertedCharLength = binding.etName.text?.length ?: 0
//                    log.info("First row.. Size before text changing is $fRInsertedCharLength")
//                }
//
//                override fun onTextChanged(nameText: CharSequence?, positionIndexChanged: Int, before: Int, count: Int) {
//
//                    if (!isGroupNameEdittextTouched) {
//                        if (nameText.toString().contains(" ")) {
//
//                            var newCharacter = ""
//                            if (fRInsertedCharLength > nameText.toString().length && nameText.toString().isNotEmpty()) {
//                                newCharacter = if (positionIndexChanged == 0) nameText?.get(0).toString()
//                                else {
//                                    nameText?.get(positionIndexChanged - 1).toString()
//                                }
//                            } else if (nameText.toString().isNotEmpty())
//                                newCharacter = nameText?.get(positionIndexChanged).toString()
//                            log.info("last character is: $newCharacter")
//
//                            validateGroupNameWithSpace(nameText.toString(), newCharacter, isGroupNameEdittextTouched)
//                        } else {
//                            validateNameWithoutSpaces(nameText.toString())
//                        }
//                    }
//                }
//            })
        }

        binding.tvRegisterAcceptTermsLink.setOnClickListener {
            val intent = Intent(this@RegistrationActivity, TtcActivity::class.java)
            startActivity(intent)
        }

//        binding.tvShowPasswords.setOnTouchListener { view, event ->
//            when (event.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT
//                    binding.etRepeatPassword.inputType = InputType.TYPE_CLASS_TEXT
//                }
//                MotionEvent.ACTION_UP -> {
//                    binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
//                    binding.etRepeatPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
//                }
//            }
//            true
//        }

        this.binding.groupNameFirstRow.run {

            setOnFocusChangeListener { _, _ ->

                isGroupNameEdittextTouched = true
            }

            addTextChangedListener(object : TextWatcher {
                var fRInsertedCharLength = 0

                override fun afterTextChanged(p0: Editable?) {
                    if (programaticSetText && binding.groupNameFirstRow.isFocused && binding.groupNameFirstRow.text.isNotEmpty() && cursorPosition[INDEX_POSITION] <= binding.groupNameFirstRow.text.length)
                        binding.groupNameFirstRow.setSelection(cursorPosition[INDEX_POSITION])
                    else if (programaticSetText && binding.groupNameFirstRow.isFocused && binding.groupNameFirstRow.text.isNotEmpty() && cursorPosition[INDEX_POSITION] >= binding.groupNameFirstRow.text.length)
                        binding.groupNameFirstRow.setSelection(binding.groupNameFirstRow.text.length)
                }

                override fun beforeTextChanged(p0: CharSequence?, start: Int, before: Int, count: Int) {
                    fRInsertedCharLength = binding.groupNameFirstRow.text.length
                    log.info("First row.. Size before text changing is $fRInsertedCharLength")
                }

                override fun onTextChanged(firstRowText: CharSequence?, positionIndexChanged: Int, before: Int, count: Int) {
                    if (programaticSetText) return

                    if (!isGroupNameEdittextTouched) {

                    } else {

                        programaticSetText = true
                        var newCharacter = ""
                        if (fRInsertedCharLength > firstRowText.toString().length && firstRowText.toString().isNotEmpty()) {
                            if (positionIndexChanged == 0)
                                newCharacter = firstRowText?.get(0).toString()
                            else
                                newCharacter = firstRowText?.get(positionIndexChanged - 1).toString()
                        } else if (firstRowText.toString().isNotEmpty())
                            newCharacter = firstRowText?.get(positionIndexChanged).toString()
                        log.info("last character is: $newCharacter")

                        if (firstRowText.toString().isEmpty()) {
                            reorderAndMoveAllTextUp()
                        } else if (!handleLastCharacterEmptySpace(binding.groupNameFirstRow, binding.groupNameSecondRow)) {
                            if (binding.groupNameFirstRow.isFocused) {

                                togetherGroupName = parseGroupName()

                                positionCursorInCurrentEdittext(fRInsertedCharLength, firstRowText.toString(), positionIndexChanged, before, count)

                                displayGroupName(togetherGroupName, newCharacter)

                                if (positionIndexChanged + count - 1 == MAX_ROW_LENGTH) {
                                    binding.groupNameSecondRow.isEnabled = true
                                    binding.groupNameSecondRow.requestFocus()
                                    if (binding.groupNameSecondRow.text.contains(" ")) {
                                        val firstWordInSecondRow = binding.groupNameSecondRow.text.split(" ")
                                        binding.groupNameSecondRow.setSelection(firstWordInSecondRow.first().length)
                                    } else {
                                        binding.groupNameSecondRow.setSelection(binding.groupNameSecondRow.text.length)
                                    }
                                }
                            }
                        }
                        programaticSetText = false
                    }
                }
            })
        }

        binding.groupNameSecondRow.isEnabled = false

        binding.groupNameSecondRow.run {

            setOnFocusChangeListener { _, _ ->

                isGroupNameEdittextTouched = true
            }

            addTextChangedListener(object : TextWatcher {

                var tRInsertedCharLength = 0

                override fun afterTextChanged(p0: Editable?) {
                    if (programaticSetText && binding.groupNameSecondRow.isFocused && binding.groupNameSecondRow.text.isNotEmpty() && cursorPosition[INDEX_POSITION] <= binding.groupNameSecondRow.text.length) {
                        binding.groupNameSecondRow.setSelection(cursorPosition[INDEX_POSITION])
                    }
                }

                override fun beforeTextChanged(p0: CharSequence?, start: Int, before: Int, count: Int) {
                    tRInsertedCharLength = binding.groupNameSecondRow.text.length
                    log.info("Second row.. Size before text changing is $tRInsertedCharLength")
                }

                override fun onTextChanged(secondRowText: CharSequence?, positionIndexChanged: Int, before: Int, count: Int) {
                    if (programaticSetText) return

                    if (!isGroupNameEdittextTouched) {

                    } else {

                        var newCharacter = ""
                        if (tRInsertedCharLength > secondRowText.toString().length && secondRowText.toString().isNotEmpty()) {
                            if (positionIndexChanged == 0)
                                newCharacter = secondRowText?.get(0).toString()
                            else
                                newCharacter = secondRowText?.get(positionIndexChanged - 1).toString()
                        } else if (secondRowText.toString().isNotEmpty())
                            newCharacter = secondRowText?.get(positionIndexChanged).toString()
                        log.info("last character is: " + newCharacter)

                        programaticSetText = true
                        if (secondRowText.toString().isEmpty()) {
                            jumpFromSecondRowToFirstRow()
                        } else if (binding.groupNameSecondRow.isFocused) {

                            togetherGroupName = parseGroupName()

                            positionCursorInCurrentEdittext(tRInsertedCharLength, secondRowText.toString(), positionIndexChanged, before, count)

                            displayGroupName(togetherGroupName, newCharacter)
                        }
                        programaticSetText = false
                    }
                }
            })
        }

        nameValue = savedInstanceState?.getString("name")
        phoneValue = savedInstanceState?.getString("phone")
        addressValue = savedInstanceState?.getString("address")
        emailValue = savedInstanceState?.getString("email")
        passwordValue = savedInstanceState?.getString("password")
        confirmPasswordValue = savedInstanceState?.getString("confirmPassword")
        termsAndConditionValue = savedInstanceState?.getBoolean("termsAndConditions")

//        binding.etName.setText(nameValue)
//        binding.etPhone.setText(phoneValue)
//        binding.etAddress.setText(addressValue)
//        binding.etEmail.setText(emailValue)
//        binding.etPassword.setText(passwordValue)
//        binding.etRepeatPassword.setText(confirmPasswordValue)
        if (termsAndConditionValue != null) {
            binding.acceptedTerms.isChecked = termsAndConditionValue as Boolean
        }
    }

    private fun parseGroupName(): String {

        var groupName = ""

        if (binding.groupNameSecondRow.text.isEmpty()) groupName = binding.groupNameFirstRow.text.toString()
        else if (binding.groupNameSecondRow.text.isNotEmpty()) {
            if (binding.groupNameFirstRow.text.last().toString() == " ")
                groupName = binding.groupNameFirstRow.text.toString() + binding.groupNameSecondRow.text.toString()
            else
                groupName = binding.groupNameFirstRow.text.toString() + " " + binding.groupNameSecondRow.text.toString()
        }
        return groupName
    }

    fun displayGroupName(groupName: String, newCharacter: String) {
        if (groupName.contains(" ")) {
            validateGroupNameWithSpace(groupName, newCharacter, isGroupNameEdittextTouched)
        } else {
            validateNameWithoutSpaces(groupName)
        }
    }

    private fun positionCursorInCurrentEdittext(currentTextLength: Int, currentText: String, positionIndexChanged: Int, before: Int, count: Int) {
        if (currentTextLength > currentText.length) {
            cursorPosition[INDEX_POSITION] = positionIndexChanged + count
        } else {
            cursorPosition[INDEX_POSITION] = positionIndexChanged + before + 1
        }
    }

    private fun reorderAndMoveAllTextUp() {
        if (binding.groupNameSecondRow.text.isNotEmpty()) {
            binding.groupNameFirstRow.setText(binding.groupNameSecondRow.text.toString())
            binding.groupNameSecondRow.setText("")
            binding.groupNameSecondRow.isEnabled = false
            binding.firstRowCharacterLength.text = "" + binding.groupNameFirstRow.text.length + "/" + MAX_ROW_LENGTH
            binding.secondRowCharacterLength.text = "" + binding.groupNameSecondRow.text.length + "/" + MAX_ROW_LENGTH
        } else {
            binding.firstRowCharacterLength.text = "" + binding.groupNameFirstRow.text.length + "/" + MAX_ROW_LENGTH
        }
    }

    private fun jumpFromSecondRowToFirstRow() {

        binding.groupNameFirstRow.requestFocus()
        binding.groupNameFirstRow.setSelection(binding.groupNameFirstRow.text.length)
        binding.groupNameSecondRow.isEnabled = false
        binding.groupNameSecondRow.setText("")
        binding.secondRowCharacterLength.text = "0/" + MAX_ROW_LENGTH
    }

    private fun handleLastCharacterEmptySpace(currentEditText: EditText, nextEditText: EditText?): Boolean {

        if (currentEditText.selectionEnd == ROW_SWITCH_POSITION && currentEditText.text.length == ROW_SWITCH_POSITION
                && currentEditText.text.last().toString() == " ") {
            nextEditText?.isEnabled = true
            nextEditText?.requestFocus()

            //if (cursorPosition[ROW_POSITION] < 3) cursorPosition[ROW_POSITION] = +1
            //ursorPosition[INDEX_POSITION] = 0
            currentEditText.setText(currentEditText.text.toString().dropLast(1))
            return true
        }
        return false
    }

    private fun validateGroupNameWithSpace(nameText: String, newCharacter: String, isGroupNameEdittextTouched: Boolean) {

        var firstRowCounterLength = 0
        var secondRowCounterLength = 0

        var firstRowText = ""
        var secondRowText = ""

        val emptySpaces = nameText.split(" ")
        //val lastCharacter = nameText.substring(nameText.length - 1, nameText.length)
        //for (index in 0..emptySpaces.size - 1) {
        for (index in 0 until emptySpaces.size) {

            if (emptySpaces[index].length > MAX_ROW_LENGTH) {

                if (index == FIRST_INDEX_IN_LIST_SPLITED_BY_SPACE) {
                    firstRowText += emptySpaces[index].substring(STARTING_FROM_FIRST_CHARACTER, MAX_ROW_LENGTH)
                    firstRowCounterLength += emptySpaces[index].substring(STARTING_FROM_FIRST_CHARACTER, MAX_ROW_LENGTH).length

                    if (emptySpaces[index].length > SECOND_ROW_LAST_INDEX) {

                        secondRowText += emptySpaces[index].substring(MAX_ROW_LENGTH, SECOND_ROW_LAST_INDEX)
                        secondRowCounterLength += emptySpaces[index].substring(MAX_ROW_LENGTH, SECOND_ROW_LAST_INDEX).length

                    } else {
                        secondRowText += emptySpaces[index].substring(MAX_ROW_LENGTH, emptySpaces[index].length) + " "
                        secondRowCounterLength += emptySpaces[index].substring(MAX_ROW_LENGTH, emptySpaces[index].length).length
                    }
                } else if (secondRowCounterLength <= ROW_SWITCH_POSITION) {

                    secondRowText += emptySpaces[index].substring(STARTING_FROM_FIRST_CHARACTER, MAX_ROW_LENGTH)
                    secondRowCounterLength += secondRowText.length + INCREASE_BY_ONE_BECAUSE_OF_EMPTY_SPACE
                }
            } else if (firstRowCounterLength < ROW_SWITCH_POSITION && (firstRowCounterLength + emptySpaces[index].length) < ROW_SWITCH_POSITION && secondRowCounterLength <= NO_CHARATER_IN_ROW_IN_EPAPER) {
                if (emptySpaces.size - 1 == index) {
                    firstRowCounterLength += emptySpaces[index].length
                    firstRowText += emptySpaces[index]
                } else {
                    firstRowText += emptySpaces[index] + " "
                    firstRowCounterLength = firstRowText.length
                }
            } else if (secondRowCounterLength < ROW_SWITCH_POSITION) {

                if (emptySpaces.size - 1 == index) {
                    secondRowCounterLength += emptySpaces[index].length
                    secondRowText += emptySpaces[index]
                } else {
                    secondRowText += emptySpaces[index] + " "
                    secondRowCounterLength = secondRowText.length
                }
            }
        }

        if (!isGroupNameEdittextTouched) {

            when {
                firstRowCounterLength < ROW_SWITCH_POSITION && secondRowCounterLength <= 0 -> {
                    firstRowText = currentEdittextRemoveLastCharacterEmptySpace(firstRowText, firstRowCounterLength, newCharacter)
                    secondRowText = notSelectedEdittextRemoveLastCharacterEmptySpace(secondRowText, secondRowCounterLength)
                }
                else -> {
                    firstRowText = notSelectedEdittextRemoveLastCharacterEmptySpace(firstRowText, firstRowCounterLength)
                    secondRowText = currentEdittextRemoveLastCharacterEmptySpace(secondRowText, secondRowCounterLength, newCharacter)
                }
            }
        } else {
            when {
                binding.groupNameFirstRow.isFocused -> {
                    firstRowText = currentEdittextRemoveLastCharacterEmptySpace(firstRowText, firstRowCounterLength, newCharacter)
                    secondRowText = notSelectedEdittextRemoveLastCharacterEmptySpace(secondRowText, secondRowCounterLength)
                }
                binding.groupNameSecondRow.isFocused -> {
                    firstRowText = notSelectedEdittextRemoveLastCharacterEmptySpace(firstRowText, firstRowCounterLength)
                    secondRowText = currentEdittextRemoveLastCharacterEmptySpace(secondRowText, secondRowCounterLength, newCharacter)
                }
            }
        }

        if (firstRowText.length >= MAX_ROW_LENGTH) {
            firstRowText = firstRowText.substring(STARTING_FROM_FIRST_CHARACTER, MAX_ROW_LENGTH)
        }
        if (secondRowText.length >= MAX_ROW_LENGTH) {
            secondRowText = secondRowText.substring(STARTING_FROM_FIRST_CHARACTER, MAX_ROW_LENGTH)
        }

        if (firstRowText != binding.groupNameFirstRow.text.toString()) {
            binding.groupNameFirstRow.setText(firstRowText)
        }
        if (secondRowText != binding.groupNameSecondRow.text.toString()) {
            binding.groupNameSecondRow.setText(secondRowText)
        }


        if (isGroupNameEdittextTouched) {
            val isGrouNameThirdFocused = binding.groupNameSecondRow.isFocused
            log.info("is third group name focused: ${isGrouNameThirdFocused}")
            when {
                binding.groupNameSecondRow.isFocused && secondRowText.isEmpty() -> {
                    binding.groupNameFirstRow.requestFocus()
                    binding.groupNameFirstRow.setSelection(binding.groupNameFirstRow.text.length)
                    binding.groupNameSecondRow.isEnabled = false
                }
            }
        }

        binding.firstRowCharacterLength.text = "" + binding.groupNameFirstRow.text.length + "/" + MAX_ROW_LENGTH
        binding.secondRowCharacterLength.text = "" + binding.groupNameSecondRow.text.length + "/" + MAX_ROW_LENGTH

        // enable or disable second and third row in epaper
        binding.groupNameSecondRow.isEnabled = secondRowText.isNotEmpty()
    }

    private fun validateNameWithoutSpaces(nameText: String) {
        if (nameText.length < ROW_SWITCH_POSITION) {
            if (nameText != binding.groupNameFirstRow.text.toString())
                binding.groupNameFirstRow.setText(nameText.substring(STARTING_FROM_FIRST_CHARACTER, nameText.length))
            binding.groupNameSecondRow.setText("")
            binding.firstRowCharacterLength.text = "" + nameText.substring(STARTING_FROM_FIRST_CHARACTER, nameText.length).length + "/" + MAX_ROW_LENGTH
            binding.secondRowCharacterLength.text = "" + STARTING_FROM_FIRST_CHARACTER + "/" + MAX_ROW_LENGTH
            // disable second  row in epaper
            binding.groupNameSecondRow.isEnabled = false
        } else if (nameText.length > MAX_ROW_LENGTH && nameText.length < LESS_THAN_TOTAL_ALLOWED_CHARACTERS) {
            binding.groupNameFirstRow.setText(nameText.substring(STARTING_FROM_FIRST_CHARACTER, MAX_ROW_LENGTH))
            if (nameText != binding.groupNameSecondRow.text.toString())
                binding.groupNameSecondRow.setText(nameText.substring(MAX_ROW_LENGTH, nameText.length))
            binding.firstRowCharacterLength.text = "" + nameText.substring(STARTING_FROM_FIRST_CHARACTER, MAX_ROW_LENGTH).length + "/" + MAX_ROW_LENGTH
            binding.secondRowCharacterLength.text = "" + nameText.substring(MAX_ROW_LENGTH, nameText.length).length + "/" + MAX_ROW_LENGTH
            // disable second row in epaper
            binding.groupNameSecondRow.isEnabled = true
        } else if (nameText.length > SECOND_ROW_LAST_INDEX) {
            if (nameText != binding.groupNameFirstRow.text.toString())
                binding.groupNameFirstRow.setText(nameText.substring(STARTING_FROM_FIRST_CHARACTER, MAX_ROW_LENGTH))
            if (nameText != binding.groupNameSecondRow.text.toString())
                binding.groupNameSecondRow.setText(nameText.substring(MAX_ROW_LENGTH, SECOND_ROW_LAST_INDEX))
            binding.firstRowCharacterLength.text = "" + nameText.substring(STARTING_FROM_FIRST_CHARACTER, MAX_ROW_LENGTH).length + "/" + MAX_ROW_LENGTH
            binding.secondRowCharacterLength.text = "" + nameText.substring(MAX_ROW_LENGTH, SECOND_ROW_LAST_INDEX).length + "/" + MAX_ROW_LENGTH

            // disable second row in epaper
            binding.groupNameSecondRow.isEnabled = true
        }
    }

    private fun currentEdittextRemoveLastCharacterEmptySpace(currentText: String, currentRowLength: Int, newCharacter: String): String {

        if (currentRowLength > NO_CHARATER_IN_ROW_IN_EPAPER && newCharacter != " ") {
            val lastCharacterInRow = currentText.substring(currentText.length - 1)
            if (lastCharacterInRow == " ") {
                val correctRowString = currentText.trim()
                return correctRowString
            } else {
                return currentText
            }
        }
        return currentText
    }

    private fun notSelectedEdittextRemoveLastCharacterEmptySpace(notSelectedText: String, notSelectedRowLength: Int): String {
        if (notSelectedRowLength > NO_CHARATER_IN_ROW_IN_EPAPER) {
            val lastCharacterInRow = notSelectedText.substring(notSelectedText.length - 1)
            if (lastCharacterInRow == " ") {
                val correctRowString = notSelectedText.trim()
                return correctRowString
            } else
                return notSelectedText
        }
        return notSelectedText
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
//        binding.etName.setText(savedInstanceState.getString("name"))
//        binding.etPhone.setText(savedInstanceState.getString("phone"))
//        binding.etAddress.setText(savedInstanceState.getString("address"))
//        binding.etEmail.setText(savedInstanceState.getString("email"))
//        binding.etPassword.setText(savedInstanceState.getString("password"))
//        binding.etRepeatPassword.setText(savedInstanceState.getString("confirmPassword"))

        binding.acceptedTerms.isChecked = savedInstanceState.getBoolean("termsAndConditions")!!
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.run {

//            putString("name", binding.etName.text.toString())
//            putString("phone", binding.etPhone.text.toString())
//            putString("address", binding.etAddress.text.toString())
//            putString("email", binding.etEmail.text.toString())
//            putString("password", binding.etPassword.text.toString())
//            putString("confirmPassword", binding.etRepeatPassword.text.toString())

            putBoolean("termsAndConditions", binding.acceptedTerms.isChecked)
        }
        super.onSaveInstanceState(outState)
    }

    private fun validate(): Boolean {
        var validated = true
        if (!validateAddress()) {
            validated = false
        }
        if (!validateUsername()) {
            validated = false
        }
        if (!validateGroupName()) {
            validated = false
        }
        //if (!validateEmail(binding.tilEmail, binding.etEmail)) {
        //    validated = false
        //}
        if (!validateNewPassword()) {
            validated = false
        }
        if (!validateRepeatPassword()) {
            validated = false
        }
        if (!validateTermsAndConditions()) {
            validated = false
        }

        return validated

    }

    private fun validateAddress(): Boolean {
        return true
//        return validateEditText(binding.tilAddress, binding.etAddress) { address ->
//            if (address.isBlank() || address.length > 100) ValidationResult.INVALID_CITY_BLANK
//            else ValidationResult.VALID
//        }
    }

    private fun validateTermsAndConditions(): Boolean {
        if (!binding.acceptedTerms.isChecked) {
            binding.acceptedTerms.error = this.getString(R.string.error_accept_terms_and_conditions)
            return false
        }
        binding.acceptedTerms.error = null
        return true
    }

    private fun validateNewPassword(): Boolean {
        return true
//        return validateEditText(binding.tilPassword, binding.etPassword) { newPassword ->
//            when {
//                newPassword.isBlank() -> ValidationResult.INVALID_PASSWORD_BLANK
//                newPassword.length < 6 -> ValidationResult.INVALID_PASSWORD_MIN_6_CHARACTERS
//                else -> ValidationResult.VALID
//            }
//        }
    }

    private fun validateRepeatPassword(): Boolean {
        return true
//        return validateEditText(binding.tilRepeatPassword, binding.etRepeatPassword) { repeatPassword ->
//            val newPassword = binding.etPassword.text.toString().trim()
//            if (repeatPassword != newPassword && binding.etPassword.text.isNotEmpty()) {
//                correctPassword = false
//                ValidationResult.INVALID_PASSWORDS_DO_NOT_MATCH
//            } else {
//                correctPassword = true
//                ValidationResult.VALID
//            }
//        }
    }

    private fun validateUsername(): Boolean {
        return true
//        return validateEditText(binding.tilName, binding.etName) { username ->
//            when {
//                username.isBlank() -> ValidationResult.INVALID_USERNAME_BLANK
//                username.length < 4 -> ValidationResult.INVALID_USERNAME_MIN_4_CHARACTERS
//                else -> ValidationResult.VALID
//            }
//        }
    }

    private fun validateGroupName(): Boolean {
        val groupNameTogetherLength = binding.groupNameFirstRow.text.length + binding.groupNameSecondRow.text.length
        if (binding.groupNameFirstRow.text.isEmpty() && binding.groupNameSecondRow.text.isEmpty()) {
            binding.groupNameWrong.visibility = View.VISIBLE
            binding.groupNameWrong.text = getString(R.string.edit_user_validation_blank_fields_exist)
            //groupNameTitle.setTextColor(ContextCompat.getColor(baseContext, R.color.colorDarkAccent))
            return false
        } else if (groupNameTogetherLength < 4) {
            binding.groupNameWrong.visibility = View.VISIBLE
            binding.groupNameWrong.text = getString(R.string.edit_user_validation_group_name_min_4_characters)
            //groupNameTitle.setTextColor(ContextCompat.getColor(baseContext, R.color.colorDarkAccent))
            return false
        } else {
            binding.groupNameWrong.visibility = View.GONE
            //groupNameTitle.setTextColor(ContextCompat.getColor(baseContext, R.color.colorBlack))
            return true
        }
    }

    private fun getColorAttrValue(attr: Int): Int? {
        val attrArray = intArrayOf(attr)
        val typedArray = this@RegistrationActivity.obtainStyledAttributes(attrArray)
        val result = try {
            typedArray.getColorOrThrow(0)
        } catch (exc: Exception) {
            null
        }
        typedArray.recycle()
        return result
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(baseContext, LoginActivity::class.java)
                startActivity(intent)
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        val intent = Intent(baseContext, LoginActivity::class.java)
        startActivity(intent)
        finish()
        super.onBackPressed()
    }

}