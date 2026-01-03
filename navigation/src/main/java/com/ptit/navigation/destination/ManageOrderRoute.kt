import kotlinx.serialization.Serializable

// Route cho danh sách đơn hàng của Shop
@Serializable
object ManageOrderListRoute

// Route cho chi tiết đơn hàng của Shop
@Serializable
data class ManageOrderDetailRoute(val orderId: String)