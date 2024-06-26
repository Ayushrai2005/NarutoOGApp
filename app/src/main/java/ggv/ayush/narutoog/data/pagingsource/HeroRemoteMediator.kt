package ggv.ayush.narutoog.data.pagingsource

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ggv.ayush.narutoog.data.local.BorutoDatabase
import ggv.ayush.narutoog.data.remote.BorutoApi
import ggv.ayush.narutoog.domain.model.Hero
import ggv.ayush.narutoog.domain.model.HeroRemoteKeys
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class HeroRemoteMediator @Inject constructor(
    private val borutoApi: BorutoApi,
    private val borutoDatabase: BorutoDatabase
) : RemoteMediator<Int, Hero>() {
    private val heroDao = borutoDatabase.heroDao()
    private val heroRemoteKeysDao = borutoDatabase.heroRemoteKeysDao()
    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, Hero>
    ): HeroRemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { heroId ->
                heroRemoteKeysDao.getRemoteKeys(heroId)
            }
        }
    }

    override suspend fun initialize(): InitializeAction {
        val currentTime = System.currentTimeMillis()
        val lastUpdated = heroRemoteKeysDao.getRemoteKeys(heroId = 1)?.lastUpdated ?: 0
        val cacheTimeout = 5
        Log.d("HeroRemoteMediator", "Current time: ${parseMillis(currentTime)}")
        Log.d("HeroRemoteMediator", "Last updated: ${parseMillis(lastUpdated)}")
        val diffInMinutes = (currentTime - lastUpdated) / 1000 / 60
        return if (diffInMinutes.toInt() > cacheTimeout) {
            Log.d("HeroRemoteMediator", "Refresh")
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            Log.d("HeroRemoteMediator", "UpToDate")
            InitializeAction.SKIP_INITIAL_REFRESH
        }


    }

    private suspend fun getRemoteKeyForFirstItem(
        state: PagingState<Int, Hero>
    ): HeroRemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { hero ->
            heroRemoteKeysDao.getRemoteKeys(hero.id)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, Hero>): HeroRemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { hero ->
            heroRemoteKeysDao.getRemoteKeys(hero.id)
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, Hero>
    ): RemoteMediator.MediatorResult {
        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> {
                    val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                    remoteKeys?.nextPage?.minus(1) ?: 1
                }

                LoadType.PREPEND -> {
                    val remoteKeys = getRemoteKeyForFirstItem(state)
                    val prevPage = remoteKeys?.prevPage
                        ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                    prevPage
                }

                LoadType.APPEND -> {
                    val remoteKeys = getRemoteKeyForLastItem(state)
                    val nextPage = remoteKeys?.nextPage
                        ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                    nextPage
                }
            }
            val response = borutoApi.getAllHeroes(page = page )
            Log.d("HeroRemoteMediator", "API response: $response")
            if (response.heroes.isNotEmpty()) {
                borutoDatabase.withTransaction {
                    if (loadType == LoadType.REFRESH) {
                        heroDao.deleteAllHeroes()
                        heroRemoteKeysDao.deleteAllRemoteKey()
                    }
                    val prevPage = response.prevPage
                    val nextPage = response.nextPage
                    val keys = response.heroes.map { hero ->
                        HeroRemoteKeys(
                            id = hero.id,
                            prevPage = prevPage,
                            nextPage = nextPage,
                            lastUpdated = response.lastUpdated
                        )
                    }
                    heroRemoteKeysDao.addAllRemoteKey(heroRemoteKeys = keys)
                    heroDao.addHero(response.heroes)


                    val result = heroDao.addHero(response.heroes)
                    Log.d("HeroRemoteMediator", "Database operation result: $result")

                }
            }
            MediatorResult.Success(endOfPaginationReached = response.nextPage == null)
        } catch (e: Exception) {
            Log.e("HeroRemoteMediator", "Error loading data", e)
            return MediatorResult.Error(e)
        }
    }

    //debuging function
    private fun parseMillis(millis: Long): String {
        val date = Date(millis)
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ROOT)
        return format.format(date )
    }


}


