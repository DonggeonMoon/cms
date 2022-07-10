package egovframework.com.cms.faq.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "MIISM_FAQ")
public class Faq {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String question;
    @Column(columnDefinition = "LONGTEXT")
    private String answer;

    @Column(columnDefinition = "TINYINT(1)")
    private Boolean publish;

    private LocalDateTime createdDate;

    // 정적 팩토리 메소드 (Faq는 이 메소드를 통해서만 객체 생성이 가능하다)
    // 오버라이딩 해서 매개변수를 다양하게 사용할 수 있음
    public static Faq of(String question, String answer, Boolean publish) {
        Faq faq = new Faq();
        faq.question = question;
        faq.answer = answer;
        faq.createdDate = LocalDateTime.now();
        faq.publish = publish;

        return faq;
    }

    // 수정 메소드
    public void update(String questionParam, String answerParam, Boolean publishParam) {
        this.question = questionParam;
        this.answer = answerParam;
        this.publish = publishParam;
    }
}
