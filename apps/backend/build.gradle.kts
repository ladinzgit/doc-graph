plugins {
	kotlin("jvm") version "2.2.21"
	kotlin("plugin.spring") version "2.2.21"
	id("org.springframework.boot") version "4.0.5"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("plugin.jpa") version "2.2.21"

}

group = "com.docgraph"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencyManagement {
	imports {
		mavenBom("org.testcontainers:testcontainers-bom:1.20.4")
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("tools.jackson.module:jackson-module-kotlin")
	runtimeOnly("org.postgresql:postgresql")
	implementation("org.springframework.boot:spring-boot-flyway")
	implementation("org.flywaydb:flyway-core")
	runtimeOnly("org.flywaydb:flyway-database-postgresql")
	developmentOnly("org.springframework.boot:spring-boot-docker-compose")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
	implementation("org.springframework.retry:spring-retry:2.0.12")
	implementation("org.springframework:spring-aspects")
	testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:postgresql")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("io.mockk:mockk:1.13.13")
	testImplementation("com.ninja-squad:springmockk:5.0.1")
	testImplementation("org.wiremock:wiremock-standalone:3.10.0")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.bootRun {
	workingDir = rootProject.projectDir
	// 개발자 개인 자격증명을 application-local.yml(gitignored)에서 주입
	systemProperty("spring.profiles.active", "local")
}

tasks.withType<Test> {
	useJUnitPlatform()

	// 외부 자격증명은 application-test.yaml로 격리 — .env 누설 차단
	systemProperty("spring.profiles.active", "test")

	// 모노레포 루트 .env에서 인프라 변수만 화이트리스트 주입 — docker-compose·Testcontainers 동등성 강제용
	// (자격증명·외부 API 키는 .env 출처 아님 — docs/development.md 참고)
	val envFile = rootProject.projectDir.resolve("../../.env").canonicalFile
	check(envFile.exists()) { "모노레포 루트 .env 없음: ${envFile.absolutePath}" }
	val infraVarsFromEnv = setOf("POSTGRES_VERSION")
	val parsed = envFile.readLines()
		.filter { it.isNotBlank() && !it.startsWith("#") }
		.associate {
			val (key, value) = it.split("=", limit = 2)
			key.trim() to value.trim()
		}
	infraVarsFromEnv.forEach { key ->
		parsed[key]?.let { environment(key, it) }
	}

	val missing = infraVarsFromEnv.filterNot { environment.containsKey(it) }
	check(missing.isEmpty()) { ".env 필수 인프라 변수 누락: $missing" }
}

tasks.register<Test>("unitTest") {
	description = "@Tag(\"unit\") 테스트만 실행"
	group = "verification"
	testClassesDirs = sourceSets["test"].output.classesDirs
	classpath = sourceSets["test"].runtimeClasspath
	useJUnitPlatform { includeTags("unit") }
}

tasks.register<Test>("sliceTest") {
	description = "@Tag(\"slice\") 테스트만 실행"
	group = "verification"
	testClassesDirs = sourceSets["test"].output.classesDirs
	classpath = sourceSets["test"].runtimeClasspath
	useJUnitPlatform { includeTags("slice") }
}

tasks.register<Test>("componentTest") {
	description = "@Tag(\"component\") 테스트만 실행"
	group = "verification"
	testClassesDirs = sourceSets["test"].output.classesDirs
	classpath = sourceSets["test"].runtimeClasspath
	useJUnitPlatform { includeTags("component") }
}
