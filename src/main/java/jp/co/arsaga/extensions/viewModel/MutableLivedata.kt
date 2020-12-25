package jp.co.arsaga.extensions.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

fun MutableLiveData<String>.removeHyphen() {
    val value = this.value
    this.value = value?.replace("-", "")
}

fun MutableLiveData<String>.removeWhiteSpace() {
    val value = this.value
    this.value = value?.replace("\\s".toRegex(), "")
}

suspend fun <X, Y> LiveData<X>.valueMainThread(query: (X?) -> Y?): Y? =
    withContext(Dispatchers.Main) { query(value) }

inline fun <reified T : Any> switchInputDataList(
    propertyList: List<KProperty0<MutableLiveData<T>>>
): (MutableMap<String, Any?>, MutableMap<String, Any?>) -> Unit =
    { saveCache: MutableMap<String, Any?>, restoreCache: MutableMap<String, Any?> ->
        switchInputDataList(saveCache, restoreCache, propertyList)
    }

inline fun <reified T : Any> switchInputDataList(
    saveCache: MutableMap<String, Any?>,
    restoreCache: MutableMap<String, Any?>,
    propertyList: List<KProperty0<MutableLiveData<T>>>
) {
    saveCacheInputDataList(saveCache, propertyList)
    restoreCacheInputDataList(restoreCache, propertyList)
}

fun saveCacheInputDataList(
    cacheMap: MutableMap<String, Any?>,
    propertyList: List<KProperty0<LiveData<out Any>>>
) {
    propertyList.forEach {
        it.apply {
            cacheMap[it.name] = get().value
        }
    }
}

inline fun <reified T> restoreCacheInputDataList(
    cacheMap: MutableMap<String, Any?>,
    propertyList: List<KProperty0<MutableLiveData<T>>>
) {
    propertyList.forEach {
        val cache = cacheMap[it.name]
        if (cache is T) {
            it.get().postValue(cache)
        }
    }
}