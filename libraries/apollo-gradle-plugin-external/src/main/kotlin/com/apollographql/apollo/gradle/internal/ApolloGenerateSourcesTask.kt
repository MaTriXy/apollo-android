package com.apollographql.apollo.gradle.internal

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

@CacheableTask
abstract class ApolloGenerateSourcesTask : ApolloGenerateSourcesBaseTask(), ApolloGenerateDataBuildersSourcesBaseTask {
  @get:InputFiles
  @get:PathSensitive(PathSensitivity.RELATIVE)
  abstract val schemaFiles: ConfigurableFileCollection

  @get:InputFiles
  @get:PathSensitive(PathSensitivity.RELATIVE)
  abstract val fallbackSchemaFiles: ConfigurableFileCollection

  @get:InputFiles
  @get:PathSensitive(PathSensitivity.RELATIVE)
  abstract val graphqlFiles: ConfigurableFileCollection

  @get:org.gradle.api.tasks.InputFile
  @get:PathSensitive(PathSensitivity.RELATIVE)
  abstract val codegenSchemaOptionsFile: RegularFileProperty

  @get:org.gradle.api.tasks.InputFile
  @get:PathSensitive(PathSensitivity.RELATIVE)
  abstract val irOptionsFile: RegularFileProperty

  @TaskAction
  fun taskAction() {
    val workQueue = getWorkQueue()

    workQueue.submit(GenerateSources::class.java) {
      it.hasPlugin = hasPlugin.get()
      it.graphqlFiles = graphqlFiles.isolate()
      it.schemaFiles = schemaFiles.isolate()
      it.fallbackSchemaFiles = fallbackSchemaFiles.isolate()
      it.codegenSchemaOptions.set(codegenSchemaOptionsFile)
      it.irOptions.set(irOptionsFile)
      it.codegenOptions.set(codegenOptionsFile)
      it.operationManifestFile.set(operationManifestFile)
      it.outputDir.set(outputDir)
      it.dataBuildersOutputDir.set(dataBuildersOutputDir)
      it.arguments = arguments.get()
      it.logLevel = logLevel.get().ordinal
      it.apolloBuildService.set(apolloBuildService)
      it.classpath = classpath
    }
  }
}

private abstract class GenerateSources : WorkAction<GenerateSourcesParameters> {
  override fun execute() {
    with(parameters) {
      runInIsolation(apolloBuildService.get(), classpath) {
        it.reflectiveCall(
            "buildSources",
            arguments,
            logLevel,
            hasPlugin,
            (schemaFiles.takeIf { it.isNotEmpty() } ?: fallbackSchemaFiles),
            graphqlFiles,
            codegenSchemaOptions.get().asFile,
            codegenOptions.get().asFile,
            irOptions.get().asFile,
            warningMessageConsumer,
            operationManifestFile.orNull?.asFile,
            outputDir.get().asFile,
            dataBuildersOutputDir.get().asFile,
        )
      }
    }
  }
}

private interface GenerateSourcesParameters : WorkParameters {
  var hasPlugin: Boolean
  var graphqlFiles: List<Any>
  var schemaFiles: List<Any>
  var fallbackSchemaFiles: List<Any>
  val codegenSchemaOptions: RegularFileProperty
  val codegenOptions: RegularFileProperty
  val irOptions: RegularFileProperty
  val operationManifestFile: RegularFileProperty
  val outputDir: DirectoryProperty
  val dataBuildersOutputDir: DirectoryProperty
  var arguments: Map<String, Any?>
  var logLevel: Int
  val apolloBuildService: Property<ApolloBuildService>
  var classpath: FileCollection
}
