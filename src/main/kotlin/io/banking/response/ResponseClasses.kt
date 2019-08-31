package io.banking.response

data class ResponseContainer(val status: Int, val resObj: Any?)
data class Response(val status: Int, val message: String)

