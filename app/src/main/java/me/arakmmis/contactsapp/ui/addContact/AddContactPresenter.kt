package me.arakmmis.contactsapp.ui.addContact

import android.util.Log
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.arakmmis.contactsapp.businesslogic.contacts.ContactsManager
import me.arakmmis.contactsapp.businesslogic.contacts.ContactsRepo
import me.arakmmis.contactsapp.businesslogic.models.Address
import me.arakmmis.contactsapp.businesslogic.models.Contact
import me.arakmmis.contactsapp.businesslogic.models.EmailAddress
import me.arakmmis.contactsapp.businesslogic.models.PhoneNumber
import me.arakmmis.contactsapp.mvpcontracts.AddContactContract
import me.arakmmis.contactsapp.utils.Cache
import me.arakmmis.contactsapp.utils.ListUtil
import me.arakmmis.contactsapp.utils.ValidationUtil

class AddContactPresenter(private val addContactView: AddContactContract.AddContactView) : AddContactContract.AddContactPresenter {

    private val contactsManager: ContactsManager = ContactsRepo()

    override fun addPhoneNumber(phoneNumber: PhoneNumber) {
        val result = ValidationUtil.errorsInPhoneNumber(phoneNumber.number)

        if (result == ValidationUtil.NO_ERRORS) {
            var exists = false
            Cache.getPhoneNumbers().forEach { existingPhoneNumber ->
                if (phoneNumber.number == existingPhoneNumber.number)
                    exists = true
            }

            if (!exists) {
                if (phoneNumber.type == "Default") {
                    Cache.setDefaultTypeUsed(true)
                }

                val newNumbers = Cache.getPhoneNumbers()

                newNumbers.add(phoneNumber)
                Cache.setPhoneNumbers(newNumbers)
                addContactView.updatePhoneList(newNumbers)
            } else {
                addContactView.showPhoneNumberError("Phone Number Already Exists")
            }
        } else {
            addContactView.showPhoneNumberError(result)
        }
    }

    override fun deletePhoneNumber(phoneNumber: PhoneNumber) {
        if (phoneNumber.type == "Default") {
            Cache.setDefaultTypeUsed(false)
        }

        val itemToBeDeleted = Cache.getPhoneNumbers().indexOf(phoneNumber)

        val newNumbers: List<PhoneNumber>

        if (Cache.getPhoneNumbers().size == 1) {
            newNumbers = ArrayList<PhoneNumber>()
        } else {
            val numbersBeforeDeleted = Cache.getPhoneNumbers().slice(IntRange(0, itemToBeDeleted - 1))
            val numbersAfterDeleted = Cache.getPhoneNumbers().slice(IntRange(itemToBeDeleted + 1, Cache.getPhoneNumbers().size - 1))
            newNumbers = numbersBeforeDeleted + numbersAfterDeleted
        }

        Cache.setPhoneNumbers(newNumbers)
        addContactView.updatePhoneList(newNumbers)
    }

    override fun addAddress(address: Address) {
        val result = ValidationUtil.errorsInAddress(address.address)

        if (result == ValidationUtil.NO_ERRORS) {
            var exists = false
            Cache.getAddresses().forEach { existingAddress ->
                if (address.address == existingAddress.address)
                    exists = true
            }

            if (!exists) {
                val newAddress = Cache.getAddresses()

                newAddress.add(address)
                Cache.setAddresses(newAddress)
                addContactView.updateAddressList(newAddress)
            } else {
                addContactView.showAddressError("Address Already Exists")
            }
        } else {
            addContactView.showAddressError(result)
        }
    }

    override fun deleteAddress(address: Address) {
        val itemToBeDeleted = Cache.getAddresses().indexOf(address)

        val newAddresses: List<Address>

        if (Cache.getAddresses().size == 1) {
            newAddresses = ArrayList<Address>()
        } else {
            val addressesBeforeDeleted = Cache.getAddresses().slice(IntRange(0, itemToBeDeleted - 1))
            val addressesAfterDeleted = Cache.getAddresses().slice(IntRange(itemToBeDeleted + 1, Cache.getAddresses().size - 1))
            newAddresses = addressesBeforeDeleted + addressesAfterDeleted
        }

        Cache.setAddresses(newAddresses)
        addContactView.updateAddressList(newAddresses)
    }

