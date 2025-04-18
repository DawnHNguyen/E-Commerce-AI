package com.ptit.data.mapping

import com.ptit.domain.entity.common.BaseErrorResponseDomainEntity
import com.ptit.data.remote.util.BaseErrorResponse

fun BaseErrorResponse.toDomainEntity(): BaseErrorResponseDomainEntity {
    return BaseErrorResponseDomainEntity(
        statusCode = this.statusCode ?: 0,
        message = this.error?.message ?: "",
        error = ""
    )
}