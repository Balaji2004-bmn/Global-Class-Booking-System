package com.assessment.booking.service;

import com.assessment.booking.dto.request.BookingRequest;
import com.assessment.booking.entity.ClassSession;
import com.assessment.booking.entity.Course;
import com.assessment.booking.entity.Offering;
import com.assessment.booking.entity.Parent;
import com.assessment.booking.entity.Teacher;
import com.assessment.booking.exception.ConflictException;
import com.assessment.booking.repository.BookingRepository;
import com.assessment.booking.repository.CourseRepository;
import com.assessment.booking.repository.OfferingRepository;
import com.assessment.booking.repository.ParentRepository;
import com.assessment.booking.repository.SessionRepository;
import com.assessment.booking.repository.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class BookingServiceConcurrencyIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("class_booking_test")
            .withUsername("booking_app")
            .withPassword("booking_app");

    @DynamicPropertySource
    static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private OfferingRepository offeringRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @BeforeEach
    void cleanDatabase() {
        bookingRepository.deleteAll();
        sessionRepository.deleteAll();
        offeringRepository.deleteAll();
        parentRepository.deleteAll();
        teacherRepository.deleteAll();
        courseRepository.deleteAll();
    }

    @Test
    void concurrentDuplicateBookingCreatesOnlyOneBooking() throws Exception {
        Parent parent = createParent();
        TestCatalog catalog = createCatalog();
        Offering offering = createOffering(
                catalog,
                "Physics Foundations",
                utc("2026-06-01T10:00:00Z"),
                utc("2026-06-01T11:00:00Z")
        );

        List<RunResult> results = runConcurrently(
                () -> bookingService.book(new BookingRequest(parent.getId(), offering.getId())),
                () -> bookingService.book(new BookingRequest(parent.getId(), offering.getId()))
        );

        assertThat(successCount(results)).isEqualTo(1);
        assertThat(failures(results))
                .singleElement()
                .satisfies(result -> assertThat(result.throwable()).isInstanceOf(ConflictException.class));
        assertThat(bookingRepository.countByParentId(parent.getId())).isEqualTo(1);
    }

    @Test
    void concurrentOverlappingBookingsCreateOnlyOneBooking() throws Exception {
        Parent parent = createParent();
        TestCatalog catalog = createCatalog();
        Offering firstOffering = createOffering(
                catalog,
                "Chemistry Lab",
                utc("2026-06-01T10:00:00Z"),
                utc("2026-06-01T11:00:00Z")
        );
        Offering secondOffering = createOffering(
                catalog,
                "Math Circle",
                utc("2026-06-01T10:30:00Z"),
                utc("2026-06-01T11:30:00Z")
        );

        List<RunResult> results = runConcurrently(
                () -> bookingService.book(new BookingRequest(parent.getId(), firstOffering.getId())),
                () -> bookingService.book(new BookingRequest(parent.getId(), secondOffering.getId()))
        );

        assertThat(successCount(results)).isEqualTo(1);
        assertThat(failures(results))
                .singleElement()
                .satisfies(result -> assertThat(result.throwable()).isInstanceOf(ConflictException.class));
        assertThat(bookingRepository.countByParentId(parent.getId())).isEqualTo(1);
    }

    private Parent createParent() {
        return parentRepository.saveAndFlush(Parent.builder()
                .name("Priya Parent")
                .timezone("Asia/Kolkata")
                .build());
    }

    private TestCatalog createCatalog() {
        Teacher teacher = teacherRepository.saveAndFlush(Teacher.builder()
                .name("Avery Teacher")
                .timezone("UTC")
                .build());
        Course course = courseRepository.saveAndFlush(Course.builder()
                .title("STEM")
                .description("STEM classes")
                .build());
        return new TestCatalog(teacher, course);
    }

    private Offering createOffering(TestCatalog catalog, String title, OffsetDateTime startUtc, OffsetDateTime endUtc) {
        Offering offering = offeringRepository.saveAndFlush(Offering.builder()
                .course(catalog.course())
                .teacher(catalog.teacher())
                .title(title)
                .timezone("UTC")
                .build());

        sessionRepository.saveAndFlush(ClassSession.builder()
                .offering(offering)
                .startTimeUtc(startUtc)
                .endTimeUtc(endUtc)
                .build());

        return offering;
    }

    private OffsetDateTime utc(String value) {
        return OffsetDateTime.parse(value).withOffsetSameInstant(ZoneOffset.UTC);
    }

    private List<RunResult> runConcurrently(ThrowingRunnable first, ThrowingRunnable second) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try {
            Future<RunResult> firstFuture = executor.submit(task(ready, start, first));
            Future<RunResult> secondFuture = executor.submit(task(ready, start, second));

            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            return List.of(
                    firstFuture.get(15, TimeUnit.SECONDS),
                    secondFuture.get(15, TimeUnit.SECONDS)
            );
        } finally {
            executor.shutdownNow();
        }
    }

    private Callable<RunResult> task(CountDownLatch ready, CountDownLatch start, ThrowingRunnable action) {
        return () -> {
            ready.countDown();
            if (!start.await(5, TimeUnit.SECONDS)) {
                return RunResult.failed(new IllegalStateException("Timed out waiting for concurrent start"));
            }

            try {
                action.run();
                return RunResult.ok();
            } catch (Throwable throwable) {
                return RunResult.failed(throwable);
            }
        };
    }

    private long successCount(List<RunResult> results) {
        return results.stream()
                .filter(RunResult::succeeded)
                .count();
    }

    private List<RunResult> failures(List<RunResult> results) {
        return results.stream()
                .filter(result -> !result.succeeded())
                .toList();
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private record RunResult(boolean succeeded, Throwable throwable) {

        static RunResult ok() {
            return new RunResult(true, null);
        }

        static RunResult failed(Throwable throwable) {
            return new RunResult(false, throwable);
        }
    }

    private record TestCatalog(Teacher teacher, Course course) {
    }
}
