package me.arakmmis.contactsapp.businesslogic.contacts

import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.realm.Case
import io.realm.Realm
import io.realm.Sort
import me.arakmmis.contactsapp.R
import me.arakmmis.contactsapp.businesslogic.models.Contact
import me.arakmmis.contactsapp.utils.App
import java.util.*

class ContactsRepo : ContactsManager {

    override fun getContacts(): Single<List<Contact>> = Single.create { received: SingleEmitter<List<Contact>> ->
        getRealmInstance().executeTransaction { realm ->
            val realmContacts = realm.where(Contact::class.java)
                    .findAll()
                    .sort("name", Sort.ASCENDING)

            val copiedFromRealm = realm.copyFromRealm(realmContacts)
            val contacts: ArrayList<Contact> = ArrayList<Contact>()
            copiedFromRealm.forEach { contact -> contacts.add(contact) }

            received.onSuccess(contacts)
        }

        getRealmInstance().close()
    }

    override fun insertContact(contact: Contact): Single<Contact> = Single.create { received: SingleEmitter<Contact> ->
        getRealmInstance().executeTransaction { realm ->
            realm.insert(contact)
        }

        received.onSuccess(contact)
        getRealmInstance().close()
    }

    override fun getContact(contactId: Int): Single<Contact> = Single.create { received: SingleEmitter<Contact> ->
        getRealmInstance().executeTransaction { realm ->
            val realmContact = realm.where(Contact::class.java)
                    .equalTo("id", contactId)
                    .findFirst()

            received.onSuccess(realm.copyFromRealm(realmContact))
        }

        getRealmInstance().close()
    }

    override fun deleteContact(contactId: Int): Single<String> = Single.create { received: SingleEmitter<String> ->
        getRealmInstance().executeTransaction { realm ->
            val realmContact = realm.where(Contact::class.java)
                    .equalTo("id", contactId)
                    .findFirst()

            realmContact.deleteFromRealm()

            received.onSuccess(App.instance?.resources!!.getString(R.string.success_message))
        }

        getRealmInstance().close()
    }

    override fun updateContact(contact: Contact): Single<Contact> = Single.create { received: SingleEmitter<Contact> ->
        getRealmInstance().executeTransaction { realm ->
            realm.copyToRealmOrUpdate(contact)
            received.onSuccess(realm.copyFromRealm(realm.where(Contact::class.java).equalTo("id", contact.id).findFirst()))
        }

        getRealmInstance().close()
    }

    override fun lookForContacts(query: String): Single<List<Contact>> = Single.create { received: SingleEmitter<List<Contact>> ->
        getRealmInstance().executeTransaction { realm ->
            val realmContacts = realm.where(Contact::class.java)
                    .contains("name", query, Case.INSENSITIVE)
                    .or()
                    .contains("phoneNumbers.number", query, Case.INSENSITIVE)
                    .or()
                    .contains("addresses.address", query, Case.INSENSITIVE)
                    .or()
                    .contains("emailAddresses.emailAddress", query, Case.INSENSITIVE)
                    .or()
                    .contains("dateOfBirth", query, Case.INSENSITIVE)
                    .findAll()
                    .sort("name", Sort.ASCENDING)

            val copiedFromRealm = realm.copyFromRealm(realmContacts)
            val contacts: ArrayList<Contact> = ArrayList<Contact>()
            copiedFromRealm.forEach { contact -> contacts.add(contact) }

            received.onSuccess(contacts)
        }

        getRealmInstance().close()
    }

    private fun getRealmInstance(): Realm = Realm.getDefaultInstance()
}