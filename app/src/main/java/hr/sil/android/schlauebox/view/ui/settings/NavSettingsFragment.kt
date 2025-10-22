package hr.sil.android.schlauebox.view.ui.settings


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.core.remote.WSUser
//import hr.sil.android.schlauebox.cache.DataCache
import hr.sil.android.schlauebox.core.remote.model.RLanguage
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.databinding.FragmentSettingsScreenBinding
import hr.sil.android.schlauebox.util.SettingsHelper
import hr.sil.android.schlauebox.util.backend.UserUtil
import hr.sil.android.schlauebox.view.ui.base.BaseFragment
import hr.sil.android.schlauebox.view.ui.dialog.LogoutDialog
import kotlinx.coroutines.*


class NavSettingsFragment : BaseFragment() {

    private lateinit var selectedLanguage: RLanguage

    private var scrollPosition: Int = 0
    private val log = logger()

    private val MAX_ROW_LENGTH = 15
    private val NO_CHARATER_IN_ROW_IN_EPAPER = 0
    private val ROW_SWITCH_POSITION = MAX_ROW_LENGTH + 1
    private val FIRST_INDEX_IN_LIST_SPLITED_BY_SPACE = 0
    private val LESS_THAN_TOTAL_ALLOWED_CHARACTERS = 31

    private val STARTING_FROM_FIRST_CHARACTER = 0

    private val SECOND_ROW_LAST_INDEX = MAX_ROW_LENGTH * 2

    private val INCREASE_BY_ONE_BECAUSE_OF_EMPTY_SPACE = 1

    private val PASSWORD_LENGTH_GREATHER_THEN_ZERO = 0
    private val PASSWORD_LENGTH_SMALLER_THEN_SIX = 6

    private val ROW_POSITION = 0
    private val INDEX_POSITION = 1
    private var cursorPosition = IntArray(2)

    private var programaticSetText = false
    private var isOnResumeFinished = false

