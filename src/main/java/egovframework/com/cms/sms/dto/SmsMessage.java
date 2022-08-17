package egovframework.com.cms.sms.dto;

import egovframework.com.cms.member.dto.Member;
import egovframework.com.cms.watchdog.aop.WatchDog;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class SmsMessage {
    private String smsNumberRaw;//받을 전화번호 콤마(,)구분
    @WatchDog
    private String smsText;//메세지
    private String smsFromName;
    private String smsFromNumber;

    public void makeNumberRawFromMemberList(List<Member> memberList) {
        //중복번호 제거하기위해 Set 을 사용한다.
        Set<String> mobileNumberSet = new HashSet<String>();
        //받을 회원 전화번호 세팅
        StringBuilder sb = new StringBuilder(1000);
        String mobileNumber = null;
        if (memberList != null) {
            for (Member member : memberList) {
                if (member != null
                        && StringUtils.isNotBlank(member.getMemberMobile1())
                        && StringUtils.isNotBlank(member.getMemberMobile2())
                        && StringUtils.isNotBlank(member.getMemberMobile3())) {
                    mobileNumber = member.getMemberMobile1() + member.getMemberMobile2() + member.getMemberMobile3();
                    mobileNumberSet.add(mobileNumber);
                }
            }
            for (String string : mobileNumberSet) {
                sb.append(",");
                sb.append(string);
            }
        }
        this.setSmsNumberRaw(StringUtils.removeStart(sb.toString(), ","));
    }
}
