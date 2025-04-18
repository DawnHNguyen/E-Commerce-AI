package com.ptit.domain.entity.common

data  class BaseErrorResponseDomainEntity (
    val statusCode: Int = 0,
    val message: String = "",
    val error: String = ""
)