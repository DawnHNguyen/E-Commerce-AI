package com.ptit.data.remote.dto.address

import com.google.gson.annotations.SerializedName

data class AddressDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String?, // Tên gợi nhớ (VD: Nhà riêng, Văn phòng...)

    @SerializedName("recipient")
    val recipient: String?, // Tên người nhận (Optional)

    @SerializedName("phoneNumber")
    val phoneNumber: String?, // Số điện thoại người nhận (Optional)

    @SerializedName("province")
    val province: String?,

    @SerializedName("district")
    val district: String?,

    @SerializedName("ward")
    val ward: String?,

    @SerializedName("street")
    val street: String?,

    // 👇 Các trường ID tích hợp GHN (quan trọng cho tính phí ship)
    @SerializedName("provinceId")
    val provinceId: Int?,

    @SerializedName("districtId")
    val districtId: Int?,

    @SerializedName("wardCode")
    val wardCode: String?,

    @SerializedName("addressType")
    val addressType: String?, // HOME, OFFICE...

    @SerializedName("isDefault")
    val isDefault: Boolean?, // Backend dùng .extend nên có thể null ở một số context khác

    @SerializedName("createdAt")
    val createdAt: String?,

    @SerializedName("updatedAt")
    val updatedAt: String?
)