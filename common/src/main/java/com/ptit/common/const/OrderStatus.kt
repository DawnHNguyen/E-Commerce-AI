package com.ptit.common.const

object OrderStatus {
    const val ALL = "ALL"
    const val PENDING = "PENDING"
    const val PROCESSING = "PROCESSING"
    const val SHIPPING = "SHIPPING"
    const val DELIVERED = "DELIVERED"
    const val RETURNED = "RETURNED"
    const val CANCELLED = "CANCELLED"

    fun getDisplayName(status: String): String {
        // Normalize status to uppercase for comparison
        val normalizedStatus = status.uppercase()
        return when (normalizedStatus) {
            ALL -> "Tất cả"
            PENDING, "PENDING_PAYMENT" -> "Chờ thanh toán"
            PROCESSING, "PENDING_PACKAGE", "PENDING_PACKAGING" -> "Chờ vận chuyển"
            SHIPPING, "PENDING_DELIVERY", "PICKUPED" -> "Đang giao hàng"
            DELIVERED -> "Đã giao"
            RETURNED -> "Trả hàng"
            CANCELLED -> "Đã hủy"
            else -> status
        }
    }

    fun getStatusColor(status: String): String {
        // Normalize status to uppercase for comparison
        val normalizedStatus = status.uppercase()
        return when (normalizedStatus) {
            PENDING, "PENDING_PAYMENT" -> "#FFA500"       // Cam - Chờ thanh toán
            PROCESSING, "PENDING_PACKAGE", "PENDING_PACKAGING" -> "#FFD700"    // Vàng - Chờ vận chuyển
            SHIPPING, "PENDING_DELIVERY", "PICKUPED" -> "#9C27B0"      // Tím - Đang giao hàng
            DELIVERED -> "#4CAF50"     // Xanh lá - Đã giao
            RETURNED -> "#FF9800"      // Amber - Trả hàng
            CANCELLED -> "#F44336"     // Đỏ - Đã hủy
            else -> "#757575"          // Xám - Mặc định
        }
    }

    fun getAllStatuses(): List<String> {
        return listOf(ALL, PENDING, PROCESSING, SHIPPING, DELIVERED, RETURNED, CANCELLED)
    }
}

