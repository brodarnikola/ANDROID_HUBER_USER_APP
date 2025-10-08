package hr.sil.android.schlauebox.view.ui.intro


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
//import com.google.android.material.textfield.TextInputLayout
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.cache.DataCache
import hr.sil.android.schlauebox.core.remote.WSUser
import hr.sil.android.schlauebox.core.remote.model.RLanguage
import hr.sil.android.schlauebox.core.util.DeviceInfo
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.util.SettingsHelper
import hr.sil.android.schlauebox.util.backend.UserUtil
import hr.sil.android.schlauebox.util.connectivity.NetworkChecker
import hr.sil.android.schlauebox.view.ui.BaseActivity
import hr.sil.android.schlauebox.view.ui.LoginActivity
import hr.sil.android.schlauebox.view.ui.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
//import org.jetbrains.anko.*
//import org.jetbrains.anko.sdk15.coroutines.onFocusChange


class InviteUserActivity : BaseActivity(noWifiViewId = R.id.no_internet_layout) {

    val emailValue: String by lazy { intent.getStringExtra("email") ?: "" }
    val passwordValue: String by lazy { intent.getStringExtra("password") ?: "" }

    val log = logger()
    val registerButton by lazy { findViewById<Button>(R.id.register_button) }
    val password by lazy { findViewById<EditText>(R.id.register_password) }
    val repeatPassword by lazy { findViewById<EditText>(R.id.register_password_repeat) }
    val name by lazy { findViewById<EditText>(R.id.register_name) }
    //val groupName by lazy { findViewById<EditText>(R.id.register_group_name) }
    val phone by lazy { findViewById<EditText>(R.id.register_phone) }
    val address by lazy { findViewById<EditText>(R.id.register_address) }
    val email by lazy { findViewById<EditText>(R.id.register_email) }

    private val groupNameFirstRow by lazy { findViewById<EditText>(R.id.register_group_name_first_row) }
    private val groupNameSecondRow by lazy { findViewById<EditText>(R.id.registerGroupNameSecondRow) }

    private val registerConstraintLayoutGroupName by lazy { findViewById<View>(R.id.register_constraint_layout_group_name) }
    private val groupNameTitle by lazy { findViewById<TextView>(R.id.groupNameTitle) }

    private val firstRowCharacterLength by lazy { findViewById<TextView>(R.id.first_row_character_length) }
    private val secondRowCharacterLength by lazy { findViewById<TextView>(R.id.second_row_character_length) }
    private val groupNameWrong by lazy { findViewById<TextView>(R.id.groupNameWrong) }

    val progress_circular: ProgressBar by lazy { findViewById<ProgressBar>(R.id.progress_circular) }

//    val nameInputLayout: TextInputLayout by lazy { findViewById<TextInputLayout>(R.id.register_name_input_layout) }
//    //val groupNameInputLayout: TextInputLayout by lazy { findViewById<TextInputLayout>(R.id.register_group_name_input_layout) }
//    val addressInputLayout: TextInputLayout by lazy { findViewById<TextInputLayout>(R.id.register_address_input_layout) }
//    val emailInputLayout: TextInputLayout by lazy { findViewById<TextInputLayout>(R.id.register_email_input_layout) }
//    val passwordInputLayout: TextInputLayout by lazy { findViewById<TextInputLayout>(R.id.register_password_input_layout) }
//    val confirmPasswordInputLayout: TextInputLayout by lazy { findViewById<TextInputLayout>(R.id.register_confirm_password_input_layout) }

    val confirmPasswordConstraintLayout: ConstraintLayout by lazy { findViewById<ConstraintLayout>(R.id.register_constraint_layout_confirm_password) }

    val groupNameMaximumLength by lazy { findViewById<TextView>(R.id.groupNameTitle) }
    val GROUP_NAME_MAXIMUM_LENGTH: Int = 18

    var wrongPassword: Boolean = false

    val pushNotificationEnabled = true
    val emailEnabled = true

    var languageData: RLanguage = RLanguage()

