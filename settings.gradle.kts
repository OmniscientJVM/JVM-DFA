plugins {
    id("com.gradle.enterprise") version "3.3.1"
}

rootProject.name = "JVM-DFA"

include(":dfa")
include(":interpreter")
include(":logging")
include(":testUtil")

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}
