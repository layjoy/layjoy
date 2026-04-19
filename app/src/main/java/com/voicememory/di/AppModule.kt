package com.voicememory.di

import android.content.Context
import androidx.room.Room
import com.voicememory.data.local.VoiceEntryDao
import com.voicememory.data.local.VoiceMemoryDatabase
import com.voicememory.data.repository.VoiceRepositoryImpl
import com.voicememory.domain.audio.AudioRecorder
import com.voicememory.domain.audio.IFlyTekSpeechRecognizer
import com.voicememory.domain.repository.VoiceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideVoiceMemoryDatabase(
        @ApplicationContext context: Context
    ): VoiceMemoryDatabase {
        return Room.databaseBuilder(
            context,
            VoiceMemoryDatabase::class.java,
            "voice_memory_db"
        ).build()
    }
    
    @Provides
    @Singleton
    fun provideVoiceEntryDao(database: VoiceMemoryDatabase): VoiceEntryDao {
        return database.voiceEntryDao()
    }
    
    @Provides
    @Singleton
    fun provideVoiceRepository(dao: VoiceEntryDao): VoiceRepository {
        return VoiceRepositoryImpl(dao)
    }
    
    @Provides
    @Singleton
    fun provideAudioRecorder(
        @ApplicationContext context: Context
    ): AudioRecorder {
        return AudioRecorder(context)
    }
    
    @Provides
    @Singleton
    fun provideSpeechRecognizer(): IFlyTekSpeechRecognizer {
        return IFlyTekSpeechRecognizer()
    }
    
    @Provides
    @Singleton
    fun provideSparkAIService(): SparkAIService {
        return SparkAIService()
    }
}
