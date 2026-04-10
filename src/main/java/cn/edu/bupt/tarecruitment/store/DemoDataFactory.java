package cn.edu.bupt.tarecruitment.store;

import cn.edu.bupt.tarecruitment.model.Position;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class DemoDataFactory {

    private DemoDataFactory() {
    }

    public static SystemData create() {
        SystemData data = new SystemData();
        data.ensureCollections();

        data.getPositions().add(position(
                "INT101",
                "Academic English Support",
                "Dr. Helen Carter",
                "helen.carter@bupt.edu.cn",
                "Provide academic English support to students, including conducting tutorial sessions, answering academic inquiries, assisting with essay and report writing, preparing weekly learning materials and worksheets, grading language assignments, and helping students improve their overall English academic skills.",
                "english writing, communication, tutorial facilitation",
                "canvas, grading, presentation",
                6,
                2));

        data.getPositions().add(position(
                "CS205",
                "Programming Fundamentals",
                "Prof. Chen Hao",
                "chenhao@bupt.edu.cn",
                "Assist in teaching basic programming concepts, supervise and guide lab sessions, help students debug code errors, maintain attendance and office hour records, answer basic programming questions during tutorials, and support instructors with course-related administrative tasks.",
                "java, debugging, algorithms",
                "git, unit testing, patience",
                8,
                1));

        data.getPositions().add(position(
                "DS301",
                "Data Science Practice",
                "Dr. Sophia Li",
                "sophia.li@bupt.edu.cn",
                "Assist in data science practical courses, guide students through data cleaning and analysis workshops, help with Jupyter notebook exercises, provide support for data processing tasks, assist in explaining machine learning basics, and help organize practical project sessions.",
                "python, statistics, data analysis",
                "pandas, visualization, teamwork",
                7,
                1));

        return data;
    }

    private static Position position(
            String moduleCode,
            String moduleName,
            String organiserName,
            String organiserEmail,
            String description,
            String requiredSkills,
            String preferredSkills,
            int weeklyHours,
            int quota) {

        Position position = new Position();
        position.setId(UUID.randomUUID().toString());
        position.setModuleCode(moduleCode);
        position.setModuleName(moduleName);
        position.setOrganiserName(organiserName);
        position.setOrganiserEmail(organiserEmail);
        position.setDescription(description);
        position.setRequiredSkills(requiredSkills);
        position.setPreferredSkills(preferredSkills);
        position.setWeeklyHours(weeklyHours);
        position.setQuota(quota);
        position.setStatus("OPEN");
        position.setCreatedAt(OffsetDateTime.now().toString());
        return position;
    }
}