    private var isGroupNameEdittextTouched = false
    private var programaticSetText = false
    private var togetherGroupName = ""
    private var isOnResumeFinished = false

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

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invited_user_registration)

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setDisplayShowHomeEnabled(true);
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    override fun onResume() {
        super.onResume()
        GlobalScope.launch {
            if (baseContext != null) {
                val languagesList = DataCache.getLanguages(true)
                languageData = languagesList.find { it.id == UserUtil.userInvitedTempdata?.languageId }
                        ?: RLanguage()
            }
        }

        val showPassword = findViewById<TextView>(R.id.login_show_password)

        registerButton.setOnClickListener {

            if (validate()) {
                progress_circular.visibility = View.VISIBLE
                registerButton.visibility = View.GONE

                wrongPassword = false

                val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
                //params.setMargins(0, dip(-20), 0, 0)
                //params.below(confirmPasswordConstraintLayout)
                //params.centerHorizontally()
                showPassword.layoutParams = params

                GlobalScope.launch {
                    if (NetworkChecker.isInternetConnectionAvailable()) {

                        val groupName = groupNameFirstRow.text.toString().trim() + " " + groupNameSecondRow.text.toString().trim()

                        val result = UserUtil.userUpdateInvited(name.text.toString(), address.text.toString(),
                                phone.text.toString(),
                                languageData.id,
                                pushNotificationEnabled,
                                emailEnabled,
                                groupName, emailValue, password.text.toString())

                        withContext(Dispatchers.Main) {

                            if( result ) {
                                SettingsHelper.userPasswordWithoutEncryption = password.text.toString()
                                SettingsHelper.userRegisterOrLogin = true
                                WSUser.registerDevice(UserUtil.fcmTokenRequest(), DeviceInfo.getJsonInstance())

                                val intent = Intent(this@InviteUserActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            else {
                                log.info("Error while login device")
                                //App.ref.toast(R.string.register_error)
                                progress_circular.visibility = View.GONE
                                registerButton.visibility = View.VISIBLE
                            }

                        }
                    } else {
                        //App.ref.toast(R.string.app_generic_no_network)
                    }
                }
            } else {
                log.error("Error while registering the user")
                if (wrongPassword) {

                    val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
                    params.setMargins(0, 0, 0, 0)
//                    params.below(confirmPasswordConstraintLayout)
//                    params.centerHorizontally()
                    showPassword.layoutParams = params
                } else {

                    val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
//                    params.setMargins(0, dip(-20), 0, 0)
//                    params.below(confirmPasswordConstraintLayout)
//                    params.centerHorizontally()
                    showPassword.layoutParams = params
                }
            }
        }

        showPassword.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    password.inputType = InputType.TYPE_CLASS_TEXT
                    repeatPassword.inputType = InputType.TYPE_CLASS_TEXT
                }
                MotionEvent.ACTION_UP -> {
                    password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    repeatPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                }
            }
            true
        }

        name.run {

            requestFocus()

            addTextChangedListener(object : TextWatcher {

                var fRInsertedCharLength = 0
                override fun afterTextChanged(p0: Editable?) {
                }

                override fun beforeTextChanged(p0: CharSequence?, start: Int, before: Int, count: Int) {
                    fRInsertedCharLength = name.text.length
                    log.info("First row.. Size before text changing is $fRInsertedCharLength")
                }

                override fun onTextChanged(nameText: CharSequence?, positionIndexChanged: Int, before: Int, count: Int) {

                    if (!isGroupNameEdittextTouched) {
                        if (nameText.toString().contains(" ")) {

                            var newCharacter = ""
                            if (fRInsertedCharLength > nameText.toString().length && nameText.toString().isNotEmpty()) {
                                newCharacter = if (positionIndexChanged == 0) nameText?.get(0).toString()
                                else {
                                    nameText?.get(positionIndexChanged - 1).toString()
                                }
                            } else if (nameText.toString().isNotEmpty())
                                newCharacter = nameText?.get(positionIndexChanged).toString()
                            log.info("last character is: $newCharacter")

                            validateNameWithSpaces(nameText.toString(), newCharacter)
                        } else {
                            validateNameWithoutSpaces(nameText.toString())
                        }
                    }
                }
            })
        }

        this.groupNameFirstRow.run {

//            onFocusChange { _, _ ->
//                isGroupNameEdittextTouched = true
//            }

            addTextChangedListener(object : TextWatcher {
                var fRInsertedCharLength = 0

                override fun afterTextChanged(p0: Editable?) {
                    if (programaticSetText && groupNameFirstRow.isFocused && groupNameFirstRow.text.isNotEmpty() && cursorPosition[INDEX_POSITION] <= groupNameFirstRow.text.length)
                        groupNameFirstRow.setSelection(cursorPosition[INDEX_POSITION])
                    else if (programaticSetText && groupNameFirstRow.isFocused && groupNameFirstRow.text.isNotEmpty() && cursorPosition[INDEX_POSITION] >= groupNameFirstRow.text.length)
                        groupNameFirstRow.setSelection(groupNameFirstRow.text.length)
                }

                override fun beforeTextChanged(p0: CharSequence?, start: Int, before: Int, count: Int) {
                    fRInsertedCharLength = groupNameFirstRow.text.length
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
                        } else if (!handleLastCharacterEmptySpace(groupNameFirstRow, groupNameSecondRow)) {
                            if (groupNameFirstRow.isFocused) {

                                togetherGroupName = parseGroupName()

                                positionCursorInCurrentEdittext(fRInsertedCharLength, firstRowText.toString(), positionIndexChanged, before, count)

                                displayGroupName(togetherGroupName, newCharacter)

                                if (positionIndexChanged + count - 1 == MAX_ROW_LENGTH) {
                                    groupNameSecondRow.isEnabled = true
                                    groupNameSecondRow.requestFocus()
                                    if (groupNameSecondRow.text.contains(" ")) {
                                        val firstWordInSecondRow = groupNameSecondRow.text.split(" ")
                                        groupNameSecondRow.setSelection(firstWordInSecondRow.first().length)
                                    } else {
                                        groupNameSecondRow.setSelection(groupNameSecondRow.text.length)
                                    }
                                }
                            }
                        }
                        programaticSetText = false
                    }
                }
            })
        }

        groupNameSecondRow.isEnabled = false

        groupNameSecondRow.run {

//            onFocusChange { _, _ ->
//                isGroupNameEdittextTouched = true
//            }

            addTextChangedListener(object : TextWatcher {

                var tRInsertedCharLength = 0

                override fun afterTextChanged(p0: Editable?) {
                    if (programaticSetText && groupNameSecondRow.isFocused && groupNameSecondRow.text.isNotEmpty() && cursorPosition[INDEX_POSITION] <= groupNameSecondRow.text.length) {
                        groupNameSecondRow.setSelection(cursorPosition[INDEX_POSITION])
                    }
                }

                override fun beforeTextChanged(p0: CharSequence?, start: Int, before: Int, count: Int) {
                    tRInsertedCharLength = groupNameSecondRow.text.length
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
                        } else if (groupNameSecondRow.isFocused) {

                            togetherGroupName = parseGroupName()

                            positionCursorInCurrentEdittext(tRInsertedCharLength, secondRowText.toString(), positionIndexChanged, before, count)

                            displayGroupName(togetherGroupName, newCharacter)
                        }
                        programaticSetText = false
                    }
                }
            })
        }

        name.setText(UserUtil.userInvitedTempdata?.name ?: "")
        //groupName.setText(UserUtil.user?.group___name ?: "")
        phone.setText(UserUtil.userInvitedTempdata?.telephone ?: "")
        address.setText(UserUtil.userInvitedTempdata?.address ?: "")
        email.setText(emailValue ?: "")
        password.setText("")
        repeatPassword.setText("")

        initializeView()

        displayGroupNameOnResume()
        isOnResumeFinished = true
    }

    private fun displayGroupNameOnResume() {
        if (UserUtil.userGroup?.name.toString().contains(" "))
            validateNameWithSpaces(UserUtil.userGroup?.name.toString(), "")
        else
            validateNameWithoutSpaces(UserUtil.userGroup?.name ?: "")
    }

    private fun validateNameWithSpaces(groupName: String?, newCharacter: String) {


        var firstRowCounterLength = 0
        var secondRowCounterLength = 0

        var firstRowText = ""
        var secondRowText = ""

        if (groupName != null) {
            val emptySpaces = groupName.split(" ")
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
        }

        when {
            groupNameFirstRow.isFocused || !isOnResumeFinished -> {
                firstRowText = currentEdittextRemoveLastCharacterEmptySpace(firstRowText, firstRowCounterLength, newCharacter)
                secondRowText = notSelectedEdittextRemoveLastCharacterEmptySpace(secondRowText, secondRowCounterLength)
            }
            groupNameSecondRow.isFocused || !isOnResumeFinished -> {
                firstRowText = notSelectedEdittextRemoveLastCharacterEmptySpace(firstRowText, firstRowCounterLength)
                secondRowText = currentEdittextRemoveLastCharacterEmptySpace(secondRowText, secondRowCounterLength, newCharacter)
            }
        }

        if (firstRowText.length >= MAX_ROW_LENGTH) {
            firstRowText = firstRowText.substring(STARTING_FROM_FIRST_CHARACTER, MAX_ROW_LENGTH)
        }
        if (secondRowText.length >= MAX_ROW_LENGTH) {
            secondRowText = secondRowText.substring(STARTING_FROM_FIRST_CHARACTER, MAX_ROW_LENGTH)
        }

        if (firstRowText != groupNameFirstRow.text.toString()) {
            groupNameFirstRow.setText(firstRowText)
        }
        if (secondRowText != groupNameSecondRow.text.toString()) {
            groupNameSecondRow.setText(secondRowText)
        }


        when {
            groupNameSecondRow.isFocused && secondRowText.isEmpty() -> {
                val isGrouNameThirdFocused = groupNameSecondRow.isFocused
                log.info("da li ce ikad uciiii aaaaa : ${isGrouNameThirdFocused}")
                groupNameFirstRow.requestFocus()
                groupNameFirstRow.setSelection(groupNameFirstRow.text.length)
                groupNameSecondRow.isEnabled = false
            }
        }

        firstRowCharacterLength.text = "" + groupNameFirstRow.text.length + "/" + MAX_ROW_LENGTH
        secondRowCharacterLength.text = "" + groupNameSecondRow.text.length + "/" + MAX_ROW_LENGTH

        // enable or disable second and third row in epaper
        groupNameSecondRow.isEnabled = secondRowText.isNotEmpty()
    }

    private fun validateNameWithoutSpaces(nameText: String) {
        if (nameText.length < ROW_SWITCH_POSITION) {
            if (nameText != groupNameFirstRow.text.toString())
                groupNameFirstRow.setText(nameText.substring(STARTING_FROM_FIRST_CHARACTER, nameText.length))
            groupNameSecondRow.setText("")
            firstRowCharacterLength.text = "" + nameText.substring(STARTING_FROM_FIRST_CHARACTER, nameText.length).length + "/" + MAX_ROW_LENGTH
            secondRowCharacterLength.text = "" + STARTING_FROM_FIRST_CHARACTER + "/" + MAX_ROW_LENGTH
            // disable second  row in epaper
            groupNameSecondRow.isEnabled = false
        } else if (nameText.length > MAX_ROW_LENGTH && nameText.length < LESS_THAN_TOTAL_ALLOWED_CHARACTERS) {
            groupNameFirstRow.setText(nameText.substring(STARTING_FROM_FIRST_CHARACTER, MAX_ROW_LENGTH))
            if (nameText != groupNameSecondRow.text.toString())
                groupNameSecondRow.setText(nameText.substring(MAX_ROW_LENGTH, nameText.length))
            firstRowCharacterLength.text = "" + nameText.substring(STARTING_FROM_FIRST_CHARACTER, MAX_ROW_LENGTH).length + "/" + MAX_ROW_LENGTH
            secondRowCharacterLength.text = "" + nameText.substring(MAX_ROW_LENGTH, nameText.length).length + "/" + MAX_ROW_LENGTH
            // disable second row in epaper
            groupNameSecondRow.isEnabled = true
        } else if (nameText.length > SECOND_ROW_LAST_INDEX) {
            if (nameText != groupNameFirstRow.text.toString())
                groupNameFirstRow.setText(nameText.substring(STARTING_FROM_FIRST_CHARACTER, MAX_ROW_LENGTH))
            if (nameText != groupNameSecondRow.text.toString())
                groupNameSecondRow.setText(nameText.substring(MAX_ROW_LENGTH, SECOND_ROW_LAST_INDEX))
            firstRowCharacterLength.text = "" + nameText.substring(STARTING_FROM_FIRST_CHARACTER, MAX_ROW_LENGTH).length + "/" + MAX_ROW_LENGTH
            secondRowCharacterLength.text = "" + nameText.substring(MAX_ROW_LENGTH, SECOND_ROW_LAST_INDEX).length + "/" + MAX_ROW_LENGTH

            // disable second row in epaper
            groupNameSecondRow.isEnabled = true
        }
    }


    private fun jumpFromSecondRowToFirstRow() {

        groupNameFirstRow.requestFocus()
        groupNameFirstRow.setSelection(groupNameFirstRow.text.length)
        groupNameSecondRow.isEnabled = false
        groupNameSecondRow.setText("")
        secondRowCharacterLength.text = "0/" + MAX_ROW_LENGTH
    }

    private fun reorderAndMoveAllTextUp() {
        if (groupNameSecondRow.text.isNotEmpty()) {
            groupNameFirstRow.setText(groupNameSecondRow.text.toString())
            groupNameSecondRow.setText("")
            groupNameSecondRow.isEnabled = false
            firstRowCharacterLength.text = "" + groupNameFirstRow.text.length + "/" + MAX_ROW_LENGTH
            secondRowCharacterLength.text = "" + groupNameSecondRow.text.length + "/" + MAX_ROW_LENGTH
        } else {
            firstRowCharacterLength.text = "" + groupNameFirstRow.text.length + "/" + MAX_ROW_LENGTH
        }
    }


    private fun parseGroupName(): String {

        var groupName = ""

        if (groupNameSecondRow.text.isEmpty()) groupName = groupNameFirstRow.text.toString()
        else if (groupNameSecondRow.text.isNotEmpty()) {
            if (groupNameFirstRow.text.last().toString() == " ")
                groupName = groupNameFirstRow.text.toString() + groupNameSecondRow.text.toString()
            else
                groupName = groupNameFirstRow.text.toString() + " " + groupNameSecondRow.text.toString()
        }
        return groupName
    }


    private fun handleLastCharacterEmptySpace(currentEditText: EditText, nextEditText: EditText?): Boolean {

        if (currentEditText.selectionEnd == ROW_SWITCH_POSITION && currentEditText.text.length == ROW_SWITCH_POSITION
                && currentEditText.text.last().toString() == " ") {
            nextEditText?.isEnabled = true
            nextEditText?.requestFocus()

            if (cursorPosition[ROW_POSITION] < 3) cursorPosition[ROW_POSITION] = +1
            cursorPosition[INDEX_POSITION] = 0
            currentEditText.setText(currentEditText.text.toString().dropLast(1))
            return true
        }
        return false
    }

    private fun positionCursorInCurrentEdittext(currentTextLength: Int, currentText: String, positionIndexChanged: Int, before: Int, count: Int) {
        if (currentTextLength > currentText.length) {
            cursorPosition[INDEX_POSITION] = positionIndexChanged + count
        } else {
            cursorPosition[INDEX_POSITION] = positionIndexChanged + before + 1
        }
    }

    fun displayGroupName(groupName: String, newCharacter: String) {
        if (groupName.contains(" ")) {
            validateNameWithSpaces(groupName, newCharacter)
        } else {
            validateNameWithoutSpaces(groupName)
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

    override fun onBackPressed() {
        super.onBackPressed()

        val startIntent = Intent(this@InviteUserActivity, LoginActivity::class.java)
        startActivity(startIntent)
        finish()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val startIntent = Intent(this@InviteUserActivity, LoginActivity::class.java)
                startActivity(startIntent)
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }


    private fun initializeView() {
        attachValidators()
    }

    private fun attachValidators() {
       // attachValidator(email) { validateEmail(emailInputLayout, email) }
    }


    private fun validate(): Boolean {

        var validated = true
        if (!validateGroupName(groupNameFirstRow.text.toString(), groupNameSecondRow.text.toString())) {
            validated = false
            if (password.text.toString().trim() == repeatPassword.text.toString().trim() && password.text.isNotEmpty()) {
                wrongPassword = false
            }
        }
        /*if (!validateGroupName(false)) {
            validated = false
            if (password.text.toString().trim() == repeatPassword.text.toString().trim() && password.text.length > 0) {
                wrongPassword = false
            }
        }*/
        if (!validateAddress(false)) {
            validated = false
            if (password.text.toString().trim() == repeatPassword.text.toString().trim() && password.text.length > 0) {
                wrongPassword = false
            }
        }
        if (!validateUsername(false)) {
            validated = false
            if (password.text.toString().trim() == repeatPassword.text.toString().trim() && password.text.length > 0) {
                wrongPassword = false
            }
        }
        if (!validateNewPassword(false)) {
            validated = false
            if (password.text.toString().trim() == repeatPassword.text.toString().trim() && password.text.length > 0) {
                wrongPassword = false
            }
        }
        if (!validateRepeatPassword(false)) {

            wrongPassword = true
            validated = false
        }

        return validated
    }

    private fun validateGroupName(groupNameFirstRow: String, groupNameSecondRow: String): Boolean {
        val groupNameTogetherLength = groupNameFirstRow.length + groupNameSecondRow.length
        if (groupNameFirstRow.isEmpty() && groupNameSecondRow.isEmpty()) {
            groupNameWrong.visibility = View.VISIBLE
            groupNameWrong.text = getString(R.string.edit_user_validation_blank_fields_exist)
            groupNameTitle.setTextColor(ContextCompat.getColor(this@InviteUserActivity, R.color.colorDarkAccent))
            return false
        } else if (groupNameTogetherLength < 4) {
            groupNameWrong.visibility = View.VISIBLE
            groupNameWrong.text = getString(R.string.edit_user_validation_group_name_min_4_characters)
            groupNameTitle.setTextColor(ContextCompat.getColor(this@InviteUserActivity, R.color.colorDarkAccent))
            return false
        } else {
            groupNameWrong.visibility = View.GONE
            groupNameTitle.setTextColor(ContextCompat.getColor(this@InviteUserActivity, R.color.colorBlack))
            return true
        }
    }

    /*private fun validateGroupName(showDialog: Boolean): Boolean {
        return validateEditText(groupNameInputLayout, groupName, showDialog) { groupNameText ->
            when {
                groupNameText.isBlank() -> ValidationResult.INVALID_PASSWORD_BLANK
                groupName.text[0].toString() == " " -> ValidationResult.INVALID_GROUP_NAME_FIRST_CHARACTER
                else -> ValidationResult.VALID
            }
        }
    }*/

    private fun validateAddress(showDialog: Boolean): Boolean {
//        return validateEditText(addressInputLayout, address) { address ->
//            if (address.isBlank() || address.length > 100) ValidationResult.INVALID_CITY_BLANK
//            else ValidationResult.VALID
//        }
        return true
    }


    private fun validateNewPassword(showDialog: Boolean): Boolean {
//        return validateEditText(passwordInputLayout, password) { newPassword ->
//            when {
//                newPassword.isBlank() -> ValidationResult.INVALID_PASSWORD_BLANK
//                newPassword == passwordValue -> ValidationResult.INVALID_PASSWORD_NEED_TO_BE_DIFFERENT
//                newPassword.length < 6 -> ValidationResult.INVALID_PASSWORD_MIN_6_CHARACTERS
//                else -> ValidationResult.VALID
//            }
//        }
        return true
    }

    private fun validateRepeatPassword(showDialog: Boolean): Boolean {
//        return validateEditText(confirmPasswordInputLayout, repeatPassword) { repeatPassword ->
//            val newPassword = password.text.toString().trim()
//            if (repeatPassword != newPassword) {
//                ValidationResult.INVALID_PASSWORDS_DO_NOT_MATCH
//            } else {
//                ValidationResult.VALID
//            }
//        }
        return true
    }

    private fun validateUsername(showDialog: Boolean): Boolean {
//        return validateEditText(nameInputLayout, name) { username ->
//            when {
//                username.isBlank() -> ValidationResult.INVALID_USERNAME_BLANK
//                username.length < 4 -> ValidationResult.INVALID_USERNAME_MIN_4_CHARACTERS
//                else -> ValidationResult.VALID
//            }
//        }
        return true
    }

    override fun onNetworkStateUpdated(available: Boolean) {
        super.onNetworkStateUpdated(available)
        networkAvailable = available
        updateUI()
    }

}
