package egovframework.com.cms.poll.dto;

import egovframework.com.cms.site.dto.MultiSiteVO;
import egovframework.com.cms.watchdog.aop.WatchDog;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class Poll extends MultiSiteVO {
    @WatchDog
    private Integer poId;
    @WatchDog
    private String poQuestion;                        //질문
    private String poDescription;                    //부가설명
    private String poType = "radio";                //라디오/체크박스
    private boolean poUseEtc = true;                //기타필드사용여부
    private boolean poMemberOnly = true;            //회원만?
    private boolean poAllowDuplicate = false;        //중복투표 허용 여부
    private boolean poShowResultAfterVote = true;    //선택완료 후 결과를 자동으로 보여줄지 여부
    private boolean poShowResultButton = true;    //결과보기버튼 표시 여부
    private boolean poShowAnswerNum = true;        //보기번호 표시여부
    private String poStartDate;                    //시작일
    private String poEndDate;                        //종료일
    private Integer poVoteLimit = 0;                //최대 참여인원 0이면 무제한
    private String poTemplate = "poll.jsp";        //view파일 지정시 파일명
    private List<PollAnswer> pollAnswers;            //보기목록
    //private List<PollVote> pollVotes;				//참여목록
    private Date poRegDate;                        //등록일시
    private Integer poVoteTotal;

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public String getPoStatus() {
        if (StringUtils.isBlank(this.getPoStartDate()) && StringUtils.isBlank(this.getPoEndDate())) {
            return "open";
        }
        //종료일만 있음
        if (StringUtils.isBlank(this.getPoStartDate()) && StringUtils.isNotBlank(this.getPoEndDate())) {
            Date today = new Date();
            Date endDate = null;
            try {
                endDate = SDF.parse(this.getPoEndDate() + " 23:59:59");
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (endDate.after(today)) {
                return "open";
            } else {
                return "close";
            }
        }
        //시작일만 있음
        else if (StringUtils.isNotBlank(this.getPoStartDate()) && StringUtils.isBlank(this.getPoEndDate())) {
            Date today = new Date();
            Date startDate = null;
            try {
                startDate = SDF.parse(this.getPoStartDate() + " 00:00:01");
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (startDate.after(today)) {
                return "before";
            } else {
                return "open";
            }
        } else if (StringUtils.isNotBlank(this.getPoStartDate()) && StringUtils.isNotBlank(this.getPoEndDate())) {
            Date today = new Date();
            Date endDate = null;
            Date startDate = null;
            try {
                startDate = SDF.parse(this.getPoStartDate() + " 00:00:01");
                endDate = SDF.parse(this.getPoEndDate() + " 23:59:59");
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (today.after(startDate) && today.before(endDate)) {
                return "open";
            } else {
                if (today.before(startDate)) {
                    return "before";
                } else {
                    return "close";
                }
            }
        } else {
            return null;
        }
    }
}
