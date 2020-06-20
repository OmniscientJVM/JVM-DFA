/*
 * This file is part of JVM-DFA.
 *
 * JVM-DFA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JVM-DFA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JVM-DFA.  If not, see <https://www.gnu.org/licenses/>.
 */

/**
 * This file cannot be in a package. That would prevent Gradle from loading it inside `plugins {}`
 * blocks.
 */
object Versions {
    const val JVM_DFA = "0.1.0"

    const val spotlessPlugin = "3.29.0"
    const val testLoggerPlugin = "2.0.0"
    const val ktlintPlugin = "9.2.1"
    const val detektPlugin = "1.9.0"

    const val kotlin = "1.3.72"
    const val ktlint = "0.36.0"
    const val junit = "5.6.2"
    const val kotest = "4.0.5"
    const val asm = "8.0.1"
    const val slf4j_log4j12 = "1.7.28"
    const val apacheLog4jExtras = "1.2.17"
    const val kotlinLogging = "1.7.9"
    const val mockk = "1.10.0"
    const val arrow = "0.10.5"

    const val jacocoTool = "0.8.5"
    const val gradleWrapper = "6.4.1"
}
