package jp.co.arsaga.extensions.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlin.reflect.KProperty0

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

fun <T> MutableLiveData<MutableList<T>>.deleteItem(values: T) {
    val value = this.value ?: mutableListOf()
    value.remove(values)
    this.value = value
}

fun saveCacheTypingDataList(cacheMap: MutableMap<String, String>, propertyList: List<KProperty0<LiveData<String>>>) {
    propertyList.forEach {
        it.apply {
            cacheMap[name] = get().value ?: ""
        }
    }
}

fun restoreCacheTypingDataList(cacheMap: MutableMap<String, String>, propertyList: List<KProperty0<MutableLiveData<String>>>) {
    propertyList.forEach {
        it.apply {
            get().postValue(cacheMap[name] ?: "")
        }
    }
}