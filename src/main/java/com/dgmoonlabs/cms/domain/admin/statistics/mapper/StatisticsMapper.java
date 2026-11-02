package com.dgmoonlabs.cms.domain.admin.statistics.mapper;

import com.dgmoonlabs.cms.domain.admin.statistics.dto.StatisticsDtos.CountRow;
import com.dgmoonlabs.cms.domain.admin.statistics.dto.StatisticsDtos.DailyRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

// "date" and "count" are quoted: both are keywords in some databases (H2 is used by the tests)
@Mapper
public interface StatisticsMapper {
    @Select("""
            select s."date" as date, sum(s."count") as visits
            from statistics s
            where s."date" between #{from} and #{to}
            group by s."date"
            order by s."date"
            """)
    List<DailyRow> dailyVisits(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Select("""
            select coalesce(s.browser, 'Unknown') as label, sum(s."count") as visits
            from statistics s
            where s."date" between #{from} and #{to}
            group by s.browser
            order by visits desc
            limit #{limit}
            """)
    List<CountRow> byBrowser(@Param("from") LocalDate from, @Param("to") LocalDate to, @Param("limit") int limit);

    @Select("""
            select coalesce(s.os, 'Unknown') as label, sum(s."count") as visits
            from statistics s
            where s."date" between #{from} and #{to}
            group by s.os
            order by visits desc
            limit #{limit}
            """)
    List<CountRow> byOs(@Param("from") LocalDate from, @Param("to") LocalDate to, @Param("limit") int limit);

    @Select("""
            select coalesce(s.url, '/') as label, sum(s."count") as visits
            from statistics s
            where s."date" between #{from} and #{to}
            group by s.url
            order by visits desc
            limit #{limit}
            """)
    List<CountRow> byUrl(@Param("from") LocalDate from, @Param("to") LocalDate to, @Param("limit") int limit);
}
