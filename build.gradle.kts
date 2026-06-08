import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    java
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.graalvm.buildtools.native") version "1.1.0"
    id("com.diffplug.spotless") version "8.1.0"
    id("checkstyle")
    id("pmd")
    id("org.openapi.generator") version "7.14.0"
    id("com.github.node-gradle.node") version "7.1.0"
//    id("org.owasp.dependencycheck") version "12.1.0"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

val avroConfluentSerializerVersion = "7.6.1"
val avroVersion = "1.11.3"
val instrumentationBomVersion = "2.27.0"
val jakartaAnnotationApiVersion = "2.1.1"
val jacksonDataBindNullableVersion = "0.2.6"
val springDocOpenApiVersion = "2.8.12"
val swaggerAnnotationsVersion = "2.2.36"

configurations.all {
    resolutionStrategy {
        force("io.swagger.core.v3:swagger-annotations:$swaggerAnnotationsVersion")
        force("io.swagger.core.v3:swagger-core:$swaggerAnnotationsVersion")
        force("io.swagger.core.v3:swagger-models:$swaggerAnnotationsVersion")
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://packages.confluent.io/maven/") }
}

val avroTools by configurations.creating

dependencies {
    avroTools("org.apache.avro:avro-tools:$avroVersion")
    implementation("io.confluent:kafka-avro-serializer:$avroConfluentSerializerVersion")
    implementation("jakarta.annotation:jakarta.annotation-api:$jakartaAnnotationApiVersion")
    implementation("org.apache.avro:avro:${avroVersion}")
    implementation("org.openapitools:jackson-databind-nullable:$jacksonDataBindNullableVersion")
    implementation("io.swagger.core.v3:swagger-annotations:$swaggerAnnotationsVersion") // pin for openapi-generator requiredMode compat
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springDocOpenApiVersion") // (required by generated code)
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-kafka")
    implementation("org.springframework.boot:spring-boot-starter-opentelemetry")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))
    implementation(platform("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:$instrumentationBomVersion"))
//    developmentOnly("org.springframework.boot:spring-boot-devtools")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    testImplementation("org.springframework.boot:spring-boot-starter-kafka-test")
    testImplementation("org.springframework.boot:spring-boot-starter-opentelemetry-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-kafka")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}

tasks.withType<Test> {
    useJUnitPlatform()
}

spotless {
    java {
        target("src/**/*.java")
        targetExclude("**/build/**")
        googleJavaFormat("1.33.0")
        trimTrailingWhitespace()
        endWithNewline()
        removeUnusedImports()
    }
}

checkstyle {
    toolVersion = "12.1.2"
    configFile = file("config/checkstyle/checkstyle.xml")
    configProperties = mapOf(
        "org.checkstyle.google.suppressionfilter.config" to
                file("config/checkstyle/checkstyle-suppressions.xml").absolutePath
    )
}

pmd {
    toolVersion = "6.55.0"
}

/*dependencyCheck {
    failBuildOnCVSS = 7.0f

    // suppressionFile = "dependency-check-suppressions.xml"

    analyzers.apply {
        nodeEnabled = true   // scan package.json / npm deps
        assemblyEnabled = false
    }

    formats = listOf("HTML", "JSON")
}*/

val avroOutputDir = layout.buildDirectory
    .dir("generated-sources/avro")
    .get()
    .asFile

tasks.register<JavaExec>("generateAvro") {

    inputs.dir("src/main/avro")
    outputs.dir(avroOutputDir)

    classpath = avroTools
    mainClass.set("org.apache.avro.tool.Main")

    args = listOf(
        "compile",
        "schema",
        "src/main/avro",
        avroOutputDir.absolutePath
    )
}

val openapiOutputDir = "${layout.buildDirectory.asFile.get()}/generated-sources/openapi"

openApiGenerate {
    generatorName.set("spring")
    inputSpec.set("${project.rootDir}/openapi/openapi.yml")
    outputDir.set(openapiOutputDir)
    apiPackage.set("com.example.generated.api")
    modelPackage.set("com.example.generated.model")
    configOptions.set(
        mapOf(
            "interfaceOnly" to "true",
            "useTags" to "true",
            "useSpringBoot4" to "true",
            "useResponseEntity" to "true",
            "useJakartaEe" to "true",
        )
    )
}

node {
    version.set("24.12.0")
    //npmVersion.set("10.5.0")
    download.set(true)
}
val openApiInput = "${projectDir}/openapi/openapi.yml"
val openApiBundled = "${layout.buildDirectory.asFile.get()}/generated-openapi-bundle/openapi.yml"
tasks.npmInstall {
    inputs.file("${projectDir}/package.json")
}
tasks.register<com.github.gradle.node.npm.task.NpmTask>("redoclyLint") {
    group = "openapi tools"
    description = "Lint OpenAPI spec with Redocly"

    dependsOn(tasks.npmInstall)

    args.set(
        listOf(
            "run",
            "redocly",
            "lint",
            openApiInput
        )
    )

    inputs.file(openApiInput)
}
tasks.register<com.github.gradle.node.npm.task.NpmTask>("redoclyBundle") {
    group = "openapi tools"
    description = "Bundle OpenAPI spec with Redocly"

    dependsOn(tasks.npmInstall, "redoclyLint")

    args.set(
        listOf(
            "exec", "--",
            "redocly",
            "bundle",
            openApiInput,
            "--output",
            openApiBundled,
            "--ext",
            "yml"
        )
    )

    inputs.file(openApiInput)
    outputs.file(openApiBundled)
}
tasks.register<Copy>("copyBundledOpenApiToResources") {
    group = "openapi tools"
    dependsOn("redoclyBundle")
    from(openApiBundled)
    into("${layout.buildDirectory.get()}/resources/static")
}

sourceSets {
    main {
        java {
            srcDir("$avroOutputDir")
            srcDir("$openapiOutputDir/src/main/java")
        }
    }
}

tasks.named("compileJava") {
    dependsOn(
        tasks.named("generateAvro"),
        tasks.named("openApiGenerate"),
        tasks.named("copyBundledOpenApiToResources"),
    )
}

tasks.named("processResources") {
    dependsOn("copyBundledOpenApiToResources")
}
