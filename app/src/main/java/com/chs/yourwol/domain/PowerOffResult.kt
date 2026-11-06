package com.chs.yourwol.domain

sealed interface PowerOffResult {
    data object Success : PowerOffResult
    data object NoCredential : PowerOffResult
    data class HostKeyMismatch(val expected: String, val actual: String) : PowerOffResult
    data class Failure(val cause: Throwable) : PowerOffResult
}
