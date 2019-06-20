package net.dreamlu.mica.social.request;

import com.fasterxml.jackson.databind.JsonNode;
import net.dreamlu.http.HttpRequest;
import net.dreamlu.mica.social.config.AuthConfig;
import net.dreamlu.mica.social.config.AuthSource;
import net.dreamlu.mica.social.exception.AuthException;
import net.dreamlu.mica.social.model.AuthToken;
import net.dreamlu.mica.social.model.AuthUser;
import net.dreamlu.mica.social.utils.GlobalAuthUtil;

import java.util.Map;

/**
 * Github登录
 *
 * @author L.cm
 */
public class AuthGithubRequest extends BaseAuthRequest {

	public AuthGithubRequest(AuthConfig config) {
		super(config, AuthSource.GITHUB);
	}

	@Override
	protected AuthToken getAccessToken(String code) {
		String result = doPostAuthorizationCode(code).asString();
		Map<String, String> res = GlobalAuthUtil.parseStringToMap(result);
		if (res.containsKey("error")) {
			throw new AuthException(res.get("error") + ":" + res.get("error_description"));
		}
		return AuthToken.builder()
			.accessToken(res.get("access_token"))
			.build();
	}

	@Override
	protected AuthUser getUserInfo(AuthToken authToken) {
		String accessToken = authToken.getAccessToken();
		JsonNode object = HttpRequest.get(authSource.userInfo())
			.query("access_token", accessToken)
			.execute()
			.asJsonNode();
		return AuthUser.builder()
			.uuid(object.get("id").asText())
			.username(object.get("login").asText())
			.avatar(object.get("avatar_url").asText())
			.blog(object.get("blog").asText())
			.nickname(object.get("name").asText())
			.company(object.get("company").asText())
			.location(object.get("location").asText())
			.email(object.get("email").asText())
			.remark(object.get("bio").asText())
			.token(authToken)
			.source(AuthSource.GITHUB)
			.build();
	}
}