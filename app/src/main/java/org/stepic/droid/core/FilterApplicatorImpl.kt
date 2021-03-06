package org.stepic.droid.core

import org.stepic.droid.model.Course
import org.stepic.droid.model.StepikFilter
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.DateTimeHelper
import javax.inject.Inject

class FilterApplicatorImpl
@Inject constructor(private val defaultFilter: DefaultFilter,
                    private val sharedPreferenceHelper: SharedPreferenceHelper) : FilterApplicator {

    override fun getFilteredFeaturedFromSharedPrefs(sourceCourses: List<Course>): List<Course> =
            resolveFiltersForList(sourceCourses, sharedPreferenceHelper.filterForFeatured)


    override fun getFilteredFeaturedFromDefault(sourceCourses: List<Course>): List<Course>? {
        val filters = StepikFilter.values().filter { defaultFilter.getDefaultFeatured(it) }.toSet()
        return resolveFiltersForList(sourceCourses, filters)
    }

    private fun resolveFiltersForList(sourceCourses: List<Course>, filters: Set<StepikFilter>): List<Course> {
        //local helper functions:
        fun resolveFilters(course: Course, now: Long, applyFilters: (Course, endDate: Long?, isAfterBeginOrNotStartable: Boolean, isBeginDateInFuture: Boolean, isEndDateInFuture: Boolean, isEnded: Boolean, filterSet: Set<StepikFilter>) -> Boolean, filterSet: Set<StepikFilter>): Boolean {
            val beginDate: Long? = course.beginDate?.let {
                DateTimeHelper.toCalendar(it).timeInMillis
            }

            val endDate: Long? = course.endDate?.let {
                DateTimeHelper.toCalendar(it).timeInMillis
            }

            val isEnded: Boolean = course.lastDeadline?.let {
                val lastDeadlineMillis = DateTimeHelper.toCalendar(it).timeInMillis
                now > lastDeadlineMillis
            } ?: false

            val isBeginDateInFuture: Boolean = beginDate?.compareTo(now) ?: -1 > 0
            val isEndDateInFuture: Boolean = endDate?.compareTo(now) ?: -1 > 0

            val isAfterBeginOrNotStartable = (beginDate != null && !isBeginDateInFuture || beginDate == null)
            return applyFilters.invoke(course, endDate, isAfterBeginOrNotStartable, isBeginDateInFuture, isEndDateInFuture, isEnded, filterSet)

        }

        fun applyFiltersForSet(course: Course, endDate: Long?, isAfterBeginOrNotStartable: Boolean, isBeginDateInFuture: Boolean, isEndDateInFuture: Boolean, isEnded: Boolean, filters: Set<StepikFilter>): Boolean {
            return (
                    filters.contains(StepikFilter.RUSSIAN) && course.language?.equals("ru") ?: false
                            || filters.contains(StepikFilter.ENGLISH) && course.language?.equals("en") ?: false
                            || (filters.contains(StepikFilter.RUSSIAN) && filters.contains(StepikFilter.ENGLISH))
                    )

                    &&
                    (filters.contains(StepikFilter.UPCOMING) && isBeginDateInFuture
                            || filters.contains(StepikFilter.ACTIVE) && (!isEnded && isAfterBeginOrNotStartable || endDate != null && isEndDateInFuture && !isBeginDateInFuture)
                            || filters.contains(StepikFilter.PAST) && (endDate == null && isEnded || endDate != null && !isEndDateInFuture))
        }

        //logic

        val possibleFilterSize = StepikFilter.values().size
        if (possibleFilterSize == filters.size
                || possibleFilterSize - 1 == filters.size && !filters.contains(StepikFilter.PERSISTENT)) {
            // if all filters are chosen or all except persistent is chosen -> do not filter
            return sourceCourses
        }

        val now: Long = DateTimeHelper.nowUtc()
        val filteredList = sourceCourses.filter { course ->
            resolveFilters(course, now, ::applyFiltersForSet, filters)
        }
        return filteredList
    }

}
