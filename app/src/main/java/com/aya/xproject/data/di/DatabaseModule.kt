package com.aya.xproject.data.di

import android.content.Context
import androidx.room.Room
import com.aya.xproject.data.local.FavoriteDao
import com.aya.xproject.data.local.XProjectDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): XProjectDatabase =
        Room.databaseBuilder(
            context,
            XProjectDatabase::class.java,
            "xproject.db"
        ).build()

    @Provides
    fun provideFavoriteDao(database: XProjectDatabase): FavoriteDao =
        database.favoriteDao()
}
