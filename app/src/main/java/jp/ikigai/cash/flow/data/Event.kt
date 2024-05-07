package jp.ikigai.cash.flow.data

sealed class Event {
    object InternalError: Event()
    object SaveSuccess : Event()
    object DeleteSuccess : Event()
    object NotEnoughBalance : Event()
    object NameAlreadyTaken: Event()
    object MinimumTwoFieldsRequired : Event()
    object SourceCategoryMethodRequired : Event()
    object CategoryMethodRequired : Event()
    object SourceCategoryRequired : Event()
    object SourceMethodRequired : Event()
    object CategoryRequired : Event()
    object MethodRequired : Event()
    object SourceRequired : Event()
}
