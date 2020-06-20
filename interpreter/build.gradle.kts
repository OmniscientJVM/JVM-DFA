description = "The interpreter."

dependencies {
    api(group = "org.ow2.asm", name = "asm", version = Versions.asm)
    api(group = "org.ow2.asm", name = "asm-util", version = Versions.asm)

    implementation(project(":logging"))

    testImplementation(project(":testUtil"))
}
