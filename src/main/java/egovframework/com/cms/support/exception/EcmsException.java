
package egovframework.com.cms.support.exception;

public class EcmsException extends RuntimeException {

    // back : history.back();
    // url : 해당 주소로 이동
    protected String locationHref = null;

    public EcmsException() {
        super();
    }

    public EcmsException(String message, Throwable cause) {
        super(message, cause);
    }

    public EcmsException(String message) {
        super(message);
    }

    public EcmsException(String message, String locationHref) {
        super(message);
        this.locationHref = locationHref;
    }

    public EcmsException(Throwable cause) {
        super(cause);
    }

    public String getLocationHref() {
        return locationHref;
    }

    public void setLocationHref(String locationHref) {
        this.locationHref = locationHref;
    }

}
