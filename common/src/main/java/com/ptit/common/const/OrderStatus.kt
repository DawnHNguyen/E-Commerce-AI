package com.ptit.common.const

object OrderStatus {
    const val ALL = "ALL"
    const val PENDING_PAYMENT = "PENDING_PAYMENT"  // ✅ Chờ thanh toán
    const val PENDING_PACKAGING = "PENDING_PACKAGING"  // ✅ Chờ đóng gói
    const val SHIPPING = "SHIPPING"  // ✅ Đang vận chuyển
    const val DELIVERED = "DELIVERED"  // ✅ Đã giao
    const val RETURNED = "RETURNED"  // ✅ Trả hàng


    fun getDisplayName(status: String): String {
        // Normalize status to uppercase for comparison
        val normalizedStatus = status.uppercase()
        return when (normalizedStatus) {
            ALL -> "Tất cả"
            PENDING_PAYMENT -> "Chờ thanh toán"
            PENDING_PACKAGING, "PENDING_PACKAGE" -> "Chờ vận chuyển"
            SHIPPING, "PENDING_DELIVERY", "PICKUPED" -> "Đang giao hàng"
            DELIVERED -> "Đã giao"
            RETURNED -> "Trả hàng"
            // ✅ Fallback for old status names
            "PENDING", "PROCESSING" -> "Chờ xử lý"
            else -> status
        }
    }

    fun getStatusColor(status: String): String {
        // Normalize status to uppercase for comparison
        val normalizedStatus = status.uppercase()
        return when (normalizedStatus) {
            PENDING_PAYMENT -> "#FFA500"       // Cam - Chờ thanh toán
            PENDING_PACKAGING, "PENDING_PACKAGE" -> "#FFD700"    // Vàng - Chờ vận chuyển
            SHIPPING, "PENDING_DELIVERY", "PICKUPED" -> "#9C27B0"      // Tím - Đang giao hàng
            DELIVERED -> "#4CAF50"     // Xanh lá - Đã giao
            // ✅ Fallback for old status names
            "PENDING", "PROCESSING" -> "#FFA500"
            else -> "#757575"          // Xám - Mặc định
        }
    }

    fun getAllStatuses(): List<String> {
        return listOf(
            ALL,
            PENDING_PAYMENT,      // ✅ Tab "Chờ thanh toán"
            PENDING_PACKAGING,    // ✅ Tab "Chờ vận chuyển"
            DELIVERED,            // ✅ Tab "Đã giao"
        )
    }
}

