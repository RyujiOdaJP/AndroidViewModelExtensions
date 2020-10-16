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

class DiffResultLiveData<T, R> private constructor(
    coroutineScope: CoroutineScope,
    source: LiveData<List<T>?>,
    refreshResultFactory: (oldList: List<T>?, newList: List<T>?, diffResult: DiffUtil.DiffResult) -> R?,
    diffUtilCallbackFactory: suspend (oldList: List<T>?, newList: List<T>?) -> DiffUtil.Callback,
    calculateThread: CoroutineDispatcher
) : MediatorLiveData<R>() {
    private var cacheData: List<T>? = source.value

    init {
        addSource(source) {
            coroutineScope.launch(Dispatchers.Default) {
                cacheData?.let { cache ->
                    diffUtilCallbackFactory(cache, it)
                        .let { withContext(calculateThread) { DiffUtil.calculateDiff(it) } }
                        .run { refreshResultFactory(cache, it, this) }
                        .run { postValue(this) }
                }
                cacheData = it
            }
        }
    }

    companion object {
        fun <T> create(
            coroutineScope: CoroutineScope,
            source: LiveData<List<T>?>,
            calculateThread: CoroutineDispatcher = Dispatchers.Default,
            diffUtilCallbackFactory: suspend (oldList: List<T>?, newList: List<T>?) -> DiffUtil.Callback,
        ): DiffResultLiveData<T, DiffUtil.DiffResult> = DiffResultLiveData(
            coroutineScope,
            source,
            { _, _, diffResult -> diffResult },
            diffUtilCallbackFactory,
            calculateThread
        )

        fun <T> create(
            coroutineScope: CoroutineScope,
            source: LiveData<List<T>?>,
            refreshResultFactory: (oldList: List<T>?, newList: List<T>?, diffResult: DiffUtil.DiffResult) -> DiffRefreshEvent?,
            calculateThread: CoroutineDispatcher = Dispatchers.Default,
            diffUtilCallbackFactory: suspend (oldList: List<T>?, newList: List<T>?) -> DiffUtil.Callback
        ): DiffResultLiveData<T, DiffRefreshEvent> = DiffResultLiveData(
            coroutineScope,
            source,
            refreshResultFactory,
            diffUtilCallbackFactory,
            calculateThread
        )
    }
}

data class DiffRefreshEvent(
    val diffResult: DiffUtil.DiffResult,
    val scrollPosition: Int
) {
    companion object {
        const val NON_SCROLL = -1
    }
}

abstract class DefaultListDiffCallback<T>(
    private val oldList: List<T>?,
    private val newList: List<T>?
) : AbstractDiffUtilCallback<T>(oldList, newList) {
    abstract fun isItemSame(oldItem: T?, newItem: T?): Boolean
    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean =
        isItemSame(oldList?.getOrNull(oldItemPosition), newList?.getOrNull(newItemPosition))

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean = oldList?.getOrNull(oldItemPosition) == newList?.getOrNull(newItemPosition)
}

abstract class AbstractDiffUtilCallback<T>(
    private val oldList: Collection<T>?,
    private val newList: Collection<T>?
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList?.size ?: 0
    override fun getNewListSize(): Int = newList?.size ?: 0
}