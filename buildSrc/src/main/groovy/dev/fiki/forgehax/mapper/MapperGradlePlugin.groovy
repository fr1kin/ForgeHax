package dev.fiki.forgehax.mapper

import org.gradle.api.Plugin
import org.gradle.api.Project

class MapperGradlePlugin implements Plugin<Project> {
  @Override
  void apply(Project project) {
    project.extensions.create('mapper', MapperExtension, project);
  }
}
