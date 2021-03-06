package io.realad.kile.adapters.local

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec

/**
 * Test case for the FileUtils class.
 *
 * @see FileUtils
 */
class FileUtilsTest : StringSpec({

    "should throw not implemented error when calling listContents" {
        shouldThrow<NotImplementedError> {
            val res = FileUtils().listContents("/")
            res
        }
    }

    "should throw not implemented error when calling fileExists" {
        shouldThrow<NotImplementedError> {
            FileUtils().fileExists("/")
        }
    }

})