    private lateinit var binding: FragmentSettingsScreenBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = FragmentSettingsScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        GlobalScope.launch {
            val list = WSUser.getLanguages() ?: listOf()
            withContext(Dispatchers.Main) {
                binding.settingsLanguageSelection.adapter = LanguageAdapter(list)
                if (context != null) {

                    val languageName = SettingsHelper.languageName
                    binding.settingsLanguageSelection.setSelection(list.indexOfFirst { it.code == languageName })
                    binding.settingsPushNotification.isChecked = SettingsHelper.pushEnabled
                    binding.settingsEmailNotification.isChecked = SettingsHelper.emailEnabled
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setUserDataIntoEdittext()

        cursorPosition[ROW_POSITION] = 0
        cursorPosition[INDEX_POSITION] = 0

        binding.settingsSignOut.setOnClickListener {
            val logoutDialog = LogoutDialog()
            activity?.supportFragmentManager?.let { it -> logoutDialog.show(it, "") }
        }

        addChangeInputListeners()

//        binding.settingsLanguageSelection.onItemSelectedListener {
//            onItemSelected { adapterView, _, position, _ ->
//                enableButtonSaveChanges()
//                selectedLanguage = adapterView?.getItemAtPosition(position) as RLanguage
//            }
//            onNothingSelected {
//            }
//        }

        var togetherGroupName = ""

        binding.registerGroupNameFirstRow.run {

            addTextChangedListener(object : TextWatcher {
                var fRInsertedCharLength = 0

                override fun afterTextChanged(p0: Editable?) {
                    if (programaticSetText && binding.registerGroupNameFirstRow.isFocused && binding.registerGroupNameFirstRow.text.isNotEmpty() && cursorPosition[INDEX_POSITION] <= binding.registerGroupNameFirstRow.text.length)
                        binding.registerGroupNameFirstRow.setSelection(cursorPosition[INDEX_POSITION])
                    else if (programaticSetText && binding.registerGroupNameFirstRow.isFocused && binding.registerGroupNameFirstRow.text.isNotEmpty() && cursorPosition[INDEX_POSITION] >= binding.registerGroupNameFirstRow.text.length)
                        binding.registerGroupNameFirstRow.setSelection(binding.registerGroupNameFirstRow.text.length)
                }

                override fun beforeTextChanged(p0: CharSequence?, start: Int, before: Int, count: Int) {
                    fRInsertedCharLength = binding.registerGroupNameFirstRow.text.length
                    log.info("First row.. Size before text changing is $fRInsertedCharLength")
                }

                override fun onTextChanged(firstRowText: CharSequence?, positionIndexChanged: Int, before: Int, count: Int) {
                    enableButtonSaveChanges()

                    if (programaticSetText) return

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
                    } else if (!handleLastCharacterEmptySpace(binding.registerGroupNameFirstRow, binding.registerGroupNameSecondRow)) {
                        if (binding.registerGroupNameFirstRow.isFocused) {

                            togetherGroupName = parseGroupName()

                            positionCursorInCurrentEdittext(fRInsertedCharLength, firstRowText.toString(), positionIndexChanged, before, count)

                            displayGroupName(togetherGroupName, newCharacter)

                            if (positionIndexChanged + count - 1 == MAX_ROW_LENGTH) {
                                binding.registerGroupNameSecondRow.isEnabled = true
                                binding.registerGroupNameSecondRow.requestFocus()
                                if (binding.registerGroupNameSecondRow.text.contains(" ")) {
                                    val firstWordInSecondRow = binding.registerGroupNameSecondRow.text.split(" ")
                                    binding.registerGroupNameSecondRow.setSelection(firstWordInSecondRow.first().length)
                                } else {
                                    binding.registerGroupNameSecondRow.setSelection(binding.registerGroupNameSecondRow.text.length)
                                }
                            }
                        }
                    }
                    programaticSetText = false
                }
            })
        }

        binding.registerGroupNameSecondRow.run {

            addTextChangedListener(object : TextWatcher {

                var tRInsertedCharLength = 0

                override fun afterTextChanged(p0: Editable?) {
                    if (programaticSetText && binding.registerGroupNameSecondRow.isFocused && binding.registerGroupNameSecondRow.text.isNotEmpty() && cursorPosition[INDEX_POSITION] <= binding.registerGroupNameSecondRow.text.length) {
                        binding.registerGroupNameSecondRow.setSelection(cursorPosition[INDEX_POSITION])
                    }
                }

                override fun beforeTextChanged(p0: CharSequence?, start: Int, before: Int, count: Int) {
                    tRInsertedCharLength = binding.registerGroupNameSecondRow.text.length
                    log.info("Second row.. Size before text changing is $tRInsertedCharLength")
                }

                override fun onTextChanged(secondRowText: CharSequence?, positionIndexChanged: Int, before: Int, count: Int) {
                    enableButtonSaveChanges()
                    if (programaticSetText) return

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
                    } else if (binding.registerGroupNameSecondRow.isFocused) {

                        togetherGroupName = parseGroupName()

                        positionCursorInCurrentEdittext(tRInsertedCharLength, secondRowText.toString(), positionIndexChanged, before, count)

                        displayGroupName(togetherGroupName, newCharacter)
                    }
                    programaticSetText = false
                }
            })
        }

//        binding.pickupPasswordInputOld.run {
//
//            addTextChangedListener(object : TextWatcher {
//
//                override fun afterTextChanged(p0: Editable?) {
//                }
//
//                override fun beforeTextChanged(p0: CharSequence?, start: Int, before: Int, count: Int) {
//                }
//
//                override fun onTextChanged(secondRowText: CharSequence?, positionIndexChanged: Int, before: Int, count: Int) {
//                    handlePasswordInput()
//                }
//            })
//        }
//
//        binding.pickupPasswordInput.run {
//
//            addTextChangedListener(object : TextWatcher {
//
//                override fun afterTextChanged(p0: Editable?) {
//                }
//
//                override fun beforeTextChanged(p0: CharSequence?, start: Int, before: Int, count: Int) {
//                }
//
//                override fun onTextChanged(secondRowText: CharSequence?, positionIndexChanged: Int, before: Int, count: Int) {
//                    handlePasswordInput()
//                }
//            })
//        }
//
//        binding.pickupPasswordRepeate.run {
//
//            addTextChangedListener(object : TextWatcher {
//
//                override fun afterTextChanged(p0: Editable?) {
//                }
//
//                override fun beforeTextChanged(p0: CharSequence?, start: Int, before: Int, count: Int) {
//                }
//
//                override fun onTextChanged(secondRowText: CharSequence?, positionIndexChanged: Int, before: Int, count: Int) {
//                    handlePasswordInput()
//                }
//            })
//        }
//
//        binding.settingsSubmit.setOnClickListener {
//
//            if (binding.registerGroupNameFirstRow.text.isEmpty()) {
//                App.ref.toast(getString(R.string.edit_user_validation_blank_fields_exist))
//            } else if (binding.registerGroupNameFirstRow.text[0].toString() == " ") {
//                App.ref.toast(getString(R.string.group_name_correct_start))
//            } else if (binding.registerGroupNameFirstRow.text.length + binding.registerGroupNameSecondRow.text.length < 4) {
//                App.ref.toast(getString(R.string.edit_user_validation_group_name_min_4_characters))
//            } else if (binding.pickupPasswordInputOld.text.toString().isNotEmpty() && binding.pickupPasswordInputOld.text.toString() != SettingsHelper.userPasswordWithoutEncryption) {
//                App.ref.toast(getString(R.string.edit_user_validation_current_password_invalid))
//            } else if (binding.pickupPasswordInput.text.toString() != binding.pickupPasswordRepeate.text.toString()) {
//                App.ref.toast(getString(R.string.app_generic_password_doesnt_match))
//            } else {
//
//                binding.progressBar.visibility = View.VISIBLE
//                binding.settingsSubmit.visibility = View.GONE
//
//                GlobalScope.launch {
//                    val groupName = binding.registerGroupNameFirstRow.text.toString().trim() + " " + binding.registerGroupNameSecondRow.text.toString().trim()
//                    var result = UserUtil.userUpdate(binding.navSettingsNameEdit.text.toString(),
//                        binding.navSettingsAddressEdit.text.toString(),
//                        binding.navSettingsPhone.text.toString(),
//                        selectedLanguage,
//                        binding.settingsPushNotification.isChecked,
//                        binding.settingsEmailNotification.isChecked,
//                        groupName,
//                        binding.settingsReducedMobility.isChecked
//                    )
//
//                    if (binding.pickupPasswordInputOld.text.toString().isNotEmpty() && binding.pickupPasswordRepeate.text.toString().isNotEmpty()
//                            && binding.pickupPasswordInput.text.toString() == binding.pickupPasswordRepeate.text.toString()) {
//                        result = UserUtil.passwordUpdate(email = binding.navSettingsEmailEdit.text.toString(), newPassword = binding.pickupPasswordInput.text.toString(), oldPassword = binding.pickupPasswordInputOld.text.toString())
//                        if (result)
//                            SettingsHelper.userPasswordWithoutEncryption = binding.pickupPasswordRepeate.text.toString()
//                    }
//
//                    withContext(Dispatchers.Main) {
//                        if (result) {
//
//                            App.ref.toast(requireContext().getString(R.string.nav_settings_password_save_success, UserUtil.user?.id.toString()))
//                            App.ref.languageCode = selectedLanguage
//                            binding.pickupPasswordInputOld.setText("")
//                            binding.pickupPasswordInput.setText("")
//                            binding.pickupPasswordRepeate.setText("")
//                            SettingsHelper.languageName = selectedLanguage.code
//                            SettingsHelper.pushEnabled = binding.settingsPushNotification.isChecked
//                            SettingsHelper.emailEnabled = binding.settingsEmailNotification.isChecked
//                            UserUtil.userGroup?.name = binding.registerGroupNameFirstRow.text.toString().trim() + " " + binding.registerGroupNameSecondRow.text.toString().trim()
//                            val intent = Intent(context, MainActivity1::class.java)
//                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//                            this@NavSettingsFragment.activity?.finish()
//                            this@NavSettingsFragment.activity?.overridePendingTransition(0, 0)
//                            this@NavSettingsFragment.activity?.startActivity(intent)
//                        } else {
//                            binding.progressBar.visibility = View.GONE
//                            binding.settingsSubmit.visibility = View.VISIBLE
//                            binding.settingsLanguageSelection.setSelection(DataCache.getLanguages().toList().indexOfFirst { it.code == App.ref.languageCode.code })
//                            App.ref.toast(requireContext().getString(R.string.error_while_saving_user, UserUtil.user?.id.toString() + " " /* + groupName.text.toString()*/))
//                        }
//                    }
//                }
//            }
//        }

        disableSaveButtonOnViewCreated()
    }

