package ggv.ayush.narutoog.DI

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import ggv.ayush.narutoog.data.repository.DataStoreOperationImpl
import ggv.ayush.narutoog.domain.repository.DataStoreOperations
import javax.inject.Singleton
import dagger.hilt.components.SingletonComponent
import ggv.ayush.narutoog.data.repository.Repository
import ggv.ayush.narutoog.domain.use_cases.UseCases
import ggv.ayush.narutoog.domain.use_cases.get_all_heroes.GetAllHeroesUseCase
import ggv.ayush.narutoog.domain.use_cases.getselectedhero.GetSelectedHeroUsecase
import ggv.ayush.narutoog.domain.use_cases.readonboarding.ReadOnBoardingUseCase
import ggv.ayush.narutoog.domain.use_cases.save_onboarding.SaveOnBoardingCase
import ggv.ayush.narutoog.domain.use_cases.search_heroes.SearchHeroesUseCase


@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideDataStoreOperations(
        context: Context
    ) : DataStoreOperations {
        return DataStoreOperationImpl(context = context )
    }

    @Provides
    @Singleton
    fun provideUseCases(repository: Repository) : UseCases  {
        return UseCases(
            saveOnBoardingCase = SaveOnBoardingCase(repository),
            readOnBoardingUseCase = ReadOnBoardingUseCase(repository),
            getAllHeroesUseCase = GetAllHeroesUseCase(repository),
            searchHeroesUseCase = SearchHeroesUseCase(repository),
            getSelectedHeroUsecase = GetSelectedHeroUsecase(repository)
        )
    }

        @Provides
        fun provideContext(@ApplicationContext app: Context): Context {
            return app
        }


}