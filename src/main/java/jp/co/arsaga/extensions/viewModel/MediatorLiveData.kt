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

typealias ListDiffResultLiveData<T> = DiffResultLiveData<T, List<T>>
typealias SetDiffResultLiveData<T> = DiffResultLiveData<T, Set<T>>

class DiffResultLiveData<T, C : Collection<T>>(
    coroutineScope: CoroutineScope,
    source: LiveData<C?>,
    diffUtilCallbackFactory: suspend (oldList: C?, newList: C?) -> DiffUtil.Callback
) : MediatorLiveData<DiffUtil.DiffResult>() {
    private var cacheData: C? = null
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

abstract class DefaultListDiffCallback(
    private val oldList: List<Any>?,
    private val newList: List<Any>?
) : AbstractDiffUtilCallback(oldList, newList) {
    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean = oldList?.getOrNull(oldItemPosition) == newList?.getOrNull(newItemPosition)
}
abstract class AbstractDiffUtilCallback(
    private val oldList: Collection<Any>?,
    private val newList: Collection<Any>?
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList?.size ?: 0
    override fun getNewListSize(): Int = newList?.size ?: 0
}