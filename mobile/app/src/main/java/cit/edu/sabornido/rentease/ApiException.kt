package cit.edu.sabornido.rentease

class ApiException(message: String, val httpCode: Int = -1) : Exception(message)
