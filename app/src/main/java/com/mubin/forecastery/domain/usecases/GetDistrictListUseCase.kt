package com.mubin.forecastery.domain.usecases

import com.mubin.forecastery.base.utils.MsLogger
import com.mubin.forecastery.base.utils.UseCase
import com.mubin.forecastery.data.model.DistrictModel
import com.mubin.forecastery.domain.repo.AppRepository
import javax.inject.Inject

/**
 * Use case for fetching the list of districts.
 *
 * This use case interacts with the repository layer to retrieve district data.
 * It encapsulates the business logic required to fetch the data while handling exceptions gracefully.
 *
 * @property appRepository The repository responsible for fetching district data.
 */
class GetDistrictListUseCase @Inject constructor(
    private val appRepository: AppRepository
) : UseCase<Result<List<DistrictModel>>, UseCase.None>() {

    /**
     * Executes the use case to fetch the district list.
     *
     * @param params Parameters for the use case. In this case, none are required.
     * @return A list of `DistrictModel` objects if successful, or `null` in case of an error.
     */
    override suspend fun run(params: None): Result<List<DistrictModel>> {
        return try {
            // Log the initiation of the use case
            MsLogger.d("GetDistrictListUseCase", "Fetching district list from the repository.")
            // Fetch the district list from the repository
            appRepository.getDistrictList().also {
                // Log the result
                MsLogger.d("GetDistrictListUseCase", "Fetched ${it.getOrNull()?.size} districts.")
            }
        } catch (e: Exception) {
            // Log the exception
            MsLogger.e("GetDistrictListUseCase", "Error while fetching district list: ${e.localizedMessage}")
            // Return null in case of an error
            Result.failure(e)
        }
    }
}