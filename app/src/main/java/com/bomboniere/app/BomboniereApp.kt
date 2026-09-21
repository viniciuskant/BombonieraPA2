package com.bomboniere.app

import android.app.Application
import com.bomboniere.app.data.AppDatabase
import com.bomboniere.app.data.BomboniereRepository

class BomboniereApp : Application() {
    val db by lazy { AppDatabase.get(this) }
    val repo by lazy { BomboniereRepository(db) }
}