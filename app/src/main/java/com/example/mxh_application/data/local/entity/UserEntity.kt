package com.example.mxh_application.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: Int,
    
    val firstName: String,
    val lastName: String,
    val maidenName: String? = null,
    val age: Int,
    val gender: String,
    val email: String,
    val phone: String,
    val username: String,
    val birthDate: String? = null,
    val image: String,
    val bloodGroup: String? = null,
    val height: Double? = null,
    val weight: Double? = null,
    val eyeColor: String? = null,

    @Embedded(prefix = "hair_")
    val hair: Hair?,
    
    @Embedded(prefix = "address_")
    val address: Address?,

    @Embedded(prefix = "company_")
    val company: Company?,
    
    val university: String? = null,
    val role: String? = null,
    
    val postCount: Int = 0, 
    val lastUpdated: Long = System.currentTimeMillis()
)

data class Hair(
    val color: String? = null,
    val type: String? = null
)

data class Address(
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
    val postalCode: String? = null
)

data class Company(
    val name: String? = null,
    val title: String? = null,
    val department: String? = null
)