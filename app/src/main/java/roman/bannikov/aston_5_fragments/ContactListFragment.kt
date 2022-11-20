package roman.bannikov.aston_5_fragments

import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import roman.bannikov.aston_5_fragments.databinding.FragmentContactListBinding
import roman.bannikov.aston_5_fragments.databinding.ListItemBinding


class ContactListFragment : Fragment() {

    private var _binding: FragmentContactListBinding? = null
    private val binding: FragmentContactListBinding get() = _binding!!

    private var cursor: Cursor? = null
    private var isDualPanel: View? = null
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isDualPanel = activity?.findViewById(R.id.details)
        if (isDualPanel != null) {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.details, DetailsFragment.newInstance()).commit()
        }
        viewModel.contactList.value = listOf()
        viewModel.contactList.observe(viewLifecycleOwner) { list ->
            binding.container.removeAllViews()
            if (list.isEmpty()) return@observe
            if (isDualPanel != null) viewModel.contactInfo.value = list[0]
            addItem(list[0]).setOnClickListener {
                viewModel.contactInfo.value = list[0]
                openDetailFragment()
            }
            addItem(list[1]).setOnClickListener {
                viewModel.contactInfo.value = list[1]
                openDetailFragment()
            }
            addItem(list[2]).setOnClickListener {
                viewModel.contactInfo.value = list[2]
                openDetailFragment()
            }
            addItem(list[3]).setOnClickListener {
                viewModel.contactInfo.value = list[3]
                openDetailFragment()
            }
        }
        Thread {
            getContacts()
        }.start()
    }

    private fun openDetailFragment() {
        if (isDualPanel == null) {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.list, DetailsFragment.newInstance())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun addItem(item: ContactModel): View {
        val lItem = layoutInflater.inflate(R.layout.list_item, binding.container, false)
        val b = ListItemBinding.bind(lItem)
        b.tvName.text = item.name
        b.tvNumber.text = item.number
        binding.container.addView(lItem)
        return lItem
    }

    private fun getContacts() {
        val list = ArrayList<ContactModel>()
        cursor = requireActivity().contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )
        if (cursor == null) return
        if (cursor!!.count > 0) {
            while (cursor!!.moveToNext()) {
                val hasPhoneNumber =
                    cursor!!.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                if (hasPhoneNumber > 0) {
                    val nameIndex = cursor!!.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    val numberIndex =
                        cursor!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val idIndex = cursor!!.getColumnIndex(ContactsContract.Contacts._ID)
                    if (idIndex == -1 || nameIndex == -1) return
                    val contactId = cursor!!.getString(idIndex)
                    val name = cursor!!.getString(nameIndex)
                    val number = cursor!!.getString(numberIndex)
                    val contactsModel = ContactModel(
                        name,
                        number,
                        contactId
                    )
                    list.add(contactsModel)
                }
            }
            cursor?.close()
        }
        activity?.runOnUiThread {
            viewModel.contactList.value = list
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = ContactListFragment()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}