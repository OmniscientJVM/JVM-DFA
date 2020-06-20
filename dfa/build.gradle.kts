description = "Data-flow analysis."

dependencies {
    api(project(":interpreter"))

    implementation(project(":logging"))

    testImplementation(project(":testUtil"))
}
