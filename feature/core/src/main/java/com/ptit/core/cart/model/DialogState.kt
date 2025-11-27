import com.ptit.domain.entity.cart.CartItemDomainEntity

// Sealed class để quản lý trạng thái dialog
sealed class DialogState {
    object Hidden : DialogState()
    data class DeleteSingleItem(val item: CartItemDomainEntity) : DialogState()
    data class DeleteMultipleItems(val itemIds: Set<String>) : DialogState()
}