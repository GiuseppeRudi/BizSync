package com.bizsync.domain.usecases


import com.bizsync.domain.model.User
import com.bizsync.domain.repository.UserLocalRepository
import javax.inject.Inject

class GetColleghiUseCase @Inject constructor(
    private val userLocalRepository: UserLocalRepository
) {
    suspend operator fun invoke(): List<User> {
        return userLocalRepository.getDipendenti()
    }
}
