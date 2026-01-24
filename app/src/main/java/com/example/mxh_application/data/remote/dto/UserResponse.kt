package com.example.mxh_application.data.remote.dto

import com.google.gson.annotations.SerializedName
data class UserResponse(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("firstName")
    val firstName: String,
    
    @SerializedName("lastName")
    val lastName: String,
    
    @SerializedName("maidenName")
    val maidenName: String? = null,
    
    @SerializedName("age")
    val age: Int,
    
    @SerializedName("gender")
    val gender: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("phone")
    val phone: String,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("password")
    val password: String? = null,
    
    @SerializedName("birthDate")
    val birthDate: String? = null,
    
    @SerializedName("image")
    val image: String,
    
    @SerializedName("bloodGroup")
    val bloodGroup: String? = null,
    
    @SerializedName("height")
    val height: Double? = null,
    
    @SerializedName("weight")
    val weight: Double? = null,
    
    @SerializedName("eyeColor")
    val eyeColor: String? = null,
    
    @SerializedName("hair")
    val hair: Hair? = null,
    
    @SerializedName("address")
    val address: Address? = null,
    
    @SerializedName("macAddress")
    val macAddress: String? = null,
    
    @SerializedName("university")
    val university: String? = null,
    
    @SerializedName("bank")
    val bank: Bank? = null,
    
    @SerializedName("company")
    val company: Company? = null,
    
    @SerializedName("ein")
    val ein: String? = null,
    
    @SerializedName("ssn")
    val ssn: String? = null,
    
    @SerializedName("userAgent")
    val userAgent: String? = null,
    
    @SerializedName("crypto")
    val crypto: Crypto? = null,
    
    @SerializedName("role")
    val role: String? = null
)

data class Hair(
    @SerializedName("color")
    val color: String? = null,
    
    @SerializedName("type")
    val type: String? = null
)

data class Address(
    @SerializedName("address")
    val address: String? = null,
    
    @SerializedName("city")
    val city: String? = null,
    
    @SerializedName("state")
    val state: String? = null,
    
    @SerializedName("stateCode")
    val stateCode: String? = null,
    
    @SerializedName("postalCode")
    val postalCode: String? = null,
    
    @SerializedName("coordinates")
    val coordinates: Coordinates? = null,
    
    @SerializedName("country")
    val country: String? = null
)

data class Coordinates(
    @SerializedName("lat")
    val lat: Double,
    
    @SerializedName("lng")
    val lng: Double
)

data class Bank(
    @SerializedName("cardExpire")
    val cardExpire: String? = null,
    
    @SerializedName("cardNumber")
    val cardNumber: String? = null,
    
    @SerializedName("cardType")
    val cardType: String? = null,
    
    @SerializedName("currency")
    val currency: String? = null,
    
    @SerializedName("iban")
    val iban: String? = null
)

data class Company(
    @SerializedName("department")
    val department: String? = null,
    
    @SerializedName("name")
    val name: String? = null,
    
    @SerializedName("title")
    val title: String? = null,
    
    @SerializedName("address")
    val address: Address? = null
)

data class Crypto(
    @SerializedName("coin")
    val coin: String? = null,
    
    @SerializedName("wallet")
    val wallet: String? = null,
    
    @SerializedName("network")
    val network: String? = null
)
