package cn.cnki.spider.dao.impl;

import cn.cnki.spider.dao.UniversityProjectsDao;
import cn.cnki.spider.entity.UniversityProjects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UniversityProjectsDaoImpl implements UniversityProjectsDao {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public int batchInsert(List<UniversityProjects> records) {
        String sql = "insert ignore into crawl_university_projects (code, `name`, `person`, `level`," +
                "teacher, university, `type`, time_range, `subject`, category, plan_date, person_info, teacher_unit," +
                " a, page, `year`, ctime)" +
                " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        List<Object[]> batchArgs = new ArrayList<>();
        for (UniversityProjects project : records) {
            batchArgs.add(new Object[]{
                    project.getCode(), project.getName(), project.getPerson(), project.getLevel(),
                    project.getTeacher(), project.getUniversity(), project.getType(),
                    project.getTimeRange(), project.getSubject(), project.getCategory(),
                    project.getPlanDate(), project.getPersonInfo(), project.getTeacherInfo(),
                    project.getA(), project.getPage(), project.getYear(), project.getCtime()});
        }
        jdbcTemplate.batchUpdate(sql, batchArgs);
        return records.size();
    }
}