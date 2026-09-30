package com.swyp.mangro.data.consumer.recipe.di

import com.swyp.mangro.data.consumer.recipe.impl.RecipeRepositoryImpl
import com.swyp.mangro.data.consumer.recipe.repository.RecipeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RecipeModule {
    @Binds
    @Singleton
    abstract fun bindRepository(implementation: RecipeRepositoryImpl): RecipeRepository
}
