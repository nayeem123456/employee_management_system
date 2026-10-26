package com.example.employeemanagement.ai;

import com.example.employeemanagement.dto.DepartmentStatDto;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class RuleBasedFallbackEngine {

    public String answerQuery(String query, long total, long active, long onLeave, long inactive,
                              List<DepartmentStatDto> departmentStats, long recentJoiners) {
        String lowerQuery = query.toLowerCase().trim();

        // 1. Largest / Smallest department queries
        if (lowerQuery.contains("largest") || lowerQuery.contains("biggest") || lowerQuery.contains("top department") || lowerQuery.contains("most employee")) {
            if (departmentStats.isEmpty()) {
                return "There are currently no departments with assigned employees.";
            }
            DepartmentStatDto top = departmentStats.get(0);
            return String.format("The department with the largest headcount is **%s (%s)** with **%d employees**, representing **%.1f%%** of the total workforce (%d active).",
                    top.getDepartmentName(), top.getDepartmentCode(), top.getTotalHeadcount(), top.getPercentage(), top.getActiveHeadcount());
        }

        // 2. Active employees per department
        if (lowerQuery.contains("active") && (lowerQuery.contains("department") || lowerQuery.contains("each"))) {
            StringBuilder sb = new StringBuilder();
            sb.append("Here is the breakdown of active employees across all departments:\n\n");
            for (DepartmentStatDto dept : departmentStats) {
                sb.append(String.format("- **%s (%s)**: %d active out of %d total (%.1f%% of dept)\n",
                        dept.getDepartmentName(), dept.getDepartmentCode(),
                        dept.getActiveHeadcount(), dept.getTotalHeadcount(),
                        dept.getTotalHeadcount() > 0 ? ((double) dept.getActiveHeadcount() / dept.getTotalHeadcount() * 100) : 0.0));
            }
            sb.append(String.format("\nAcross the entire company, there are **%d active employees** out of **%d total** (%.1f%%).",
                    active, total, total > 0 ? ((double) active / total * 100) : 0.0));
            return sb.toString();
        }

        // 3. Compare headcount across departments
        if (lowerQuery.contains("compare") || lowerQuery.contains("comparison") || lowerQuery.contains("headcount across")) {
            StringBuilder sb = new StringBuilder();
            sb.append("### Department Headcount Comparison\n\n");
            sb.append("| Department | Code | Total Headcount | Active Members | Share of Workforce |\n");
            sb.append("|---|---|---|---|---|\n");
            for (DepartmentStatDto dept : departmentStats) {
                sb.append(String.format("| %s | %s | %d | %d | %.1f%% |\n",
                        dept.getDepartmentName(), dept.getDepartmentCode(),
                        dept.getTotalHeadcount(), dept.getActiveHeadcount(), dept.getPercentage()));
            }
            if (departmentStats.size() >= 2) {
                DepartmentStatDto first = departmentStats.get(0);
                DepartmentStatDto last = departmentStats.get(departmentStats.size() - 1);
                sb.append(String.format("\n**Insight:** Headcount variance ranges from %d in %s down to %d in %s.",
                        first.getTotalHeadcount(), first.getDepartmentName(), last.getTotalHeadcount(), last.getDepartmentName()));
            }
            return sb.toString();
        }

        // 4. Summarize employee distribution
        if (lowerQuery.contains("summar") || lowerQuery.contains("distribution") || lowerQuery.contains("breakdown") || lowerQuery.contains("overview")) {
            StringBuilder sb = new StringBuilder();
            sb.append("### Workforce Distribution Summary\n\n");
            sb.append(String.format("The organization currently employs **%d total personnel** across **%d departments**.\n\n",
                    total, departmentStats.size()));
            sb.append("**Employment Status Distribution:**\n");
            sb.append(String.format("- **Active:** %d employees (%.1f%%)\n", active, total > 0 ? ((double) active / total * 100) : 0.0));
            sb.append(String.format("- **On Leave:** %d employees (%.1f%%)\n", onLeave, total > 0 ? ((double) onLeave / total * 100) : 0.0));
            sb.append(String.format("- **Inactive / Archived:** %d employees (%.1f%%)\n\n", inactive, total > 0 ? ((double) inactive / total * 100) : 0.0));
            sb.append("**Key Department Shares:**\n");
            for (DepartmentStatDto dept : departmentStats.stream().limit(4).collect(Collectors.toList())) {
                sb.append(String.format("- %s: %d personnel (%.1f%%)\n", dept.getDepartmentName(), dept.getTotalHeadcount(), dept.getPercentage()));
            }
            return sb.toString();
        }

        // 5. Total, Inactive or On-leave queries
        if (lowerQuery.contains("total") || lowerQuery.contains("how many employee")) {
            return String.format("The organization currently has **%d total registered employees** (%d active, %d on leave, and %d inactive).",
                    total, active, onLeave, inactive);
        }

        if (lowerQuery.contains("leave") || lowerQuery.contains("on leave")) {
            return String.format("There are currently **%d employee(s) on temporary leave**, representing %.1f%% of the total workforce.",
                    onLeave, total > 0 ? ((double) onLeave / total * 100) : 0.0);
        }

        if (lowerQuery.contains("inactive") || lowerQuery.contains("terminated")) {
            return String.format("There are currently **%d inactive employee record(s)** in the system.", inactive);
        }

        // Default intelligent analytical answer
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Based on current organizational metrics, the system tracks **%d employees** (%d active, %d on leave, %d inactive).\n\n",
                total, active, onLeave, inactive));
        sb.append("Top departments by staffing:\n");
        for (DepartmentStatDto dept : departmentStats.stream().limit(3).collect(Collectors.toList())) {
            sb.append(String.format("- **%s**: %d employees (%.1f%% of company)\n",
                    dept.getDepartmentName(), dept.getTotalHeadcount(), dept.getPercentage()));
        }
        sb.append("\nYou can ask specific questions such as \"Which departments have the largest headcount?\", \"How many active employees work in each department?\", or \"Summarize employee distribution\".");
        return sb.toString();
    }

    public List<String> generateTrends(long total, long active, long onLeave, long inactive,
                                       List<DepartmentStatDto> departmentStats, long newJoiners) {
        List<String> trends = new ArrayList<>();

        double activeRatio = total > 0 ? ((double) active / total * 100) : 0.0;
        if (activeRatio >= 85.0) {
            trends.add(String.format("Strong workforce stability with %.1f%% active personnel deployment across departments.", activeRatio));
        } else {
            trends.add(String.format("Elevated proportion of non-active personnel (%.1f%% non-active: %d on leave, %d inactive).", 100.0 - activeRatio, onLeave, inactive));
        }

        if (!departmentStats.isEmpty()) {
            DepartmentStatDto top = departmentStats.get(0);
            if (top.getPercentage() > 30.0) {
                trends.add(String.format("High organizational concentration in %s, accounting for %.1f%% of overall staffing.",
                        top.getDepartmentName(), top.getPercentage()));
            } else {
                trends.add(String.format("Balanced staffing distribution, with the leading department (%s) comprising %.1f%% of total headcount.",
                        top.getDepartmentName(), top.getPercentage()));
            }
        }

        if (newJoiners > 0) {
            trends.add(String.format("Positive recruitment velocity with %d new joiners onboarded in the evaluated timeframe.", newJoiners));
        } else {
            trends.add("Static recruitment cycle with no new joiners recorded in the specified period.");
        }

        long smallDepts = departmentStats.stream().filter(d -> d.getTotalHeadcount() <= 2).count();
        if (smallDepts > 0) {
            trends.add(String.format("%d department(s) operate with lean headcount (<= 2 members), which may present key-person dependency risks.", smallDepts));
        }

        return trends;
    }

    public List<String> generateRecommendations(long total, long active, long onLeave, long inactive,
                                               List<DepartmentStatDto> departmentStats, long newJoiners) {
        List<String> recommendations = new ArrayList<>();

        if (onLeave > 0) {
            recommendations.add(String.format("Establish cross-functional coverage protocols for the %d team members currently on leave to safeguard operational continuity.", onLeave));
        }

        for (DepartmentStatDto dept : departmentStats) {
            if (dept.getTotalHeadcount() <= 2) {
                recommendations.add(String.format("Review resource allocation for %s (%d team members) to mitigate operational bottlenecks and redundancy gaps.",
                        dept.getDepartmentName(), dept.getTotalHeadcount()));
            }
        }

        if (inactive > 0) {
            recommendations.add(String.format("Conduct data hygiene audit on the %d inactive profiles to verify exit records and access de-provisioning compliance.", inactive));
        }

        recommendations.add("Incorporate quarterly department headcount rebalancing reviews aligned with strategic delivery milestones.");

        return recommendations;
    }

    public String generateExecutiveReportSummary(LocalDate dateFrom, LocalDate dateTo, long total, long active,
                                                 long onLeave, long inactive, List<DepartmentStatDto> departmentStats,
                                                 long newJoiners, String focusArea) {
        String periodDesc = (dateFrom != null && dateTo != null)
                ? String.format("from %s to %s", dateFrom, dateTo)
                : "across all historical company records";

        String topDeptStr = !departmentStats.isEmpty()
                ? String.format("The primary workforce hub is %s with %d employees (%.1f%% of total headcount).",
                    departmentStats.get(0).getDepartmentName(), departmentStats.get(0).getTotalHeadcount(), departmentStats.get(0).getPercentage())
                : "No departments currently populated.";

        double activeRatio = total > 0 ? ((double) active / total * 100) : 0.0;

        return String.format(
                "Executive Assessment (%s): The organization maintains an aggregate headcount of %d individuals distributed across %d active functional divisions. Operational readiness stands at %.1f%% active duty (%d active, %d on leave, %d inactive). During this assessment window, %d new personnel were successfully onboarded. %s Overall organizational telemetry demonstrates steady operational momentum with targeted opportunities for departmental balancing.",
                periodDesc, total, departmentStats.size(), activeRatio, active, onLeave, inactive, newJoiners, topDeptStr
        );
    }
}
