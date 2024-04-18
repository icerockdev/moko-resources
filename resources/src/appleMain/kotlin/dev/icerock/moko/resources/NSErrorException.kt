/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import platform.Foundation.NSError

/**
 * Wraps an NSError in a Throwable so that Kotlin methods can be annotated
 * with `@Throws(NSErrorException::class)` to be picked up by Swift, without
 * losing the nested `NSError` details.
 *
 * To improve the experience in Swift, create this extensions:
 *
 *     extension Error {
 *         var wrappedNSError: NSError? {
 *             let kotlinException = (self as NSError).userInfo["KotlinException"] as? KotlinException
 *             let nsErrorException = kotlinException as? NSErrorException
 *             return nsErrorException?.nsError as? NSError
 *         }
 *     }
 *
 * Example usage:
 *
 *     do {
 *         try kotlinMethodThatThrowsNSErrorException()
 *     } catch {
 *         if let wrappedError = error.wrappedNSError {
 *             print("wrappedError domain: \(wrappedError.domain)")
 *             print("wrappedError code: \(wrappedError.code)")
 *             print("wrappedError userInfo: \(wrappedError.userInfo)")
 *         }
 *     }
 */
@Suppress("MemberVisibilityCanBePrivate", "CanBeParameter")
class NSErrorException(val nsError: NSError) : Exception(nsError.localizedDescription)
