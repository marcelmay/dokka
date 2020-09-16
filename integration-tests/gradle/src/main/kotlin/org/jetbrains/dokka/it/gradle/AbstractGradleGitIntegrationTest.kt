package org.jetbrains.dokka.it.gradle

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import java.nio.file.Path

@RunWith(Parameterized::class)
abstract class AbstractGradleGitIntegrationTest : AbstractGradleIntegrationTest() {

    protected open fun copyAndApplyGitDiff(diffFile: File) =
        copyGitDiffFileToParent(diffFile).let(::applyGitDiffFromFile)

    protected open fun copyGitDiffFileToParent(originalDiffFile: File) =
        originalDiffFile.copyTo(File(projectDir.parent, originalDiffFile.name))

    protected open fun applyGitDiffFromFile(diffFile: File) {
        val projectGitFile = projectDir.resolve(".git")
        val git = if (projectGitFile.exists()) {
            if (projectGitFile.isFile) {
                println(".git file inside project directory exists, removing")
                removeGitFile(projectDir.toPath())
                Git.init().setDirectory(projectDir).call()
            } else {
                println(".git directory inside project directory exists, reusing")
                FileRepositoryBuilder().apply {
                    isMustExist = true
                    gitDir = projectDir
                }.let { Git(it.build()) }
            }
        } else {
            Git.init().setDirectory(projectDir).call()
        }
        git.apply().setPatch(diffFile.inputStream()).call()
    }

    protected open fun removeGitFile(repository: Path) =
        repository.toFile()
            .listFiles().orEmpty()
            .filter { it.name.toLowerCase() == ".git" }
            .forEach { it.delete() }

}
