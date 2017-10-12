package me.arakmmis.contactsapp.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.home_activity.*
import me.arakmmis.contactsapp.R
import me.arakmmis.contactsapp.businesslogic.models.Contact
import me.arakmmis.contactsapp.mvpcontracts.HomeContract
import me.arakmmis.contactsapp.ui.add.AddContactActivity
import me.arakmmis.contactsapp.ui.details.ContactDetailsActivity
import me.arakmmis.contactsapp.ui.home.adapter.ContactsAdapter
import me.arakmmis.contactsapp.utils.Cache
import me.arakmmis.contactsapp.utils.Callback

/**
 * Created by arakm on 10/4/2017.
 */
class HomeActivity : AppCompatActivity(), HomeContract.HomeView, Callback<Contact> {

    private lateinit var presenter: HomeContract.HomePresenter
    private var adapterContacts: ContactsAdapter? = null

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, HomeActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)

        presenter = HomePresenter(this)

        rv_contacts.layoutManager = LinearLayoutManager(this@HomeActivity, LinearLayoutManager.VERTICAL, false)
    }

    override fun setContacts(contacts: List<Contact>) {
        if (contacts.isEmpty()) {
            Toast.makeText(this@HomeActivity, getString(R.string.no_contacts_to_display), Toast.LENGTH_SHORT).show()
        } else {
            if (adapterContacts == null) {
                adapterContacts = ContactsAdapter(this, contacts)
                rv_contacts.adapter = adapterContacts
            } else {
                adapterContacts?.setData(contacts)
            }
        }
    }

    override fun onClick(item: Contact) {
        ContactDetailsActivity.start(this@HomeActivity, item.id)
    }

    fun addContact(v: View) {
        AddContactActivity.start(this@HomeActivity)
    }

    override fun onStart() {
        super.onStart()
        Cache.removeTempUser()
    }
}
