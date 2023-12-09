package live.lafi.util.public_model

/**
 * OkHttp3 에 ChatGpt 토큰을 전달하기 위한 싱글톤 클래스.
 * 1. App이 최초 실행 될 때 토큰을 DataStore에서 가져와 set
 * 2. 유저 인터렉션으로 변경 되었을 때 set 해준다.
 */
object GptTokenManager {
    private var token = ""

    fun getApiToken() = token

    fun editToken(token: String) {
        GptTokenManager.token = token
    }
}