package com.dgmoonlabs.cms.domain.common.regex.service;

import com.dgmoonlabs.cms.domain.business.BusinessNumbers;
import com.dgmoonlabs.cms.domain.common.regex.constant.RegexTarget;
import com.dgmoonlabs.cms.domain.common.regex.dto.RegexDtos.*;
import com.dgmoonlabs.cms.domain.common.regex.entity.RegexBinding;
import com.dgmoonlabs.cms.domain.common.regex.entity.RegexRule;
import com.dgmoonlabs.cms.domain.common.regex.repository.RegexBindingRepository;
import com.dgmoonlabs.cms.domain.common.regex.repository.RegexRuleRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegexService {
    private final RegexRuleRepository ruleRepository;
    private final RegexBindingRepository bindingRepository;

    private volatile Map<RegexTarget, CompiledRule> cache;

    private record CompiledRule(Pattern pattern, String message) {
    }

    public boolean matches(final RegexTarget target, final String value) {
        return violation(target, value).isEmpty();
    }

    public Optional<String> violation(final RegexTarget target, final String value) {
        CompiledRule rule = compiled().get(target);
        if (rule == null || value == null || rule.pattern().matcher(value).matches()) {
            return Optional.empty();
        }
        return Optional.of(rule.message());
    }

    public String requireNumberFormat(final RegexTarget target, final String number) {
        if (!matches(target, number)) {
            throw new BusinessException(ErrorCode.NUMBER_FORMAT_MISMATCH);
        }
        return number;
    }

    @Transactional
    public void registerDefaults() {
        for (RegexTarget target : RegexTarget.values()) {
            if (bindingRepository.findByTarget(target).isPresent()) {
                continue;
            }
            RegexRule rule = ruleRepository.findByRuleKey(target.name()).orElseGet(() -> ruleRepository.save(RegexRule.builder()
                    .ruleKey(target.name())
                    .name(target.getLabel())
                    .pattern(target.getDefaultPattern())
                    .message(target.getDefaultMessage())
                    .hint(target.getDefaultHint())
                    .build()));
            bindingRepository.save(RegexBinding.builder().target(target).regexRuleId(rule.getId()).build());
        }
        invalidateCache();
    }

    @Transactional(readOnly = true)
    public List<RegexResponse> getRules() {
        Map<Long, List<RegexTarget>> usage = bindingRepository.findAll().stream()
                .filter(binding -> binding.getRegexRuleId() != null)
                .sorted(Comparator.comparing(RegexBinding::getTarget))
                .collect(Collectors.groupingBy(RegexBinding::getRegexRuleId, Collectors.mapping(RegexBinding::getTarget, Collectors.toList())));
        return ruleRepository.findAllByDeletedAtIsNullOrderByIdAsc().stream()
                .map(rule -> RegexResponse.of(rule, usage.getOrDefault(rule.getId(), List.of())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RegexOption> getOptions() {
        return ruleRepository.findAllByDeletedAtIsNullOrderByIdAsc().stream()
                .map(rule -> new RegexOption(rule.getId(), rule.getName(), rule.getPattern()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PublicRegex> getPublicRules() {
        Map<Long, RegexRule> rules = rulesById();
        return bindingRepository.findAll().stream()
                .filter(binding -> binding.getTarget().getGroup() == RegexTarget.Group.MEMBER)
                .filter(binding -> rules.containsKey(binding.getRegexRuleId()))
                .sorted(Comparator.comparing(RegexBinding::getTarget))
                .map(binding -> {
                    RegexRule rule = rules.get(binding.getRegexRuleId());
                    return new PublicRegex(binding.getTarget(), rule.getPattern(), rule.getMessage(), rule.getHint());
                })
                .toList();
    }

    @Transactional
    public Long create(final RegexSaveRequest request) {
        RegexRule rule = ruleRepository.save(RegexRule.builder()
                .ruleKey("CUSTOM_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT))
                .name(request.name().trim())
                .pattern(request.pattern())
                .message(request.message().trim())
                .hint(blankToNull(request.hint()))
                .description(blankToNull(request.description()))
                .build());
        return rule.getId();
    }

    @Transactional
    public void update(final Long id, final RegexSaveRequest request) {
        RegexRule rule = findRule(id);
        bindingRepository.findAll().stream()
                .filter(binding -> id.equals(binding.getRegexRuleId()))
                .map(RegexBinding::getTarget)
                .filter(target -> target.getNumberPrefix() != null)
                .forEach(target -> requireGeneratedNumbersMatch(target, request.pattern()));
        rule.update(request.name().trim(), request.pattern(), request.message().trim(), blankToNull(request.hint()),
                blankToNull(request.description()));
        invalidateCache();
    }

    @Transactional(readOnly = true)
    public Long selected(final RegexTarget target) {
        return bindingRepository.findByTarget(target).map(RegexBinding::getRegexRuleId).orElse(null);
    }

    @Transactional(readOnly = true)
    public Map<RegexTarget, Long> selected(final RegexTarget.Group group) {
        Map<RegexTarget, Long> selected = new EnumMap<>(RegexTarget.class);
        for (RegexTarget target : RegexTarget.values()) {
            if (target.getGroup() == group) {
                selected.put(target, selected(target));
            }
        }
        return selected;
    }

    @Transactional
    public void select(final RegexTarget target, final Long regexRuleId) {
        if (regexRuleId != null) {
            RegexRule rule = findRule(regexRuleId);
            if (target.getNumberPrefix() != null) {
                requireGeneratedNumbersMatch(target, rule.getPattern());
            }
        }
        bindingRepository.findByTarget(target)
                .orElseGet(() -> bindingRepository.save(RegexBinding.builder().target(target).build()))
                .select(regexRuleId);
        invalidateCache();
    }

    public RegexTestResult test(final RegexTestRequest request) {
        try {
            return new RegexTestResult(true, Pattern.compile(request.pattern()).matcher(request.value()).matches(), null);
        } catch (PatternSyntaxException e) {
            return new RegexTestResult(false, false, e.getDescription());
        }
    }

    private void requireGeneratedNumbersMatch(final RegexTarget target, final String pattern) {
        String sample = BusinessNumbers.next(target.getNumberPrefix(), number -> false);
        if (!Pattern.compile(pattern).matcher(sample).matches()) {
            throw new BusinessException(ErrorCode.REGEX_NUMBER_FORMAT_MISMATCH);
        }
    }

    // 지금 비워 이 트랜잭션이 변경을 보게 하고, 끝날 때 한 번 더 비워 그사이 캐시된 옛 값을 버린다.
    private void invalidateCache() {
        cache = null;
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(final int status) {
                    cache = null;
                }
            });
        }
    }

    private Map<RegexTarget, CompiledRule> compiled() {
        Map<RegexTarget, CompiledRule> current = cache;
        if (current == null) {
            Map<Long, RegexRule> rules = rulesById();
            Map<RegexTarget, CompiledRule> built = new EnumMap<>(RegexTarget.class);
            for (RegexBinding binding : bindingRepository.findAll()) {
                RegexRule rule = rules.get(binding.getRegexRuleId());
                if (rule != null) {
                    built.put(binding.getTarget(), new CompiledRule(Pattern.compile(rule.getPattern()), rule.getMessage()));
                }
            }
            current = Collections.unmodifiableMap(built);
            cache = current;
        }
        return current;
    }

    private Map<Long, RegexRule> rulesById() {
        return ruleRepository.findAllByDeletedAtIsNullOrderByIdAsc().stream()
                .collect(Collectors.toMap(RegexRule::getId, Function.identity()));
    }

    private RegexRule findRule(final Long id) {
        return ruleRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(BusinessException::notFound);
    }

    private static String blankToNull(final String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
