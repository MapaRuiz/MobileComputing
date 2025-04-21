package com.example.taller2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.taller2.databinding.ItemContactBinding

// Data class para representar un contacto
data class Contact(val fullName: String)

class ContactsAdapter(context: Context, contacts: List<Contact>) :
    ArrayAdapter<Contact>(context, 0, contacts) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: ItemContactBinding = if (convertView == null) {
            ItemContactBinding.inflate(LayoutInflater.from(context), parent, false)
        } else {
            ItemContactBinding.bind(convertView)
        }

        val contact = getItem(position)!!
        // Se asigna el índice (posición + 1) y el nombre completo del contacto
        binding.textViewIndex.text = "${position + 1}."
        binding.textViewFullName.text = contact.fullName

        return binding.root
    }
}