    private fun setUserDataIntoEdittext() {
//        binding.navSettingsNameEdit.setText(UserUtil.user?.name)
//        binding.navSettingsPhone.setText(UserUtil.user?.telephone)
//        binding.navSettingsEmailEdit.setText(UserUtil.user?.email)
//        binding.navSettingsAddressEdit.setText(UserUtil.user?.address)
        binding.settingsReducedMobility.isChecked = UserUtil.user?.reducedMobility ?: false
        binding.tvAppVersion.setText(resources.getString(R.string.nav_settings_app_version, resources.getString(R.string.app_version)))
    }

    private fun handlePasswordInput( ) {

//        var correctPasswordInput = true
//        when {
//            binding.pickupPasswordInputOld.text.isEmpty() && binding.pickupPasswordInput.text.isEmpty() && binding.pickupPasswordRepeate.text.isEmpty() -> {
//                binding.wrongOldPassword.visibility = View.GONE
//                binding.wrongNewPassword.visibility = View.GONE
//                binding.wrongRetypePassword.visibility = View.GONE
//            }
//            else -> {
//
//                when {
//                    binding.pickupPasswordInputOld.text.isEmpty() -> {
//                        binding.wrongOldPassword.visibility = View.VISIBLE
//                        binding.wrongOldPassword.setText(requireContext().getText(R.string.change_password_missing_fields))
//                        correctPasswordInput = false
//                    }
//                    binding.pickupPasswordInputOld.text.length > PASSWORD_LENGTH_GREATHER_THEN_ZERO && binding.pickupPasswordInputOld.text.length < PASSWORD_LENGTH_SMALLER_THEN_SIX -> {
//                        binding.wrongOldPassword.visibility = View.VISIBLE
//                        binding.wrongOldPassword.setText(requireContext().getText(R.string.edit_user_validation_password_min_6_characters))
//                        correctPasswordInput = false
//                    }
//                    else -> binding.wrongOldPassword.visibility = View.GONE
//                }
//
//                when {
//                    binding.pickupPasswordInput.text.isEmpty() -> {
//                        binding.wrongNewPassword.visibility = View.VISIBLE
//                        binding.wrongNewPassword.setText(requireContext().getText(R.string.change_password_missing_fields))
//                        correctPasswordInput = false
//                    }
//                    binding.pickupPasswordInput.text.length > PASSWORD_LENGTH_GREATHER_THEN_ZERO && binding.pickupPasswordInput.text.length < PASSWORD_LENGTH_SMALLER_THEN_SIX -> {
//                        binding.wrongNewPassword.visibility = View.VISIBLE
//                        binding.wrongNewPassword.setText(requireContext().getText(R.string.edit_user_validation_password_min_6_characters))
//                        correctPasswordInput = false
//                    }
//                    else -> binding.wrongNewPassword.visibility = View.GONE
//                }
//
//                when {
//                    binding.pickupPasswordRepeate.text.isEmpty() -> {
//                        binding.wrongRetypePassword.visibility = View.VISIBLE
//                        binding.wrongRetypePassword.setText(requireContext().getText(R.string.change_password_missing_fields))
//                        correctPasswordInput = false
//                    }
//                    binding.pickupPasswordRepeate.text.length > PASSWORD_LENGTH_GREATHER_THEN_ZERO && binding.pickupPasswordRepeate.text.length < PASSWORD_LENGTH_SMALLER_THEN_SIX -> {
//                        binding.wrongRetypePassword.visibility = View.VISIBLE
//                        binding.wrongRetypePassword.setText(requireContext().getText(R.string.edit_user_validation_password_min_6_characters))
//                        correctPasswordInput = false
//                    }
//                    else -> binding.wrongRetypePassword.visibility = View.GONE
//                }
//            }
//        }
//
//        when {
//            correctPasswordInput -> {
//                binding.settingsSubmit.background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_button)
//                binding.settingsSubmit.isEnabled = true
//            }
//            else -> {
//                binding.settingsSubmit.background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_button_gray)
//                binding.settingsSubmit.isEnabled = false
//            }
//        }
    }

