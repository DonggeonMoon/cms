package com.dgmoonlabs.cms.domain.common.user.entity.member;

import com.dgmoonlabs.cms.domain.common.user.constant.BirthDateInput;
import com.dgmoonlabs.cms.domain.common.user.constant.NameFormat;
import com.dgmoonlabs.cms.domain.common.user.constant.PersonalInfoItem;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class MemberJoinSetting extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    @Comment("이름 입력 방식 (FULL: 성명 한 칸, SPLIT: 성·이름 따로)")
    private NameFormat nameFormat = NameFormat.FULL;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("true")
    @Comment("이름 수집 여부")
    private boolean collectName = true;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("닉네임 사용 여부")
    private boolean collectNickname;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "varchar(20)")
    @ColumnDefault("'SELECT'")
    @Enumerated(EnumType.STRING)
    @Comment("생년월일 입력 방식 (SELECT: 년/월/일 선택, EIGHT_DIGITS: 19900410, SIX_DIGITS: 900410)")
    private BirthDateInput birthDateInput = BirthDateInput.SELECT;

    @Builder.Default
    @Column(nullable = false)
    @Convert(converter = ItemsConverter.class)
    @Comment("수집할 개인정보 항목 (이름은 항상 수집)")
    private Set<PersonalInfoItem> collectedItems = EnumSet.copyOf(PersonalInfoItem.DEFAULTS);

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("휴대폰 번호에 국가 코드 입력란을 둘지 여부 (휴대폰 번호를 수집할 때만 적용)")
    private boolean useMobilePhoneCountryCode;

    public static MemberJoinSetting defaults() {
        return MemberJoinSetting.builder().build();
    }

    public void update(final boolean collectName, final NameFormat nameFormat, final boolean collectNickname,
                       final BirthDateInput birthDateInput, final Set<PersonalInfoItem> collectedItems,
                       final boolean useMobilePhoneCountryCode) {
        this.collectName = collectName;
        this.collectNickname = collectNickname;
        this.nameFormat = nameFormat;
        this.birthDateInput = birthDateInput;
        this.collectedItems = collectedItems.isEmpty() ? EnumSet.noneOf(PersonalInfoItem.class) : EnumSet.copyOf(collectedItems);
        this.useMobilePhoneCountryCode = useMobilePhoneCountryCode;
    }

    public boolean collects(final PersonalInfoItem item) {
        return collectedItems.contains(item);
    }

    public String keep(final PersonalInfoItem item, final String value) {
        return collects(item) && value != null && !value.isBlank() ? value : null;
    }

    public String keepMobilePhoneCountryCode(final String value) {
        return collects(PersonalInfoItem.MOBILE_PHONE) && useMobilePhoneCountryCode && value != null && !value.isBlank()
                ? value : null;
    }

    public LocalDate keep(final PersonalInfoItem item, final LocalDate value) {
        return collects(item) ? value : null;
    }

    @Converter
    static class ItemsConverter implements AttributeConverter<Set<PersonalInfoItem>, String> {
        @Override
        public String convertToDatabaseColumn(final Set<PersonalInfoItem> items) {
            return items == null ? "" : items.stream().map(Enum::name).sorted().collect(Collectors.joining(","));
        }

        @Override
        public Set<PersonalInfoItem> convertToEntityAttribute(final String value) {
            Set<PersonalInfoItem> items = EnumSet.noneOf(PersonalInfoItem.class);
            if (value != null && !value.isBlank()) {
                Arrays.stream(value.split(",")).map(String::trim).map(PersonalInfoItem::valueOf).forEach(items::add);
            }
            return items;
        }
    }
}
