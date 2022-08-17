package egovframework.com.cms.comment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//sns서비스업체 응답 문자열 json데이터의 모든 프로퍼티를 다쓰진 않으므로 여기 없는건 무시하라는 어노테이션 - 없으면 에러남
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CommentProfile {
    @JsonProperty
    private String id;
    @JsonProperty
    private String name;
    @JsonProperty
    private String link; //facebook snsHomeUrl
    private String profileImage;
    private String serviceName;
    private String twitterOauthToken;//twitter
    private String twitterOauthTokenSecret;//twitter
    private String facebookAccessToken;//facebook
    private String googlePlusAccessToken;

    @Override
    public String toString() {
        return String.format("CommentProfile [\nid=%s\n, name=%s\n, link=%s\n, profileImage=%s\n, serviceName=%s\n, twitterOauthToken=%s\n, twitterOauthTokenSecret=%s\n, facebookAccessToken=%s\n]",
                id, name, link, profileImage, serviceName, twitterOauthToken, twitterOauthTokenSecret, facebookAccessToken);
    }
}
