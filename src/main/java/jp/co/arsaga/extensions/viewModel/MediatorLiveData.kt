package jp.co.arsaga.extensions.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import kotlinx.coroutines.*

fun <T> MediatorLiveData<T>.setObservableList(
    observableList: List<LiveData<out Any?>>,
    coroutineScope: CoroutineScope,
    hasNullable: Boolean = false,
    convertLogic: suspend (Any?) -> T
): MediatorLiveData<T> = this.apply {
    Observer<Any?> {
        if (!hasNullable && (it == null || observableList.any { it.value == null })) return@Observer
        coroutineScope.launch(Dispatchers.Default) {
            postValue(convertLogic(it))
        }
    }.run {
        observableList.forEach { addSource(it, this) }
    }
}

fun <X, Y> LiveData<X>.disposableMap(
    disposeExpression: (X) -> Boolean = { true },
    convertLogic: (X) -> Y
): MediatorLiveData<Y> = MediatorLiveData<Y>().also { result ->
    result.addSource(this) {
        result.postValue(convertLogic(it))
        if (disposeExpression(it)) result.removeSource(this)
    }
}

class DiffResultLiveData<T>(
    coroutineScope: CoroutineScope,
    source: LiveData<Collection<T>?>,
    diffUtilCallbackFactory: suspend (oldList: Collection<T>?, newList: Collection<T>?) -> DiffUtil.Callback
) : MediatorLiveData<DiffUtil.DiffResult>() {
    private var cacheData: Collection<T>? = null
    init {
        addSource(source) {
            coroutineScope.launch(Dispatchers.Default) {
                diffUtilCallbackFactory(cacheData, it)
                    .run { DiffUtil.calculateDiff(this) }
                    .run { postValue(this) }
                cacheData = it
            }
        }
    }
}