    override fun onResume() {
        super.onResume()
        if (scrollPosition > 0) {
            binding.navSettingsScrollList.scrollTo(0, scrollPosition)
            scrollPosition = binding.navSettingsScrollList.scrollY
        }
        displayGroupNameOnResume()
        isOnResumeFinished = true
    }

    private fun displayGroupNameOnResume() {
        if (UserUtil.userGroup?.name.toString().contains(" "))
            validateNameWithSpaces(UserUtil.userGroup?.name.toString(), "")
        else
            validateNameWithoutSpaces(UserUtil.userGroup?.name)
    }

    private fun validateNameWithoutSpaces(nameText: String?) {
        if (nameText != null) {
            if (nameText.length < ROW_SWITCH_POSITION) {
                if (nameText != binding.registerGroupNameFirstRow.text.toString())
                    binding.registerGroupNameFirstRow.setText(nameText.substring(STARTING_FROM_FIRST_CHARACTER, nameText.length))
                binding.registerGroupNameSecondRow.setText("")
                binding.firstRowCharacterLength.text = "" + nameText.substring(STARTING_FROM_FIRST_CHARACTER, nameText.length).length + "/" + MAX_ROW_LENGTH
                binding.secondRowCharacterLength.text = "" + STARTING_FROM_FIRST_CHARACTER + "/" + MAX_ROW_LENGTH
                // disable second  row in epaper
                binding.registerGroupNameSecondRow.isEnabled = false
            } else if (nameText.length > MAX_ROW_LENGTH && nameText.length < LESS_THAN_TOTAL_ALLOWED_CHARACTERS) {
                binding.registerGroupNameFirstRow.setText(nameText.substring(STARTING_FROM_FIRST_CHARACTER, MAX_ROW_LENGTH))
                if (nameText != binding.registerGroupNameSecondRow.text.toString())
                    binding.registerGroupNameSecondRow.setText(nameText.substring(MAX_ROW_LENGTH, nameText.length))
                binding.firstRowCharacterLength.text = "" + nameText.substring(STARTING_FROM_FIRST_CHARACTER, MAX_ROW_LENGTH).length + "/" + MAX_ROW_LENGTH
                binding.secondRowCharacterLength.text = "" + nameText.substring(MAX_ROW_LENGTH, nameText.length).length + "/" + MAX_ROW_LENGTH
                // disable second row in epaper
                binding.registerGroupNameSecondRow.isEnabled = true
            } else if (nameText.length > SECOND_ROW_LAST_INDEX) {
                if (nameText != binding.registerGroupNameFirstRow.text.toString())
                    binding.registerGroupNameFirstRow.setText(nameText.substring(STARTING_FROM_FIRST_CHARACTER, MAX_ROW_LENGTH))
                if (nameText != binding.registerGroupNameSecondRow.text.toString())
                    binding.registerGroupNameSecondRow.setText(nameText.substring(MAX_ROW_LENGTH, SECOND_ROW_LAST_INDEX))
                binding.firstRowCharacterLength.text = "" + nameText.substring(STARTING_FROM_FIRST_CHARACTER, MAX_ROW_LENGTH).length + "/" + MAX_ROW_LENGTH
                binding.secondRowCharacterLength.text = "" + nameText.substring(MAX_ROW_LENGTH, SECOND_ROW_LAST_INDEX).length + "/" + MAX_ROW_LENGTH

                // disable second row in epaper
                binding.registerGroupNameSecondRow.isEnabled = true
            }
        }
    }

