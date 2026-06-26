package com.lamdayne.humify.auth.security.oauth2;

import lombok.Builder;
import lombok.Getter;

/**
 * Thông tin user trả về sau khi verify Google OAuth2 code.
 * sub  = Google unique user ID (dùng làm providerId trong user_social_accounts)
 */
@Getter
@Builder
public class GoogleUserInfo {

}
