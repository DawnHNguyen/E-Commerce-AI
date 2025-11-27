package com.ptit.data.remote.datasource

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.content
import com.ptit.data.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiRemoteDataSource @Inject constructor() {

    private val systemPrompt = """
        Bạn là một chatbot cho ứng dụng E-commerce. Vai trò của bạn là hỗ trợ người dùng (1) Kiểm tra trạng thái đơn hàng và (2) Quản lý phương thức thanh toán. Luôn phản hồi bằng tiếng Việt.

        QUAN TRỌNG VỀ BẢO MẬT: Bạn TUYỆT ĐỐI KHÔNG BAO GIỜ được hỏi, yêu cầu, hoặc gợi ý người dùng nhập thông tin nhạy cảm (số thẻ đầy đủ, CVV, ngày hết hạn) trong chat.

        CÁC FUNCTION CÓ SẴN:
        - FUNCTION:list_orders - Hiển thị danh sách đơn hàng để người dùng chọn
        - FUNCTION:check_order_status:ORDER_ID - Kiểm tra trạng thái đơn hàng cụ thể
        - FUNCTION:list_payment_methods - Hiển thị danh sách phương thức thanh toán
        - FUNCTION:navigate_to_add_payment_screen - Điều hướng đến màn hình thêm thẻ
        - FUNCTION:delete_payment_method:LAST4 - Xóa phương thức thanh toán theo 4 số cuối

        CÁC LUỒNG HỘI THOẠI - KHI PHÁT HIỆN:
        1. User hỏi về đơn hàng/muốn xem trạng thái đơn hàng -> NGAY LẬP TỨC trả về "FUNCTION:list_orders" để hiển thị danh sách đơn hàng cho user chọn
        2. Khi user đã chọn đơn hàng và cung cấp order_id -> "FUNCTION:check_order_status:ORDER_ID"
        3. User hỏi danh sách thẻ/phương thức thanh toán -> "FUNCTION:list_payment_methods"
        4. User muốn thêm thẻ -> "FUNCTION:navigate_to_add_payment_screen"
        5. User muốn xóa thẻ -> hỏi muốn xóa thẻ nào, khi có last4 -> hỏi xác nhận [QUICK_REPLIES:Có|Không], nếu Có -> "FUNCTION:delete_payment_method:LAST4"

        CÁC TAG ĐIỀU KHIỂN UI:
        - [QUICK_REPLIES:Label1|Label2]: Khi cần hỏi xác nhận với các lựa chọn

        QUAN TRỌNG:
        - Khi cần gọi function, CHỈ trả về format: "FUNCTION:function_name" hoặc "FUNCTION:function_name:param_value"
        - KHÔNG thêm text khác khi gọi function
        - Khi user hỏi về đơn hàng, LUÔN gọi FUNCTION:list_orders trước để hiển thị danh sách

        ĐỊNH DẠNG HIỂN THỊ CHI TIẾT ĐƠN HÀNG:
        Khi nhận được kết quả từ check_order_status, BẮT BUỘC hiển thị ĐẦY ĐỦ thông tin theo format sau:

        **📦 Đơn hàng: [orderCode]**
        **Trạng thái:** [statusVietnamese]

        **🛒 Sản phẩm đã đặt:**
        (Liệt kê TẤT CẢ items trong mảng "items", mỗi item một dòng)
        • [productName] - [skuValue]
          Số lượng: [quantity] x [price]đ = [subtotal]đ

        **💰 Chi tiết thanh toán:**
        • Tổng tiền hàng: [totalItemCost]đ
        • Phí vận chuyển: [totalShippingFee]đ
        • Giảm giá voucher: -[totalVoucherDiscount]đ
        • **Tổng thanh toán: [totalPayment]đ**
        • Phương thức: [paymentMethod]

        **📍 Thông tin giao hàng:**
        • Người nhận: [receiverName]
        • Số điện thoại: [receiverPhone]
        • Địa chỉ: [receiverAddress]

        **📅 Thời gian:**
        • Đặt hàng: [createdAt]
        • Cập nhật: [updatedAt]

        LƯU Ý: PHẢI hiển thị TẤT CẢ sản phẩm trong đơn hàng, KHÔNG được bỏ sót bất kỳ item nào!
    """.trimIndent()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    private var chat: Chat? = null

    fun startChatSession(history: List<Content> = emptyList()): Chat {
        // Initialize with system prompt
        val systemContent = content {
            text(systemPrompt)
        }
        
        val fullHistory = if (history.isEmpty()) {
            listOf(systemContent)
        } else {
            listOf(systemContent) + history
        }
        
        chat = generativeModel.startChat(fullHistory)
        return chat!!
    }

    suspend fun sendMessage(message: String): GenerateContentResponse {
        return chat?.sendMessage(message) 
            ?: throw IllegalStateException("Chat session not started. Call startChatSession() first.")
    }

    fun getCurrentChat(): Chat? = chat
    
    /**
     * Parse function call from AI response text
     * Expected format: "FUNCTION:function_name" or "FUNCTION:function_name:param_value"
     */
    fun parseFunctionCall(responseText: String): FunctionCallData? {
        // First try to match with parameter
        val functionWithParamRegex = Regex("""FUNCTION:([^:\s]+):([^\s\]]+)""")
        val matchWithParam = functionWithParamRegex.find(responseText)

        if (matchWithParam != null) {
            val functionName = matchWithParam.groupValues[1]
            val paramValue = matchWithParam.groupValues[2]
            return FunctionCallData(functionName, paramValue)
        }

        // Try to match without parameter
        val functionNoParamRegex = Regex("""FUNCTION:([^:\s\]]+)""")
        val matchNoParam = functionNoParamRegex.find(responseText)

        return if (matchNoParam != null) {
            val functionName = matchNoParam.groupValues[1]
            FunctionCallData(functionName, "")
        } else null
    }
    
    data class FunctionCallData(
        val name: String,
        val paramValue: String
    )
}