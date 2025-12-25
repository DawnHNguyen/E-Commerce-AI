package com.ptit.domain.repository

import com.ptit.domain.entity.chat.ChatDeliveryAddress
import com.ptit.domain.entity.chat.ChatMessage
import com.ptit.domain.entity.chat.ChatSession
import com.ptit.domain.entity.shipping.DistrictEntity
import com.ptit.domain.entity.shipping.ProvinceEntity
import com.ptit.domain.entity.shipping.WardEntity
import com.ptit.domain.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    // Session management
    fun getAllSessions(): Flow<List<ChatSession>>
    suspend fun createSession(title: String = "Cuộc trò chuyện mới"): Resource<ChatSession>
    suspend fun deleteSession(sessionId: String): Resource<Unit>

    // Message operations
    suspend fun sendMessage(sessionId: String, content: String): Flow<Resource<Unit>>
    fun getChatHistory(sessionId: String): Flow<List<ChatMessage>>
    suspend fun clearChatHistory(sessionId: String): Resource<Unit>

    // Checkout address - structured flow matching CreateOrderScreen
    suspend fun getProvinces(): Resource<List<ProvinceEntity>>
    suspend fun getDistricts(provinceId: Int): Resource<List<DistrictEntity>>
    suspend fun getWards(districtId: Int): Resource<List<WardEntity>>

    fun updateDeliveryAddress(address: ChatDeliveryAddress)
    fun getDeliveryAddress(): ChatDeliveryAddress?
    fun getUserNameAndPhone(): Pair<String, String>?

    // Calculate shipping fee for current address and cart
    suspend fun calculateShippingFee(): Int
}