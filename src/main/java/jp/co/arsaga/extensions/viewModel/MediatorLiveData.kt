package jp.co.arsaga.extensions.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import kotlinx.coroutines.*

fun <T> MediatorLiveData<T>.setObservableList(
    observableList: List<LiveData<out Any?>>,
    coroutineScope: CoroutineScope,
    convertLogic: suspend (Any?) -> T
): MediatorLiveData<T> = this.apply {
    Observer<Any?> {
        coroutineScope.launch(Dispatchers.Default) {
            postValue(convertLogic(it))
        }
    }.run {
        observableList.forEach { addSource(it, this) }
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