package com.company.secureapp

object BuildConfigHelper {
    fun getMattermostServerUrl(): String {
        return if (BuildConfig.MATTERMOST_SERVER_URL.isNotEmpty()) {
            BuildConfig.MATTERMOST_SERVER_URL
        } else {
            // Fallback для разработки
            "https://your-mattermost-server.com"
        }
    }

    fun getMattermostChannelId(): String {
        return BuildConfig.MATTERMOST_CHANNEL_ID.ifEmpty { "your_channel_id" }
    }

    fun getMattermostLogin(): String {
        return BuildConfig.MATTERMOST_LOGIN.ifEmpty { "your_login" }
    }

    fun getMattermostToken(): String {
        return BuildConfig.MATTERMOST_TOKEN.ifEmpty { "your_token" }
    }

    fun getServerEndpointUrl(): String {
        return BuildConfig.SERVER_ENDPOINT_URL.ifEmpty { "https://your-server.com/alert" }
    }

    fun getDefaultSmsNumber(): String {
        return BuildConfig.DEFAULT_SMS_NUMBER.ifEmpty { "+1234567890" }
    }
}
