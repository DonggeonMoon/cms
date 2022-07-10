package egovframework.com.cms.config.model;

public class ConfigOption {
    private String confId;//confId + optKey = unique
    private String optKey;//confId + optKey = unique
    private String optValue;
    private String optName;
    private String optHelp;
    private String optType = "text";//옵션유형 text,radio,checkbox,textarea
    private boolean optHidden = false;
    private String optUnitText;

    public String getConfId() {
        return confId;
    }

    public void setConfId(String confId) {
        this.confId = confId;
    }

    public String getOptKey() {
        return optKey;
    }

    public void setOptKey(String optKey) {
        this.optKey = optKey;
    }

    public String getOptValue() {
        return optValue;
    }

    public void setOptValue(String optValue) {
        this.optValue = optValue;
    }

    public String getOptName() {
        return optName;
    }

    public void setOptName(String optName) {
        this.optName = optName;
    }

    public String getOptHelp() {
        return optHelp;
    }

    public void setOptHelp(String optHelp) {
        this.optHelp = optHelp;
    }

    public String getOptType() {
        return optType;
    }

    public void setOptType(String optType) {
        this.optType = optType;
    }

    public boolean isOptHidden() {
        return optHidden;
    }

    public void setOptHidden(boolean optHidden) {
        this.optHidden = optHidden;
    }

    public String getOptUnitText() {
        return optUnitText;
    }

    public void setOptUnitText(String optUnitText) {
        this.optUnitText = optUnitText;
    }
}
