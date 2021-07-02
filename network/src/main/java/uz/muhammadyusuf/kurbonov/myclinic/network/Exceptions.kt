package uz.muhammadyusuf.kurbonov.myclinic.network

/**
 * Error while communicationg with API
 *
 */
class APIException(val statusCode: Int, val apiMessage: String) : Exception()

/**
 *
 * No internet connection
 *
 */
class NotConnectedException : Exception()

/**
 *
 * Auth request exception
 *
 */
class AuthRequestException : Exception()

/**
 *
 * Customer not found
 *
 */
class CustomerNotFoundException : Exception()