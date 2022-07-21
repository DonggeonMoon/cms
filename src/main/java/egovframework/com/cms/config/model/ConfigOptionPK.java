package egovframework.com.cms.config.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ConfigOptionPK implements Serializable {
    private String confId;
    private String optKey;
}
