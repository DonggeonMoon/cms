package egovframework.com.cms.bulkmail.dto;

import com.ibm.icu.text.SimpleDateFormat;
import egovframework.com.cms.support.Constant;
import egovframework.com.cms.support.util.Utils;
import egovframework.com.cms.watchdog.aop.WatchDog;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;
import java.util.*;

@Getter
@Setter
public class BulkMail {
    @WatchDog
    private Integer bmId;
    @WatchDog
    private String bmMemberId;//작업자 아이디
    private String bmBulkMailToRaw;//컬럼 없음
    private List<BulkMailTo> bulkMailToList;
    private String bmFromAddress;
    private String bmFromName;
    @WatchDog
    private String bmSubject;
    private String bmContent;
    private String bmContentType = "html";
    private String bmAttachmentFileName;
    private Long bmAttachmentFileSize;
    private MultipartFile bmAttachmentFile;//디비컬럼 없음
    private boolean bmDeleteAttachment = false;

    private String bmStatus = "ready";//ready|sending|complete

    private String bmSendType = "after_insert";//instant|scheduled|after_insert instant=목록에서 발송버튼 클릭시 전송
    private String bmSendDate;//발송일시
    private String bmScheduledDate;
    private String bmScheduledHour;
    private String bmScheduledMin;

    private Date bmRegDate;//메일정보가 등록된 시각

    private Integer bmSendedCount;
    private Integer bmMailToTotal;

    private boolean bmSendToAll = false;//전체메일 여부
    private Integer bmSendToAllTotal = 0;//전체메일 발송 건수 ( 전체메일 발송인 경우 bmMailToTotal 대신 이걸 사용해서 목록에 표시해준다. )

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bmId == null) ? 0 : bmId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BulkMail other = (BulkMail) obj;
        if (bmId == null) {
            if (other.bmId != null)
                return false;
        } else if (!bmId.equals(other.bmId))
            return false;
        return true;
    }

    public String convertSetToBulkMailToRawText() {
        if (this.getBulkMailToList() == null || this.getBulkMailToList().isEmpty()) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder(20 * this.getBulkMailToList().size());
            for (BulkMailTo bmt : this.getBulkMailToList()) {
                sb.append(bmt.getBmtAddress());
                sb.append("\r\n");
            }
            return sb.toString();
        }
    }

    public List<BulkMailTo> convertBulkMailToRawToList() {
        if (StringUtils.isBlank(this.bmBulkMailToRaw)) {
            return null;
        }
        String[] temps = this.bmBulkMailToRaw.split(Constant.TEXTAREA_ENTER);
        int initialCapacity = temps.length;
        Set<BulkMailTo> set = new HashSet<BulkMailTo>(initialCapacity);

        BulkMailTo bulkMailTo = null;
        for (String email : temps) {
            if (StringUtils.isNotBlank(email) && Utils.isEmail(email)) {
                bulkMailTo = new BulkMailTo();
                bulkMailTo.setBulkMail(this);
                bulkMailTo.setBmtAddress(email);
                bulkMailTo.setBmtService(email.split("@")[1]);
                set.add(bulkMailTo);
            }
        }

        return new ArrayList<BulkMailTo>(set);
    }

    public void validateScheduledDate() {
        if ("scheduled".equals(this.getBmSendType())) {
            Date scheduled = null;
            try {
                scheduled = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(this.getBmScheduledFullDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (scheduled.before(new Date())) {
                this.setBmSendType("instant");
                this.setBmScheduledDate(null);
                this.setBmScheduledHour(null);
                this.setBmScheduledMin(null);
            }
        }
    }

    public String getBmScheduledFullDate() {
        if (StringUtils.isNotBlank(this.getBmScheduledDate()) && StringUtils.isNotBlank(this.getBmScheduledHour()) && StringUtils.isNotBlank(this.getBmScheduledMin())) {
            return this.getBmScheduledDate() + " " + this.getBmScheduledHour() + ":" + this.getBmScheduledMin() + ":00";
        }
        return null;
    }

    public Date getBmScheduledFullDateTypeDate() {
        if (StringUtils.isNotBlank(this.getBmScheduledFullDate())) {
            try {
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(this.getBmScheduledFullDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


}