    fun displayGroupName(groupName: String, newCharacter: String) {
        if (groupName.contains(" ")) {
            validateNameWithSpaces(groupName, newCharacter)
        } else {
            validateNameWithoutSpaces(groupName)
        }
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
            binding.registerGroupNameFirstRow.isFocused || !isOnResumeFinished -> {
                firstRowText = currentEdittextRemoveLastCharacterEmptySpace(firstRowText, firstRowCounterLength, newCharacter)
                secondRowText = notSelectedEdittextRemoveLastCharacterEmptySpace(secondRowText, secondRowCounterLength)
            }
            binding.registerGroupNameSecondRow.isFocused || !isOnResumeFinished -> {
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

        if (firstRowText != binding.registerGroupNameFirstRow.text.toString()) {
            binding.registerGroupNameFirstRow.setText(firstRowText)
        }
        if (secondRowText != binding.registerGroupNameSecondRow.text.toString()) {
            binding.registerGroupNameSecondRow.setText(secondRowText)
        }


        when {
            binding.registerGroupNameSecondRow.isFocused && secondRowText.isEmpty() -> {
                val isGrouNameThirdFocused = binding.registerGroupNameSecondRow.isFocused
                log.info("da li ce ikad uciiii aaaaa : ${isGrouNameThirdFocused}")
                binding.registerGroupNameFirstRow.requestFocus()
                binding.registerGroupNameFirstRow.setSelection(binding.registerGroupNameFirstRow.text.length)
                binding.registerGroupNameSecondRow.isEnabled = false
            }
        }

        binding.firstRowCharacterLength.text = "" + binding.registerGroupNameFirstRow.text.length + "/" + MAX_ROW_LENGTH
        binding.secondRowCharacterLength.text = "" + binding.registerGroupNameSecondRow.text.length + "/" + MAX_ROW_LENGTH

        // enable or disable second and third row in epaper
        binding.registerGroupNameSecondRow.isEnabled = secondRowText.isNotEmpty()
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

    private fun parseGroupName(): String {

        var groupName = ""

        if (binding.registerGroupNameSecondRow.text.isEmpty()) groupName = binding.registerGroupNameFirstRow.text.toString()
        else if (binding.registerGroupNameSecondRow.text.isNotEmpty()) {
            if (binding.registerGroupNameFirstRow.text.last().toString() == " ")
                groupName = binding.registerGroupNameFirstRow.text.toString() + binding.registerGroupNameSecondRow.text.toString()
            else
                groupName = binding.registerGroupNameFirstRow.text.toString() + " " + binding.registerGroupNameSecondRow.text.toString()
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
        when {
            currentTextLength > currentText.length -> cursorPosition[INDEX_POSITION] = positionIndexChanged + count
            else -> cursorPosition[INDEX_POSITION] = positionIndexChanged + before + 1
        }
    }

    private fun jumpFromSecondRowToFirstRow() {

        binding.registerGroupNameFirstRow.requestFocus()
        binding.registerGroupNameFirstRow.setSelection(binding.registerGroupNameFirstRow.text.length)
        binding.registerGroupNameSecondRow.isEnabled = false
        binding.registerGroupNameSecondRow.setText("")
        binding.secondRowCharacterLength.text = "0/" + MAX_ROW_LENGTH
    }

    private fun reorderAndMoveAllTextUp() {
        if (binding.registerGroupNameSecondRow.text.isNotEmpty()) {
            binding.registerGroupNameFirstRow.setText(binding.registerGroupNameSecondRow.text.toString())
            binding.registerGroupNameSecondRow.setText("")
            binding.registerGroupNameSecondRow.isEnabled = false
            binding.firstRowCharacterLength.text = "" + binding.registerGroupNameFirstRow.text.length + "/" + MAX_ROW_LENGTH
            binding.secondRowCharacterLength.text = "" + binding.registerGroupNameSecondRow.text.length + "/" + MAX_ROW_LENGTH
        } else {
            binding.firstRowCharacterLength.text = "" + binding.registerGroupNameFirstRow.text.length + "/" + MAX_ROW_LENGTH
        }
    }

    private fun enableButtonSaveChanges() {
        if(!binding.settingsSubmit.isEnabled) {
            binding.settingsSubmit.isEnabled = true
            binding.settingsSubmit.background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_button)
        }
    }

    private fun disableSaveButtonOnViewCreated() {
        lifecycleScope.launch(Dispatchers.Main) {
            delay(300)
            binding.settingsSubmit.isEnabled = false
            binding.settingsSubmit.background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_button_gray)
        }
    }

    private fun addChangeInputListeners() {

//        binding.navSettingsNameEdit.addTextChangedListener(object : TextWatcher {
//
//            override fun afterTextChanged(p0: Editable?) {
//            }
//
//            override fun beforeTextChanged(p0: CharSequence?, start: Int, before: Int, count: Int) {
//            }
//
//            override fun onTextChanged(firstRowText: CharSequence?, positionIndexChanged: Int, before: Int, count: Int) {
//                enableButtonSaveChanges()
//            }
//        })
//
//        binding.navSettingsAddressEdit.addTextChangedListener(object : TextWatcher {
//
//            override fun afterTextChanged(p0: Editable?) {
//            }
//
//            override fun beforeTextChanged(p0: CharSequence?, start: Int, before: Int, count: Int) {
//            }
//
//            override fun onTextChanged(firstRowText: CharSequence?, positionIndexChanged: Int, before: Int, count: Int) {
//                enableButtonSaveChanges()
//            }
//        })
//
//        binding.navSettingsPhone.addTextChangedListener(object : TextWatcher {
//
//            override fun afterTextChanged(p0: Editable?) {
//            }
//
//            override fun beforeTextChanged(p0: CharSequence?, start: Int, before: Int, count: Int) {
//            }
//
//            override fun onTextChanged(firstRowText: CharSequence?, positionIndexChanged: Int, before: Int, count: Int) {
//                enableButtonSaveChanges()
//            }
//        })

        binding.settingsReducedMobility.setOnCheckedChangeListener { _, _ ->
            enableButtonSaveChanges()
        }

        binding.settingsPushNotification.setOnCheckedChangeListener { _, _ ->
            enableButtonSaveChanges()
        }

        binding.settingsEmailNotification.setOnCheckedChangeListener { _, _ ->
            enableButtonSaveChanges()
        }
    }

}