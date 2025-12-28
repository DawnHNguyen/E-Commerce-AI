package com.ptit.domain.entity.address

data class AddressDomainEntity(
    val id: String,
    val label: String, // Mapping từ 'name' (VD: Nhà riêng)
    val recipientName: String, // Tên người nhận
    val phoneNumber: String,
    val fullAddress: String, // Chuỗi địa chỉ đã ghép
    val isDefault: Boolean,

    // Giữ lại các ID để dùng khi cần Edit lại địa chỉ
    val provinceId: Int,
    val districtId: Int,
    val wardCode: String
)