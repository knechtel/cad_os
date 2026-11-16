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
) {
    val isLocked: Boolean
        get() = devices.any { it.isDelivered || it.deliveredAt != null }
}

fun isCustomerLocked(customerWithDevices: CustomerWithDevices?): Boolean {
    return customerWithDevices?.isLocked == true
}

fun isCustomerLocked(devices: List<Device>): Boolean {
    return devices.any { it.isDelivered || it.deliveredAt != null }
}
