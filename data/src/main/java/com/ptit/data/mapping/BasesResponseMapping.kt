package com.ptit.data.mapping

import com.ptit.domain.entity.BaseErrorResponseDomainEntity
import com.ptit.data.remote.util.BaseErrorResponse

fun BaseErrorResponse.toDomainEntity(): BaseErrorResponseDomainEntity {
    return BaseErrorResponseDomainEntity(
        statusCode = this.statusCode ?: "",
        message = this.message ?: "",
        error = this.error ?: ""
    )
}