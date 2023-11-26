package live.lafi.domain.usecase.local_setting

import live.lafi.domain.repository.LocalSettingRepository

class LoadMaxUseTokenUseCase(private val localSettingRepository: LocalSettingRepository) {
    suspend operator fun invoke() = localSettingRepository.loadMaxUseToken()
}