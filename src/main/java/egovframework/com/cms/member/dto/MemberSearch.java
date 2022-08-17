package egovframework.com.cms.member.dto;

import egovframework.com.cms.support.pagination.PagingSearch;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

@Getter
@Setter
public class MemberSearch extends PagingSearch {
    private String roleCode;
    private String memberActive;
    private String groupId;
    private String memberSignUpKeyChecked;
    private String memberLevel;
    private String memberMailing;
    private String memberSms;
    private Date termCheckDate;//약관동의 검색기간
    private String joinSDate;//가입기간 시작일
    private String joinEDate;//가입기간 종료일
    private String memberCityArea;//거주지 구 true|false
    private String fromYear;
    private String toYear;

    @Override
    public String getQueryString() {

        StringBuilder sb = new StringBuilder(100);
        sb.append(super.getQueryString());

        if (StringUtils.isNotBlank(this.getRoleCode())) {
            sb.append("&amp;roleCode=");
            sb.append(this.getRoleCode());
        }

        if (StringUtils.isNotBlank(this.getMemberActive())) {
            sb.append("&amp;memberActive=");
            sb.append(this.getMemberActive());
        }

        if (StringUtils.isNotBlank(this.getMemberSignUpKeyChecked())) {
            sb.append("&amp;getMemberSignUpKeyChecked=");
            sb.append(this.getMemberSignUpKeyChecked());
        }

        if (StringUtils.isNotBlank(this.getGroupId())) {
            sb.append("&amp;groupId=");
            sb.append(this.getGroupId());
        }

        if (StringUtils.isNotBlank(this.getMemberLevel())) {
            sb.append("&amp;memberLevel=");
            sb.append(this.getMemberLevel());
        }

        if (StringUtils.isNotBlank(this.getMemberMailing())) {
            sb.append("&amp;memberMailing=");
            sb.append(this.getMemberMailing());
        }

        if (StringUtils.isNotBlank(this.getMemberSms())) {
            sb.append("&amp;memberSms=");
            sb.append(this.getMemberSms());
        }

        return sb.toString();
    }
}
