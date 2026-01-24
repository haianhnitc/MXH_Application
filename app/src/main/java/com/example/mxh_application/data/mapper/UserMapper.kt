package com.example.mxh_application.data.mapper

import com.example.mxh_application.data.local.entity.Address
import com.example.mxh_application.data.local.entity.Company
import com.example.mxh_application.data.local.entity.Hair
import com.example.mxh_application.data.local.entity.UserEntity
import com.example.mxh_application.data.remote.dto.UserResponse

fun UserResponse.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        firstName = firstName,
        lastName = lastName,
        maidenName = maidenName,
        age = age,
        gender = gender,
        email = email,
        phone = phone,
        username = username,
        birthDate = birthDate,
        image = image,
        bloodGroup = bloodGroup,
        height = height,
        weight = weight,
        eyeColor = eyeColor,
        hair = hair?.let { Hair(color = it.color, type = it.type) },
        address = address?.let {
            Address(
                address = it.address,
                city = it.city,
                state = it.state,
                country = it.country,
                postalCode = it.postalCode
            )
        },
        company = company?.let {
            Company(
                name = it.name,
                title = it.title,
                department = it.department
            )
        },
        university = university,
        role = role,
        lastUpdated = System.currentTimeMillis()
    )
}


fun List<UserResponse>.toEntityList(): List<UserEntity> {
    return map { it.toEntity() }
}