    override fun addEmailAddress(email: EmailAddress) {
        val result = ValidationUtil.errorsInEmail(email.emailAddress)

        if (result == ValidationUtil.NO_ERRORS) {
            var exists = false
            Cache.getEmails().forEach { existingEmail ->
                if (email.emailAddress == existingEmail.emailAddress)
                    exists = true
            }

            if (!exists) {
                val newEmails = Cache.getEmails()

                newEmails.add(email)
                Cache.setEmails(newEmails)
                addContactView.updateEmailAddressesList(newEmails)
            } else {
                addContactView.showEmailAddressError("Email Address Already Exists")
            }
        } else {
            addContactView.showEmailAddressError(result)
        }
    }

    override fun deleteEmailAddress(email: EmailAddress) {
        val itemToBeDeleted = Cache.getEmails().indexOf(email)

        val newEmails: List<EmailAddress>

        if (Cache.getEmails().size == 1) {
            newEmails = ArrayList<EmailAddress>()
        } else {
            val emailsBeforeDeleted = Cache.getEmails().slice(IntRange(0, itemToBeDeleted - 1))
            val emailsAfterDeleted = Cache.getEmails().slice(IntRange(itemToBeDeleted + 1, Cache.getEmails().size - 1))
            newEmails = emailsBeforeDeleted + emailsAfterDeleted
        }

        Cache.setEmails(newEmails)
        addContactView.updateEmailAddressesList(newEmails)
    }

    override fun addContact(profilePic: ByteArray, contactName: String, contactBirthDate: String) {
        if (validateInput(contactName, contactBirthDate)) {
            val contact = Contact(name = contactName,
                    profilePic = profilePic,
                    dateOfBirth = contactBirthDate,
                    phoneNumbers = ListUtil<PhoneNumber>().listToRealmList(Cache.getPhoneNumbers()),
                    defaultPhoneNumber = Cache.getPhoneNumbers().filter { phoneNumber -> phoneNumber.type == "Default" }[0].number,
                    addresses = ListUtil<Address>().listToRealmList(Cache.getAddresses()),
                    emailAddresses = ListUtil<EmailAddress>().listToRealmList(Cache.getEmails()))

            contactsManager.insertContact(contact)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : SingleObserver<Contact> {
                        override fun onError(e: Throwable) {
                            Log.e("ACP: addContact", e.message)
                        }

                        override fun onSubscribe(d: Disposable) {
                        }

                        override fun onSuccess(contact: Contact) {
                            Log.d("ACP: addContact", "Contact: " + contact.name)
                            addContactView.navigateToContactDetails(contact)
                        }
                    })
        }
    }

    private fun validateInput(name: String, birthDate: String): Boolean {
        val results = ValidationUtil.validateAddContactInput(name, birthDate, Cache.getPhoneNumbers(), Cache.getEmails())

        var proceed = true

        if (results[ValidationUtil.NAME_KEY] != ValidationUtil.NO_ERRORS) {
            addContactView.showNameError(errorMessage = results[ValidationUtil.NAME_KEY] as String)
            proceed = false
        } else
            addContactView.disableFieldError(field = ValidationUtil.NAME_KEY)

        if (results[ValidationUtil.DATE_KEY] != ValidationUtil.NO_ERRORS) {
            addContactView.showDateError(errorMessage = results[ValidationUtil.DATE_KEY] as String)
            proceed = false
        } else
            addContactView.disableFieldError(field = ValidationUtil.DATE_KEY)

        if (results[ValidationUtil.PHONE_NUMBERS_KEY] != ValidationUtil.NO_ERRORS) {
            addContactView.showPhoneNumbersListError(errorMessage = results[ValidationUtil.PHONE_NUMBERS_KEY] as String)
        } else
            addContactView.disableFieldError(field = ValidationUtil.PHONE_NUMBERS_KEY)

        if (results[ValidationUtil.EMAILS_KEY] != ValidationUtil.NO_ERRORS) {
            addContactView.showEmailsListError(errorMessage = results[ValidationUtil.EMAILS_KEY] as String)
        } else
            addContactView.disableFieldError(field = ValidationUtil.EMAILS_KEY)

        return proceed
    }
}