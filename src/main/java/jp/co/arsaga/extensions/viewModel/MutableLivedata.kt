package jp.co.arsaga.extensions.viewModel

import androidx.lifecycle.MutableLiveData

fun <T> MutableLiveData<MutableList<T>>.addItem(values: T) {
    val value = this.value ?: mutableListOf()
    value.add(values)
    this.value = value
}

fun <T> MutableLiveData<MutableList<T>>.deleteItem(position: Int) {
    val value = this.value ?: mutableListOf()
    value.removeAt(position)
    this.value = value
}
