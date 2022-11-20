package roman.bannikov.aston_5_fragments

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    val contactList = MutableLiveData<List<ContactModel>>()
    val contactInfo = MutableLiveData<ContactModel>()
}