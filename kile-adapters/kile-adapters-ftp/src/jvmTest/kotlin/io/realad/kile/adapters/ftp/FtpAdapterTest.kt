package io.realad.kile.adapters.ftp

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.realad.kile.error.FilesystemError
import io.realad.kile.fp.Either
import io.realad.kile.fp.left
import io.realad.kile.fp.right

class FtpAdapterTest : StringSpec({

    lateinit var options: FtpOptions
    lateinit var provider: FtpProvider
    lateinit var connection: FtpConnection
    lateinit var adapter: FtpAdapter

    beforeTest {
        options = mockk()
        provider = mockk()
        connection = mockk()
        adapter = FtpAdapter(options, provider)
    }

    "should return a list of the directory contents returned from connection without reconnection" {
        val location = "/root/test"
        val listDirectoryResult = listOf("one", "two", "three")
        every { provider.getConnection(any()) } returns connection.right()
        every { connection.mlistDir(any()) } returns listDirectoryResult
        val response = adapter.listContents(location)
        response.isLeft() shouldBe false
        response.isRight() shouldBe true
        (response as Either.Right).r shouldNotBe null
        response.r shouldBe listDirectoryResult
        response.r shouldContainExactly listDirectoryResult
        verify(exactly = 1) { provider.getConnection(any()) }
        verify(exactly = 1) { connection.mlistDir(any()) }
        confirmVerified(connection, provider)
    }

    "should return a list of the directory contents returned from connection with a single reconnection" {
        val location = "/root/test"
        val listDirectoryResult = listOf("one", "two", "three")
        every { provider.getConnection(any()) } returns FilesystemError("hello error").left() andThen connection.right()
        every { connection.mlistDir(any()) } returns listDirectoryResult
        val response = adapter.listContents(location)
        response.isLeft() shouldBe false
        response.isRight() shouldBe true
        (response as Either.Right).r shouldNotBe null
        response.r shouldBe listDirectoryResult
        response.r shouldContainExactly listDirectoryResult
        verify(exactly = 2) { provider.getConnection(any()) }
        verify(exactly = 1) { connection.mlistDir(any()) }
        confirmVerified(connection, provider)
    }

    "should return a list of the directory contents returned from connection with a triple reconnection" {
        val location = "/root/test"
        val listDirectoryResult = listOf("one", "two", "three")
        every { provider.getConnection(any()) } returns FilesystemError("hello error").left() andThen FilesystemError("hello error").left() andThen connection.right()
        every { connection.mlistDir(any()) } returns listDirectoryResult
        val response = adapter.listContents(location)
        response.isLeft() shouldBe false
        response.isRight() shouldBe true
        (response as Either.Right).r shouldNotBe null
        response.r shouldBe listDirectoryResult
        response.r shouldContainExactly listDirectoryResult
        verify(exactly = 3) { provider.getConnection(any()) }
        verify(exactly = 1) { connection.mlistDir(any()) }
        confirmVerified(connection, provider)
    }

    "should return an error after more than three reconnections" {
        val location = "/root/test"
        val listDirectoryResult = listOf("one", "two", "three")
        every { provider.getConnection(any()) } returns FilesystemError("hello error").left() andThen FilesystemError("hello error").left() andThen FilesystemError("hello error").left()
        every { connection.mlistDir(any()) } returns listDirectoryResult
        val response = adapter.listContents(location)
        response.isLeft() shouldBe true
        response.isRight() shouldBe false
        (response as Either.Left).l shouldNotBe null
        response.l.getPrevious() shouldNotBe null
        response.l.getPrevious()?.getPrevious() shouldNotBe null
        response.l.getPrevious()?.getPrevious()?.getPrevious() shouldBe null
        verify(exactly = 3) { provider.getConnection(any()) }
        verify(exactly = 0) { connection.mlistDir(any()) }
        confirmVerified(connection, provider)
    }

})
