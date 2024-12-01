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
) : UseCase<List<DistrictModel>, UseCase.None>() {

    /**
     * Executes the use case to fetch the district list.
     *
     * @param params Parameters for the use case. In this case, none are required.
     * @return A list of `DistrictModel` objects if successful, or `null` in case of an error.
     */
    override suspend fun run(params: None): List<DistrictModel>? {
        return try {
            // Log the initiation of the use case
            MsLogger.d("GetDistrictListUseCase", "Fetching district list from the repository.")

            // Fetch the district list from the repository
            val districtList = appRepository.getDistrictList()

            // Log success and return the result
            MsLogger.d("GetDistrictListUseCase", "Successfully fetched ${districtList?.size} districts.")
            districtList
        } catch (e: Exception) {
            // Log the exception
            MsLogger.e("GetDistrictListUseCase", "Error while fetching district list: ${e.localizedMessage}")

            // Return null in case of an error
            null
        }
    }
}