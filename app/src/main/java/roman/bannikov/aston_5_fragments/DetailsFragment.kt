package roman.bannikov.aston_5_fragments

import android.content.ContentProviderOperation
import android.content.ContentProviderResult
import android.content.Context
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import roman.bannikov.aston_5_fragments.databinding.FragmentDetailsBinding


class DetailsFragment : Fragment() {
    private var contact: ContactModel? = null
    private lateinit var binding: FragmentDetailsBinding
    private val model: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSave.setOnClickListener {
            if (contact == null) return@setOnClickListener
            updateNameAndNumber(
                requireContext(),
                contact!!.name,
                contact!!.number,
                binding.tvName.text.toString(),
                binding.tvNumber.text.toString(),
                binding.tvLastName.text.toString()
            )
            val d = requireActivity().findViewById<View>(R.id.details)
            if (d != null) {
                model.contactList.value = listOf()
                getContacts()
            } else {
                activity?.supportFragmentManager?.popBackStack()
            }
        }
        model.contactInfo.observe(viewLifecycleOwner) {
            val nameSplit = it.name.split(" ")
            val lastName = if (nameSplit.size > 1) {
                nameSplit[1]
            } else {
                ""
            }
            contact = it
            binding.tvName.setText(it.name.split(" ")[0])
            binding.tvLastName.setText(lastName)
            binding.tvNumber.setText(it.number)
        }
    }

    private val dataC = arrayOf(
        ContactsContract.Data.MIMETYPE,
        ContactsContract.Data.DATA1,
        ContactsContract.Data.DISPLAY_NAME
    )

    private fun updateNameAndNumber(
        context: Context?,
        contactId: String,
        number: String?,
        newName: String?,
        newNumber: String?,
        lastName: String?,
    ): Boolean {
        var newNumber = newNumber
        if (context == null || number == null || number.trim { it <= ' ' }.isEmpty()) return false
        if (newNumber != null && newNumber.trim { it <= ' ' }.isEmpty()) newNumber = null
        if (newNumber == null) return false

        //selection for name
        var where = String.format(
            "%s = '%s' AND %s = ?",
            dataC[0],  //mimetype
            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
            dataC[2] /*contactId*/
        )
        val args = arrayOf(contactId)
        val operations: ArrayList<ContentProviderOperation> = ArrayList()
        operations.add(
            ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(where, args)
                .withValue(
                    ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                    newName?.split(" ")?.get(0)
                ).withValue(
                    ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
                    lastName
                )
                .build()
        )

        //change selection for number
        where = String.format(
            "%s = '%s' AND %s = ?",
            dataC[0],  //mimetype
            Phone.CONTENT_ITEM_TYPE,
            dataC[1] /*number*/
        )

        //change args for number
        args[0] = number
        operations.add(
            ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(where, args)
                .withValue(dataC[1], newNumber)
                .build()
        )
        try {
            val results: Array<ContentProviderResult> =
                context.contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
            for (result in results) {
                Log.d("Update Result", result.toString())
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private fun getContacts() {
        val list = ArrayList<ContactModel>()
        val cursor = requireActivity().contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )
        if (cursor == null) return
        if (cursor.count > 0) {
            while (cursor.moveToNext()) {
                val hasPhoneNumber =
                    cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                if (hasPhoneNumber > 0) {
                    val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    val numberIndex =
                        cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                    if (idIndex == -1 || nameIndex == -1) return
                    val contactId = cursor.getString(idIndex)
                    val name = cursor.getString(nameIndex)
                    val number = cursor.getString(numberIndex)
                    val contactsModel = ContactModel(
                        name,
                        number,
                        contactId
                    )
                    list.add(contactsModel)
                }
            }
            cursor.close()
        }
        requireActivity().runOnUiThread {
            model.contactList.value = list
        }
    }


    companion object {
        @JvmStatic
        fun newInstance() = DetailsFragment()
    }
}