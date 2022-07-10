package egovframework.com.cms.support;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonIgnore;

@Getter
@Setter
@NoArgsConstructor
public class ServerMessage {
    private boolean success;
    private String text;

    @JsonIgnore
    public ServerMessage(boolean success, String text) {
        this.success = success;
        this.text = text;
    }
}
