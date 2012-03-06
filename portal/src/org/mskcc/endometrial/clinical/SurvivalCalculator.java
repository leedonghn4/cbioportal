package org.mskcc.endometrial.clinical;

import java.text.DecimalFormat;

/**
 * Calculator for OS and DFS Survival Intervals.
 */
public class SurvivalCalculator {
    private static final double ONE_DAY = 0.0328549112;
    private static final String NA_OUTPUT = "NA";
    private static final String NA_INPUT = "[Not Available]";
    private static final String LIVING = "LIVING";
    private static final String DECEASED = "DECEASED";
    private static DecimalFormat formatter = new DecimalFormat("###.##");

    /**
     * Calculates OS Interval in Months.
     *
     * @param vitalStatus   Vital Status, e.g. LIVING or DECEASED.
     * @param daysToFu      Days to Follow-Up.
     * @param daysToAlive   Days to Alive.
     * @param daysToDead    Days to Death.
     * @return OS Interval in Months or NA.
     */
    public static String calculateOsMonths(String vitalStatus, String daysToFu, String daysToAlive,
        String daysToDead) {
        String osMonthsStr = NA_OUTPUT;
        if (vitalStatus.equals(DECEASED)) {
            double osMonths = convertDaysToMonths(daysToDead);
            osMonthsStr = formatter.format(osMonths);
        } else if (vitalStatus.equals(LIVING)) {
            boolean daysToAliveHasData = fieldHasData(daysToAlive);
            boolean daysToFUHasData = fieldHasData(daysToFu);
            if (daysToAliveHasData && daysToFUHasData) {
                double osMonths1 = convertDaysToMonths(daysToAlive);
                double osMonths2= convertDaysToMonths(daysToFu);
                double osMonths = Math.max(osMonths1, osMonths2);
                osMonthsStr = formatter.format(osMonths);
            } else if (daysToAliveHasData) {
                double osMonths = convertDaysToMonths(daysToAlive);
                osMonthsStr = formatter.format(osMonths);
            } else if (daysToFUHasData) {
                double osMonths = convertDaysToMonths(daysToFu);
                osMonthsStr = formatter.format(osMonths);
            } else {
                osMonthsStr = NA_OUTPUT;
            }
        } else {
            throw new IllegalArgumentException("Aborting.  Cannot process VITAL STATUS:  " + vitalStatus);
        }
        return osMonthsStr;
    }

    /**
     * Calculates DFS in Months.
     *
     * @param recurredStatus                                RecurredStatus:  YES or NO.
     * @param daysToNewTumorEventAfterInitialTreatment      Days to New Tumor After Initial Treatment.
     * @param daysToFollowUp                                Days to Follow-up.
     * @return DFS Months or NA.
     */
    public static String calculateDfsMonths(String recurredStatus,
        String daysToNewTumorEventAfterInitialTreatment, String daysToFollowUp) {
        String dfsMonthsStr = NA_OUTPUT;

        if (recurredStatus.equalsIgnoreCase("YES")) {
            if (fieldHasData(daysToNewTumorEventAfterInitialTreatment)) {
                double dfsMonths = convertDaysToMonths(daysToNewTumorEventAfterInitialTreatment);
                dfsMonthsStr = formatter.format(dfsMonths);
            } else {
                dfsMonthsStr = NA_OUTPUT;
            }
        } else if (recurredStatus.equalsIgnoreCase("NO")) {
            if (fieldHasData(daysToFollowUp)) {
                double dfsMonths = convertDaysToMonths(daysToFollowUp);
                dfsMonthsStr = formatter.format(dfsMonths);
            } else {
                dfsMonthsStr = NA_OUTPUT;
            }
        } else {
            dfsMonthsStr = NA_OUTPUT;
        }
        return dfsMonthsStr;
    }

    private static double convertDaysToMonths(String numberOfDays) {
        int numDays = Integer.parseInt(numberOfDays);
        return numDays * ONE_DAY;
    }

    private static boolean fieldHasData(String fieldValue) {
        if (fieldValue.trim().length() > 0) {
            if (!fieldValue.equalsIgnoreCase(NA_INPUT)) {
                return true;
            }
        }
        return false;
    }
}