package me.arakmmis.contactsapp.ui.edit.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.edit_contact_rv_item_address.view.*
import kotlinx.android.synthetic.main.edit_contact_rv_item_email_address.view.*
import kotlinx.android.synthetic.main.edit_contact_rv_item_phone_number.view.*
import me.arakmmis.contactsapp.businesslogic.models.Address
import me.arakmmis.contactsapp.businesslogic.models.EmailAddress
import me.arakmmis.contactsapp.businesslogic.models.PhoneNumber
import me.arakmmis.contactsapp.customlisteners.EditContactCallback

class DetailsViewHolder<T>(itemView: View?, private val callback: EditContactCallback<T>) : RecyclerView.ViewHolder(itemView) {

    var t: T? = null

    fun setData(t: T) {
        this.t = t

        when (t) {
            is PhoneNumber -> initPNView(t)
            is EmailAddress -> initEAView(t)
            is Address -> initAddressView(t)
            else -> Toast.makeText(itemView?.context, "Error with T", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initPNView(phoneNumber: PhoneNumber) {
        itemView?.tv_phone_number?.text = phoneNumber.number
        itemView?.tv_phone_number_type?.text = phoneNumber.type

        itemView?.iv_delete_pn?.setOnClickListener {
            callback.onDeleteClicked(t!!)
        }

        itemView?.iv_edit_pn?.setOnClickListener {
            callback.onEditClicked(t!!, adapterPosition)
        }
    }

    private fun initEAView(emailAddress: EmailAddress) {
        itemView?.tv_email_address?.text = emailAddress.emailAddress
        itemView?.tv_email_address_type?.text = emailAddress.type

        itemView?.iv_delete_ea?.setOnClickListener { _ ->
            callback.onDeleteClicked(t!!)
        }

        itemView?.iv_edit_ea?.setOnClickListener {
            callback.onEditClicked(t!!, adapterPosition)
        }
    }

    private fun initAddressView(address: Address) {
        itemView?.tv_address?.text = address.address
        itemView?.tv_address_type?.text = address.type

        itemView?.iv_delete_a?.setOnClickListener { _ ->
            callback.onDeleteClicked(t!!)
        }

        itemView?.iv_edit_a?.setOnClickListener {
            callback.onEditClicked(t!!, adapterPosition)
        }
    }
}