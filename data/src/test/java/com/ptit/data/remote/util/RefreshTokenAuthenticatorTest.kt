package com.ptit.data.remote.util

import com.ptit.common.const.SecureStorageKey
import com.ptit.data.remote.api.NoAuthInterceptApi
import com.ptit.data.remote.dto.auth.LoginResponse
import com.ptit.data.remote.dto.auth.RefreshTokenRequest
import com.ptit.domain.utils.Resource
import com.tencent.mmkv.MMKV
import io.mockk.*
import kotlinx.coroutines.test.runTest
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit test cho RefreshTokenAuthenticator
 *
 * Test cases:
 * 1. ✅ Refresh token thành công - trả về request mới với token mới
 * 2. ✅ Refresh token null - trả về null (không retry)
 * 3. ✅ Refresh token API thất bại - clear tokens và trả về null
 * 4. ✅ Access token mới empty - clear tokens và trả về null
 */
class RefreshTokenAuthenticatorTest {

    private lateinit var authenticator: RefreshTokenAuthenticator
    private lateinit var remoteService: NoAuthInterceptApi
    private lateinit var mmkv: MMKV

    @Before
    fun setup() {
        // Mock MMKV
        mockkStatic(MMKV::class)
        mmkv = mockk(relaxed = true)
        every { MMKV.defaultMMKV() } returns mmkv

        // Mock remote service
        remoteService = mockk()

        // Create authenticator
        authenticator = RefreshTokenAuthenticator(remoteService)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `authenticate - refresh token null - return null`() = runTest {
        // Given
        every { mmkv.decodeString(SecureStorageKey.REFRESH_TOKEN) } returns null
        val response = mockk<Response>(relaxed = true)

        // When
        val result = authenticator.authenticate(null, response)

        // Then
        assertNull(result)
        verify(exactly = 0) { remoteService.refreshAccessToken(any()) }
    }

    @Test
    fun `authenticate - refresh token success - return new request with new token`() = runTest {
        // Given
        val oldRefreshToken = "old_refresh_token"
        val newAccessToken = "new_access_token"
        val newRefreshToken = "new_refresh_token"

        every { mmkv.decodeString(SecureStorageKey.REFRESH_TOKEN) } returns oldRefreshToken

        val loginResponse = LoginResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )

        coEvery {
            remoteService.refreshAccessToken(RefreshTokenRequest(oldRefreshToken))
        } returns Resource.Success(loginResponse)

        val originalRequest = mockk<Request>(relaxed = true) {
            every { newBuilder() } returns mockk(relaxed = true) {
                every { header(any(), any()) } returns this
                every { build() } returns mockk()
            }
        }

        val response = mockk<Response>(relaxed = true) {
            every { request } returns originalRequest
        }

        // When
        val result = authenticator.authenticate(null, response)

        // Then
        assertNotNull(result)
        verify { mmkv.encode(SecureStorageKey.ACCESS_TOKEN, newAccessToken) }
        verify { mmkv.encode(SecureStorageKey.REFRESH_TOKEN, newRefreshToken) }
    }

    @Test
    fun `authenticate - refresh token failed - clear tokens and return null`() = runTest {
        // Given
        val oldRefreshToken = "expired_refresh_token"

        every { mmkv.decodeString(SecureStorageKey.REFRESH_TOKEN) } returns oldRefreshToken

        coEvery {
            remoteService.refreshAccessToken(RefreshTokenRequest(oldRefreshToken))
        } returns Resource.Error(Exception("Refresh token expired"))

        val response = mockk<Response>(relaxed = true)

        // When
        val result = authenticator.authenticate(null, response)

        // Then
        assertNull(result)
        verify {
            mmkv.removeValuesForKeys(
                arrayOf(SecureStorageKey.ACCESS_TOKEN, SecureStorageKey.REFRESH_TOKEN)
            )
        }
    }

    @Test
    fun `authenticate - new access token empty - clear tokens and return null`() = runTest {
        // Given
        val oldRefreshToken = "old_refresh_token"

        every { mmkv.decodeString(SecureStorageKey.REFRESH_TOKEN) } returns oldRefreshToken

        val loginResponse = LoginResponse(
            accessToken = "", // Empty access token
            refreshToken = "new_refresh_token"
        )

        coEvery {
            remoteService.refreshAccessToken(RefreshTokenRequest(oldRefreshToken))
        } returns Resource.Success(loginResponse)

        val response = mockk<Response>(relaxed = true)

        // When
        val result = authenticator.authenticate(null, response)

        // Then
        assertNull(result)
        verify {
            mmkv.removeValuesForKeys(
                arrayOf(SecureStorageKey.ACCESS_TOKEN, SecureStorageKey.REFRESH_TOKEN)
            )
        }
    }
}

