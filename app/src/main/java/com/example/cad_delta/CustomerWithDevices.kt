package com.example.cad_delta

import androidx.room.Embedded
import androidx.room.Relation

data class CustomerWithDevices(
    @Embedded val customer: Customer,
    @Relation(
        parentColumn = "id",
        entityColumn = "customerId"
    )
    val devices: List<Device>
)
