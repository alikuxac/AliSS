package com.alikuxac.aliss.domain.model

data class S3Config(
    val endpoint: String = "",
    val bucket: String = "",
    val accessKey: String = "",
    val secretKey: String = "",
    val isServiceEnabled: Boolean = false
)
