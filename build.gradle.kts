plugins {
    id("java")
}

group = "dev.diego"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Lombook para generar código, poner  esto para que funcione
    implementation("org.projectlombok:lombok:1.18.28")
    testImplementation("org.projectlombok:lombok:1.18.28")
    testImplementation("org.projectlombok:lombok:1.18.28")

    // https://mvnrepository.com/artifact/com.zaxxer/HikariCP
    implementation("com.zaxxer:HikariCP:2.3.2")

    //h2
    implementation("com.h2database:h2:2.2.224")

    //ibaitis
    implementation("org.mybatis:mybatis:3.5.13")

    //project reactor
    implementation("io.projectreactor:reactor-core:3.4.10")

    //R2DBC
    implementation("io.r2dbc:r2dbc-h2:0.8.4.RELEASE")
    implementation("io.r2dbc:r2dbc-pool:0.8.5.RELEASE")
    implementation("org.projectreactor:reactor-core:3.4.8")

    // https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
    implementation("ch.qos.logback:logback-classic:1.4.11")

    //test
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}