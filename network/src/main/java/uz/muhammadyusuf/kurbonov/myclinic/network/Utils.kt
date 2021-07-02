package uz.muhammadyusuf.kurbonov.myclinic.network

import retrofit2.HttpException
import java.io.IOException
import java.rmi.UnexpectedException

fun mapToDomainExceptions(e: Exception): Exception {
    return when (e) {
        is HttpException -> {
            if (e.code() == 401)
                AuthRequestException()
            else
                APIException(e.code(), e.message())
        }
        is CustomerNotFoundException -> CustomerNotFoundException()
        is IOException -> {
            if (e.cause?.cause is APIException)
            // this is for HTTP 400 error. Because interceptor
            // cancels with IOException only
                e.cause?.cause as APIException
            else
                NotConnectedException()
        }
        else -> UnexpectedException("request exception", e)
    }
}