package com.example.landmarkremark.interfaces

interface IOnCompleteListener {
    fun onSuccess()
    fun onError(err: Exception?)
}