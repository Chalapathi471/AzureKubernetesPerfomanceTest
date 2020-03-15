package test.scala

import io.gatling.recorder.GatlingRecorder
import io.gatling.recorder.config.RecorderPropertiesBuilder

object Recorder extends App {

	val props = new RecorderPropertiesBuilder()
		.simulationsFolder(IDEPathHelper.mavenSourcesDirectory.toString)
		.resourcesFolder(IDEPathHelper.mavenResourcesDirectory.toString)
		.simulationPackage("kubernetespackage")

	GatlingRecorder.fromMap(props.build, Some(IDEPathHelper.recorderConfigFile))
}
