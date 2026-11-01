package com.example.cad_delta

import android.app.Application

class CadApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val repository: AppRepository by lazy { AppRepository(database.customerDao(), database.deviceDao()) }
}
