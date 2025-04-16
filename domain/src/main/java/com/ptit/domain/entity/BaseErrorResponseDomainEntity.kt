package com.ptit.domain.entity

data  class BaseErrorResponseDomainEntity (
    val statusCode: String = "",
    val message: String = "",
    val error: String = ""
)
