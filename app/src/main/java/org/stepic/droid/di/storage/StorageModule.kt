package org.stepic.droid.di.storage

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepic.droid.di.qualifiers.EnrolledCoursesDaoQualifier
import org.stepic.droid.di.qualifiers.FeaturedCoursesDaoQualifier
import org.stepic.droid.model.*
import org.stepic.droid.model.Unit
import org.stepic.droid.model.code.CodeSubmission
import org.stepic.droid.notifications.model.Notification
import org.stepic.droid.storage.DatabaseHelper
import org.stepic.droid.storage.dao.*
import org.stepic.droid.storage.operations.CrudOperations
import org.stepic.droid.storage.operations.CrudOperationsImpl
import org.stepic.droid.storage.operations.StepInfoOperation
import org.stepic.droid.storage.operations.StepInfoOperationImpl
import org.stepic.droid.storage.structure.DbStructureEnrolledAndFeaturedCourses
import org.stepic.droid.web.ViewAssignment

@Module
abstract class StorageModule {

    @StorageSingleton
    @Binds
    internal abstract fun bindsProgressPublishingOperation(
            stepInfoOperationImpl: StepInfoOperationImpl): StepInfoOperation

    @StorageSingleton
    @Binds
    internal abstract fun bindsCrud(crudOperationsImpl: CrudOperationsImpl): CrudOperations

    @StorageSingleton
    @Binds
    internal abstract fun provideSqlOpenHelper(databaseHelper: DatabaseHelper): SQLiteOpenHelper


    @StorageSingleton
    @Binds
    internal abstract fun provideCodeSubmissionDao(codeSubmissionDaoImpl: CodeSubmissionDaoImpl): IDao<CodeSubmission>

    @StorageSingleton
    @Binds
    internal abstract fun provideSectionDao(sectionDao: SectionDaoImpl): IDao<Section>

    @StorageSingleton
    @Binds
    internal abstract fun provideUnitDao(unitDao: UnitDaoImpl): IDao<Unit>

    @StorageSingleton
    @Binds
    internal abstract fun provideProgressDao(progressDao: ProgressDaoImpl): IDao<Progress>

    @Binds
    internal abstract fun provideAssignmentDao(assignmentDao: AssignmentDaoImpl): IDao<Assignment>

    @Binds
    internal abstract fun provideCertificateDao(certificateViewItemDao: CertificateViewItemDaoImpl): IDao<CertificateViewItem>

    @Binds
    internal abstract fun provideLessonDao(lessonDao: LessonDaoImpl): IDao<Lesson>

    @StorageSingleton
    @Binds
    internal abstract fun provideViewAssignment(viewAssignmentDao: ViewAssignmentDaoImpl): IDao<ViewAssignment>

    @StorageSingleton
    @Binds
    internal abstract fun provideDownloadEntity(downloadEntityDao: DownloadEntityDaoImpl): IDao<DownloadEntity>

    @StorageSingleton
    @Binds
    internal abstract fun provideCalendarSection(calendarSectionDao: CalendarSectionDaoImpl): IDao<CalendarSection>

    @StorageSingleton
    @Binds
    internal abstract fun provideCachedVideo(persistentVideoDao: PersistentVideoDaoImpl): IDao<CachedVideo>

    @StorageSingleton
    @Binds
    internal abstract fun provideBlockWrapper(blockDao: BlockDaoImpl): IDao<BlockPersistentWrapper>

    @StorageSingleton
    @Binds
    internal abstract fun provideStep(stepDao: StepDaoImpl): IDao<Step>

    @StorageSingleton
    @Binds
    internal abstract fun provideNotification(notificationDao: NotificationDaoImpl): IDao<Notification>

    @Binds
    @StorageSingleton
    internal abstract fun provideVideoTimeStamp(videoTimestampDao: VideoTimestampDaoImpl): IDao<VideoTimestamp>

    @Binds
    @StorageSingleton
    internal abstract fun provideLastStepDao(persistentLastStepDao: PersistentLastStepDaoImpl): IDao<PersistentLastStep>

    @Binds
    @StorageSingleton
    internal abstract fun provideExternalVideoUrlDao(videoUrlDao: VideoUrlDaoImpl): IDao<DbVideoUrl>

    @Module
    companion object {

        @StorageSingleton
        @Provides
        @JvmStatic
        internal fun provideStorageGson(): Gson =
                GsonBuilder()
                        .enableComplexMapKeySerialization()
                        .create()

        @StorageSingleton
        @Provides
        @JvmStatic
        internal fun provideWritableDatabase(helper: SQLiteOpenHelper): SQLiteDatabase =
                helper.writableDatabase

        @StorageSingleton
        @Provides
        @JvmStatic
        @EnrolledCoursesDaoQualifier
        internal fun provideEnrolledCoursesDao(crudOperations: CrudOperations, cachedVideo: IDao<CachedVideo>, externalVideos: IDao<DbVideoUrl>): IDao<Course> =
                CourseDaoImpl(crudOperations, cachedVideo, externalVideos, DbStructureEnrolledAndFeaturedCourses.ENROLLED_COURSES)

        @StorageSingleton
        @Provides
        @JvmStatic
        @FeaturedCoursesDaoQualifier
        internal fun provideFeaturedCoursesDao(crudOperations: CrudOperations, cachedVideo: IDao<CachedVideo>, externalVideos: IDao<DbVideoUrl>): IDao<Course> =
                CourseDaoImpl(crudOperations, cachedVideo, externalVideos, DbStructureEnrolledAndFeaturedCourses.FEATURED_COURSES)
    }
}
