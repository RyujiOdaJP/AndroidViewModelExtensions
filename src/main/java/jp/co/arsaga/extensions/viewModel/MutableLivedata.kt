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

fun saveCacheInputDataList(cacheMap: MutableMap<String, Any?>, propertyList: List<KProperty0<LiveData<out Any>>>) {
    propertyList.forEach {
        it.apply {
            cacheMap[it.name] = get().value
        }
    }
}

inline fun <reified T>restoreCacheTypingDataList(cacheMap: MutableMap<String, Any?>, propertyList: List<KProperty0<MutableLiveData<T>>>) {
    propertyList.forEach {
        val cache = cacheMap[it.name]
        if(cache is T) {
            it.get().postValue(cache)
        }
    }
}